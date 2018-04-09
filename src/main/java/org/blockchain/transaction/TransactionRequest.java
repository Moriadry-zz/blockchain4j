package org.blockchain.transaction;

/**
 * Created by dingpeng on 2018/3/27.
 */
public class TransactionRequest {

    public String transactionOutputId;
    public TransactionResponse UTXO;

    public TransactionRequest(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
}
