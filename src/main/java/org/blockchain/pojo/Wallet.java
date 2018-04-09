package org.blockchain.pojo;

import org.blockchain.Main;
import org.blockchain.transaction.Transaction;
import org.blockchain.transaction.TransactionRequest;
import org.blockchain.transaction.TransactionResponse;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dingpeng on 2018/3/27.
 */

    public class Wallet {
        public PrivateKey privateKey;
        public PublicKey publicKey;

        public Map<String,TransactionResponse> UTXOs = new HashMap<>();

        public Wallet(){
            generateKeyPair();
        }

        /**
         * 使用ECC（椭圆曲线算法）生成公私钥
         */
        public void generateKeyPair(){
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA","BC");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

                keyPairGenerator.initialize(ecSpec,random);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                privateKey = keyPair.getPrivate();
                publicKey = keyPair.getPublic();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 计算余额并其更新到本地存储
         * @return
         */
        public float getBalance(){
            float total = 0;
            for (Map.Entry<String, TransactionResponse> item: Main.UTXOs.entrySet()){
                TransactionResponse UTXO = item.getValue();
                if(UTXO.isMine(publicKey)) {
                    this.UTXOs.put(UTXO.id,UTXO);
                    total += UTXO.value ;
                }
            }
            return total;
        }

        /**
         * 发起一笔转账
         * @param _recipient
         * @param value
         * @return
         */
        public Transaction sendFunds(PublicKey _recipient, float value) {
            if (getBalance() < value) {
                System.out.println();
            }
            List<TransactionRequest> inputs = new ArrayList<>();

            float total = 0;

            for (Map.Entry<String, TransactionResponse> item : UTXOs.entrySet()) {
                TransactionResponse UTXO = item.getValue();
                total += UTXO.value;
                inputs.add(new TransactionRequest(UTXO.id));
                if (total > value) break;
            }

            Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);

            newTransaction.generateSignature(privateKey);

            for (TransactionRequest input : inputs) {
                UTXOs.remove(input.transactionOutputId);
            }

            return newTransaction;
        }

    }
