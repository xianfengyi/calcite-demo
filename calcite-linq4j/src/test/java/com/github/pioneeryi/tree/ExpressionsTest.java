package com.github.pioneeryi.tree;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.tree.*;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.util.BuiltInMethod;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ExpressionsTest {

    private final Expression ONE = Expressions.constant(1);
    private final Expression TWO = Expressions.constant(2);

    @Test
    public void TestAdd() {
        BlockBuilder b = new BlockBuilder(true);


        Expression x = b.append("x", Expressions.add(ONE, TWO));
        b.add(Expressions.return_(null, Expressions.add(x, x)));

        System.out.println(b.toBlock().toString());
    }


    @Test
    public void TestMethodCall() throws NoSuchMethodException {
        BlockBuilder bb = new BlockBuilder();
        bb.append("_i", Expressions.call(
                Expressions.new_(Identity.class),
                Identity.class.getMethod("apply", Object.class),
                Expressions.constant("test")));
        String expected = "{\n" +
                "  final Object _i = new com.github.pioneeryi.tree.ExpressionsTest.Identity().apply(\"test\");\n" +
                "}\n";
        Assert.assertEquals(expected, bb.toBlock().toString());
    }

    @Test
    public void TestMethodDecl() {
        List<MemberDeclaration> memberDeclarations = new ArrayList();
        MethodDeclaration methodDeclaration = Expressions.methodDecl(
                0,
                int.class,
                "test",
                Collections.emptyList(),
                Blocks.toFunctionBlock(Expressions.add(ONE, TWO)));
        memberDeclarations.add(methodDeclaration);

        String expr = Expressions.toString(memberDeclarations, "\n", false);

        String expected = "int test() {\n" +
                "  return 1 + 2;\n" +
                "}\n" +
                "\n" +
                "\n";
        Assert.assertEquals(expected, expr);
    }

    @Test
    public void testTryCatch() {
        int fieldCount = 2;
        BlockBuilder builder = new BlockBuilder();
        final Expression values_ = builder.append("values",
                Expressions.newArrayBounds(Object.class, 1, Expressions.constant(fieldCount)));
        builder.add(Expressions.return_(null, values_));

        final ParameterExpression e_ = Expressions.parameter(SQLException.class, builder.newName("e"));

        BlockStatement blockStatement = Expressions.block(
                Expressions.tryCatch(
                        builder.toBlock(),
                        Expressions.catch_(
                                e_,
                                Expressions.throw_(
                                        Expressions.new_(
                                                RuntimeException.class,
                                                e_)))));
        String expected = "{\n" +
                "  try {\n" +
                "    return new Object[2];\n" +
                "  } catch (java.sql.SQLException e) {\n" +
                "    throw new RuntimeException(\n" +
                "      e);\n" +
                "  }\n" +
                "}\n";
        Assert.assertEquals(expected, blockStatement.toString());
    }

    @Test
    public void testLamda() {
        int fieldCount = 2;
        BlockBuilder builder = new BlockBuilder();
        final Expression values_ = builder.append("values",
                Expressions.newArrayBounds(Object.class, 1, Expressions.constant(fieldCount)));
        builder.add(Expressions.return_(null, values_));

        final ParameterExpression e_ = Expressions.parameter(SQLException.class, builder.newName("e"));

        BlockStatement blockStatement = Expressions.block(
                Expressions.tryCatch(
                        builder.toBlock(),
                        Expressions.catch_(
                                e_,
                                Expressions.throw_(
                                        Expressions.new_(
                                                RuntimeException.class,
                                                e_)))));
        FunctionExpression funcExpr = Expressions.lambda(blockStatement);
        String expected = "new org.apache.calcite.linq4j.function.Function0() {\n" +
                "  public Object apply() {\n" +
                "    try {\n" +
                "      return new Object[2];\n" +
                "    } catch (java.sql.SQLException e) {\n" +
                "      throw new RuntimeException(\n" +
                "        e);\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        Assert.assertEquals(expected, funcExpr.toString());

        GotoStatement statement = Expressions.return_(null, funcExpr);
        expected = "return new org.apache.calcite.linq4j.function.Function0() {\n" +
                "    public Object apply() {\n" +
                "      try {\n" +
                "        return new Object[2];\n" +
                "      } catch (java.sql.SQLException e) {\n" +
                "        throw new RuntimeException(\n" +
                "          e);\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                ";\n";
        Assert.assertEquals(expected, statement.toString());

        BlockStatement blockStatement1 = Expressions.block(statement);
        expected = "{\n" +
                "  return new org.apache.calcite.linq4j.function.Function0() {\n" +
                "      public Object apply() {\n" +
                "        try {\n" +
                "          return new Object[2];\n" +
                "        } catch (java.sql.SQLException e) {\n" +
                "          throw new RuntimeException(\n" +
                "            e);\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ;\n" +
                "}\n";
        Assert.assertEquals(expected, blockStatement1.toString());

        FunctionExpression funcExpr1 = Expressions.lambda(blockStatement1, Expressions.parameter(Modifier.FINAL,
                ResultSet.class,
                builder.newName("resultSet")));

        expected = "new org.apache.calcite.linq4j.function.Function1() {\n" +
                "  public org.apache.calcite.linq4j.function.Function0 apply(final java.sql.ResultSet resultSet) {\n" +
                "    return new org.apache.calcite.linq4j.function.Function0() {\n" +
                "        public Object apply() {\n" +
                "          try {\n" +
                "            return new Object[2];\n" +
                "          } catch (java.sql.SQLException e) {\n" +
                "            throw new RuntimeException(\n" +
                "              e);\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ;\n" +
                "  }\n" +
                "  public Object apply(final Object resultSet) {\n" +
                "    return apply(\n" +
                "      (java.sql.ResultSet) resultSet);\n" +
                "  }\n" +
                "}\n";
        Assert.assertEquals(expected, funcExpr1.toString());

        final BlockBuilder builder0 = new BlockBuilder(false);
        builder0.append("rowBuilderFactory", funcExpr1);
        expected = "{\n" +
                "  final org.apache.calcite.linq4j.function.Function1 rowBuilderFactory = new org.apache.calcite" +
                ".linq4j.function.Function1() {\n" +
                "    public org.apache.calcite.linq4j.function.Function0 apply(final java.sql.ResultSet resultSet) " +
                "{\n" +
                "      return new org.apache.calcite.linq4j.function.Function0() {\n" +
                "          public Object apply() {\n" +
                "            try {\n" +
                "              return new Object[2];\n" +
                "            } catch (java.sql.SQLException e) {\n" +
                "              throw new RuntimeException(\n" +
                "                e);\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ;\n" +
                "    }\n" +
                "    public Object apply(final Object resultSet) {\n" +
                "      return apply(\n" +
                "        (java.sql.ResultSet) resultSet);\n" +
                "    }\n" +
                "  }\n" +
                "  ;\n" +
                "}\n";
        String expr = builder0.toBlock().toString();
        Assert.assertEquals(expected, expr);


        final List<MemberDeclaration> memberDeclarations = new ArrayList<>();
        memberDeclarations.add(
                Expressions.methodDecl(
                        Modifier.PUBLIC,
                        Enumerable.class,
                        BuiltInMethod.BINDABLE_BIND.method.getName(),
                        Expressions.list(DataContext.ROOT),
                        builder0.toBlock()));

        memberDeclarations.add(
                Expressions.methodDecl(Modifier.PUBLIC, Class.class,
                        BuiltInMethod.TYPED_GET_ELEMENT_TYPE.method.getName(),
                        ImmutableList.of(),
                        Blocks.toFunctionBlock(
                                Expressions.return_(null,
                                        Expressions.constant(RelDataTypeSystem.DEFAULT)))));

        ClassDeclaration classDeclaration = Expressions.classDecl(Modifier.PUBLIC,
                "Baz",
                null,
                Collections.singletonList(Bindable.class),
                memberDeclarations);

        expr = Expressions.toString(classDeclaration.memberDeclarations, "\n", false);

        expected = "public org.apache.calcite.linq4j.Enumerable bind(final org.apache.calcite.DataContext root) {\n" +
                "  final org.apache.calcite.linq4j.function.Function1 rowBuilderFactory = new org.apache.calcite" +
                ".linq4j.function.Function1() {\n" +
                "    public org.apache.calcite.linq4j.function.Function0 apply(final java.sql.ResultSet resultSet) " +
                "{\n" +
                "      return new org.apache.calcite.linq4j.function.Function0() {\n" +
                "          public Object apply() {\n" +
                "            try {\n" +
                "              return new Object[2];\n" +
                "            } catch (java.sql.SQLException e) {\n" +
                "              throw new RuntimeException(\n" +
                "                e);\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ;\n" +
                "    }\n" +
                "    public Object apply(final Object resultSet) {\n" +
                "      return apply(\n" +
                "        (java.sql.ResultSet) resultSet);\n" +
                "    }\n" +
                "  }\n" +
                "  ;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "public Class getElementType() {\n" +
                "  return org.apache.calcite.rel.type.RelDataTypeSystem$1@61d47554;\n" +
                "}\n" +
                "\n" +
                "\n";
        Assert.assertEquals(expected, expr);
    }

    static class Identity<I> implements Function<I, I> {
        @Override
        public I apply(I i) {
            return i;
        }
    }

}
