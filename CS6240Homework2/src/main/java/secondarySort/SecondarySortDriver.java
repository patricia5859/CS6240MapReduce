package secondarySort;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import noCombiner.TwoWritable;


// class for composite key containing stationID, year

class MapKey implements Writable, WritableComparable<MapKey> {

	// variables

	String stationID;
	String year;

	// constructors

	public MapKey() {
		stationID = "";
		year = "";
	}

	public MapKey(String stationID, String year) {
		this.stationID = stationID;
		this.year = year;
	}

	// overriden methods

	public void write(DataOutput out) throws IOException {
		out.writeUTF(stationID);
		out.writeUTF(year);

	}

	public void readFields(DataInput in) throws IOException {
		stationID = in.readUTF();
		year = in.readUTF();

	}

	public int compareTo(MapKey mapObject) {

		int res = stationID.compareTo(mapObject.stationID);
		if (res == 0) {
			return year.compareTo(mapObject.year);
		}

		return res;
	}
	
	@Override
	public String toString() {
		return (stationID + " " + year + " ");
	}

}

// class to store weather record: TMAX or TMIN with count

class WeatherRecord implements Writable {

	// variables

	int minTemp;
	int minCount;
	int maxTemp;
	int maxCount;
	String year;

	// constructor
	
	public WeatherRecord() {}

	public WeatherRecord(int minTemp, int minCount, int maxTemp, int maxCount, String year) {
		this.minTemp = minTemp;
		this.minCount = minCount;
		this.maxTemp = maxTemp;
		this.maxCount = maxCount;
		this.year = year;
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(minTemp);
		out.writeInt(minCount);
		out.writeInt(maxTemp);
		out.writeInt(maxCount);
		out.writeUTF(year);

	}

	public void readFields(DataInput in) throws IOException {
		minTemp = in.readInt();
		minCount = in.readInt();
		maxTemp = in.readInt();
		maxCount = in.readInt();
		year = in.readUTF();
	}
	
	@Override
	public String toString() {
		return (minTemp + " " + minCount + " " + maxTemp + " " + maxCount);
	}

}

// Combiner class for Map output

class WeatherCombiner extends Reducer<MapKey, WeatherRecord, MapKey, WeatherRecord> {

