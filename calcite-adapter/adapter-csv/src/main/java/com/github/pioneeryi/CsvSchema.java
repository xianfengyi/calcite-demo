package com.github.pioneeryi;

import com.github.pioneeryi.table.CsvScannableTable;
import com.google.common.collect.ImmutableMap;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Source;
import org.apache.calcite.util.Sources;

import java.net.URL;
import java.util.Map;

public class CsvSchema extends AbstractSchema {

    private Map<String, Table> tableMap;

    public CsvSchema() {
    }

    @Override
    protected Map<String, Table> getTableMap() {
        URL url = CsvSchema.class.getResource("/depts.csv");
        Source source = Sources.of(url);
        if (tableMap == null) {
            final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
            builder.put("DEPTS", new CsvScannableTable(source));
            tableMap = builder.build();
        }
        return tableMap;
    }
}
