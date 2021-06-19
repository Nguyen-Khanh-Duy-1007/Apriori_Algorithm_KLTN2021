import java.io.*;
import java.util.*;
import java.io.File;

public class Apriori extends Observable {
    public static void main(String[] args) throws Exception {
        Apriori ap = new Apriori(args);
    }

    /** the list of current itemsets */
    private List<int[]> itemsets ;

    private String transaFile; 

    /** number of different items in the dataset */
    private int numItems; 

    /** total number of transactions in transaFile */
    private int numTransactions; 

    /** minimum support for a frequent itemset in percentage, e.g. 0.8 */
    private double minSup; 
    
    /** by default, Apriori is used with the command line interface */
    private boolean usedAsLibrary = false;
    
    File file = new File("/home/khanhduyuser/AprioriJava/outputApriori/output.txt");
    private boolean del = file.delete();

    /** This is the main interface to use this class as a library */
    public  Apriori(String[] args, Observer ob) throws Exception
    {
    	usedAsLibrary = true;
    	configure(args);
    	this.addObserver(ob);
    	go();
    }

    /** generates the apriori itemsets from a file
     * 
     * @param args configuration parameters: args[0] is a filename, args[1] the min support (e.g. 0.8 for 80%)
     */
    public  Apriori(String[] args) throws Exception
    {
        configure(args);
        go();
    }

    /** starts the algorithm after configuration */
    private void go() throws Exception {
        //start timer
        long start = System.currentTimeMillis();

        // first we generate the candidates of size 1
        createItemsetsOfSize1();       
        int itemsetNumber=1; //the current itemset being looked at
        int nbFrequentSets=0;
        
        while (itemsets.size()>0)
        {
            calculateFrequentItemsets();
            if(itemsets.size()!=0)
            {
                nbFrequentSets+=itemsets.size();
                //log("Found "+itemsets.size()+" frequent itemsets of size " + itemsetNumber + " (with support "+(minSup*100)+"%)");;
                createNewItemsetsFromPreviousOnes();
            }
            itemsetNumber++;
        } 

        //display the execution time
        long end = System.currentTimeMillis();
        log("Execution time is: "+((double)(end-start)/1000) + " seconds.");
        log("Found "+nbFrequentSets+ " frequents sets for support "+(minSup*100)+"% (absolute "+Math.round(numTransactions*minSup)+")");
        log("Done");
    }

