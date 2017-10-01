package combiner;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import noCombiner.TwoWritable;

public class TempCombiner extends MapReduceBase implements Reducer<Text, TwoWritable, Text, TwoWritable> {

	public void reduce(Text key, Iterator<TwoWritable> values, OutputCollector<Text, TwoWritable> output,
			Reporter reporter) throws IOException {

		// variables
		int sumMax = 0;
		int sumMin = 0;
		int countMax = 0;
		int countMin = 0;
		int[] arr;

		while (values.hasNext()) {
			TwoWritable obj = values.next();

			// get TMIN & TMAX values
			arr = obj.getFields();

			/*
			 * check which type of temperature value: TMAX or TMIN exits in
			 * current obj from list of values
			 */
			if (arr[0] != Integer.MIN_VALUE) {
				sumMin += arr[0];
				countMin += arr[1];
			} else {
				sumMax += arr[2];
				countMax += arr[3];
			}
		}

		output.collect(key, new TwoWritable(sumMin, countMin, sumMax, countMax));
		System.out.println("In combiner: "+ key + " "+ sumMin + " " + countMin + " " + sumMax + " " +countMax);

	}

}
