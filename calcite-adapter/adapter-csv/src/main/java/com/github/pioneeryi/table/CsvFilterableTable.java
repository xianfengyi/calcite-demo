package com.github.pioneeryi.table;

import com.github.pioneeryi.CsvEnumerator;
import com.github.pioneeryi.CsvTable;
import com.github.pioneeryi.converter.ArrayRowConverter;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.FilterableTable;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.util.ImmutableIntList;
import org.apache.calcite.util.Source;

import java.util.List;

public class CsvFilterableTable extends CsvTable implements FilterableTable {

    public CsvFilterableTable(Source source) {
        super(source);
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root, List<RexNode> filters) {
        JavaTypeFactory typeFactory = root.getTypeFactory();
        final List<RelDataType> fieldTypes = getFieldTypes(typeFactory);
        final String[] filterValues = new String[fieldTypes.size()];
        filters.removeIf(filter -> addFilter(filter, filterValues));
        final List<Integer> fields = ImmutableIntList.identity(fieldTypes.size());
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new CsvEnumerator<>(source, new ArrayRowConverter(fieldTypes, fields));
            }
        };
    }

    private static boolean addFilter(RexNode filter, Object[] filterValues) {
        if (filter.isA(SqlKind.AND)) {
            // We cannot refine(remove) the operands of AND,
            // it will cause o.a.c.i.TableScanNode.createFilterable filters check failed.
            ((RexCall) filter).getOperands().forEach(subFilter -> addFilter(subFilter, filterValues));
        } else if (filter.isA(SqlKind.EQUALS)) {
            final RexCall call = (RexCall) filter;
            RexNode left = call.getOperands().get(0);
            if (left.isA(SqlKind.CAST)) {
                left = ((RexCall) left).operands.get(0);
            }
            final RexNode right = call.getOperands().get(1);
            if (left instanceof RexInputRef
                    && right instanceof RexLiteral) {
                final int index = ((RexInputRef) left).getIndex();
                if (filterValues[index] == null) {
                    filterValues[index] = ((RexLiteral) right).getValue2().toString();
                    return true;
                }
            }
        }
        return false;
    }
}
