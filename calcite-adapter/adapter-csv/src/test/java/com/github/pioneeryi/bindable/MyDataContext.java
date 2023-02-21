package com.github.pioneeryi.bindable;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.schema.SchemaPlus;

public class MyDataContext implements DataContext {

    private final CalciteSchema rootSchema;
    private final QueryProvider queryProvider;
    private final JavaTypeFactory typeFactory;

    public MyDataContext(CalciteConnection connection, CalciteSchema rootSchema) {
        this.queryProvider = connection;
        this.typeFactory = connection.getTypeFactory();
        this.rootSchema = rootSchema;
    }

    @Override
    public SchemaPlus getRootSchema() {
        return rootSchema == null ? null : rootSchema.plus();
    }

    @Override
    public JavaTypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public QueryProvider getQueryProvider() {
        return queryProvider;
    }

    @Override
    public Object get(String name) {
        return null;
    }
}
