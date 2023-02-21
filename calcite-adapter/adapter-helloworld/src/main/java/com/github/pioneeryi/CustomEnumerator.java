package com.github.pioneeryi;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.util.Source;

import java.io.BufferedReader;
import java.io.IOException;

public class CustomEnumerator<E> implements Enumerator<E> {

    private E current;

    private BufferedReader br;

    public CustomEnumerator(Source source) {
        try {
            this.br = new BufferedReader(source.reader());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public E current() {
        return current;
    }

    public boolean moveNext() {
        try {
            String line = br.readLine();
            if (line == null) {
                return false;
            }
            current = (E) new Object[]{line};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void reset() {
        System.out.println("不支持此操作");
    }

    public void close() {

    }
}
