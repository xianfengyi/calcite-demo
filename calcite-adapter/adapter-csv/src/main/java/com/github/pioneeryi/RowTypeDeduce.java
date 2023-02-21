package com.github.pioneeryi;

import com.opencsv.CSVReader;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Source;

import java.util.ArrayList;
import java.util.List;

public class RowTypeDeduce {

    private List<String> names;
    private List<RelDataType> types;

    public RowTypeDeduce() {
        names = new ArrayList<>();
        types = new ArrayList<>();
    }

    public List<String> getNames() {
        return names;
    }

    public List<RelDataType> getTypes() {
        return types;
    }

    /**
     * 解析 CSV 文件的列名和类型.
     *
     * @param typeFactory RelDataTypeFactory
     * @param source      Source
     */
    public void deduce(RelDataTypeFactory typeFactory, Source source) {
        try (CSVReader reader = new CSVReader(source.reader())) {
            String[] columns = reader.readNext();
            for (String column : columns) {
                final String name;
                final RelDataType fieldType;
                final int colonIndex = column.indexOf(':');
                if (colonIndex >= 0) {
                    name = column.substring(0, colonIndex);
                    String typeString = column.substring(colonIndex + 1);
                    fieldType = getFieldType((JavaTypeFactory) typeFactory, typeString);
                } else {
                    name = column;
                    fieldType = typeFactory.createSqlType(SqlTypeName.VARCHAR);
                }
                names.add(name);
                types.add(fieldType);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        if (names.isEmpty()) {
            names.add("line");
            types.add(typeFactory.createSqlType(SqlTypeName.VARCHAR));
        }
    }

    private RelDataType getFieldType(JavaTypeFactory typeFactory, String typeString) {
        RelDataType fieldType;
        switch (typeString) {
            case "string":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.VARCHAR);
                break;
            case "boolean":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.BOOLEAN);
                break;
            case "byte":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.TINYINT);
                break;
            case "char":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.CHAR);
                break;
            case "short":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.SMALLINT);
                break;
            case "int":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.INTEGER);
                break;
            case "long":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.BIGINT);
                break;
            case "float":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.REAL);
                break;
            case "double":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.DOUBLE);
                break;
            case "date":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.DATE);
                break;
            case "timestamp":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.TIMESTAMP);
                break;
            case "time":
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.TIME);
                break;
            default:
                fieldType = toNullableRelDataType(typeFactory, SqlTypeName.VARCHAR);
                break;
        }
        return fieldType;
    }

    private static RelDataType toNullableRelDataType(JavaTypeFactory typeFactory,
                                                     SqlTypeName sqlTypeName) {
        return typeFactory.createTypeWithNullability(typeFactory.createSqlType(sqlTypeName), true);
    }
}
