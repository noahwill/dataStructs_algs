package impl;

import static impl.OptimalBSTMap.dummy;

import java.util.Arrays;

import impl.OptimalBSTMap.Internal;
import impl.OptimalBSTMap.Node;



/**
 * OptimalBSTMapFactory
 * 
 * Build an optimal BST, given the keys, values, key probabilities
 * and miss probabilities.
 * 
 * @author Thomas VanDrunen
 * CSCI 345, Wheaton College
 * Feb 25, 2015
 */

public class OptimalBSTMapFactory {

    /**
     * Exception to throw if the input to building an optimal BST
     * is not right: either the number of keys, values, key probs,
     * and miss probs aren't consistent, or the total probability
     * is not 1.
     */
    public static class BadOptimalBSTInputException extends RuntimeException {
        private static final long serialVersionUID = -444687298513060315L;

        private BadOptimalBSTInputException(String msg) {
            super(msg);
        }
    }
    
    /**
     * Build an optimal BST from given raw data, passed as a single object.
     * A convenient overloading of the other buildOptimalBST().
     * @param rawData The collection of data for building this BST
     * @return A BST with the given keys and values, optimal with the
     * given probabilities.
     */
    public static OptimalBSTMap buildOptimalBST(OptimalBSTData rawData) {
        return buildOptimalBST(rawData.keys, rawData.values, rawData.keyProbs, rawData.missProbs);
    }
    
    /**
     * Build an optimal BST from given raw data, passed as individual arrays.
     * @param rawData The collection of data for building this BST
     * @return A BST with the given keys and values, optimal with the
     * given probabilities.
     */
    public static OptimalBSTMap buildOptimalBST(String[] keys, String[] values, double[] keyProbs,
            double[] missProbs) {
        // keep these checks
        checkLengths(keys, values, keyProbs, missProbs);
        checkProbs(keyProbs, missProbs);        
        
        // The number of keys (so we don't need to say keys.length every time)
        int n = keys.length;
        
        // optTrees[0][n-1] should be the optimal subtrees
        Node[][] root = new Node[n][n];
        
        // C[i][j] = total weighted depth for the best tree for key range [k[i], k[j]]
        double[][] C = new double[n][n];
        
        // T[i][j] = total probability for the best tree for key range [k[i],k[j]]
        double[][] T = new double[n][n];
        
        double bestC = 0.0;
        double currentC = 0.0;
        Internal best;
        
        for (int i = 0; i < n; i++) {
        	T[i][i] = missProbs[i] + keyProbs[i] + missProbs[i+1];
        	C[i][i] = (2*missProbs[i]) + keyProbs[i] + (2*missProbs[i+1]);
        	best = new Internal(dummy, keys[i], values[i], dummy);
        	root[i][i] = best;
        }
        
        
        for (int l = 1; l < n; l++) {
        	for (int i = 0; i < n - l; i++) {
        		int j = i + l;
        		// each tree will have the same total probability
        		T[i][j] = missProbs[i] + keyProbs[i] + T[i+1][j];
        		
        		// r = i
        		bestC = missProbs[i] + T[i][j] + C[i+1][j];
        		best = new Internal(dummy, keys[i], values[i], root[i+1][j]);
        		
        		// i < r < j
        		for(int r = i+1; r < j; r++) {
    				currentC = C[i][r-1] + T[i][j] + C[r+1][j];
        			if(currentC < bestC) {
    					best = new Internal(root[i][r-1], keys[r], values[r], root[r+1][j]);
    					//best = new Internal(root[i][j-1], keys[j], values[j], dummy);
    					bestC = currentC;
    				}
    			}
        		
        		// r = j
        		currentC = T[i][j] + C[i][j-1] + missProbs[j+1];
        		if (currentC < bestC) {
        			bestC = currentC;
        			best = new Internal(root[i][j-1], keys[j], values[j], dummy);
        		}
        		
        		// assign the values in the tables
        		C[i][j] = bestC;
        		root[i][j] = best;
        	}
        }
        
        return new OptimalBSTMap(root[0][n-1]);
        
     
    }

    /**
     * Check that the given probabilities sum to 1, throw an
     * exception if not.
     * @param keyProbs 
     * @param missProbs
     */
    public static void checkProbs(double[] keyProbs, double[] missProbs) {
        double[] allProbs = new double[keyProbs.length + missProbs.length];
        int i = 0;
        for (double keyProb : keyProbs)
            allProbs[i++] = keyProb;
        for (double missProb : missProbs)
            allProbs[i++] = missProb;
        // When summing doubles, sum from smallest to greatest
        // to reduce round-off error.
        Arrays.sort(allProbs);
        double totalProb = 0;
        for (double prob : allProbs)
            totalProb += prob;
        // Don't compare doubles for equality directly. Check that their
        // difference is less than some epsilon.
        if (Math.abs(1.0 - totalProb) > .0001)
            throw new BadOptimalBSTInputException("Probabilities total to " + totalProb);
    }

    /**
     * Check that the arrays have appropriate lengths (keys, values, and
     * keyProbs all the same, missProbs one extra), throw an exception
     * if not.
     * @param keys
     * @param values
     * @param keyProbs
     * @param missProbs
     */
    public static void checkLengths(String[] keys, String[] values,
            double[] keyProbs, double[] missProbs) {
        int n = keys.length;
        if (values.length != n || keyProbs.length != n || missProbs.length != n+1)
            throw new BadOptimalBSTInputException(n + "keys, " + values.length + " values, " +
                    keyProbs.length + " key probs, and " + missProbs.length + " miss probs");
    }
    
}
