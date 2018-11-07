package com.lin.blockchain.blockchaindemo.transation;

import java.security.PublicKey;

/**
 * 交易输出
 */
public class TransactionOutput {

    /** 在此次交易输出的交易id */
    private int index = 0;

    /** 交易收款人地址 （公钥）*/
    private PublicKey sender;

    /** 交易额 */
    private float value;


    public  TransactionOutput(float value,PublicKey sender){
        this.sender = sender;
        this.value = value;
    }


    /** 是否是自己的余额 */
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == sender);
    }

    public PublicKey getSender() {
        return sender;
    }

    public void setSender(PublicKey sender) {
        this.sender = sender;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
