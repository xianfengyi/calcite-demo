package com.github.pioneeryi;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetUtil {

    public static void printRs(ResultSet rs) throws SQLException {
        int count = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= count; i++) {
                String label = rs.getMetaData().getColumnLabel(i);
                Object val = rs.getObject(i);
                String value = "null";
                if (val != null) {
                    value = val.toString();
                }
                sb.append(label + ":" + value);
                if (i != count) {
                    sb.append(" , ");
                }
            }
            System.out.println(sb);
        }
    }
}
