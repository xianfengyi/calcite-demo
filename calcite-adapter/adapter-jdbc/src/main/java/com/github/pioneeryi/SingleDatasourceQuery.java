package com.github.pioneeryi;

import java.sql.*;
import java.util.Properties;

public class SingleDatasourceQuery {
    public static void main(String[] args) throws Exception {
        String path = MultiDatasourceQuery.class.getResource("/single-datasource-model.json").toString();
        Properties properties = new Properties();
        properties.setProperty("caseSensitive", "false");
        properties.put("model", path.replace("file:", ""));

        String sql = "select sex,count(sex) as sex_count\n" +
                "from student\n" +
                "where id>0\n" +
                "group by sex";
        try (Connection conn = DriverManager.getConnection("jdbc:calcite:",properties)) {
            // 查询数据
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetUtil.printRs(rs);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
