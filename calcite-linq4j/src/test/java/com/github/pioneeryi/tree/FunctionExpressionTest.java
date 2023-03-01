package com.github.pioneeryi.tree;

import org.apache.calcite.linq4j.tree.BlockBuilder;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.ParameterExpression;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.runtime.SqlFunctions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.calcite.linq4j.Nullness.castNonNull;

public class FunctionExpressionTest {

    @Test
    public void testGetMethod() {
        BlockBuilder bb = new BlockBuilder();
        ParameterExpression resultSet_ = new ParameterExpression(null);
        bb.append("values", Expressions.call(resultSet_, jdbcGetMethod(Primitive.INT),
                Expressions.constant(0)));
        String expected = "{\n" +
                "  final Object _i = new com.github.pioneeryi.tree.ExpressionsTest.Identity().apply(\"test\");\n" +
                "}\n";
        Assert.assertEquals(expected, bb.toBlock().toString());
    }

    private static String jdbcGetMethod(@Nullable Primitive primitive) {
        return primitive == null
                ? "getObject"
                : "get" + SqlFunctions.initcap(castNonNull(primitive.primitiveName));
    }
}
