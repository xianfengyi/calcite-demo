package com.github.pioneeryi.bindable;

import com.github.pioneeryi.EnumerableUtil;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.runtime.ArrayBindable;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.runtime.Typed;
import org.apache.calcite.runtime.Utilities;
import org.apache.calcite.schema.Schemas;
import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IClassBodyEvaluator;
import org.codehaus.commons.compiler.ICompilerFactory;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class BindableExecute {

    private static String executeExpression = "public org.apache.calcite.linq4j.Enumerable bind(final org.apache" +
            ".calcite" +
            ".DataContext root) {\n" +
            "  final org.apache.calcite.linq4j.function.Function1 rowBuilderFactory = new org.apache.calcite.linq4j" +
            ".function.Function1() {\n" +
            "    public org.apache.calcite.linq4j.function.Function0 apply(final java.sql.ResultSet resultSet) {\n" +
            "      return new org.apache.calcite.linq4j.function.Function0() {\n" +
            "          public Object apply() {\n" +
            "            try {\n" +
            "              final Object[] values = new Object[2];\n" +
            "              values[0] = resultSet.getObject(1);\n" +
            "              values[1] = resultSet.getLong(2);\n" +
            "              if (resultSet.wasNull()) {\n" +
            "                values[1] = null;\n" +
            "              }\n" +
            "              return values;\n" +
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
            "  final org.apache.calcite.runtime.ResultSetEnumerable enumerable = org.apache.calcite.runtime" +
            ".ResultSetEnumerable.of((javax.sql.DataSource) root.getRootSchema().getSubSchema(\"db1\").unwrap(javax" +
            ".sql.DataSource.class), \"SELECT `sex`, COUNT(*) AS `SEX_COUNT`\\nFROM `student`\\nWHERE `id` > " +
            "0\\nGROUP BY `sex`\", rowBuilderFactory);\n" +
            "  enumerable.setTimeout(root);\n" +
            "  return enumerable;\n" +
            "}\n" +
            "\n" +
            "public Class getElementType() {\n" +
            "  return java.lang.Object[].class;\n" +
            "}\n";

    public static void main(String[] args) throws Exception {
        DataContext dataContext = getDataContext();
        Bindable bindable = getBindable(2);
        Enumerable enumerable = bindable.bind(dataContext);
        EnumerableUtil.printEnumerator(enumerable.enumerator());
    }

    private static DataContext getDataContext() throws Exception {
        CalciteConnection calciteConnection = (CalciteConnection) getConnection();
        return Schemas.createDataContext(calciteConnection, calciteConnection.getRootSchema());
    }

    private static Bindable getBindable(int fieldCount) throws Exception {
        ICompilerFactory compilerFactory = CompilerFactoryFactory.getDefaultCompilerFactory();
        final IClassBodyEvaluator cbe = compilerFactory.newClassBodyEvaluator();
        cbe.setClassName("Baz");
        cbe.setExtendedClass(Utilities.class);
        cbe.setImplementedInterfaces(fieldCount == 1 ? new Class[]{Bindable.class, Typed.class}
                : new Class[]{ArrayBindable.class});
        cbe.setParentClassLoader(EnumerableInterpretable.class.getClassLoader());
        return (Bindable) cbe.createInstance(new StringReader(executeExpression));
    }

    private static Connection getConnection() throws Exception {
        String path = BindableExecute.class.getResource("/single-datasource-model.json").toString();
        Properties info = new Properties();
        info.setProperty("caseSensitive", "false");
        info.put("model", path.replace("file:", ""));
        return DriverManager.getConnection("jdbc:calcite:", info);
    }
}
