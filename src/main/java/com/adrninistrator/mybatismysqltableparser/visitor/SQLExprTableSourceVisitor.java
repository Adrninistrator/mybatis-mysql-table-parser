package com.adrninistrator.mybatismysqltableparser.visitor;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;

/**
 * @author adrninistrator
 * @date 2023/10/7
 * @description: 处理SQLExprTableSource的visitor，用于获取表名（一个）
 */
public class SQLExprTableSourceVisitor implements MySqlASTVisitor {

    private String tableName;

    protected String getTableNameFromTableSource(SQLExprTableSource sqlExprTableSource) {
        String tableName = sqlExprTableSource.getTableName();
        if (tableName == null) {
            SQLExpr sqlExpr = sqlExprTableSource.getExpr();
            if (sqlExpr instanceof SQLVariantRefExpr) {
                // 表名使用${}形式的情况
                tableName = ((SQLVariantRefExpr) sqlExpr).getName();
            }
        }
        return tableName;
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        String tableName = getTableNameFromTableSource(x);
        if (!MyBatisTableParserConstants.FLAG_ALL.equals(tableName)) {
            this.tableName = tableName;
        }
        return true;
    }

    public String getTableName() {
        return tableName;
    }
}
