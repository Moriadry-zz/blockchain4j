package org.blockchain;

import com.google.gson.GsonBuilder;
import org.blockchain.pojo.Block;
import org.blockchain.pojo.Wallet;
import org.blockchain.transaction.Transaction;
import org.blockchain.transaction.TransactionRequest;
import org.blockchain.transaction.TransactionResponse;
import org.blockchain.util.CryptologyUtil;

import java.security.Security;
import java.util.*;

/**
 * Created by dingpeng on 2018/3/24.
 */
public class Main {

    public static List<Block> blockChain = new ArrayList<>();

    //为了提高交易效率，使用额外的数据记录输出信息。
    public static Map<String, TransactionResponse> UTXOs = new HashMap<>();

    //可以通过设置不同的值来测试挖矿时间（不要设置太大）
    public static final int difficulty = 4;

    //设置最小交易额
    public static final float minimumTransaction = 0.1f;

    public static Wallet wallet1;

    public static Wallet wallet2;

    //创世交易
    public static Transaction genesisTransaction;


    public static void main(String args[]) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        mineTest();
        System.out.println(Math.abs("1932981771929600288479".hashCode()) %8);
        transactionTest();
    }

    //test mining blocks
    public static void mineTest() {
        long startTime = System.currentTimeMillis();

        blockChain.add(new Block("The first block", "0"));
        System.out.println("Start mine block 1... ");
        blockChain.get(0).mineBlock(difficulty);

        blockChain.add(new Block("The second block", blockChain.get(blockChain.size() - 1).hash));
        System.out.println("Start mine block 2... ");
        blockChain.get(1).mineBlock(difficulty);

        blockChain.add(new Block("The third block", blockChain.get(blockChain.size() - 1).hash));
        System.out.println("Start mine block 3... ");
        blockChain.get(2).mineBlock(difficulty);

        long endTime = System.currentTimeMillis();

        String blockChainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        System.out.println("\nTotal time :" + (endTime - startTime) + "ms .The block chain is:");
        System.out.println(blockChainJson);

        System.out.println("\nBlockChain is Valid: " + isBlockChainValid());
    }

    //test simple transaction
    public static void transactionTest() {
        wallet1 = new Wallet();
        wallet2 = new Wallet();

        System.out.println("Private and public keys:");
        System.out.println(CryptologyUtil.getStringFromKey(wallet1.privateKey));
        System.out.println();
        System.out.println(CryptologyUtil.getStringFromKey(wallet1.publicKey));

        Transaction transaction = new Transaction(wallet1.publicKey, wallet2.publicKey, 5, null);
        transaction.generateSignature(wallet1.privateKey);

        System.out.println("\nIs signature verified:" + transaction.verifySignature());


    }

    /**
     * 校验区块链中的信息是否有效
     * @return
     */
    public static Boolean isBlockChainValid() {
        Block currentBlock;
        Block previousBlock;
        Map<String,TransactionResponse> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id,genesisTransaction.outputs.get(0));

        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        for (int i = 1; i < blockChain.size(); i++) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i - 1);

            //首先校验当前区块是否有效
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }

            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }

            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block has not been mined");
                return false;
            }

            //校验当前区块中的每笔交易是否有效
            for(int j=0;j<currentBlock.transactions.size();j++){
                Transaction currentTransaction = currentBlock.transactions.get(j);

                if(!currentTransaction.verifySignature()){
                    System.out.println("Signature of "+ j +" transaction is invalid");
                    return false;
                }

                if(currentTransaction.getInputValue() != currentTransaction.getOutputValue()){
                    System.out.println("Input is not equals output in "+ j +" transaction");
                    return false;
                }

                //校验一笔交易中的每一个输入是否有效
                TransactionResponse tempOutput;
                for(TransactionRequest input:currentTransaction.inputs){
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null){
                        System.out.println("output reference missed in "+ j +" transaction");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value){
                        System.out.println("output value is invalid in "+ j +" transaction");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }
                //将每笔交易得到的输出加入到临时余额集合中。
                for(TransactionResponse output:currentTransaction.outputs){
                    tempUTXOs.put(output.id,output);
                }

                //校验当前交易的输出是否有效
                if(!Objects.equals(currentTransaction.outputs.get(0).recipient,currentTransaction.recipient)){
                    System.out.println("output for recipient is not invalid "+ j +" transaction");
                    return false;
                }
                //检验交易结余是否返回给sender
                if(!Objects.equals(currentTransaction.outputs.get(1).recipient,currentTransaction.sender)){
                    System.out.println("output for sender is not invalid "+ j +" transaction");
                    return false;
                }

            }
        }
        //校验通过
        System.out.println("validation finish!");
        return true;
    }

}
