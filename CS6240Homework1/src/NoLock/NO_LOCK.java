/*
 *	 NO_LOCK: Main class and includes following methods:
 * 
 * 			a. main(): Entry point of the program
 * 
 * 			b. getIndicesForSubList(): Calculate start, end index for subset of list

 * 			c. getAverageTemp(): Calculates the average temperature for all stations
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

package NoLock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import SequentialAndLoader.Loader;

import java.util.Scanner;

public class NO_LOCK {

	// Global variables
	int sizeOfList;
	int subSetSize;
	int startIndex;
	int endIndex;
	HashMap<String, Integer> avgTemp;

	// Entry point of the program
	public static void main(String args[]) {

		// Variables
		Scanner sc;
		String filePath;
		List<String> list;
		PrintWriter writer = null;
		String outputFilePath = "Output_NO_LOCK.txt";
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

		// Find out number of threads that can be spawned
		int processors = Runtime.getRuntime().availableProcessors();

		// Initialization
		SequentialAndLoader.Loader load = new SequentialAndLoader.Loader();
		NO_LOCK noLockObj = new NO_LOCK();
		HashMap<String, int[]> accumulationDS = new HashMap<String, int[]>();

		try {
			writer = new PrintWriter(new File(outputFilePath));

			// Invoke loaderRoutine() from Loader.java
			load.loaderRoutine(filePath);

			// Get list of all lines from file
			list = load.getList();
			writer.println("Total records: " + list.size());

			// Loop to time execution of code 10 times
			for (int i = 0; i < numberOfExecutions; i++) {
				
				try {
					// Note start time
					long start = System.currentTimeMillis();

					// Initialize threads
					Thread[] threadName = new Thread[processors];
					
					// create as many number of threads that can be spawned
					for (int j = 0; j < processors; j++) {
						
						// Get start, end index for subset of list
						int[] indices = noLockObj.getIndicesForSubList(list, j, processors);
						
						// create and start thread
						threadName[j] = new Thread(new NoLockRunner((list.subList(indices[0], indices[1])), accumulationDS, fibonaciiFlag));
						threadName[j].start();
					}

					// Wait for threads to complete
					for (int k = 0; k < threadName.length; k++) {
						try {
							threadName[k].join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// Invoke method to find average of all stations
					noLockObj.getAverageTemp(accumulationDS);

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

			// Log total number of stations
			writer.println("Total stations: " + accumulationDS.size());

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			writer.flush();
			writer.close();
		}

	}

	// Calculate start, end index for subset of list
	public int[] getIndicesForSubList(List list, int i, int processors) {

		// calculate size of sub set list for each thread
		sizeOfList = list.size();
		subSetSize = sizeOfList / processors;

		int[] indices = new int[2];

		// Set starting index
		indices[0] = i * subSetSize;

		// Check if it is the last subset
		if (i == (processors - 1)) {
			indices[1] = list.size(); // to compensate remaining lines in list
		} 
		else {
			indices[1] = ((i + 1) * subSetSize);
		}

		return indices;
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
				} 
				else {
					avgTemp.put(entry.getKey(), (sumCountForEachStation[0] / sumCountForEachStation[1]));
				}
			} 
			else {
				avgTemp.put(entry.getKey(), 0);
			}
		}

		return avgTemp;
	}

}
