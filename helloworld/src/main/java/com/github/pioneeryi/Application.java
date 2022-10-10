package com.github.pioneeryi;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        try {
            String path = URLDecoder.decode(Application.class.getResource("/model.json").toString(), "UTF-8");
            Properties info = new Properties();
            info.put("model", path.replace("file:", ""));
            Connection connection = DriverManager.getConnection("jdbc:calcite:", info);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from T_TEST_TABLE");
            while (resultSet.next()) {
                System.out.println(resultSet.getObject("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
