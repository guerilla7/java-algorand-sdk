package com.algorand.algosdk.example;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.algod.client.AlgodClient;
import com.algorand.algosdk.algod.client.api.AlgodApi;
import com.algorand.algosdk.algod.client.ApiException;
import com.algorand.algosdk.algod.client.auth.ApiKeyAuth;
import com.algorand.algosdk.algod.client.model.NodeStatus;
import com.algorand.algosdk.algod.client.model.Supply;
import com.algorand.algosdk.algod.client.model.TransactionID;
import com.algorand.algosdk.algod.client.model.TransactionParams;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.crypto.Digest;
import com.algorand.algosdk.kmd.client.KmdClient;
import com.algorand.algosdk.kmd.client.api.KmdApi;
import com.algorand.algosdk.kmd.client.model.APIV1POSTWalletResponse;
import com.algorand.algosdk.kmd.client.model.CreateWalletRequest;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;

import java.security.Security;
import java.math.BigInteger;


public class Main {

    public static void main(String args[]) throws Exception {
        final String ALGOD_API_ADDR = "http://localhost:8080";
        final String ALGOD_API_TOKEN = "b3ff7b3765bf33c7557a2cd334238eca07578e3ce266cb03fad93d12aab0622e";
        final String SRC_ACCOUNT = "viable grain female caution grant mind cry mention pudding oppose orchard people forget similar social gossip marble fish guitar art morning ring west above concert";
        final String DEST_ADDR = "KV2XGKMXGYJ6PWYQA5374BYIQBL3ONRMSIARPCFCJEAMAHQEVYPB7PL3KU";

        AlgodClient client = new AlgodClient();
        client.setBasePath(ALGOD_API_ADDR);
        // Configure API key authorization: api_key
        ApiKeyAuth api_key = (ApiKeyAuth) client.getAuthentication("api_key");
        api_key.setApiKey(ALGOD_API_TOKEN);

        AlgodApi algodApiInstance = new AlgodApi(client);
        try {
            Supply supply = algodApiInstance.getSupply();
            System.out.println("Total Algorand Supply: " + supply.getTotalMoney());
            System.out.println("Online Algorand Supply: " + supply.getOnlineMoney());
        } catch (ApiException e) {
            System.err.println("Exception when calling algod#getSupply");
            e.printStackTrace();
        }

        Account src = new Account(SRC_ACCOUNT);
        System.out.println("My Address: " + src.getAddress());

        BigInteger feePerByte;
        String genesisID;
        Digest genesisHash;
        long firstRound = 301;
        try {
            TransactionParams params = algodApiInstance.transactionParams();
            feePerByte = params.getFee();
            genesisHash = new Digest(params.getGenesishashb64());
            genesisID = params.getGenesisID();
            System.out.println("Suggested Fee: " + feePerByte);
            NodeStatus s = algodApiInstance.getStatus();
            firstRound = s.getLastRound().longValue();
            System.out.println("Current Round: " + firstRound);
        } catch (ApiException e) {
            throw new RuntimeException("Could not get params", e);
        }

        // Generate a new transaction using randomly generated accounts (this is invalid, since src has no money...)
        long amount = 100000;
        long lastRound = firstRound + 1000; // 1000 is the max tx window
        Transaction tx = new Transaction(src.getAddress(), new Address(DEST_ADDR), amount, firstRound, lastRound, genesisID, genesisHash);
        SignedTransaction signedTx = src.signTransactionWithFeePerByte(tx, feePerByte);
        System.out.println("Signed transaction with txid: " + signedTx.transactionID);

        // send the transaction to the network
        try {
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            TransactionID id = algodApiInstance.rawTransaction(encodedTxBytes);
            System.out.println("Successfully sent tx with id: " + id);
        } catch (ApiException e) {
            // This is generally expected, but should give us an informative error message.
            System.err.println("Exception when calling algod#rawTransaction: " + e.getResponseBody());
        }

        // now, demo kmd api
//        kmdApi();
    }

    public static void kmdApi() {
        final String KMD_API_ADDR = "http://localhost:7833";
        final String KMD_API_TOKEN = "1bce699faef65c80da8da6201bd0639b3ea4205c4fa05d24f94469efa2418f2d";

        // Create a wallet with kmd rest api
        KmdClient client = new KmdClient();
        client.setBasePath(KMD_API_ADDR);
        // Configure API key authorization: api_key
        com.algorand.algosdk.kmd.client.auth.ApiKeyAuth api_key = (com.algorand.algosdk.kmd.client.auth.ApiKeyAuth) client.getAuthentication("api_key");
        api_key.setApiKey(KMD_API_TOKEN);
        KmdApi kmdApiInstance = new KmdApi(client);

        APIV1POSTWalletResponse wallet;
        try {
            CreateWalletRequest req = new CreateWalletRequest()
                    .walletName("arbitrary-wallet-name-3")
                    .walletDriverName("sqlite");
            wallet = kmdApiInstance.createWallet(req);
            System.out.println("Created wallet id: " + wallet.getWallet().getId());
        } catch (com.algorand.algosdk.kmd.client.ApiException e) {
            System.out.println("Failed to create wallet: " + e.getResponseBody());
            e.printStackTrace();
        }

    }
}
