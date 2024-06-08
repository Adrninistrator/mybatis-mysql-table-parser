package com.adrninistrator.mybatismysqltableparser.visitor;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author adrninistrator
 * @date 2024/3/3
 * @description: 处理SQLExprTableSource的visitor，用于获取表名（多个）
 */
public class SQLExprTableSourceMultiVisitor extends SQLExprTableSourceVisitor {

    private final List<String> tableNameList;

    public SQLExprTableSourceMultiVisitor() {
        tableNameList = new ArrayList<>();
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        String tableName = getTableNameFromTableSource(x);
        if (!MyBatisTableParserConstants.FLAG_ALL.equals(tableName)) {
            tableNameList.add(tableName);
        }
        return true;
    }

    public List<String> getTableNameList() {
        return tableNameList;
    }
}
