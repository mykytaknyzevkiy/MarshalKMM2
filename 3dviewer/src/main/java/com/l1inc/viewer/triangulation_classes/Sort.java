package com.l1inc.viewer.triangulation_classes;

import java.util.Comparator;

/**
 * Created by Kirill Kartukov on 30.03.2018.
 */

public class Sort {
    static private Sort instance;

    private TimSort timSort;
    private ComparableTimSort comparableTimSort;

    public <T> void sort (Array<T> a) {
        if (comparableTimSort == null) comparableTimSort = new ComparableTimSort();
        comparableTimSort.doSort((Object[])a.items, 0, a.size);
    }

    public <T> void sort (T[] a) {
        if (comparableTimSort == null) comparableTimSort = new ComparableTimSort();
        comparableTimSort.doSort(a, 0, a.length);
    }

    public <T> void sort (T[] a, int fromIndex, int toIndex) {
        if (comparableTimSort == null) comparableTimSort = new ComparableTimSort();
        comparableTimSort.doSort(a, fromIndex, toIndex);
    }

    public <T> void sort (Array<T> a, Comparator<? super T> c) {
        if (timSort == null) timSort = new TimSort();
        timSort.doSort((Object[])a.items, (Comparator)c, 0, a.size);
    }

    public <T> void sort (T[] a, Comparator<? super T> c) {
        if (timSort == null) timSort = new TimSort();
        timSort.doSort(a, c, 0, a.length);
    }

    public <T> void sort (T[] a, Comparator<? super T> c, int fromIndex, int toIndex) {
        if (timSort == null) timSort = new TimSort();
        timSort.doSort(a, c, fromIndex, toIndex);
    }

    /** Returns a Sort instance for convenience. Multiple threads must not use this instance at the same time. */
    static public Sort instance () {
        if (instance == null) instance = new Sort();
        return instance;
    }
}