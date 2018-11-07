package com.lin.blockchain.blockchaindemo.bean;

import pow.ProofOfWork;
import transation.MerkleTree;
import transation.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 区块实体：
 *      其中包含80字符的区块头和交易列表
 *         区块头：index，timestamp,hash,prevHash,merkleRoot,difficulty,nonce
 *         交易列表:transaction
 */

public class Block {

    /**区块ID*/
    private Integer blockId;

    /**版本号*/
    private String vassion;
    /** 时间戳*/
    private long timestamp;
    /**指向前一个块的 SHA256 散列值(区块头)*/
    private String prevHash;
    /**默克尔根 */
    private String merkleRoot;
    /**挖块难度 */
    private int difficulty;
    /**需要改变的随机数，实现固定的前导‘0’个数*/
    private int nonce;

    /**是这个区块头通过 SHA256 算法生成的散列值*/
    private String hash;
    /**每个块包含的交易 */
    public List<Transaction> transactions = new ArrayList<Transaction>();
    /**这个区块的交易数量*/
    private Integer tranSize = transactions.size();
    /**这个区块的交易数量字节数*/
    private int hashSize = tranSize.byteValue();

    public Block(){}

    public Block(String hash,String prevHash,long timestamp,List<Transaction> transactions){
        this.difficulty = 1;
        this.hash = hash;
        this.vassion = "2.0";
        this.merkleRoot = hashTransaction();
        this.prevHash = prevHash;
        this.transactions = transactions;
        this.timestamp = timestamp;
    }


    public String getVassion() {
        return vassion;
    }

    public void setVassion(String vassion) {
        this.vassion = vassion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * <p> 创建创世区块 </p>
     *
     * @param coinbase
     * @return
     */
    public static Block newGenesisBlock(Transaction coinbase) {
        return new Block().newBlock("",Arrays.asList(coinbase),4);
    }

    /**
     * 对区块中的交易信息进行Hash计算
     *
     * @return
     */
    public String hashTransaction() {
        byte[][] txIdArrays = new byte[this.getTransactions().size()][];
        for (int i = 0; i < this.getTransactions().size(); i++) {
            txIdArrays[i] = this.getTransactions().get(i).hash();
        }
        return new MerkleTree(txIdArrays).getRoot().getHash().toString();
    }

    /**
     * <p> 创建新区块 </p>
     *
     * @param previousHash
     * @param transactions
     * @return
     */
    public Block newBlock(String previousHash, List<Transaction> transactions,int difficulty) {
        Block block = new Block("",previousHash,Instant.now().getEpochSecond(),transactions);
        ProofOfWork pow = new ProofOfWork(block);
        return pow.generateBlock(previousHash, merkleRoot);
    }

}