	@Override
	public void reduce(MapKey key, Iterable<WeatherRecord> values, Context context) throws IOException {

		// variables

		int sumMax = 0;
		int sumMin = 0;
		int maxCount = 0;
		int minCount = 0;
		String year = null;
		
		Iterator<WeatherRecord> ite = values.iterator();

		while (ite.hasNext()) {
			WeatherRecord obj = ite.next();

			/*
			 * check which type of temperature value: TMAX or TMIN exits in
			 * current obj from list of values
			 */
			if (obj.minTemp != Integer.MIN_VALUE) {
				sumMin += obj.minTemp;
				minCount += obj.minCount;
			} else {
				sumMax += obj.maxTemp;
				maxCount += obj.maxCount;
			}

			year = obj.year;
		}

		try {
			context.write(key, new WeatherRecord(sumMin, minCount, sumMax, maxCount, year));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("In combiner: "+ key + " "+ sumMin + " " +
		// minCount + " " + sumMax + " " +maxCount);

	}
}

// Grouping Comparator to group all records with same stationID

class WeatherGroupingComparator extends WritableComparator {

	// constructor

	public WeatherGroupingComparator() {
		super(MapKey.class, true);
	}

	@Override
	public int compare(WritableComparable w1, WritableComparable w2) {
		MapKey key1 = (MapKey) w1;
		MapKey key2 = (MapKey) w2;

		return (key1.stationID.compareTo(key2.stationID));

	}

}

/*
 * Custom Partitioner to partition the output from map based on stationIDs
 */

class WeatherCustomPartitioner extends Partitioner<MapKey, WeatherRecord> {
	// @Override
	public int getPartition(MapKey key, WeatherRecord temp, int reducers) {
		// System.out.println(key.stationId.hashCode()%reducers);
		return ((key.stationID.hashCode() & Integer.MAX_VALUE) % reducers);
	}
}

/*
 * Mapper class in which each map() receives a data entry, parse it emit <K, V>
 * as <MapKey, WeatherRecord>
 */

class WeatherMapper extends Mapper<Object, Text, MapKey, WeatherRecord> {

	// variables
	String TMIN = "TMIN";
	String TMAX = "TMAX";

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {

		// variables

		int minCount = 1;
		int maxCount = 1;

		String line = value.toString();
		String[] values = line.split(",");

		// extract values

		String stationID = values[0];
		int temperature = Integer.parseInt(values[3]);
		String year = values[1].substring(0, 4);

		MapKey mapKey = new MapKey(stationID, year);

		/*
		 * check if type of temperature is either TMAX or TMIN, if it is of type
		 * TMAX emit <stationID, (0, TMAXTemperature)> as <k,v> if it is of type
		 * TMIN emit <stationID, (TMINTemperature, 0)> as <k,v>
		 */
		if (values[2].equals(TMIN)) {
			context.write(mapKey, new WeatherRecord(temperature, minCount, Integer.MAX_VALUE, 0, year));
			// System.out.println("From map--------------------------------"+
			// stationID + " Min : "+ temperature);
		} else if (values[2].equals(TMAX)) {
			context.write(mapKey, new WeatherRecord(Integer.MIN_VALUE, 0, temperature, maxCount, year));
			// System.out.println("From map--------------------------------"+
			// stationID + " Max : "+ temperature);
		}
	}

}

/*
 * Reducer class in which each reduce call iterates over weather record per
 * station per year and finally emits values for following string:
 * "StationID, [(Year0, MeanMin0, MeanMax0), (Year1, MeanMin1, MeanMax1)........]"
 */

class WeatherReducer extends Reducer<MapKey, WeatherRecord, NullWritable, Text> {

	@Override
	public void reduce(MapKey key, Iterable<WeatherRecord> records, Context context) throws IOException, InterruptedException {

		// variables

		String outputString = key.stationID + ", [";
		int sumMax = 0;
		int sumMin = 0;
		int maxCount = 0;
		int minCount = 0;
		String year = null;
		Iterator<WeatherRecord> ite = records.iterator();

		while(ite.hasNext()) {
			
			WeatherRecord record = ite.next();

			if (year != null && (!year.equals(record.year))) {
				outputString += "(" + year + ", ";
				outputString += (minCount > 0) ? (sumMin / minCount) + "," : "None ,";
				outputString += (maxCount > 0) ? (sumMax / maxCount) + ",)" : "None ,)";
				sumMax = 0;
				sumMin = 0;
				maxCount = 0;
				minCount = 0;

			}

			/*
			 * check which type of temperature value: TMAX or TMIN exits in
			 * current record from list of values
			 */
			if (record.minTemp != Integer.MIN_VALUE) {
				sumMin += record.minTemp;
				minCount += record.minCount;
			} if (record.maxTemp != Integer.MAX_VALUE){
				sumMax += record.maxTemp;
				maxCount += record.maxCount;
			}

			year = record.year;
		}
		
		outputString += "(" + year + ", ";
        outputString += ( minCount > 0) ?(sumMin / minCount)+ "," : "None ,";
        outputString += (maxCount > 0) ?(sumMax / maxCount)+ ",)]" : "None ,)]";

		// emit the final result
        context.write(NullWritable.get(), new Text(outputString));
	}

}

// Driver class for Secondary Sort

public class SecondarySortDriver extends Configured implements Tool {

	public int run(String args[]) throws Exception {

		Configuration conf = new Configuration();
		Job job = new Job(conf, "MinMaxtTemp");
		// the key is "stationID"
		
		job.setOutputKeyClass(Text.class);
		
		// the value will be combination of "MeanMinTemp, MeanMaxTemp"
		
		job.setOutputValueClass(Text.class);

		// set all the classes
		
		job.setMapperClass(WeatherMapper.class);
		job.setReducerClass(WeatherReducer.class);
		job.setCombinerClass(WeatherCombiner.class);
		job.setPartitionerClass(WeatherCustomPartitioner.class);
		job.setGroupingComparatorClass(WeatherGroupingComparator.class);
//		job.setCombinerKeyGroupingComparator(WeatherGroupingComparator.class);
		
		
		// read command line arguments
		List<String> other_args = new ArrayList<String>();
		for (int i = 0; i < args.length; ++i) {
			other_args.add(args[i]);
		}
		
		// make sure there are exactly 2 parameters left.
		if (other_args.size() != 3) {
			System.out.println("ERROR: Wrong number of parameters: " + other_args.size() + " instead of 2.");
			return printUsage();
		}

		// set input/output path
		for (int i = 0; i < 2; i++) {
			FileInputFormat.addInputPaths(job, other_args.get(i));
		}
		FileOutputFormat.setOutputPath(job, new Path(other_args.get(2)));

		// set
		job.setMapOutputKeyClass(MapKey.class);
		job.setMapOutputValueClass(WeatherRecord.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		// assign the job to Hadoop framework
		job.waitForCompletion(true);

		return 0;
	}

	static int printUsage() {
		System.out.println("mean min/max temperature [-m <maps>] [-r <reduces>] <input> <output>");
		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}

	// entry point of the code
	public static void main(String args[]) throws Exception {

		int exitCode = ToolRunner.run(new Configuration(), new SecondarySortDriver(), args);
		System.exit(exitCode);
	}

}
