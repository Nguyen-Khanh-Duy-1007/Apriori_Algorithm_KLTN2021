package apriori;

import list.ItemSet;
import list.Transaction;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import utils.AprioriUtils;
import trie.Trie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AprioriPassKMapper extends Mapper<LongWritable, Text, Text, IntWritable>
{
    final static IntWritable one = new IntWritable(1);
    Text item = new Text();
    List<ItemSet> itemSetsPrevPass = new ArrayList<>();
    List<ItemSet> candidateItemSets = null;
    Trie trie = null;

    @Override
    public void setup(Context context)
            throws IOException {
        /** COMPLETE **/ // Ignore this Complete
        int passNum = context.getConfiguration().getInt("passNum", 2);      // getInt(String name, int defaultValue) : Get the value of the name property as an int
        String lastPassOutputFile = "/home/khanhduyuser/output" + (passNum - 1) + "/part-r-00000";	

        // In try part, it reads the itemSet from the previous pass.
        try {
            Path path = new Path(lastPassOutputFile);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            BufferedReader fis = new BufferedReader(new InputStreamReader(fs.open(path)));
            String currLine;

            while ((currLine = fis.readLine()) != null) {
                currLine = currLine.replace("[", "");
                currLine = currLine.replace("]", "");
                currLine = currLine.trim();
                String[] words = currLine.split("[\\s\\t]+");
                if (words.length < 2) {
                    continue;
                }

                String finalWord = words[words.length - 1];
                int support = Integer.parseInt(finalWord);
                ItemSet itemSet = new ItemSet(support);

                for (int k = 0; k < words.length - 1; k++) {
                    String csvItemIds = words[k];
                    String[] itemIds = csvItemIds.split(",");
                    for (String itemId : itemIds) {
                        itemSet.add(Integer.parseInt(itemId));
                    }
                }
                itemSetsPrevPass.add(itemSet);
            }
        }
        catch (Exception e) {

        }
        // Generate the candidateItemSets using Self-Joining and pruning.
        candidateItemSets = AprioriUtils.getCandidateItemSets(itemSetsPrevPass, (passNum - 1));

        /** COMPLETE **/ // Ignore this Complete
        trie = new Trie(passNum);

        int candidateItemSetsSize = candidateItemSets.size();
        for (int i = 0; i < candidateItemSetsSize; i++) {
            ItemSet itemSet = candidateItemSets.get(i);
            trie.add(itemSet);
        }
    }

    public void map(LongWritable key, Text txnRecord, Context context)
            throws IOException, InterruptedException {
        Transaction txn = AprioriUtils.getTransaction((int) key.get(), txnRecord.toString());
        /** COMPLETE **/
        Collections.sort(txn); // Sort the transactions list 
        ArrayList<ItemSet> matchedItemSet = new ArrayList<>();
        trie.findItemSets(matchedItemSet, txn); // Find all the candidates matching with itemsets
        
        for(ItemSet itemSet : matchedItemSet) { // For each match emit it with value 1
        	item.set(itemSet.toString()); 
        	context.write(item, one);
        }
    }
}
