package combiner;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import noCombiner.TwoWritable;

public class TempCombiner extends Reducer<Text, TwoWritable, Text, TwoWritable> {

	@Override
	public void reduce(Text key, Iterable<TwoWritable> values, Context context) throws IOException {

		// variables
		int sumMax = 0;
		int sumMin = 0;
		int countMax = 0;
		int countMin = 0;
		int[] arr;

		Iterator<TwoWritable> ite = values.iterator();
		while (ite.hasNext()) {
			TwoWritable obj = ite.next();

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

		try {
			context.write(key, new TwoWritable(sumMin, countMin, sumMax, countMax));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("In combiner: "+ key + " "+ sumMin + " " + countMin + " " + sumMax + " " +countMax);

	}

}
