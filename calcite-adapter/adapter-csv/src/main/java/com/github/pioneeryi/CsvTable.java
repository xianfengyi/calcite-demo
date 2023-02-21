package com.github.pioneeryi;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Source;

import java.util.ArrayList;
import java.util.List;

public class CsvTable extends AbstractTable {

    protected final Source source;

    private List<RelDataType> fieldTypes;

    public CsvTable(Source source) {
        this.source = source;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        RowTypeDeduce deducer = new RowTypeDeduce();
        deducer.deduce(typeFactory, source);

        List<String> names = deducer.getNames();
        List<RelDataType> types = deducer.getTypes();

        return typeFactory.createStructType(Pair.zip(names, types));
    }

    /**
     * Returns the field types of this CSV table.
     */
    public List<RelDataType> getFieldTypes(RelDataTypeFactory typeFactory) {
        if (fieldTypes == null) {
            fieldTypes = new ArrayList<>();

            RowTypeDeduce deducer = new RowTypeDeduce();
            deducer.deduce(typeFactory, source);
            fieldTypes.addAll(deducer.getTypes());
        }
        return fieldTypes;
    }
}
