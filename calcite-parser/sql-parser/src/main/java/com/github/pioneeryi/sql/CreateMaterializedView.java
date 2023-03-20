package com.github.pioneeryi.sql;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParserPos;

import java.util.ArrayList;
import java.util.List;

public class CreateMaterializedView extends SqlCall {
    public static final SqlSpecialOperator CREATE_MATERIALIZED_VIEW = new SqlSpecialOperator(
            "CREATE_MATERIALIZED_VIEW", SqlKind.OTHER_DDL);
    SqlIdentifier viewName;
    boolean existenceCheck;
    SqlSelect query;

    public CreateMaterializedView(SqlParserPos pos, SqlIdentifier viewName, boolean existenceCheck, SqlSelect query) {
        super(pos);
        this.viewName = viewName;
        this.existenceCheck = existenceCheck;
        this.query = query;
    }

    @Override
    public SqlOperator getOperator() {
        return CREATE_MATERIALIZED_VIEW;
    }

    @Override
    public List<SqlNode> getOperandList() {
        List<SqlNode> operands = new ArrayList<>();
        operands.add(viewName);
        operands.add(SqlLiteral.createBoolean(existenceCheck, SqlParserPos.ZERO));
        operands.add(query);
        return operands;
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        writer.keyword("CREATE MATERIALIZED VIEW");
        if (existenceCheck) {
            writer.keyword("IF NOT EXISTS");
        }
        viewName.unparse(writer, leftPrec, rightPrec);
        writer.keyword("AS");
        query.unparse(writer, leftPrec, rightPrec);
    }
}