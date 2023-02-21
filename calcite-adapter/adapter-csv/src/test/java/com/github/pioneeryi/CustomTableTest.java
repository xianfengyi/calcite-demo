package com.github.pioneeryi;

import com.github.pioneeryi.util.ResultSetUtil;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class CustomTableTest {

    @Test
    public void testSelecAll() {
        String sql = "select * from depts";

        String expected = "EMPNO, NAME, DEPTNO, GENDER, CITY, EMPID, AGE, SLACKER, MANAGER, JOINEDAT\n"
                + "100, Fred, 10, , , 30, 25, true, false, 1996-08-03\n"
                + "110, Eric, 20, M, San Francisco, 3, 80, null, false, 2001-01-01\n"
                + "110, John, 40, M, Vancouver, 2, 10, false, true, 2002-05-03\n"
                + "120, Wilma, 20, F, , 1, 5, false, true, 2005-09-07\n"
                + "130, Alice, 40, F, Vancouver, 2, 13, false, true, 2007-01-01\n";
        String result = executeSql(sql);
        Assert.assertEquals(expected, result);
    }

    private String executeSql(String sql) {
        String path = CustomTableTest.class.getResource("/model-with-custom-table.json").toString();
        Properties info = new Properties();
        info.setProperty("caseSensitive", "false");
        info.put("model", path.replace("file:", ""));
        try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
            Statement statement = connection.createStatement();
            // 查询所有数据
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSetUtil.resultString(resultSet, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
