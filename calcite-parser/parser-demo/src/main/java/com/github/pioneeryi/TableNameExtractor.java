package com.github.pioneeryi;

import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;

import java.util.ArrayList;
import java.util.List;

public class TableNameExtractor {

    private static final List<String> tables = new ArrayList<>();

    public static List<String> extractTableName(SqlNode node) {
        node = ((SqlSelect) node).getFrom();
        if (node == null) {
            return tables;
        }

        if (node.getKind() == SqlKind.IDENTIFIER){
            tables.add(node.toString());
        }

        if (node.getKind() == SqlKind.JOIN){
            SqlNode left = ((SqlJoin)node).getLeft();
            if (left.getKind() == SqlKind.IDENTIFIER){
                tables.add(left.toString());
            }
            SqlNode right = ((SqlJoin)node).getRight();
            if (right.getKind() == SqlKind.IDENTIFIER){
                tables.add(right.toString());
            }
        }
        return tables;
    }
}
