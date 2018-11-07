package com.lin.blockchain.blockchaindemo.pow;

import bean.Block;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transation.Transaction;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProofOfWork {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProofOfWork.class);
    /**
     * 区块
     */
    private Block block;
    /**
     * 难度目标值
     */
    private int target;

    /**交易*/
    public List<Transaction> transactions = new ArrayList<Transaction>();

    /**版本号*/
    private int vassion = 1;

    public ProofOfWork(Block block){
        this.target = block.getDifficulty();
        this.block = block;
        this.transactions = block.getTransactions();
        calculateHash(block);
    }
    /**
     * 计算区块的hash值
     *
     * @param block
     *            区块
     * @return
     */
    public static String calculateHash(Block block) {
        String record = block.getVassion() + block.getMerkleRoot()+ block.getTimestamp() + block.getDifficulty() + block.getPrevHash()+block.getNonce();
        MessageDigest digest = DigestUtils.getSha256Digest();
        byte[] hash = digest.digest(StringUtils.getBytesUtf8(record));
        return Hex.encodeHexString(hash);
    }

    /**
     * 区块的生成
     *
     * @param prevHash
     * @param merkleRoot
     * @return
     */
    public Block generateBlock(String prevHash, String merkleRoot) {
        Block newBlock = new Block();

        newBlock.setVassion(String.valueOf(vassion));
        newBlock.setTimestamp(Long.parseLong(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        newBlock.setMerkleRoot(merkleRoot);
        newBlock.setPrevHash(prevHash);
        newBlock.setDifficulty(this.target);
        newBlock.setTransactions(transactions);
		/*
		 * 这里的 for 循环很重要： 获得 i 的十六进制表示 ，将 Nonce 设置为这个值，并传入 calculateHash 计算哈希值。
		 * 之后通过上面的 isHashValid 函数判断是否满足难度要求，如果不满足就重复尝试。 这个计算过程会一直持续，
		 * 直到求得了满足要求的 Nonce 值，之后将新块加入到链上。
		 */
        for (int i = 0;; i++) {
            String hex = String.format("%x", i);
            newBlock.setNonce(Integer.parseInt(hex));
            if (!isHashValid(calculateHash(newBlock), newBlock.getDifficulty())) {
                LOGGER.info("{} need do more work!", calculateHash(newBlock));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("error:", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                LOGGER.info("{} work done!", calculateHash(newBlock));
                newBlock.setHash(calculateHash(newBlock));
                break;
            }
        }
        return newBlock;
    }

    private static String repeat(String str, int repeat) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            buf.append(str);
        }
        return buf.toString();
    }

    /**
     * 校验HASH的合法性
     *
     * @param hash
     * @param difficulty
     * @return
     */
    public static boolean isHashValid(String hash, int difficulty) {
        String prefix = repeat("0", difficulty);
        return hash.startsWith(prefix);
    }

    /**
     * 校验区块的合法性（有效性）
     *
     * @param newBlock
     * @param oldBlock
     * @return
     */
    public static boolean isBlockValid(Block newBlock, Block oldBlock) {
        if (oldBlock.getBlockId() + 1 != newBlock.getBlockId()){
            return false;
        }
        if (!oldBlock.getHash().equals(newBlock.getPrevHash())) {
            return false;
        }
        if (!calculateHash(newBlock).equals(newBlock.getHash())) {
            return false;
        }
        return true;
    }

}
