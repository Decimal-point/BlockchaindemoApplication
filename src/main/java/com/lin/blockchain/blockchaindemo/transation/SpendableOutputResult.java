package com.lin.blockchain.blockchaindemo.transation;

import java.util.Map;

/**
 * 未被花费的交易输出结果
 */

public class SpendableOutputResult {
    /**
     * 交易时的支付金额(查询可交易的交易输出的额度总和[稍微大于或者等于])
     */
    private int accumulated;
    /**
     * 未花费的交易[key:数据库名,对应RocksDBUtils中CHAINSTATE_BUCKET_KEY, value:未交易的输出ID]
     */
    private Map<String, int[]> unspentOuts;

    public SpendableOutputResult(int accumulated, Map<String, int[]> unspentOuts) {
        this.accumulated = accumulated;
        this.unspentOuts = unspentOuts;
    }

    public int getAccumulated() {
        return accumulated;
    }

    public void setAccumulated(int accumulated) {
        this.accumulated = accumulated;
    }

    public Map<String, int[]> getUnspentOuts() {
        return unspentOuts;
    }

    public void setUnspentOuts(Map<String, int[]> unspentOuts) {
        this.unspentOuts = unspentOuts;
    }
}
