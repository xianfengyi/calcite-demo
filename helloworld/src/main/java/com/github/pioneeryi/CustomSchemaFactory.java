package com.github.pioneeryi;

import java.util.Map;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

public class CustomSchemaFactory implements SchemaFactory {

    public Schema create(SchemaPlus schemaPlus, String name, Map<String, Object> operand) {
        return new CustomSchema();
    }
}
