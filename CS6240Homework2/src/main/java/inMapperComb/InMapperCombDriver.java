package inMapperComb;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import noCombiner.TempReducer;
import noCombiner.TwoWritable;

public class InMapperCombDriver extends Configured implements Tool {

	public int run(String args[]) throws Exception {

		JobConf job = new JobConf(getConf(), InMapperCombDriver.class);
		job.setJobName("MinMaxTemperature");

		// the key is "stationID"
		job.setOutputKeyClass(Text.class);
		// the value will be combination of "MeanMinTemp, MeanMaxTemp"
		job.setOutputValueClass(Text.class);

		job.setMapperClass(InMapperCombMapper.class);
		job.setReducerClass(TempReducer.class);

		// read command line arguments
		List<String> other_args = new ArrayList<String>();
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-m".equals(args[i])) {
					job.setNumMapTasks(Integer.parseInt(args[++i]));
				} else if ("-r".equals(args[i])) {
					job.setNumReduceTasks(Integer.parseInt(args[++i]));
				} else {
					other_args.add(args[i]);
				}
			} catch (NumberFormatException except) {
				System.out.println("ERROR: Integer expected instead of " + args[i]);
				return printUsage();
			} catch (ArrayIndexOutOfBoundsException except) {
				System.out.println("ERROR: Required parameter missing from " + args[i - 1]);
				return printUsage();
			}
		}
		// make sure there are exactly 2 parameters left.
		if (other_args.size() != 2) {
			System.out.println("ERROR: Wrong number of parameters: " + other_args.size() + " instead of 2.");
			return printUsage();
		}

		// set input/output path
		FileInputFormat.setInputPaths(job, other_args.get(0));
		FileOutputFormat.setOutputPath(job, new Path(other_args.get(1)));

		// set
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(TwoWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(TwoWritable.class);

		// assign the job to Hadoop framework
		JobClient.runJob(job);

		return 0;
	}

	static int printUsage() {
		System.out.println("mean min/max temperature [-m <maps>] [-r <reduces>] <input> <output>");
		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}

	// entry point of the code
	public static void main(String args[]) throws Exception {

		int exitCode = ToolRunner.run(new Configuration(), new InMapperCombDriver(), args);
		System.exit(exitCode);
	}

}
