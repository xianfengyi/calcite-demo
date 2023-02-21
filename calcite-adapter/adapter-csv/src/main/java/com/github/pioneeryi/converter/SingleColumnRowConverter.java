package com.github.pioneeryi.converter;

import org.apache.calcite.rel.type.RelDataType;

public class SingleColumnRowConverter extends RowConverter<Object> {

    private final RelDataType fieldType;
    private final int fieldIndex;

    public SingleColumnRowConverter(RelDataType fieldType, int fieldIndex) {
        this.fieldType = fieldType;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public Object convertRow(String[] strings) {
        return convert(fieldType, strings[fieldIndex]);
    }
}
