package com.github.pioneeryi;

import org.apache.calcite.linq4j.tree.*;
import org.apache.calcite.runtime.Bindable;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
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
                "  final Object _i = new com.github.pioneeryi.ExpressionsTest.Identity().apply(\"test\");\n" +
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

    static class Identity<I> implements Function<I, I> {
        @Override
        public I apply(I i) {
            return i;
        }
    }

}
