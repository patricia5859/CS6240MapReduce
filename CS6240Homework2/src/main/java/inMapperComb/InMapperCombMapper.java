package inMapperComb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import noCombiner.TwoWritable;

public class InMapperCombMapper extends MapReduceBase implements Mapper<Object, Text, Text, TwoWritable> {

	// variables
	String TMIN = "TMIN";
	String TMAX = "TMAX";
	HashMap<String, int[]> hashMap = new HashMap<String, int[]>();
	int[] arr = new int[4];

	public void map(Object key, Text value, OutputCollector<Text, TwoWritable> output, Reporter r) throws IOException {

		// extract values
		String line = value.toString();
		String[] values = line.split(",");
		int minCount = 1;
		int maxCount = 1;
		int temperature = Integer.parseInt(values[3]);

		Text stationID = new Text(values[0]);

		/*
		 * check if type of temperature is either TMAX or TMIN, if it is of type
		 * TMAX emit <stationID, (0, TMAXTemperature)> as <k,v> if it is of type
		 * TMIN emit <stationID, (TMINTemperature, 0)> as <k,v>
		 */
		if (values[2].equals(TMIN)) {
			if (hashMap.containsKey(stationID.toString())) {
				arr[0] += hashMap.get(stationID.toString())[0];
				arr[1] = hashMap.get(stationID.toString())[1] + 1;
			} else {
				arr[0] = temperature;
				arr[1] = minCount;
			}
			arr[2] = Integer.MAX_VALUE;
			arr[3] = 0;

			hashMap.put(key.toString(), arr);
			// output.collect(stationID, new TwoWritable(temperature, minCount,
			// Integer.MAX_VALUE, 0));
			// System.out.println("From map--------------------------------"+
			// stationID + " Min : "+ temperature);
		} else if (values[2].equals(TMAX)) {

			if (hashMap.containsKey(stationID.toString())) {
				arr[2] += hashMap.get(stationID.toString())[0];
				arr[3] = hashMap.get(stationID.toString())[1] + 1;
			} else {
				arr[2] = temperature;
				arr[3] = minCount;
			}
			arr[0] = Integer.MIN_VALUE;
			arr[1] = 0;

			hashMap.put(stationID.toString(), arr);
			//output.collect(stationID, new TwoWritable(Integer.MIN_VALUE, 0, temperature, maxCount));
			// System.out.println("From map--------------------------------"+
			// stationID + " Max : "+ temperature);
			
			System.out.println("In map()------------------------------");
		}
	}
	
	public void cleanup(OutputCollector<Text, TwoWritable> output) {
		System.out.println("In map-combiner: -------------------------"); 
		/*
		 * iterate over hashMap, emit each <k, v> pair
		 * */
		for(Map.Entry<String, int[]> entry: hashMap.entrySet()) {
			int[] values = entry.getValue();
			try {
				output.collect(new Text(entry.getKey()), 
						new TwoWritable(values[0], values[1], values[2], values[3]));
				
						//entry.getKey() + " " + values[0] + " " + values[1] + " " + values[2] + " " + values[3] );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}