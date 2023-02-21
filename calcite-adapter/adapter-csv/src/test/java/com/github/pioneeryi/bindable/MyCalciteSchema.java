package com.github.pioneeryi.bindable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.*;
import org.apache.calcite.util.NameMap;
import org.apache.calcite.util.NameMultimap;
import org.apache.calcite.util.NameSet;

import java.util.Collection;
import java.util.List;

public class MyCalciteSchema extends CalciteSchema {

    public MyCalciteSchema(CalciteSchema parent, Schema schema, String name) {
        this(parent, schema, name, null, null, null, null, null, null, null, null);
    }

    private MyCalciteSchema(CalciteSchema parent, Schema schema,
                            String name, NameMap<CalciteSchema> subSchemaMap,
                            NameMap<TableEntry> tableMap, NameMap<LatticeEntry> latticeMap, NameMap<TypeEntry> typeMap,
                            NameMultimap<FunctionEntry> functionMap, NameSet functionNames,
                            NameMap<FunctionEntry> nullaryFunctionMap,
                            List<? extends List<String>> path) {
        super(parent, schema, name, subSchemaMap, tableMap, latticeMap, typeMap,
                functionMap, functionNames, nullaryFunctionMap, path);
    }

    @Override
    protected CalciteSchema getImplicitSubSchema(String schemaName, boolean caseSensitive) {
        // Check implicit schemas.
        Schema s = schema.getSubSchema(schemaName);
        if (s != null) {
            return new MyCalciteSchema(this, s, schemaName);
        }
        return null;
    }

    @Override
    protected TableEntry getImplicitTable(String tableName, boolean caseSensitive) {
        // Check implicit tables.
        Table table = schema.getTable(tableName);
        if (table != null) {
            return tableEntry(tableName, table);
        }
        return null;
    }

    @Override
    protected TypeEntry getImplicitType(String name, boolean caseSensitive) {
        // Check implicit types.
        RelProtoDataType type = schema.getType(name);
        if (type != null) {
            return typeEntry(name, type);
        }
        return null;
    }

    @Override
    protected TableEntry getImplicitTableBasedOnNullaryFunction(String tableName, boolean caseSensitive) {
        Collection<Function> functions = schema.getFunctions(tableName);
        if (functions != null) {
            for (Function function : functions) {
                if (function instanceof TableMacro
                        && function.getParameters().isEmpty()) {
                    final Table table = ((TableMacro) function).apply(ImmutableList.of());
                    return tableEntry(tableName, table);
                }
            }
        }
        return null;
    }

    @Override
    protected void addImplicitSubSchemaToBuilder(ImmutableSortedMap.Builder<String, CalciteSchema> builder) {
        ImmutableSortedMap<String, CalciteSchema> explicitSubSchemas = builder.build();
        for (String schemaName : schema.getSubSchemaNames()) {
            if (explicitSubSchemas.containsKey(schemaName)) {
                // explicit subschema wins.
                continue;
            }
            Schema s = schema.getSubSchema(schemaName);
            if (s != null) {
                CalciteSchema calciteSchema = new MyCalciteSchema(this, s, schemaName);
                builder.put(schemaName, calciteSchema);
            }
        }
    }

    @Override
    protected void addImplicitTableToBuilder(ImmutableSortedSet.Builder<String> builder) {
        builder.addAll(schema.getTableNames());
    }

    @Override
    protected void addImplicitFunctionsToBuilder(ImmutableList.Builder<Function> builder, String name,
                                                 boolean caseSensitive) {
        Collection<Function> functions = schema.getFunctions(name);
        if (functions != null) {
            builder.addAll(functions);
        }
    }

    @Override
    protected void addImplicitFuncNamesToBuilder(ImmutableSortedSet.Builder<String> builder) {
        builder.addAll(schema.getFunctionNames());
    }

    @Override
    protected void addImplicitTypeNamesToBuilder(ImmutableSortedSet.Builder<String> builder) {
        builder.addAll(schema.getTypeNames());
    }

    @Override
    protected void addImplicitTablesBasedOnNullaryFunctionsToBuilder(ImmutableSortedMap.Builder<String, Table> builder) {
        ImmutableSortedMap<String, Table> explicitTables = builder.build();

        for (String s : schema.getFunctionNames()) {
            // explicit table wins.
            if (explicitTables.containsKey(s)) {
                continue;
            }
            for (Function function : schema.getFunctions(s)) {
                if (function instanceof TableMacro
                        && function.getParameters().isEmpty()) {
                    final Table table = ((TableMacro) function).apply(ImmutableList.of());
                    builder.put(s, table);
                }
            }
        }
    }

    @Override
    protected CalciteSchema snapshot(CalciteSchema parent, SchemaVersion version) {
        return null;
    }

    @Override
    protected boolean isCacheEnabled() {
        return false;
    }

    @Override
    public void setCache(boolean cache) {

    }

    @Override
    public CalciteSchema add(String name, Schema schema) {
        final CalciteSchema calciteSchema =
                new MyCalciteSchema(this, schema, name);
        subSchemaMap.put(name, calciteSchema);
        return calciteSchema;
    }
}
