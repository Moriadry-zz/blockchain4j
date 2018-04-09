package org.blockchain.pojo;

import org.blockchain.transaction.Transaction;
import org.blockchain.util.CryptologyUtil;
import org.blockchain.util.TreeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by dingpeng on 2018/3/24.
 */
public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public String data;

    public List<Transaction> transactions = new ArrayList<>();
    private long timeStamp;
    private int nonce;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public Block(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash  = calculateHash();
    }

    public String calculateHash() {
        String calculatedHash = CryptologyUtil.applySha256(
            previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                merkleRoot
        );
        return calculatedHash;
    }

    /**
     * 不停的重复计算hash，直至前difficulty位是0
     * @param difficulty
     */
    public void mineBlock(int difficulty){
        merkleRoot = TreeUtil.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0','0');
        while (!hash.substring(0,difficulty).equals(target)){
            //可以用random值作为nonce进行尝试
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Mine successful! hash = " + hash);
    }

    public boolean addTransaction(Transaction transaction){
        if(Objects.isNull(transaction)){
            return false;
        }
        if(!Objects.equals(previousHash,"0")){
            if(!transaction.processTransaction()) {
                System.out.println("Transaction failed to process.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction successfully added to block");
        return true;
    }
}
