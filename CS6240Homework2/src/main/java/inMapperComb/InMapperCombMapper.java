package inMapperComb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import noCombiner.TwoWritable;

public class InMapperCombMapper extends Mapper<Object, Text, Text, TwoWritable> {

	// variables
	String TMIN = "TMIN";
	String TMAX = "TMAX";
	HashMap<String, int[]> hashMap = new HashMap<String, int[]>();
	
	
	@Override
	public void map(Object key, Text value, Context context) throws IOException {

		// Array for minTemp, minCount, maxTemp, maxCount
		int[] arr = {0, 0, 0, 0};
		
		// extract values
		String line = value.toString();
		String[] values = line.split(",");
		int temperature = Integer.parseInt(values[3]);
		
		System.out.println("");
		for(int i=0; i<values.length; i++) {
			System.out.print(values[i] + "  ");
		}
		
		System.out.println("---------------------");

		Text stationID = new Text(values[0]);
		
		/* 
		 * Extract values for minTemp, countMin, maxTemp, countMax
		 * if hashMpa contains entry for stationID
		 * */ 
		
		if(hashMap.containsKey(stationID.toString())) {
			arr[0] = hashMap.get(stationID.toString())[0];
			arr[1] = hashMap.get(stationID.toString())[1];
			arr[2] = hashMap.get(stationID.toString())[2];
			arr[3] = hashMap.get(stationID.toString())[3];
		}
		
		if (values[2].equals(TMIN)) {
			arr[0] += temperature;
			arr[1] ++;
		}
		
		else if(values[2].equals(TMAX)) {
			arr[2] += temperature;
			arr[3] ++;
		}
		
		hashMap.put(stationID.toString(), arr);

		/*
		 * check if type of temperature is either TMAX or TMIN, if it is of type
		 * TMAX emit <stationID, (0, TMAXTemperature)> as <k,v> if it is of type
		 * TMIN emit <stationID, (TMINTemperature, 0)> as <k,v>
		 */
//		if (values[2].equals(TMIN)) {
//			
//			if (hashMap.containsKey(stationID.toString())) {
//				arr[0] = hashMap.get(stationID.toString())[0] + temperature;
//				arr[1] = hashMap.get(stationID.toString())[1] + 1;
//			} else {
//				arr[0] = temperature;
//				arr[1] = minCount;
//			}
//
//		} else if (values[2].equals(TMAX)) {
//
//			if (hashMap.containsKey(stationID.toString())) {
//				arr[2] = hashMap.get(stationID.toString())[2] + temperature;
//				arr[3] = hashMap.get(stationID.toString())[3] + 1;
//			} else {
//				arr[2] = temperature;
//				arr[3] = maxCount;
//			}
//		}
//		
		System.out.println("");
		
		for(int i=0; i<arr.length; i++) {
			System.out.print(arr[i] + "  ");
		}
		
		//System.out.println("---------------------");
		
	}
	
	
	@Override
	public void cleanup(Context context) {
		
		/*
		 * iterate over hashMap, emit each <k, v> pair
		 * */
		for(Map.Entry<String, int[]> entry: hashMap.entrySet()) {
			int[] values = entry.getValue();
			try {
				try {
					context.write(new Text(entry.getKey()), 
							new TwoWritable(values[0], values[1], values[2], values[3]));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}