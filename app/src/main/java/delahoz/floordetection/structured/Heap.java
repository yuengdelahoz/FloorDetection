package delahoz.floordetection.structured;

import java.util.ArrayList;

public class Heap {
	ArrayList<Line> HeapArray;
	int lstidx;
	int len;

	public Heap(ArrayList<Line> array) {

		HeapArray = new ArrayList<Line>();
		HeapArray.add(null);

		for (int i = 0; i < array.size(); i++)
			HeapArray.add(array.get(i));

		len = HeapArray.size();
		lstidx = len - 1;
	}

	public boolean isEmpty() {
		if (lstidx == 0)
			return true;
		else
			return false;
	}

	public ArrayList<Line> bottomUpHeap() {

		// walk backward form last internal node to root, i.e., reverse order in
		// array
		// Node i's parent is floor (i/2)
		// Node i's children are 2*i and 2*i+1
		if (lstidx > 1) {
			for (int k = lstidx; k > 1; k = k - 2) {
				int pidx = k / 2;
				if (pidx == 1) {
					while (true) {
						double parent_value = HeapArray.get(pidx).getLength();
						double kid1_value = HeapArray.get(2 * pidx).getLength();
						double kid2_value;
						int idx;
						if ((2 * pidx + 1) <= lstidx)
							kid2_value = HeapArray.get(2 * pidx + 1)
									.getLength();
						else
							kid2_value = 999999999999999999999999.9;
						double min_kid = 0;

						if (kid1_value <= kid2_value) {
							min_kid = kid1_value;
							idx = 2 * pidx;
						} else {
							min_kid = kid2_value;
							idx = 2 * pidx + 1;
						}

						if (min_kid < parent_value) {
							Line temp = HeapArray.get(pidx);
							HeapArray.set(pidx, HeapArray.get(idx));
							HeapArray.set(idx, temp);
							pidx = idx;
							if (2 * pidx > lstidx)
								break;
						} else
							break;

					}

				} else {
					double parent_value = HeapArray.get(pidx).getLength();
					double kid1_value = HeapArray.get(2 * pidx).getLength();
					double kid2_value;
					int idx;
					if ((2 * pidx + 1) <= lstidx)
						kid2_value = HeapArray.get(2 * pidx + 1).getLength();
					else
						kid2_value = 999999999999999999999999.9;
					double min_kid = 0;

					if (kid1_value <= kid2_value) {
						min_kid = kid1_value;
						idx = 2 * pidx;
					} else {
						min_kid = kid2_value;
						idx = 2 * pidx + 1;
					}

					if (min_kid < parent_value) {
						Line temp = HeapArray.get(pidx);
						HeapArray.set(pidx, HeapArray.get(idx));
						HeapArray.set(idx, temp);
					}

				}

			}
		}
		return HeapArray;

	}

	public Line removeMin() {
		int pidx = 1;
		Line min = HeapArray.get(pidx);
		HeapArray.set(pidx, HeapArray.get(lstidx));
		HeapArray.set(lstidx, null);
		lstidx = lstidx - 1;
		if (lstidx > 1) {
			while (true) {
				double parent_value = HeapArray.get(pidx).getLength();
				double kid1_value = HeapArray.get(2 * pidx).getLength();
				double kid2_value;
				int idx;
				if ((2 * pidx + 1) <= lstidx)
					kid2_value = HeapArray.get(2 * pidx + 1).getLength();
				else
					kid2_value = 999999999999999999999999.9;
				double min_kid = 0;

				if (kid1_value <= kid2_value) {
					min_kid = kid1_value;
					idx = 2 * pidx;
				} else {
					min_kid = kid2_value;
					idx = 2 * pidx + 1;
				}

				if (min_kid < parent_value) {
					Line temp = HeapArray.get(pidx);
					HeapArray.set(pidx, HeapArray.get(idx));
					HeapArray.set(idx, temp);
					pidx = idx;
					if (2 * pidx > lstidx)
						break;
				} else
					break;

			}

		}
		return min;

	}

	public void PrintHeap() {
		for (int i = 1; i <= lstidx; i++)
			System.out.println(HeapArray.get(i));

	}
}
