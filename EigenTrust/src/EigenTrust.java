import java.util.*;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

//import org.web3j;

//package org.web3j.sample;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
//import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

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
	
    private void getRatings() throws Exception {

        // We start by creating a new web3j instance to connect to remote nodes on the network.
        // Note: if using web3j Android, use Web3jFactory.build(...
        Web3j web3j = Web3j.build(new HttpService(
                "https://rinkeby.infura.io/<your token>"));  // FIXME: Enter your Infura token here;
        System.out.println("Connected to Ethereum client version: "
                + web3j.web3ClientVersion().send().getWeb3ClientVersion());

        // We then need to load our Ethereum wallet file
        // FIXME: Generate a new wallet file using the web3j command line tools https://docs.web3j.io/command_line.html
        Credentials credentials =
                WalletUtils.loadCredentials(
                        "<password>",
                        "/path/to/<walletfile>");
        System.out.println("Credentials loaded");
        
        //call mike's contract
    }
	
	public static double[] computeRating(ArrayList<ArrayList<Integer> > ratings, float error, String user) {
		
		int num_peers = ratings.size();
		
		//Initialize t 
		double[] t = new double[num_peers];
		for(int i = 0; i < num_peers; i ++) {
			t[i] = 1.0/num_peers;
		}
		
		double e = Double.MAX_VALUE;
		
		while(e > error) {
			int rows = ratings.size();
			int cols = ratings.get(0).size();
			double[][] C = new double[rows][cols];
			for(int i = 0; i < rows; i ++) {
				for(int j = 0; j < cols; j ++) {
					double sum = 0;
					int rating = ratings.get(i).get(j);
					for(int x = 0; x < cols; x ++) {
						sum = sum + max(0, rating);
					}
					C[i][j] = max(0, rating)/sum;
				}
			}
			
			double[][] C_t = transpose(C);
			double[] t_new = multiply(C_t, t);
			
			e = distance(t_new, t);
			t = t_new;			
		}		
		return t;		
	}

	public static void main(String[] args) {
		System.out.println("Hello Word");

	}
	
	

}
