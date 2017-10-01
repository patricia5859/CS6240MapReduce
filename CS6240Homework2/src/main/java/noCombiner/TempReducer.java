package noCombiner;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class TempReducer extends MapReduceBase implements Reducer<Text, TwoWritable, Text, Text> {

	public void reduce(Text key, Iterator<TwoWritable> values, OutputCollector<Text, Text> output, Reporter r)
			throws IOException {
			
		// variables
		int sumMax = 0;
		int sumMin = 0;
		int countMax = 0;
		int countMin = 0;
		int minTemp = 0;
		int maxTemp = 0;
		int[] arr;
		
		while (values.hasNext()) {
			TwoWritable obj = values.next();
			
			// get TMIN & TMAX values
			arr = obj.getFields();
			
			/*
			 * check which type of temperature value exits in current obj 
			 * from list of values
			 * */
			if (arr[0] != Integer.MIN_VALUE) {
				sumMin += arr[0];
				countMin += arr[1];
			} 
			if (arr[2] != Integer.MAX_VALUE){
				sumMax += arr[2];
				countMax += arr[3];
			}
			
			//System.out.println("In Reducer: ---------------------------" +
			//key + " " + sumMin + " " + countMin + " " + sumMax + " " + countMax );
		}
		
		String value = "";
		
		if(countMin !=0 && countMax !=0) {
			// calculate average for both TMIN and TMAX for each station
			value = (sumMin/countMin) + " , " + (sumMax/countMax);
		}

		// emit the final result
		output.collect(key, new Text(value));
	}

}
