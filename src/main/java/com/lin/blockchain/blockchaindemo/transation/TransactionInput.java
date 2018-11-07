package com.lin.blockchain.blockchaindemo.transation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.security.PublicKey;

/**
 * 交易输入
 */
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInput {

    /** 上一笔交易的输出号*/
    private String transactionOutputId;

    /** 上一笔交易输出的索引号*/
    private Integer index = -1;

    /** 解锁脚本(秘钥验证签名)*/
    private String signature;

    private PublicKey pubKey;

    /** 解锁脚本长度*/
    private Integer scrptLen = 0;

    /** 序列号*/
    private String sequence = "1.0";

    public TransactionInput(String transactionOutputId,Integer index,String signature){
        this.index = index;
        this.signature = signature;
        this.scrptLen = signature.toString().length();
        this.sequence = "1.0";
        this.transactionOutputId = transactionOutputId;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public void setTransactionOutputId(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }
}
