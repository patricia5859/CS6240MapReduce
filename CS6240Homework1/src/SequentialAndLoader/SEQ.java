/*
 * The file contains SEQ class with following methods:
 * 
 * 1. getAccumulationDS(): It creates the accumulation data structure
 * 
 * 2. getAverageTemp(): Calculates the average temperature for all stations
 * 
 * 3. fibonacci(): Method to calculate fibonacci number and introduce delays
 * 
 * 4. main(): Entry point of the program
 *   
 * 
 * @Input:
 * 
 *  1. Input file path
 *  2. FibonaciiFlag (true or false)
 *  
 * @Output:
 *  
 *  10 execution times, average execution time, minimum and maximum execution time
 *  
 * */

package SequentialAndLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SEQ {

	// variables
	HashMap<String, int[]> accumulationDS;
	HashMap<String, Integer> avgTemp;
	String splitBy = ",";
	String TMAX = "TMAX";
	Boolean fibonaciiFlag = false;

	// get data structure which stores <StationID, int[sum, count]>>
	public HashMap<String, int[]> getAccumulationDS(List<String> list, HashMap<String, int[]> accumulationDS,
			Boolean fibonaciiFlag) {

		// Map to store StationID, array with accumulated sum, count of records
		// accumulationDS = new HashMap<String, int[]>();

		// iterate over each line
		for (String eachLine : list) {
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

			if (type.equals(TMAX)) {

				// Check if stationID already exists in the data structure
				if (accumulationDS.containsKey(stationID)) {
					sum = accumulationDS.get(stationID)[0];
					count = accumulationDS.get(stationID)[1];
				}

				// Introduce delay if fibonaciiFlag is true
				if (fibonaciiFlag) {
					long c = fibonacci(17);
				}

				sum += temperature;
				count++;
				valueForEachStation[0] = sum;
				valueForEachStation[1] = count;
				accumulationDS.put(stationID, valueForEachStation);
			}
		}

		return accumulationDS;
	}

	// get average TMAX for all stations
	public HashMap<String, Integer> getAverageTemp(HashMap<String, int[]> accumulationDS) {

		// Map to store average TMAX for all stations
		avgTemp = new HashMap<String, Integer>();

		// iterate over each station
		for (Map.Entry<String, int[]> entry : accumulationDS.entrySet()) {

			// [sum, count],i.e. value of each stationID key
			int[] sumCountForEachStation = entry.getValue();

			if (sumCountForEachStation[0] != 0) {

				// if only one TMAX entry for station
				if (sumCountForEachStation[1] == 1) {
					avgTemp.put(entry.getKey(), sumCountForEachStation[0]);
				} else {
					avgTemp.put(entry.getKey(), (sumCountForEachStation[0] / sumCountForEachStation[1]));
				}
			} else {
				avgTemp.put(entry.getKey(), 0);
			}
		}

		return avgTemp;
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

	// Main entry point of program
	public static void main(String args[]) {

		// Variables
		Scanner sc;
		String filePath;
		List<String> list;
		HashMap<String, int[]> accumulationDS = new HashMap<String, int[]>();
		HashMap<String, Integer> avgTemp;
		PrintWriter writer = null;
		String outputFilePath = "Output_SEQ.txt";
		double[] executionTime = new double[10];
		double totalExecutionTime = 0;
		int numberOfExecutions = 10;
		double averageExecutionTime = 0;
		double minExecutionTime = 0;
		double maxExecutionTime = 0;
		Boolean fibonaciiFlag = false;

		// Read command line arguments:
		filePath = args[0];
		fibonaciiFlag = Boolean.parseBoolean(args[1]);

		// Initialization

		// Instance of Loader required to load the csv file and return
		// List<String>
		SequentialAndLoader.Loader load = new SequentialAndLoader.Loader();
		SEQ obj = new SEQ();

		try {
			writer = new PrintWriter(new File(outputFilePath));
			// Invoke loaderRoutine()
			load.loaderRoutine(filePath);

			// Get list of all lines from file
			list = load.getList();

			// Loop to time execution of code 10 times
			for (int i = 0; i < numberOfExecutions; i++) {

				try {
					// Note start time
					long start = System.currentTimeMillis();

					// Get accumulation data structure
					accumulationDS = obj.getAccumulationDS(list, accumulationDS, fibonaciiFlag);

					// Get average TMAX temperature for all stations
					avgTemp = obj.getAverageTemp(accumulationDS);

					// Note end time
					long end = System.currentTimeMillis();

					executionTime[i] = (double) (end - start) / 1000;

					if (i == 0) {
						minExecutionTime = executionTime[i];
						maxExecutionTime = executionTime[i];
					} else {
						if (executionTime[i] > maxExecutionTime) {
							maxExecutionTime = executionTime[i];
						}
						if (executionTime[i] < minExecutionTime) {
							minExecutionTime = executionTime[i];
						}
					}

					// Sum up the total execution time
					totalExecutionTime += executionTime[i];

					// Log total time taken
					writer.println("Time taken: " + executionTime[i]);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			writer.println("-------------------------------------------");

			averageExecutionTime = totalExecutionTime / numberOfExecutions;
			// Log average time taken
			writer.println("Average Time taken: " + averageExecutionTime);

			// Log Minimum Execution Time
			writer.println("Minimum Time taken: " + minExecutionTime);

			// Log Maximum Execution Time
			writer.println("Maximum Time taken: " + maxExecutionTime);

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			writer.flush();
			writer.close();
		}

	}
}
