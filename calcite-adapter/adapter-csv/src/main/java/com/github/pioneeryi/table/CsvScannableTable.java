package com.github.pioneeryi.table;

import com.github.pioneeryi.CsvEnumerator;
import com.github.pioneeryi.CsvTable;
import com.github.pioneeryi.converter.ArrayRowConverter;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.util.ImmutableIntList;
import org.apache.calcite.util.Source;

import java.util.List;

public class CsvScannableTable extends CsvTable implements ScannableTable {

    public CsvScannableTable(Source source) {
        super(source);
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        JavaTypeFactory typeFactory = root.getTypeFactory();
        final List<RelDataType> fieldTypes = getFieldTypes(typeFactory);
        final List<Integer> fields = ImmutableIntList.identity(fieldTypes.size());
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new CsvEnumerator<>(source, new ArrayRowConverter(fieldTypes, fields));
            }
        };
    }
}
