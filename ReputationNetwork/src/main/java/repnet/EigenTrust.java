package repnet;

import java.util.*;
import static java.lang.Math.sqrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import static java.lang.Math.pow;

//import org.web3j;

//package org.web3j.sample;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
//import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;


import repnet.Repnet.RateEventEventResponse;

import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;


public class EigenTrust {
	

	
    // return B = A^T
    public static double[][] transpose(double[][] a) {
        int m = a.length;
        int n = a[0].length;
        double[][] b = new double[n][m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                b[j][i] = a[i][j];
        return b;
    }
    
    // return c = a * b
    public static double[][] multiply(double[][] a, double[][] b) {
        int m1 = a.length;
        int n1 = a[0].length;
        int m2 = b.length;
        int n2 = b[0].length;
        if (n1 != m2) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] c = new double[m1][n2];
        for (int i = 0; i < m1; i++)
            for (int j = 0; j < n2; j++)
                for (int k = 0; k < n1; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }
    
    // matrix-vector multiplication (y = A * x)
    public static double[] multiply(double[][] a, double[] x) {
        int m = a.length;
        int n = a[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += a[i][j] * x[j];
        return y;
    }
	
    public static double distance(double[] a, double[] b) {
    	double sum = 0;
    	for(int i = 0; i < a.length; i ++) {
    		sum = sum + Math.pow(a[i] - b[i], 2); 
    	}
    	return Math.sqrt(sum);
    }
    
	public static double max(double a, double b) {
		if(a > b) return a;
		return b;
	}
	
	public static void t() {

	}
	
    private static List<Repnet.RateEventEventResponse> getRatings() throws Exception {
    	
        // We start by creating a new web3j instance to connect to remote nodes on the network.
        // Note: if using web3j Android, use Web3jFactory.build(...
        //Web3j web3j = Web3j.build(new HttpService());  // FIXME: Enter your Infura token here;
        //"https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"
        //System.out.println("Connected to Ethereum client version: "+ web3j.web3ClientVersion().send().getWeb3ClientVersion());
        Web3j web3j = Web3j.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));

        // We then need to load our Ethereum wallet file
        // FIXME: Generate a new wallet file using the web3j command line tools https://docs.web3j.io/command_line.html
        Credentials credentials = WalletUtils.loadCredentials("reputation", "walletfile");
        System.out.println("Credentials loaded");      
        
        //web3j solidity generate [--javaTypes|--solidityTypes] /path/to/<smart-contract>.bin /path/to/<smart-contract>.abi -o /path/to/src/main/java -p com.your.organisation.name
        BigInteger bi = BigInteger.valueOf(10);
        String contractAddress = "0x35d38490eE059e94BCC062dB965Ab45871637A9c";
        Repnet contract = Repnet.load(contractAddress, web3j, credentials, bi, bi);
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        
        final Event event = new Event("RateEvent", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint8>() {}));
       
        
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, contractAddress);
        filter.addSingleTopic(EventEncoder.encode(event));

        List<Repnet.RateEventEventResponse> res = new ArrayList<Repnet.RateEventEventResponse>();
        try {
            EthLog ethLog = web3j.ethGetLogs(filter).send();

            for (EthLog.LogResult l : ethLog.getLogs()) {
                Repnet.RateEventEventResponse e = contract.getRateEventEvent((org.web3j.protocol.core.methods.response.Log) l);
                res.add(e);
                System.out.println("test" + e.receipient + ", " + e.sender + ", " + e.score);
            }
        } catch (IOException e) { 
            e.printStackTrace();
        }
        return res;
        
        //call mike's contract
    }
	
	public static double[] computeRating(double ratings[][], double error, String user) {
		
		int num_peers = ratings.length;
		
		double def = 1.0/num_peers;
		
		//Initialize t 
		double[] t = new double[num_peers];
		for(int i = 0; i < num_peers; i ++) {
			t[i] = def;
		}
		
		double e = Double.MAX_VALUE;
		
		while(e > error) {
			int rows = ratings.length;
			int cols = ratings.length;
			double[][] C = new double[rows][cols];
			for(int i = 0; i < rows; i ++) {
				for(int j = 0; j < cols; j ++) {
					double sum = 0;;
					for(int x = 0; x < cols; x ++) {
						sum = sum + max(0, ratings[i][x]);
					}
					if(sum > 0) C[i][j] = max(0, ratings[i][j])/sum;
					else C[i][j] = def;
				}
			}
			System.out.println("C VALUES");
			for(int i = 0; i < rows; i ++) {
				for(int j = 0; j < cols; j ++) {
					System.out.print(C[i][j] + " " );
				}
				System.out.println();
			}
			
			double[][] C_t = transpose(C);
			double[] t_new = multiply(C_t, t);
			e = distance(t_new, t);
			t = t_new;			
		}		
		return t;		
	}
	
		
	public static double get_user_score(String user) throws Exception {
		List<Repnet.RateEventEventResponse> ratings = getRatings();
		
		
		Map<String, Integer> user_to_index = new HashMap<String, Integer>();
		Map<Integer, String> index_to_user = new HashMap<Integer, String>();
		int num_users = 0;
		for(Repnet.RateEventEventResponse r : ratings) {
			if(!user_to_index.containsKey(r.sender)) {
				user_to_index.put(r.sender, num_users);
				index_to_user.put(num_users, r.sender);
				num_users ++;
			}
			if(!user_to_index.containsKey(r.receipient)) {
				user_to_index.put(r.receipient, num_users);
				index_to_user.put(num_users, r.receipient);
				num_users ++;
			}
		}
		
		//INITIALIZE SCORES TO ZERO 
		double[][] converted_ratings = new double[num_users][num_users];
		for(int i = 0; i < num_users; i ++) {
			for(int j = 0; j < num_users; j ++) {
				converted_ratings[i][j] = 0;
			}
		}
		
		for(Repnet.RateEventEventResponse r : ratings) {
			int i = user_to_index.get(r.sender);
			int j = user_to_index.get(r.receipient);
			converted_ratings[i][j] = (r.score.intValue())/5.0;
		}
		
		for(int i = 0; i < num_users; i ++) {
			for(int j = 0; j < num_users; j ++) {
				System.out.print(converted_ratings[i][j] + " ");
			}
			System.out.println();
		}
		
		double[] scores = computeRating(converted_ratings, 0.001, "");
		double max = 0;
		for(double s : scores) {
			if(s > max) max = s;
		}
		
		Map<String, Double> user_scores = new HashMap<String, Double>();
		for(int i = 0; i < scores.length; i ++) {
		    double score = ((int)(scores[i]/max * 500.0))/100.0;
		    
		    String u = index_to_user.get(i);
		    user_scores.put(u, score);
			System.out.println("User " + user + " score " + score);
		}
		return user_scores.get(user);
	}

	public static void main(String[] args) throws Exception {
		int portNumber = 1234;
		
		try (
			    ServerSocket serverSocket = new ServerSocket(portNumber);
			    Socket clientSocket = serverSocket.accept();
			    PrintWriter out =
			        new PrintWriter(clientSocket.getOutputStream(), true);
			    BufferedReader in = new BufferedReader(
			        new InputStreamReader(clientSocket.getInputStream()));
			) {
			    String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			        double score = get_user_score(inputLine);
			        out.println(score);
			    }
		}

	}
}

