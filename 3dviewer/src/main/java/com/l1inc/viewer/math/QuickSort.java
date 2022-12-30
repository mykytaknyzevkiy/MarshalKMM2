package com.l1inc.viewer.math;

import java.util.Comparator;

/**
 * Created by Yevhen Paschenko on 2/13/2017.
 */

public class QuickSort {

//	private T[] numbers;
//	private int number;
//	private Comparator comparator;

	public static <T> void sortInplace(T[] values, Comparator comparator) {
		// check for empty or null array
		if (values ==null || values.length==0){
			return;
		}
//		this.comparator = comparator;
//		this.numbers = values;
//		number = values.length;
		quicksort(values, 0, values.length - 1, comparator);
	}

	private static <T> void quicksort(T[] values, int low, int high, Comparator comparator) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		T pivot = values[low + (high-low)/2];

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (comparator.compare(values[i], pivot) < 0) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (comparator.compare(values[j], pivot) > 0) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(values, i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(values, low, j, comparator);
		if (i < high)
			quicksort(values, i, high, comparator);
	}

	private static <T> void exchange(T[] values, int i, int j) {
		T temp = values[i];
		values[i] = values[j];
		values[j] = temp;
	}
}
