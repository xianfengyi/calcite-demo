package com.github.pioneeryi;


import java.sql.*;
import java.util.Properties;

public class MultiDatasourceQuery {

    public static void main(String[] args) throws Exception {
        String path = MultiDatasourceQuery.class.getResource("/multi-datasource-model.json").toString();
        Properties properties = new Properties();
        properties.setProperty("caseSensitive", "false");
        properties.put("model", path.replace("file:", ""));

        String sql = "select student.name, sum(score.grade) as grade " +
                "from db1.student as student join db2.score as score on student.id=score.student_id " +
                "where student.id>0 " +
                "group by student.name ";

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