    /** triggers actions if a frequent item set has been found  */
    private void foundFrequentItemSet(int[] itemset, int support) {
    	if (usedAsLibrary) {
            this.setChanged();
            notifyObservers(itemset);
    	}

    	try {
			File fout = new File("/home/khanhduyuser/AprioriJava/outputApriori/output.txt");
			FileOutputStream fos = new FileOutputStream(fout.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			//bw.write(Arrays.toString(itemset) + "  ("+ ((support / (double) numTransactions))+" "+support+")");
			bw.write(Arrays.toString(itemset) + "  ("+support+")");
			bw.newLine();
			
			bw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /** outputs a message in Sys.err if not used as library */
    private void log(String message) {
    	if (!usedAsLibrary) {
    		System.err.println(message);
    	}
    }

    /** computes numItems, numTransactions, and sets minSup */
    private void configure(String[] args) throws Exception
    {        
        if (args.length!=0)  transaFile = args[0];
	else transaFile = "/home/khanhduyuser/AprioriJava/Apriori/T10I4D100K.txt";
	// setting minsupport
	if (args.length>=2) minSup = (Double.valueOf(args[1]).doubleValue());    	
	else minSup = 0.05;// by default

	if (minSup>1 || minSup<0) {
  		System.out.println("MinSup > 0 & MinSup < 1");
	}
    	
    	// going thourgh the file to compute numItems and  numTransactions
    	numItems = 0;
    	numTransactions=0;
    	BufferedReader data_in = new BufferedReader(new FileReader(transaFile));
    	while (data_in.ready()) {    		
    		String line=data_in.readLine();	
    		if (line.matches("\\s*")) continue; // be friendly with empty lines
    		numTransactions++;
    		StringTokenizer t = new StringTokenizer(line," ");
    		while (t.hasMoreTokens()) {
    			int x = Integer.parseInt(t.nextToken());
    			//log(x);
    			if (x+1>numItems) numItems=x+1;
    		}    		
    	}  
        outputConfig();
    }

   /** outputs the current configuration
     */ 
	private void outputConfig() {
		//output config info to the user
		 log("Input configuration: "+numItems+" items, "+numTransactions+" transactions, ");
		 log("minsup = "+minSup*100+"%");
	}

	/** puts in itemsets all sets of size 1, 
	 * i.e. all possibles items of the datasets
	 */
	private void createItemsetsOfSize1() {
		itemsets = new ArrayList<int[]>();
        	for(int i=0; i<numItems; i++)
        	{
        		int[] cand = {i};
        		itemsets.add(cand);
        	}
	}
			
    /**
     * if m is the size of the current itemsets,
     * generate all possible itemsets of size n+1 from pairs of current itemsets
     * replaces the itemsets of itemsets by the new ones
     */
    private void createNewItemsetsFromPreviousOnes()
    {
    	// by construction, all existing itemsets have the same size
    	int currentSizeOfItemsets = itemsets.get(0).length; 
    	HashMap<String, int[]> tempCandidates = new HashMap<String, int[]>(); //temporary candidates
    	
        // compare each pair of itemsets of size n-1
        for(int i=0; i<itemsets.size(); i++)
        {
            for(int j=i+1; j<itemsets.size(); j++)
            {
                int[] X = itemsets.get(i);
                int[] Y = itemsets.get(j);

                assert (X.length==Y.length);
                
                //make a string of the first n-2 tokens of the strings
                int [] newCand = new int[currentSizeOfItemsets+1];
                for(int s=0; s<newCand.length-1; s++) {
                	newCand[s] = X[s];
                }
                    
                int ndifferent = 0;
                // then we find the missing value
                for(int s1=0; s1<Y.length; s1++)
                {
                	boolean found = false;
                	// is Y[s1] in X?
                    for(int s2=0; s2<X.length; s2++) {
                    	if (X[s2]==Y[s1]) { 
                    		found = true;
                    		break;
                    	}
                	}
                	if (!found){ // Y[s1] is not in X
                		ndifferent++;
                		// we put the missing value at the end of newCand
                		newCand[newCand.length -1] = Y[s1];
                	}
            	}
                
                // we have to find at least 1 different, otherwise it means that we have two times the same set in the existing candidates
                assert(ndifferent>0);
                
                
                if (ndifferent==1) {
                    // HashMap does not have the correct "equals" for int[] :-(
                    // I have to create the hash myself using a String :-(
                    // I use Arrays.toString to reuse equals and hashcode of String
                	Arrays.sort(newCand);
                	tempCandidates.put(Arrays.toString(newCand),newCand);
                }
            }
        }
        
        //set the new itemsets
        itemsets = new ArrayList<int[]>(tempCandidates.values());
    }

    /** put "true" in trans[i] if the integer i is in line */
    private void line2booleanArray(String line, boolean[] trans) {
	    Arrays.fill(trans, false);
	    StringTokenizer stFile = new StringTokenizer(line, " "); //read a line from the file to the tokenizer
	    //put the contents of that line into the transaction array
	    while (stFile.hasMoreTokens())
	    {
	    	
	        int parsedVal = Integer.parseInt(stFile.nextToken());
			trans[parsedVal]=true; //if it is not a 0, assign the value to true
	    }
    }
    
    /** passes through the data to measure the frequency of sets in {@link itemsets},
     *  then filters thoses who are under the minimum support (minSup)
     */
    private void calculateFrequentItemsets() throws Exception
    {
        List<int[]> frequentCandidates = new ArrayList<int[]>(); //the frequent candidates for the current itemset
        boolean match; //whether the transaction has all the items in an itemset
        int count[] = new int[itemsets.size()]; //the number of successful matches, initialized by zeros

	// load the transaction file	
	boolean[] trans = new boolean[numItems];
	int dem=0;
	for (int c = 0; c < itemsets.size(); c++) {	
		int[] cand = itemsets.get(c);
		BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(transaFile)));
		for (int i = 0; i < numTransactions; i++) {
			dem++;
			// boolean[] trans = extractEncoding1(data_in.readLine());
			String line = data_in.readLine();
			line2booleanArray(line, trans);
			match = true; // reset match to false
			// tokenize the candidate so that we know what items need to be
			// present for a match
			//dem++;
			for (int xx : cand) {
				if (trans[xx] == false) {
					match = false;
					break;
				}
			}
			if (match) { // if at this point it is a match, increase the count
				count[c]++;
			}	
		}
		data_in.close();
	}
		
	try {
		File fout = new File("/home/khanhduyuser/AprioriJava/outputApriori/output.txt");
		FileOutputStream fos = new FileOutputStream(fout.getAbsoluteFile(), true);
		 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	
		bw.write("K-Itemset: "+itemsets.get(0).length);
		bw.write(" So Transaction: "+dem);
		bw.newLine();
		bw.close();
        } 
	catch (Exception e) {
            	System.out.println(e);
        }
		
	for (int i = 0; i < itemsets.size(); i++) {
		// if the count% is larger than the minSup%, add to the candidate to
		// the frequent candidates
		if ((count[i] / (double) (numTransactions)) >= minSup) {
			foundFrequentItemSet(itemsets.get(i),count[i]);
			frequentCandidates.add(itemsets.get(i));
		}
	}

	System.out.println("K-Itemset: "+itemsets.get(0).length);
	System.out.println("So Transaction: "+dem);

        //new candidates are only the frequent candidates
        itemsets = frequentCandidates;
    }
}
