/*
 *  CoarseLockRunner: Implements Runnable Interface and includes following methods:
 *  		
 *  		a. run(): Overrriden method which stores <key, value> pairs in data structure,
 *  				  along with a lock on the accumulation data structure 
 *  		
 *  		b. fibonacii(): Method to calculate fibonacci number and introduce delays.
 * */

package CoarseLock;

import java.util.HashMap;
import java.util.List;

class CoarseLockRunner implements Runnable {

	// Variables
	HashMap<String, int[]> accumulationDS;
	HashMap<String, Integer> avgTemp;
	List<String> subList;
	String splitBy = ",";
	String TMAX = "TMAX";
	Boolean fibonaciiFlag = false;

	// Constructor
	public CoarseLockRunner(List<String> list, HashMap<String, int[]> accumulationDS, Boolean fibonaciiFlag) {

		this.subList = list;
		this.accumulationDS = accumulationDS;
		this.fibonaciiFlag = fibonaciiFlag;
	}

	@Override
	public void run() {
		// iterate over each line
		for (String eachLine : subList) {
			String[] entries = eachLine.split(splitBy);
			String stationID = entries[0];
			String type = entries[2];
			String temp = entries[3];
			int temperature = Integer.parseInt(temp);
			int count = 0;
			int sum = 0;

			// Array with accumulated sum, count of number of records for each
			// station
			int[] valueForEachStation = new int[2];

			// Apply lock on the accumulation data structure
			synchronized (accumulationDS) {

				// Check if type of temperature is TMAX
				if (type.equals(TMAX)) {

					// Check if stationID already exists in DS
					if (accumulationDS.containsKey(stationID)) {
						sum = accumulationDS.get(stationID)[0];
						count = accumulationDS.get(stationID)[1];
					}

					// Introduce delay if fibonaciiFlag is true
					if (fibonaciiFlag) {
						long c = fibonacci(17);
					}

					// Increase the sum and count
					sum += temperature;
					count++;
					valueForEachStation[0] = sum;
					valueForEachStation[1] = count;

					// Add <key, value> in the accumulationDS
					accumulationDS.put(stationID, valueForEachStation);

				}
			}
		}
	}

	// Program for Fibonacii numbers
	public static long fibonacci(int n) {
		if (n < 0)
			return (long) -1;
		else if (n == 0)
			return (long) 0;
		else if (n == 1)
			return (long) 1;
		else
			return (long) (fibonacci(n - 1) + fibonacci(n - 2));
	}

}
