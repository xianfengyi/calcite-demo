package com.github.pioneeryi;

import com.github.pioneeryi.parser.impl.SqlParserImpl;
import com.github.pioneeryi.sql.CreateMaterializedView;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.junit.Assert;
import org.junit.Test;

public class SqlParserTest {

    @Test
    public void testSubmitWrongOK() {
        String sql = "select * from t_story_info";
        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.configBuilder()
                        .setParserFactory(SqlParserImpl.FACTORY)
                        .setCaseSensitive(false)
                        .setQuoting(Quoting.BACK_TICK)
                        .setQuotedCasing(Casing.TO_UPPER)
                        .setUnquotedCasing(Casing.TO_UPPER)
                        .build())
                .build();
        SqlParser parser = SqlParser.create(sql, config.getParserConfig());

        try {
            SqlNode sqlNode = parser.parseStmt();
            System.out.println(sqlNode.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void test() throws SqlParseException {
        String sql = "CREATE MATERIALIZED VIEW IF NOT EXISTS MyView AS SELECT * FROM STORY";

        SqlParser.Config myConfig = SqlParser.config()
                .withQuoting(Quoting.DOUBLE_QUOTE)
                .withQuotedCasing(Casing.UNCHANGED)
                .withParserFactory(SqlParserImpl.FACTORY);
        SqlParser parser = SqlParser.create(sql, myConfig);
        SqlNode sqlNode = parser.parseQuery();
        Assert.assertTrue(sqlNode instanceof CreateMaterializedView);
        System.out.println(sqlNode);
    }
}
