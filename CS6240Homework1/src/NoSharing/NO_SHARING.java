/*
 *  NO_SHARING includes following methods:
 * 
 * 			a. main(): Entry point of the program
 * 
 * 			b. mergeAllMaps(): Combines all data structures from number of threads to final result
 * 
 * 			c. getIndicesForSubList(): Calculate start, end index for subset of list

 * 			d. getAverageTemp(): Calculates the average temperature for all stations
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

package NoSharing;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import SequentialAndLoader.Loader;

public class NO_SHARING {

	// Global variables
	int sizeOfList;
	int subSetSize;
	int startIndex;
	int endIndex;
	HashMap<String, Integer> avgTemp;

	public static void main(String args[]) {

		String filePath;
		List<String> list;
		ArrayList<HashMap<String, int[]>> accumulationDSList; // list of accumulation data structures
		HashMap<String, int[]> finalDataStructure = new HashMap<>(); // will hold combined <key, values>
		PrintWriter writer = null;
		String outputFilePath = "Output_NO_SHARING.txt";
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
		NO_SHARING noShareObj = new NO_SHARING();

		try {
			writer = new PrintWriter(new File(outputFilePath));

			// Invoke loaderRoutine() from Loader.java
			load.loaderRoutine(filePath);

			// Get list of all lines from file
			list = load.getList();
			writer.println("Total records: " + list.size());
			
			// Instantiate number of accumulation data structures 
			accumulationDSList = new ArrayList<HashMap<String, int[]>>(); 
			
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
						int[] indices = noShareObj.getIndicesForSubList(list, j, processors);
						
						// Create a new accumulation data structure for each thread
						HashMap<String, int[]> map = new HashMap<>();
						accumulationDSList.add(map);

						// create and start thread
						threadName[j] = new Thread(
								new NoSharingRunner((list.subList(indices[0], indices[1])), map, fibonaciiFlag));
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
					
					finalDataStructure = noShareObj.mergeAllMaps(accumulationDSList);

					// Invoke method to find average of all stations
					noShareObj.getAverageTemp(finalDataStructure);

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
			writer.println("Total stations: " + finalDataStructure.size());

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			writer.flush();
			writer.close();
		}
	}
	
	// Method to merge all Maps
	public HashMap<String, int[]> mergeAllMaps(ArrayList<HashMap<String, int[]>> accumulationDSList) {
		
		HashMap<String, int[]> finalAccumulationDS = new HashMap<>();
		Set<String> tempKeySet = new HashSet<>();
		int[] values = new int[2];
		int sum = 0;
		int count = 0;

		// Add all keys into tempSet
		for (int i=0; i <accumulationDSList.size(); i++) {
			tempKeySet.addAll(accumulationDSList.get(i).keySet());
		}
		
		// Iterate over each key and check if any data structure contains that key
		for (String key: tempKeySet) {
			for (int i=0; i < accumulationDSList.size(); i++) {
				
				// If key exists in data structure add its value
				if (accumulationDSList.get(i).containsKey(key)) {
					sum += accumulationDSList.get(i).get(key)[0];
					count += accumulationDSList.get(i).get(key)[1];
				}
			}
			
			values[0] = sum;
			values[1] = count;
			
			// Add <key, value> to final data structure
			finalAccumulationDS.put(key, values);
		}
		
		return finalAccumulationDS;
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
		} else {
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
				} else {
					avgTemp.put(entry.getKey(), (sumCountForEachStation[0] / sumCountForEachStation[1]));
				}
			} else {
				avgTemp.put(entry.getKey(), 0);
			}
		}

		return avgTemp;
	}

}
