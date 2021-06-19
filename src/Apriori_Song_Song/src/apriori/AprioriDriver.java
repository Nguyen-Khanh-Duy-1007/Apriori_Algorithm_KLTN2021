package apriori;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class AprioriDriver extends Configured implements Tool {

    static String jobPrefix = "Phase ";

    @Override
    public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        if (args.length != 5) {
            System.err.println("USAGE : hadoop jar HadoopBasedApriori.jar AprioriDriver [dataSet] [outputFileName] [passNum] [minSup] [numTransactions]");
            return -1;
        }

        String hdfsInputDir = args[0];                        // dataSet
        String hdfsOutputDirPrefix = args[1];                 // outputFileName
        int maxPasses = Integer.parseInt(args[2]);            // passNum
        Double minSup = Double.parseDouble(args[3]);          // minSup
        Integer numTransactions = Integer.parseInt(args[4]);  // numberOfTransaction

        for (int i = 1; i <= maxPasses; i++) {
            boolean isPassKJobDone = runPassKJob(hdfsInputDir, hdfsOutputDirPrefix, i, minSup, numTransactions);
            if (!isPassKJobDone) {
                System.err.println("Phase1 MapReduce job failed. Exiting !!");
                return -1;
            }
        }
        return 1;
    }

    static boolean runPassKJob(String inputDir, String outputDirPrefix, int passNum, Double minSup, Integer numTransactions)
            throws IOException, InterruptedException, ClassNotFoundException {

        boolean isJobSuccess;

        // Create configuration
        Configuration conf = new Configuration();
        conf.setInt("passNum", passNum);
        conf.setDouble("minSup", minSup);
        conf.setInt("numTxns", numTransactions);
        System.out.println("Starting Phase" + passNum + "Job");

        // Create job
        Job job = new Job(conf, jobPrefix + passNum);

        job.setJarByClass(AprioriDriver.class);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(outputDirPrefix + passNum));

        // Specify key / value type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Setup map reduce job
        if (passNum == 1) {
            job.setMapperClass(AprioriPass1Mapper.class);
        }
        else {
            job.setMapperClass(AprioriPassKMapper.class);
        }
        job.setReducerClass(AprioriReducer.class);

        // Execute job
        isJobSuccess = (job.waitForCompletion(true) ? true : false);
        System.out.println("Finished Phase" + passNum + "Job");

        return isJobSuccess;
    }

    public static void main(String[] args) throws Exception {
	long startTime = System.nanoTime();
        int exitCode = ToolRunner.run(new AprioriDriver(), args);
	long endTime   = System.nanoTime();
	double totalTime = (endTime - startTime)/1000000000;
	System.out.println("Thoi gian thuc thi: " + totalTime);
	
        System.exit(exitCode);
    }
}
