/*
 * Following class is used to load the data from an input file
 * and create a List<String> containing all rows from a csv file.
 * 
 *	@Methods: 
 *
 *		1. loaderRoutine(): reads a file and create List<String>
 *		2. getList(): returns the List<String> created
 * 	
 * 	@Input: File Path
 * 
 * 	@Output: List<String> of all rows from csv file
 * 
 * 	@Note: Uncomment the main() method to run the file individually
 * 
 * */

package SequentialAndLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Loader {
	List<String> list;

	// loader routine to read file and return list of lines
	public void loaderRoutine(String filePath) {

		// variables
		FileReader fileReader = null;
		BufferedReader br;
		

		// Read file
		try {
			fileReader = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		br = new BufferedReader(fileReader);

		// Populate list to store each line from file
		list = new ArrayList<String>();

		// extract lines from file
		String line;
		try {
			while ((line = br.readLine()) != null) {
				list.add(line);
				// System.out.println(line);
			}
			//System.out.println(list.size());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	// getter for list
	public List<String> getList() {
		return list;
	}

//	public static void main(String args[]) {
//
//		Loader loader = new Loader();
//		List<String> list = new ArrayList<String>();
//		String filePath = args[0];
//
//		// invoke loaderRoutine() 
//		loader.loaderRoutine(filePath);
//		list = loader.getList();
//	}
}
