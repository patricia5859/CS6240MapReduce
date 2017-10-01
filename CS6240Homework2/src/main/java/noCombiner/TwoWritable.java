package noCombiner;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;


public class TwoWritable implements Writable {

	// variables
	
	private int minTemp;
	private int maxTemp;
	private int minCount;
	private int maxCount;
	
	// constructors
	
	public TwoWritable() {
		minTemp = maxTemp = minCount = maxCount = 0;
	}

	public TwoWritable(int min, int minCount, int max, int maxCount) {
		this.minTemp = min;
		this.minCount = minCount;
		this.maxTemp = max;
		this.maxCount = maxCount;
	}
	
	// methods
	
	public void write(DataOutput out) throws IOException {
		out.writeInt(minTemp);
		out.writeInt(minCount);
		out.writeInt(maxTemp);
		out.writeInt(maxCount);
	}

	public void readFields(DataInput in) throws IOException {
		minTemp = in.readInt();
		minCount = in.readInt();
		maxTemp = in.readInt();
		maxCount = in.readInt();
	}
	
	public int[] getFields() {
		int[] arr = { minTemp, minCount, maxTemp, maxCount };
		return arr;
	}
	
}