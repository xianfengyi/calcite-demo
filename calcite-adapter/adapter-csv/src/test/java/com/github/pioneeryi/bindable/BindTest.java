package com.github.pioneeryi.bindable;

import com.github.pioneeryi.CsvSchema;
import com.github.pioneeryi.ScannableTableTest;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.config.CalciteSystemProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.runtime.ArrayBindable;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.runtime.Typed;
import org.apache.calcite.runtime.Utilities;
import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IClassBodyEvaluator;
import org.codehaus.commons.compiler.ICompilerFactory;
import org.junit.Test;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Properties;

public class BindTest {

    @Test
    public void testBind() throws Exception {
        DataContext dataContext = getDataContext();

        int fieldCount = 3;
        ICompilerFactory compilerFactory = CompilerFactoryFactory.getDefaultCompilerFactory();
        final IClassBodyEvaluator cbe = compilerFactory.newClassBodyEvaluator();
        cbe.setClassName("Baz");
        cbe.setExtendedClass(Utilities.class);
        cbe.setImplementedInterfaces(
                fieldCount == 1
                        ? new Class[]{Bindable.class, Typed.class}
                        : new Class[]{ArrayBindable.class});
        cbe.setParentClassLoader(EnumerableInterpretable.class.getClassLoader());
        if (CalciteSystemProperty.DEBUG.value()) {
            // Add line numbers to the generated janino class
            cbe.setDebuggingInformation(true, true, true);
        }

        Bindable bindable = (Bindable) cbe.createInstance(new StringReader(getExpr()));

        Enumerable enumerable = bindable.bind(dataContext);
        print(enumerable.enumerator());
    }

    private void print(Enumerator e) {
        while (e.moveNext()) {
            Object[] row = (Object[])e.current();
            System.out.println(Arrays.toString(row));
        }
    }

    private DataContext getDataContext() throws Exception {
        CalciteSchema calciteSchema = new MyCalciteSchema(null, new CsvSchema(), "SALES");
        calciteSchema.add("SALES", new CsvSchema());
        DataContext dataContext = new MyDataContext(getConnection(), calciteSchema);
        return dataContext;
    }

    private String getExpr() {
        return "public org.apache.calcite.linq4j.Enumerable bind(final org.apache.calcite.DataContext root) {\n" +
                "  final org.apache.calcite.linq4j.Enumerable _inputEnumerable = org.apache.calcite.schema.Schemas" +
                ".enumerable((org.apache.calcite.schema.ScannableTable) root.getRootSchema().getSubSchema(\"SALES\")" +
                ".getTable(\"DEPTS\"), root);\n" +
                "  return new org.apache.calcite.linq4j.AbstractEnumerable(){\n" +
                "      public org.apache.calcite.linq4j.Enumerator enumerator() {\n" +
                "        return new org.apache.calcite.linq4j.Enumerator(){\n" +
                "            public final org.apache.calcite.linq4j.Enumerator inputEnumerator = _inputEnumerable" +
                ".enumerator();\n" +
                "            public void reset() {\n" +
                "              inputEnumerator.reset();\n" +
                "            }\n" +
                "\n" +
                "            public boolean moveNext() {\n" +
                "              while (inputEnumerator.moveNext()) {\n" +
                "                final Object[] current = (Object[]) inputEnumerator.current();\n" +
                "                final String inp1_ = current[1] == null ? (String) null : current[1].toString();\n" +
                "                if (inp1_ != null && org.apache.calcite.runtime.SqlFunctions.eq(inp1_, \"Alice\")) " +
                "{\n" +
                "                  return true;\n" +
                "                }\n" +
                "              }\n" +
                "              return false;\n" +
                "            }\n" +
                "\n" +
                "            public void close() {\n" +
                "              inputEnumerator.close();\n" +
                "            }\n" +
                "\n" +
                "            public Object current() {\n" +
                "              final Object[] current = (Object[]) inputEnumerator.current();\n" +
                "              return new Object[] {\n" +
                "                  current[1],\n" +
                "                  current[3],\n" +
                "                  current[6]};\n" +
                "            }\n" +
                "\n" +
                "          };\n" +
                "      }\n" +
                "\n" +
                "    };\n" +
                "}\n" +
                "\n" +
                "\n" +
                "public Class getElementType() {\n" +
                "  return java.lang.Object[].class;\n" +
                "}\n" +
                "\n" +
                "\n";
    }

    public CalciteConnection getConnection() throws Exception {
        String path = ScannableTableTest.class.getResource("/model.json").toString();
        Properties info = new Properties();
        info.setProperty("caseSensitive", "false");
        info.put("model", path.replace("file:", ""));
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        return (CalciteConnection) connection;
    }
}
