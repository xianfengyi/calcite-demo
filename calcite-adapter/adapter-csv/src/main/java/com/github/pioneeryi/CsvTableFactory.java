package com.github.pioneeryi;

import com.github.pioneeryi.table.CsvScannableTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TableFactory;
import org.apache.calcite.util.Source;
import org.apache.calcite.util.Sources;

import java.net.URL;
import java.util.Map;

public class CsvTableFactory implements TableFactory<CsvTable> {

    @Override
    public CsvTable create(SchemaPlus schema, String name, Map<String, Object> operand, RelDataType rowType) {
        URL url = CsvTableFactory.class.getResource("/depts.csv");
        Source source = Sources.of(url);
        return new CsvScannableTable(source);
    }
}
