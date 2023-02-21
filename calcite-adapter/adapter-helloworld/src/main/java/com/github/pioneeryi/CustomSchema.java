package com.github.pioneeryi;

import com.google.common.collect.ImmutableMap;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Source;
import org.apache.calcite.util.Sources;

import java.net.URL;
import java.util.Map;

public class CustomSchema extends AbstractSchema {

    private Map<String, Table> tableMap;

    @Override
    protected Map<String, Table> getTableMap() {
        URL url = CustomSchema.class.getResource("/data.txt");
        Source source = Sources.of(url);
        if (tableMap == null) {
            final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
            builder.put("T_TEST_TABLE", new CustomTable(source));
            tableMap = builder.build();
        }
        return tableMap;
    }
}
