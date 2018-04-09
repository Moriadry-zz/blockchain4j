package org.blockchain.transaction;

import org.blockchain.util.CryptologyUtil;

import java.security.PublicKey;

/**
 * Created by dingpeng on 2018/3/27.
 */
public class TransactionResponse {

    public String id;
    public PublicKey recipient;
    public float value;
    public String parentTransactionId;

    public TransactionResponse(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = CryptologyUtil.applySha256(CryptologyUtil.getStringFromKey(recipient) +
            Float.toString(value) + parentTransactionId);

    }

    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}
