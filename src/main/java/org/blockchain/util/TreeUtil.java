package org.blockchain.util;

import org.blockchain.transaction.Transaction;

import java.util.List;

/**
 * Created by dingpeng on 2018/3/27.
 */
public class TreeUtil {
    /**
     * get root node of the merkle tree which records transactions.
     * @param transactions
     * @return
     */
    public static String getMerkleRoot(List<Transaction> transactions){
        MerkleTree merkleTree = new MerkleTree(transactions);
        return merkleTree.buildTree();
    }
}
