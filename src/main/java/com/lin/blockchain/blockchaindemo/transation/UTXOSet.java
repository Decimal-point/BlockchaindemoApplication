package com.lin.blockchain.blockchaindemo.transation;

import bean.BlockChain;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import source.RocksDBUtils;
import util.SerializeUtils;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

/**
 * 未被花费的交易输出池
 */
public class UTXOSet {
    //本地区块链
    private BlockChain blockchain;

    public UTXOSet(BlockChain blockchain) {
        this.blockchain = blockchain;
    }


    /**
     * 寻找能够花费的交易
     *
     * @param publicKey 钱包公钥Hash
     * @param amount     花费金额
     */
    public SpendableOutputResult findSpendableOutputs(PublicKey publicKey, int amount) {
        Map<String, int[]> unspentOuts = Maps.newHashMap();
        int accumulated = 0;
        Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance().getChainstateBucket();
        for (Map.Entry<String, byte[]> entry : chainstateBucket.entrySet()) {
            String txId = entry.getKey();
            List<TransactionOutput> txOutputs = (List<TransactionOutput>) SerializeUtils.deserialize(entry.getValue());

            for (int outId = 0; outId < txOutputs.size(); outId++) {
                TransactionOutput transactionOutput = txOutputs.get(outId);
                if (transactionOutput.isMine(publicKey) && accumulated < amount) {
                    accumulated += transactionOutput.getValue();

                    int[] outIds = unspentOuts.get(txId);
                    if (outIds == null) {
                        outIds = new int[]{outId};
                    } else {
                        outIds = ArrayUtils.add(outIds, outId);
                    }
                    unspentOuts.put(txId, outIds);
                    if (accumulated >= amount) {
                        break;
                    }
                }
            }
        }
        return new SpendableOutputResult(accumulated, unspentOuts);
    }

}
