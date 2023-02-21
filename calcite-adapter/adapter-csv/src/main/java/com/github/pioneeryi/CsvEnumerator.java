package com.github.pioneeryi;

import com.github.pioneeryi.converter.ArrayRowConverter;
import com.github.pioneeryi.converter.RowConverter;
import com.github.pioneeryi.converter.SingleColumnRowConverter;
import com.opencsv.CSVReader;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.util.Source;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CsvEnumerator<E> implements Enumerator<E> {

    private E current;

    private CSVReader reader;
    private final RowConverter<E> rowConverter;

    public CsvEnumerator(Source source, AtomicBoolean cancelFlag,
                         List<RelDataType> fieldTypes, List<Integer> fields) {
        //noinspection unchecked
        this(source, (RowConverter<E>) converter(fieldTypes, fields));
    }

    public CsvEnumerator(Source source, RowConverter<E> rowConverter) {
        this.rowConverter = rowConverter;
        try {
            this.reader = new CSVReader(source.reader());
            // skip header row
            this.reader.readNext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RowConverter<?> converter(List<RelDataType> fieldTypes, List<Integer> fields) {
        if (fields.size() == 1) {
            final int field = fields.get(0);
            return new SingleColumnRowConverter(fieldTypes.get(field), field);
        } else {
            return new ArrayRowConverter(fieldTypes, fields);
        }
    }

    @Override
    public E current() {
        return current;
    }

    @Override
    public boolean moveNext() {
        try {
            final String[] fields = reader.readNext();
            if (fields == null) {
                current = null;
                reader.close();
                return false;
            }

            final Object[] objects = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                objects[i] = fields[i];
            }
            current = rowConverter.convertRow(fields);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing CSV reader", e);
        }
    }

    public static int[] identityList(int n) {
        int[] integers = new int[n];
        for (int i = 0; i < n; i++) {
            integers[i] = i;
        }
        return integers;
    }
}
