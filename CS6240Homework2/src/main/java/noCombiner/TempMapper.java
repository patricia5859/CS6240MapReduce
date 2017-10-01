package noCombiner;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class TempMapper extends MapReduceBase implements Mapper<Object, Text, Text, TwoWritable> {
	
	// variables
	String TMIN = "TMIN";
	String TMAX = "TMAX";


	public void map(Object key, Text value, OutputCollector<Text, TwoWritable> output, Reporter r) throws IOException {

		// extract values
		String line = value.toString();
		String[] values = line.split(",");
		int minCount = 1;
		int maxCount = 1;
		int temperature = Integer.parseInt(values[3]);
		
		Text stationID = new Text(values[0]);
		
		/* check if type of temperature is either TMAX or TMIN,
		*  if it is of type TMAX emit <stationID, (0, TMAXTemperature)> as <k,v>
		*  if it is of type TMIN emit <stationID, (TMINTemperature, 0)> as <k,v>
		*/
		if (values[2].equals(TMIN)) {
			output.collect(stationID, new TwoWritable(temperature, minCount, Integer.MAX_VALUE, 0));
			System.out.println("From map--------------------------------"+ stationID + " Min : "+ temperature + " "+ minCount);
		} else if (values[2].equals(TMAX)) {
			output.collect(stationID, new TwoWritable(Integer.MIN_VALUE, 0, temperature, maxCount));
			System.out.println("From map--------------------------------"+ stationID + " Max : "+ temperature + " "+ maxCount);
		}
	}

}
