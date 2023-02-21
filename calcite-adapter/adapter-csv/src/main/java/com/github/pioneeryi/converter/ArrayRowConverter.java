package com.github.pioneeryi.converter;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.util.ImmutableIntList;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;

public class ArrayRowConverter extends RowConverter<Object[]> {

    private final List<RelDataType> fieldTypes;
    private final ImmutableIntList fields;

    public ArrayRowConverter(List<RelDataType> fieldTypes, List<Integer> fields) {
        this.fieldTypes = ImmutableNullableList.copyOf(fieldTypes);
        this.fields = ImmutableIntList.copyOf(fields);
    }

    @Override
    public Object[] convertRow(String[] rows) {
        final Object[] objects = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            int field = fields.get(i);
            objects[i] = convert(fieldTypes.get(field), rows[field]);
        }
        return objects;
    }
}
