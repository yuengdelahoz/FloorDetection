package delahoz.floordetection.structured;

import java.util.ArrayList;


public class Sorting {

	public ArrayList MergeSort(ArrayList A) {
		return A;
	}

	public ArrayList QuickSort(ArrayList A) {
		return A;
	}

	public ArrayList<Line> InsertionSort(ArrayList<Line> A) {
		// Array to sort
		ArrayList<Line> lines = A;
		//List I. Full at beginning 
		int I = A.size();
		//List S. Empty at beginning
		int S = 0;

		// While list I is not empty  
		while (I != 0) {
			for (int i = 0; i < A.size(); i++) {
				
				// Compare all elements in list S with current element in I
				for (int j = 0; j < S; j++) {
					// Current element in list S
					double Slength = lines.get(j).getLength();
					
					// Current element in list I. Note list I is fixed through out this loop.
					double Ilength = lines.get(i).getLength();
					
					// If element in S is bigger than element in I, then insert element in S.
					if (Slength > Ilength) {

						Line temp = lines.get(i);
						// Shift elements to the right
						for (int k = S; k > j; k--) {
							lines.set(k, lines.get(k - 1));
						}
						// Insert in correct position
						lines.set(j, temp);
						break;

					}

				}
				S++;
				I--;
			}
		}

		return lines;
	}

	public ArrayList<Line> SelectionSort(ArrayList<Line> A) {	
		// Array to sort
		ArrayList<Line> lines = A;

		//List I. Full at beginning 
		int I = A.size();
		//List S. Empty at beginning
		int S = 0;
		
		// Temp variable to calculate min value in I
		double minLength = 0;
		
		// Position of min value
		int pos = 0;
		
		// Temp variable
		Line temp = null;
		
		//Flag to control unecessary swaps.
		boolean flag = false;

		for (int i = 0; i < A.size(); i++) {

			// Initialize min value with current element in list I
			minLength = lines.get(i).getLength();
			pos = i;
			
			// Find min value in list I
			for (int j = S; j < A.size(); j++) {

				double Ilength = lines.get(j).getLength();

				if (Ilength < minLength) {
					minLength = Ilength;
					pos = j;
					flag = true;

				}

			}
			
			// if min value is not the same value 
			if (flag) {
				
				// Append min value to S.
				temp = lines.get(i);
				lines.set(i, lines.get(pos));
				lines.set(pos, temp);
				flag = false;
			}
			S++;
			I--;
			if (I == 1)
				break;

		}

		return lines;
	}

	public ArrayList<Line> HeapSort(ArrayList<Line> A){
		// Array to sort		
		ArrayList<Line> lines = new ArrayList<Line>();
		
		// Toss elements to Heap
		Heap hp = new Heap (A);
		
		// Force min-Heap property
		hp.bottomUpHeap();
		
		while (!hp.isEmpty()){
			lines.add(hp.removeMin());
		}
		
		return lines;
	}
}
