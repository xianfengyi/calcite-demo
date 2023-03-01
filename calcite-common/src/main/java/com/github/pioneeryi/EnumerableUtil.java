package com.github.pioneeryi;

import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;

import java.util.Arrays;

public class EnumerableUtil {

    private static void printEnumerable(Enumerable e) {
        printEnumerator(e.enumerator());
    }

    public static void printEnumerator(Enumerator e) {
        while (e.moveNext()) {
            Object[] row = (Object[]) e.current();
            System.out.println(Arrays.toString(row));
        }
    }
}
