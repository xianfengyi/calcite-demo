package com.github.pioneeryi.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ResultSet 工具类.
 */
public class ResultSetUtil {

    /**
     * ResultSet 内容转换为字符串.
     *
     * @param resultSet ResultSet
     *
     * @return 字符串
     *
     * @throws SQLException 如果失败，抛出此异常
     */
    public static String resultString(ResultSet resultSet) throws SQLException {
        return resultString(resultSet, false);
    }

    /**
     * ResultSet 内容转换为字符串.
     *
     * @param resultSet   ResultSet
     * @param printHeader 是否打印 Header
     *
     * @return 字符串
     *
     * @throws SQLException 如果失败，抛出此异常
     */
    public static String resultString(ResultSet resultSet, boolean printHeader) throws SQLException {
        List<List<Object>> resultList = resultList(resultSet, printHeader);
        return resultString(resultList);
    }

    private static String resultString(List<List<Object>> resultList) throws SQLException {
        StringBuilder builder = new StringBuilder();
        resultList.forEach(row -> {
            String rowStr = row.stream()
                    .map(columnValue -> columnValue + ", ")
                    .collect(Collectors.joining());
            rowStr = rowStr.substring(0, rowStr.lastIndexOf(", ")) + "\n";
            builder.append(rowStr);
        });
        return builder.toString();
    }


    /**
     * ResultSet 内容转换为数组结构.
     *
     * @param resultSet ResultSet
     *
     * @return 字符串
     *
     * @throws SQLException 如果失败，抛出此异常
     */
    public static List<List<Object>> resultList(ResultSet resultSet) throws SQLException {
        return resultList(resultSet, false);
    }

    /**
     * ResultSet 内容转换为数组结构.
     *
     * @param resultSet   ResultSet
     * @param printHeader 是否打印 Header
     *
     * @return 字符串
     *
     * @throws SQLException 如果失败，抛出此异常
     */
    public static List<List<Object>> resultList(ResultSet resultSet, boolean printHeader) throws SQLException {
        ArrayList<List<Object>> results = new ArrayList<>();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        if (printHeader) {
            ArrayList<Object> header = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                header.add(metaData.getColumnName(i));
            }
            results.add(header);
        }
        while (resultSet.next()) {
            ArrayList<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(resultSet.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
}
