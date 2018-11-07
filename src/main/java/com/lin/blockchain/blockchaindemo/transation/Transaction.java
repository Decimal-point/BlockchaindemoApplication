package com.lin.blockchain.blockchaindemo.transation;


import bean.BlockChain;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;
import source.RocksDBUtils;
import util.SerializeUtils;
import wallet.Wallet;
import wallet.WalletUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.*;

/**
 * 交易记录
 */
public class Transaction {
    private static final int SUBSIDY = 10;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RocksDBUtils.class);

    /** 此次交易号 */
    private String transactionId;
    /** 交易版本号 */
    private String vassion;

    /** 输入源 */
    private List<TransactionInput> inputs = new ArrayList<>();

    /** 输出源 */
    private List<TransactionOutput> outputs = new ArrayList<>();

    /**输出源个数*/
    private Integer inputSize;

    /**输入源个数*/
    private Integer outputSize;

    private Long createTime;

    public Transaction(String transactionId,List<TransactionInput> inputs,List<TransactionOutput> outputs,Long createTime){
        this.vassion = "1.0";
        this.transactionId = transactionId;
        this.inputs = inputs;
        this.outputs = outputs;
        this.inputSize = inputs.size();
        this.outputSize = outputs.size();
        this.createTime = createTime;
    }

    public byte[] hash() {
        // 使用序列化的方式对Transaction对象进行深度复制
        byte[] serializeBytes = SerializeUtils.serialize(this);
        Transaction copyTx = (Transaction) SerializeUtils.deserialize(serializeBytes);
        return DigestUtils.sha256(SerializeUtils.serialize(copyTx));
    }

    /**
     * 从 from 向  to 支付一定的 amount 的金额
     *
     * @param from       支付钱包地址
     * @param to         收款钱包地址
     * @param amount     交易金额
     * @param blockchain 区块链
     * @return
     */
    public static Transaction newUTXOTransaction(PublicKey from, PublicKey to, int amount, BlockChain blockchain) throws Exception {
        // 获取钱包
        Wallet senderWallet = WalletUtils.getInstance().getWallet(from.toString());
        PublicKey publicKey = senderWallet.getPublicKey();
      //  byte[] pubKeyHash = BtcAddressUtils.ripeMD160Hash(publicKey.toString().getBytes());

        SpendableOutputResult result = new UTXOSet(blockchain).findSpendableOutputs(publicKey, amount);
        int accumulated = result.getAccumulated();
        Map<String, int[]> unspentOuts = result.getUnspentOuts();

        if (accumulated < amount) {
            log.error("ERROR: Not enough funds ! accumulated=" + accumulated + ", amount=" + amount);
            throw new RuntimeException("ERROR: Not enough funds ! ");
        }
        Iterator<Map.Entry<String, int[]>> iterator = unspentOuts.entrySet().iterator();

        ArrayList<TransactionInput> txInputs = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, int[]> entry = iterator.next();
            String txIdStr = entry.getKey();
            int[] outIds = entry.getValue();
            for (int outIndex : outIds) {
                txInputs.add(new TransactionInput(txIdStr, outIndex, null));
            }
        }

        ArrayList<TransactionOutput> outputs = new ArrayList<>();
        outputs.add(new TransactionOutput(amount, to));
        if (accumulated > amount) {
            outputs.add(new TransactionOutput((accumulated - amount), from));
        }

        Transaction newTx = new Transaction(null, txInputs, outputs, System.currentTimeMillis());
        newTx.setTransactionId(newTx.hash().toString());

        // 进行交易签名
        blockchain.signTransaction(newTx, senderWallet.getPrivateKey());

        return newTx;
    }

    /**
     * 是否为 Coinbase 交易
     *
     * @return
     */
    public boolean isCoinbase() {
        return this.getInputs().size() == 1
                && this.inputs.get(0).getTransactionOutputId().length() == 0
                && this.inputs.get(0).getIndex() == -1;
    }
    /**
     * 创建CoinBase交易
     *
     * @param to   收账的钱包地址（公钥）
     * @param data 解锁脚本数据
     * @returntring
     */
    public static Transaction newCoinbase(PublicKey to, String data) {
        if (StringUtils.isBlank(data)) {
            data = String.format("Reward to '%s'", to);
        }
        // 创建交易输入
        TransactionInput transactionInput = new TransactionInput("",0, data);
        // 创建交易输出
        TransactionOutput transactionOutput = new TransactionOutput(SUBSIDY, to);
        // 创建交易
        Transaction tx = new Transaction(null,Arrays.asList(transactionInput), Arrays.asList(transactionOutput),System.currentTimeMillis());
        tx.setTransactionId(tx.hash().toString());
        return tx;
    }

    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param prevTxMap  前面多笔交易集合
     */
    public void sign(PrivateKey privateKey, Map<String, Transaction> prevTxMap) throws Exception {
        // coinbase 交易信息不需要签名，因为它不存在交易输入信息
        if (this.isCoinbase()) {
            return;
        }
        // 再次验证一下交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
        for (TransactionInput txInput : this.getInputs()) {
            if (prevTxMap.get(txInput.getTransactionOutputId()) == null) {
                throw new RuntimeException("ERROR: Previous transaction is not correct");
            }
        }

        // 创建用于签名的交易信息的副本
        Transaction txCopy = this.trimmedCopy();
        //进行数据签名的初始化
        Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);

        for (int i = 0; i < txCopy.getInputs().size(); i++) {
            TransactionInput txInputCopy = txCopy.getInputs().get(i);
            // 获取交易输入TxID对应的交易数据
            Transaction prevTx = prevTxMap.get(txInputCopy.getTransactionOutputId());
            // 获取交易输入所对应的上一笔交易中的交易输出
            TransactionOutput transactionOutput = prevTx.getOutputs().get(txInputCopy.getIndex());
            txInputCopy.setPubKey(transactionOutput.getSender()); //因为只有上一笔交易输出的地址是自己才是可用的
            txInputCopy.setSignature(null);
            // 得到要签名的数据，即交易ID
            txCopy.setTransactionId(txCopy.hash().toString());

            // 对整个交易信息仅进行签名，即对交易ID进行签名
            ecdsaSign.update(txCopy.getTransactionId().getBytes());
            byte[] signature = ecdsaSign.sign();

            // 将整个交易数据的签名赋值给交易输入，因为交易输入需要包含整个交易信息的签名
            // 注意是将得到的签名赋值给原交易信息中的交易输入
            this.getInputs().get(i).setSignature(signature.toString());
        }
    }

    /**
     * 创建用于签名的交易数据副本，交易输入的 signature 和 pubKey 需要设置为null
     *
     * @return
     */
    public Transaction trimmedCopy() {
        ArrayList<TransactionInput> transactionInputs = new ArrayList<>();
        for (int i = 0; i < this.getInputs().size(); i++) {
            TransactionInput transactionInput = inputs.get(i);
            transactionInputs.add(i,new TransactionInput(transactionInput.getTransactionOutputId(), transactionInput.getIndex(), null));
        }

        ArrayList<TransactionOutput> transactionOutputs = new ArrayList<>();
        for (int i = 0; i < this.getOutputs().size(); i++) {
            TransactionOutput txOutput = this.getOutputs().get(i);
            transactionOutputs.add(i,new TransactionOutput(txOutput.getValue(), txOutput.getSender()));
        }

        return new Transaction(this.getTransactionId(), transactionInputs, transactionOutputs, this.getCreateTime());
    }




    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }
}
