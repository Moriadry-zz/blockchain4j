package org.blockchain.transaction;

import org.blockchain.Main;
import org.blockchain.util.CryptologyUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingpeng on 2018/3/27.
 */
public class Transaction {
    public String transactionId;

    public PublicKey sender;
    public PublicKey recipient;

    public float value;
    public byte[] signature;

    public List<TransactionRequest> inputs = new ArrayList<>();
    public List<TransactionResponse> outputs = new ArrayList<>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, float value, List<TransactionRequest> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = CryptologyUtil.getStringFromKey(sender) + CryptologyUtil.getStringFromKey(recipient) +
            Float.toString(value);
        signature = CryptologyUtil.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = CryptologyUtil.getStringFromKey(sender) + CryptologyUtil.getStringFromKey(recipient) +
            Float.toString(value);
        return CryptologyUtil.verifyECDSASig(sender,data,signature);
    }

    public boolean processTransaction(){

        if(!verifySignature()){
            System.out.println("Transaction Signature failed to verify");
            return false;
        }

        for(TransactionRequest input:inputs){
            input.UTXO = Main.UTXOs.get(input.transactionOutputId);
        }

        if(getInputValue()< Main.minimumTransaction){
            System.out.println("Transaction Inputs too small: " + getInputValue());
            return false;
        }

        transactionId = calculateHash();

        float leftOver = getInputValue() - value;
        if(leftOver < 0.0){
            System.out.println("sum of Inputs is smaller than value, sum:" + getInputValue()+",value:"+value);
            return false;
        }

        outputs.add(new TransactionResponse(this.recipient,value,transactionId));
        outputs.add(new TransactionResponse(this.sender,leftOver,transactionId));

        for(TransactionResponse output:outputs){
            Main.UTXOs.put(output.id,output);
        }

        for(TransactionRequest input:inputs){
            if(input.UTXO!=null){
                Main.UTXOs.remove(input.UTXO.id);
            }
        }

        return true;
    }

    public float getInputValue(){
        float total = 0;
        for(TransactionRequest input:inputs ){
            if(input.UTXO == null){
                continue;
            }
            total += input.UTXO.value;
        }
        return total;
    }

    public float getOutputValue(){
        float total = 0;
        for(TransactionResponse output: outputs){
            total += output.value;
        }
        return total;
    }

    private String calculateHash() {
        sequence++;
        return CryptologyUtil.applySha256(
            CryptologyUtil.getStringFromKey(sender) +
                CryptologyUtil.getStringFromKey(recipient) +
                Float.toString(value) + sequence
        );
    }
}
