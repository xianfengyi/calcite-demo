package com.github.pioneeryi;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.util.Map;

public class CustomSchemaFactory implements SchemaFactory {

    public Schema create(SchemaPlus schemaPlus, String name, Map<String, Object> operand) {
        return new CustomSchema();
    }
}
