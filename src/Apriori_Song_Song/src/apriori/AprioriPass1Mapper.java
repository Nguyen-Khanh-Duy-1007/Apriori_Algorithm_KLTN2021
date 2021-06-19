package apriori;

import list.Transaction;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import utils.AprioriUtils;
import java.io.IOException;

public class AprioriPass1Mapper extends Mapper<LongWritable, Text, Text, IntWritable>
{
    final static IntWritable one = new IntWritable(1);
    Text item = new Text();

    @Override
    public void map(LongWritable key, Text txnRecord, Context context)
            throws IOException, InterruptedException {
        Transaction txn = AprioriUtils.getTransaction((int) key.get(), txnRecord.toString());
        /** COMPLETE **/
        for(Integer val : txn) { // Go by each element in a transaction
        	item.set("[" + val + "]"); 
        	context.write(item, one); // Emit encountered values with one
        }
    }
}
