import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static int MPS = 0;  // Index of Most Popular Song
	public static int SPS = 0;  // Index of Second Most Popular Song

	public static void main(String[] args) throws IOException {

		// Read value data
		List<List<String>> list = readValues();
		//System.out.println(list.get(0));

		// Initialize values and weights.
		List<Integer> valueList = new ArrayList<Integer>();
		List<Integer> weightList = new ArrayList<Integer>();

		// Assign weights and values
		for (int i = 1; i < list.size(); i++) {
			valueList.add(Integer.parseInt(list.get(i).get(4)));    // value is set to the song popularity
			weightList.add(Integer.parseInt(list.get(i).get(5)));   // weight is set to the song duration
		}

		long startTime = System.nanoTime();

		// Solve for problem 1 and get Most Popular and Second-most popular songs (MPS and SPS)
		solveProblem1(valueList, weightList);

		long endTime = System.nanoTime();

		System.out.println("Problem 1 was solved in " + (endTime - startTime) + " nanoseconds.");

		// Read sequential data
		List<List<String>> list1 = readSequential();
		List<ArrayList<Double>> sequential_data = new ArrayList<ArrayList<Double>>();

		// Assign the sequential data
		for (int i = 1; i < list1.size(); i++) {
			ArrayList<Double> row = new ArrayList<>();
			for (int j = 1; j < list1.get(0).size(); j++) {
				row.add(Double.parseDouble(list1.get(i).get(j)));
			}
			sequential_data.add(row);
		}

		long startTime2 = System.nanoTime();

		// Solve for problem 2
		solveProblem2(sequential_data, weightList);

		long endTime2 = System.nanoTime();

		System.out.println("Problem 1 was solved in " + (endTime2 - startTime2) + " nanoseconds.");

	}

	public static List<List<String>> readValues() throws IOException {
		try{
			List<List<String>> data = new ArrayList<>();	//list of lists to store data
			String file = "term_project_value_data.csv";	//file path
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			// Read until no lines left
			String line = br.readLine();
			while(line != null) {
				List<String> lineData = Arrays.asList(line.split(",")); //splitting lines
				data.add(lineData);
				line = br.readLine();
			}

			// Print the fetched data
            /*
            for(List<String> list : data) {
                for(String str : list) System.out.print(str + " ");
                System.out.println();
            }
            */
			br.close();

			return data;
		}

		catch(Exception e) {
			System.out.print(e);
			List< List<String> > data = new ArrayList<>();  //list of lists to store data
			return data;
		}
	}

	public static List<List<String>> readSequential() throws IOException {
		try
		{
			List< List<String> > data = new ArrayList<>();      //list of lists to store data
			String file = "term_project_sequential_data.csv";   //file path
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			// Read until no lines left
			String line = br.readLine();
			while(line != null)
			{
				List<String> lineData = Arrays.asList(line.split(",")); //splitting lines
				data.add(lineData);
				line = br.readLine();
			}

			//  Print the fetched data
            /*
            for(List<String> list : data)
            {
                for(String str : list)
                    System.out.print(str + " ");
                System.out.println();
            }
            */
			br.close();
			return data;
		}
		catch(Exception e)
		{
			System.out.print(e);
			List< List<String> > data = new ArrayList<>();//list of lists to store data
			return data;
		}

	}

	public static void solveProblem1(List<Integer> valueList, List<Integer> weightList) {
		System.out.println("##### Problem 1 #####");

		// Assert if the shape of valueList is equal to weightList.
		System.out.println(valueList.size() == weightList.size());

		// Initialize sortedGain, sortedIndices, and inclusion array.
		double[] sortedGain = new double[valueList.size()];
		int[] sortedIndices = new int[valueList.size()];
		int[] X_i = new int[valueList.size()];

		// Assign initial array values. All are either unsorted or 0 for now.
		for(int i = 0; i < sortedGain.length; i++) {
			// Assign unsorted gains. These gain values are in minutes (multiplied by 60000), otherwise division would round down to 0.
			sortedGain[i] = (valueList.get(i)*60_000) / weightList.get(i);

			// Assign indices as they are now.
			sortedIndices[i] = i;

			// Mark every song as excluded.
			X_i[i] = 0;
		}

		// Sort the songs.
		for (int i = 0; i < sortedGain.length; i++) {
			double maxGain = sortedGain[i];
			int maxIndex = i;

			for(int j = i+1; j < sortedGain.length; j++) {
				if( sortedGain[j] > maxGain) {
					maxIndex = j;
					maxGain = sortedGain[j];
				}
			}

			// Create swap values.
			double tempGain = sortedGain[i];

			// Swap the max value and the current value.
			sortedGain[i] = maxGain;
			sortedIndices[i] = maxIndex;
			sortedGain[maxIndex] = tempGain;
			sortedIndices[maxIndex] = i;

		}

		// Initialize cumulative total duration of included songs in ms.
		int totalDuration = 0;

		// Include the elements with most gain.
		for (int i = 0; i < sortedGain.length; i++) {
			// If the total duration + this iteration's song can be added. Add it moruk.
			if( (totalDuration + weightList.get(sortedIndices[i])) <= 1_800_000) {
				totalDuration += weightList.get(sortedIndices[i]);
				X_i[sortedIndices[i]] = 1;

			}
		}

		// Report the solution.
		System.out.println("Songs that are included: ");
		for(int i = 0; i < X_i.length; i++) {
			System.out.println(i + " " + X_i[i]);
		}

		// Even though the popularity is int it is initialized double to be safe in objective value calculation.
		// Since the punishment will be double, just to be safe with operating with both.
		double punishment = ( (1_800_000 - totalDuration) / 1000)*0.02;
		double popularity = 0;

		// Calculate objective function value
		for(int i = 0; i < X_i.length; i++) {
			if ( X_i[i] == 1) {
				popularity += valueList.get(sortedIndices[i]);
			}
		}
		double objVal = popularity - punishment;

		// Print objective function value
		System.out.println("Objective function value: " + objVal);

		// Assign MPS and SPS for the second problem.
		MPS = (int) sortedIndices[0];
		SPS = (int) sortedIndices[1];
		// For this problem, the MPS and SPS could be initialized as double values.
		// However, I checked the values and for these values we don't need to be too safe. Casting is enough.
	}

	public static void solveProblem2(List<ArrayList<Double>> enjoy, List<Integer> weights) {
		System.out.println("##### Problem 2 #####");

		// Initialize the inclusion array.
		int[] X_i = new int[weights.size()];

		// Mark every song as excluded.
		for(int i = 0; i < X_i.length; i++) {
			X_i[i] = 0;
		}

		// Include the Most Popular song. Second most popular song will be included later.
		X_i[MPS] = 1;

		// Initialize variables.
		int totalDuration = weights.get(MPS);   // Total collective duration (in ms) of included songs. Only MPS is included now.
		double objVal = 0;                      // Objective function value.
		int jMax = 0;                           // Initialize the next song's index. Which will be selected from the best option of all j's.

		// Start from MPS. Select the next song by the best value.
		for(int i = MPS; i < X_i.length; i = jMax) {

			double maxEnjoy = 0;

			for (int j = 0; j < X_i.length; j++) {

				// Just take action for songs that are not included yet. Also, do not include SPS.
				if (X_i[j] == 0 && j != SPS) {
					if (enjoy.get(i).get(j) > maxEnjoy) {
						if ( totalDuration + weights.get(j) + weights.get(SPS) <= 1_800_000) {
							maxEnjoy = enjoy.get(i).get(j);
							jMax = j;
						}
					}
				}
			}

			if (maxEnjoy == 0) {
				break;
			} else {
				objVal += maxEnjoy;
				X_i[jMax] = 1;
				totalDuration += weights.get(jMax);
				System.out.println("Song "+ i + " and " + jMax + " are added with a " + maxEnjoy + " value.");
			}
		}

		// After the algorithm finishes processing include SPS as the last song.
		System.out.println("Song "+ jMax + "and " + SPS + " are added with a " + enjoy.get(jMax).get(SPS) + " value.");
		totalDuration += weights.get(SPS);
		objVal += enjoy.get(jMax).get(SPS);
		X_i[SPS] = 1;

		// Punish by the missing time.
		objVal -= 0.02 * ( (1_800_000 - totalDuration) / 1000);

		// Report the solution
		System.out.println("Songs that are included: "  );
		for(int i = 0; i < 50; i++) {
			System.out.println(i + " " + X_i[i]);
		}

		// Report total duration of the album.
		System.out.println("The total duration of the album is: " + totalDuration/1000 +
				" seconds which is exactly: "+ ((double)totalDuration)/60000 + " minutes.");

		// Report the objective function value.
		System.out.println("Objective function value: " + objVal);
	}
}