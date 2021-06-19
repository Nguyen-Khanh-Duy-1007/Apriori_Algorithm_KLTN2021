package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import list.ItemSet;
import list.Transaction;


public class AprioriUtils
{
    // Returns a transaction object for the input txn record.
    public static Transaction getTransaction(int id, String txnRecord) {
        String currLine = txnRecord.trim();
        String[] words = currLine.split(" ");
        Transaction transaction = new Transaction(id);

        for (int i = 0; i < words.length; i++) {
            transaction.add(Integer.parseInt(words[i].trim()));
        }

        return transaction;
    }


    // Determines if an item with the specified frequency has minimum support or not.
    public static boolean hasMinSupport(double minSup, int numTxns, int itemCount) {
        /** COMPLETE **/
    	double support = itemCount * 1.0 / numTxns;
    	if(Double.compare(support, minSup) >= 0) return true;
    	return false;
    }

    public static List<ItemSet> getCandidateItemSets(List<ItemSet> prevPassItemSets, int itemSetSize) {
        List<ItemSet> candidateItemSets = new ArrayList<>();
        Map<Integer, ItemSet> itemSetMap = generateItemSetMap(prevPassItemSets);
        Collections.sort(prevPassItemSets);
        int prevPassItemSetsSize = prevPassItemSets.size();

        /** COMPLETE **/
        List<Integer> hashCodes = new ArrayList<>(); // for case {@code itemSetSize == 1} ? 
        for(int i = 0; i < prevPassItemSetsSize; i++) { // generates hash codes for each itemset in the {@code candidateItemSets} except their last elements 
        	ItemSet subItemSet = new ItemSet();
        	subItemSet.addAll(prevPassItemSets.get(i).subList(0, itemSetSize - 1));
        	if(subItemSet.size() > 0) hashCodes.add(subItemSet.hashCode());
        	else hashCodes.add(0);
        }
        
        for(int i = 0; i < prevPassItemSetsSize; i++) { // for case {@code itemSetSize == 1} ? 
        	for(int j = i + 1; j < prevPassItemSetsSize; j++) {
        		if(hashCodes.get(i).equals(hashCodes.get(j))) {
        			ItemSet newItemSet = new ItemSet();
        			if(itemSetSize > 1) newItemSet.addAll(prevPassItemSets.get(i).subList(0, itemSetSize - 1));
        			newItemSet.add(prevPassItemSets.get(i).get(itemSetSize - 1));
        			newItemSet.add(prevPassItemSets.get(j).get(itemSetSize - 1));
        			candidateItemSets.add(newItemSet);
        		} else {
        			break;
        		}
        	}
        }
        
        for(Iterator<ItemSet> iterator = candidateItemSets.iterator(); iterator.hasNext();) {
        	ItemSet candidate = iterator.next();
        	if(prune(itemSetMap, candidate) == false) {
        		iterator.remove();
        	}
        }
        
        return candidateItemSets;
    }

    public static Map<Integer, ItemSet> generateItemSetMap(List<ItemSet> itemSets) {
        Map<Integer, ItemSet> itemSetMap = new HashMap<>();

        for (ItemSet itemSet : itemSets) {
            int hashCode = itemSet.hashCode();
            if (!itemSetMap.containsKey(hashCode)) {
                itemSetMap.put(hashCode, itemSet);
            }
        }
        return itemSetMap;
    }

    static boolean prune(Map<Integer, ItemSet> itemSetsMap, ItemSet newItemSet) {
        List<ItemSet> subsets = getSubSets(newItemSet);

        for (ItemSet subItemSet : subsets) {
            int hashCodeToSearch = subItemSet.hashCode();
            if (!itemSetsMap.containsKey(hashCodeToSearch)) {
                return false;
            }
        }
        return true;
    }
   
    static void findSubSets(int v, ItemSet itemSet, List<ItemSet> subSets, boolean[] taken, ItemSet newItemSet) {
    	if(newItemSet.size() == itemSet.size() - 1){ // Add to the {@code subSets} newly formed itemSet
    		ItemSet newItemSetCpy = new ItemSet();
    		newItemSetCpy.addAll(newItemSet);
    		subSets.add(newItemSetCpy);
    		return;
    	}
    	for(int i = v; i < itemSet.size(); i++){
    		if(taken[i] == false) { // If a value standing on index {@code i} is not taken, then take it
    			taken[i] = true;
    			newItemSet.add(itemSet.get(i));
    			findSubSets(i + 1, itemSet, subSets, taken, newItemSet); // Go down to the next recursive subtree
    			newItemSet.remove(newItemSet.size() - 1); // Remove previously inserted value
    			taken[i] = false;
    		}
    	}
    }
    
    //  Generate all possible k-1 subsets for ItemSet (preserves order)
    static List<ItemSet> getSubSets(ItemSet itemSet) {
        List<ItemSet> subSets = new ArrayList<>();
        /** COMPLETE **/
        boolean taken[] = new boolean[itemSet.size()]; // element is {@code true} 
        								
        for(int i = 0; i < itemSet.size(); i++) {
        	taken[i] = false;
        }
        ItemSet newItemSet = new ItemSet();
        if(itemSet.size() > 1) { // If the size of {@code itemSet} is greater than 1,
        	
        	findSubSets(0, itemSet, subSets, taken, newItemSet); 
        }
        return subSets;
    }
}
