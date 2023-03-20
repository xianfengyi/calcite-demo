package com.github.pioneeryi;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TableNameExtractorTest {

    @Test
    public void extractTableNam_Simple() throws Exception {
        SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder();
        parserConfig.setCaseSensitive(false);

        String sql = "select * from test where name='pioneeryi'";

        SqlParser parser = SqlParser.create(sql, parserConfig.build());
        SqlNode sqlNode = parser.parseQuery();

        List<String> tables = TableNameExtractor.extractTableName(sqlNode);
        Assert.assertEquals(1, tables.size());
        Assert.assertEquals("TEST", tables.get(0));
    }

    @Test
    public void extractTableNam_Join() throws Exception {
        SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder();
        parserConfig.setCaseSensitive(false);

        String sql = "select * from test1 join test2 on test1.id= test2.id where test1.name='pioneeryi'";

        SqlParser parser = SqlParser.create(sql, parserConfig.build());
        SqlNode sqlNode = parser.parseQuery();

        List<String> tables = TableNameExtractor.extractTableName(sqlNode);
        Assert.assertEquals(2, tables.size());
        Assert.assertEquals("[TEST1, TEST2]",tables.toString());
    }
}