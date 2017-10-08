package noCombiner;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TempMapper extends Mapper<Object, Text, Text, TwoWritable> {
	
	// variables
	String TMIN = "TMIN";
	String TMAX = "TMAX";


	@Override
	public void map(Object key, Text value, Context context) throws IOException {

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
			try {
				context.write(stationID, new TwoWritable(temperature, minCount, Integer.MAX_VALUE, 0));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("From map--------------------------------"+ stationID + " Min : "+ temperature);
		} else if (values[2].equals(TMAX)) {
			try {
				context.write(stationID, new TwoWritable(Integer.MIN_VALUE, 0, temperature, maxCount));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("From map--------------------------------"+ stationID + " Max : "+ temperature);
		}
	}

}
