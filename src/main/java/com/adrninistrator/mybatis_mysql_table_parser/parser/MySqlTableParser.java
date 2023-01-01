package com.adrninistrator.mybatis_mysql_table_parser.parser;

import com.adrninistrator.mybatis_mysql_table_parser.common.enums.MySqlStatementEnum;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MySqlTableInfo;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLSomeExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLBlockStatement;
import com.alibaba.druid.sql.ast.statement.SQLCallStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLMergeStatement;
import com.alibaba.druid.sql.ast.statement.SQLReplaceStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLShowTablesStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTruncateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLValuesTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlDeclareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author adrninistrator
 * @date 2022/12/20
 * @description: 解析sql语句中使用的表名
 */
public class MySqlTableParser {
    private static final Logger logger = LoggerFactory.getLogger(MySqlTableParser.class);

    /**
     * 解析sql语句中使用的表名
     *
     * @param fullSql
     * @return
     */
    public MySqlTableInfo parseTablesInSql(String fullSql) {
        MySqlTableInfo mySqlTableInfo = new MySqlTableInfo();
        try {
            MySqlStatementParser parser = new MySqlStatementParser(fullSql);
            SQLStatement sqlStatement = parser.parseStatement();

            if (sqlStatement instanceof SQLSelectStatement) {
                // 解析select语句
                parseSelectStatement((SQLSelectStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof MySqlInsertStatement) {
                // 解析insert into语句
                parseInsertStatement((MySqlInsertStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLReplaceStatement) {
                // 解析replace into语句
                parseReplaceStatement((SQLReplaceStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLUpdateStatement) {
                // 解析update语句
                parseUpdateStatement((SQLUpdateStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLDeleteStatement) {
                // 解析delete语句
                parseDeleteStatement((SQLDeleteStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLAlterTableStatement) {
                // 解析alter table语句
                parseAlterStatement((SQLAlterTableStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLTruncateStatement) {
                // 解析truncate table语句
                parseTruncateStatement((SQLTruncateStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof MySqlCreateTableStatement) {
                // 解析create table语句
                parseCreateStatement((MySqlCreateTableStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLDropTableStatement) {
                // 解析drop table语句
                parseDropStatement((SQLDropTableStatement) sqlStatement, mySqlTableInfo);
            } else if (!(sqlStatement instanceof SQLSetStatement) &&
                    !(sqlStatement instanceof MySqlSetTransactionStatement) &&
                    !(sqlStatement instanceof SQLCallStatement) &&
                    !(sqlStatement instanceof SQLMergeStatement) &&
                    !(sqlStatement instanceof SQLBlockStatement) &&
                    !(sqlStatement instanceof MySqlDeclareStatement) &&
                    !(sqlStatement instanceof SQLShowTablesStatement)
            ) {
                /*
                    SQLSetStatement                         MySQL对系统变量进行修改的SET语句
                    MySqlSetTransactionStatement            MySQL SET TRANSACTION语句
                    SQLCallStatement                        调用存储过程
                    SQLMergeStatement                       MERGE INTO语句
                    SQLBlockStatement                       使用BEGIN...END包含的语句等
                    MySqlDeclareStatement                   declare语句
                    SQLShowTablesStatement                  SHOW TABLES LIKE语句
                    MySqlShowProfilesStatement              SHOW PROFILES语句
                 */
                logger.error("暂未处理的SQLStatement类型 {}", sqlStatement.getClass().getName());
            }
        } catch (Exception e) {
            logger.error("error {} ", fullSql, e);
            mySqlTableInfo.setParseFail(true);
        }
        return mySqlTableInfo;
    }

    // 解析select语句
    private void parseSelectStatement(SQLSelectStatement sqlSelectStatement, MySqlTableInfo mySqlTableInfo) {
        SQLSelect sqlSelect = sqlSelectStatement.getSelect();
        if (sqlSelect.getQuery() != null) {
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(sqlSelect.getQuery(), mySqlTableInfo);
        }
    }

    // 处理SQLSelectQuery对象
    private void handleSQLSelectQuery(SQLSelectQuery sqlSelectQuery, MySqlTableInfo mySqlTableInfo) {
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            MySqlSelectQueryBlock mySqlSelectQueryBlock = null;
            if (sqlSelectQuery instanceof MySqlSelectQueryBlock) {
                mySqlSelectQueryBlock = (MySqlSelectQueryBlock) sqlSelectQuery;
            }
            // 一般的表名形式
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
            if (sqlTableSource != null) {
                // 判断是select还是select for update
                MySqlStatementEnum mySqlStatementEnum = MySqlStatementEnum.DSE_SELECT;
                if (mySqlSelectQueryBlock != null && mySqlSelectQueryBlock.isForUpdate()) {
                    mySqlStatementEnum = MySqlStatementEnum.DSE_SELECT_4_UPDATE;
                }

                // 处理涉及的表名
                handleSQLTableSource(sqlTableSource, mySqlTableInfo, mySqlStatementEnum);
            }

            SQLExpr whereSQLExpr = sqlSelectQueryBlock.getWhere();
            if (whereSQLExpr != null) {
                // 处理where语句
                handleWhereSqlExpr(whereSQLExpr, mySqlTableInfo);
            }
            return;
        }

        if (sqlSelectQuery instanceof SQLUnionQuery) {
            // union查询的形式
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            for (SQLSelectQuery childSqlSelectQuery : sqlUnionQuery.getChildren()) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(childSqlSelectQuery, mySqlTableInfo);
            }
            return;
        }

        if (!(sqlSelectQuery instanceof SQLValuesTableSource)) {
            /*
                SQLValuesTableSource    values next value for xxx
             */
            logger.error("暂未处理的SQLSelectQuery类型 {}", sqlSelectQuery.getClass().getName());
        }
    }

    // 处理where语句
    private void handleWhereSqlExpr(SQLExpr whereSqlExpr, MySqlTableInfo mySqlTableInfo) {
        if (whereSqlExpr == null) {
            return;
        }

        if (whereSqlExpr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) whereSqlExpr;
            if (sqlInSubQueryExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlInSubQueryExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLExistsExpr) {
            SQLExistsExpr sqlExistsExpr = (SQLExistsExpr) whereSqlExpr;
            if (sqlExistsExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlExistsExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLAllExpr) {
            SQLAllExpr sqlAllExpr = (SQLAllExpr) whereSqlExpr;
            if (sqlAllExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlAllExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLAnyExpr) {
            SQLAnyExpr sqlAnyExpr = (SQLAnyExpr) whereSqlExpr;
            if (sqlAnyExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlAnyExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLSomeExpr) {
            SQLSomeExpr sqlSomeExpr = (SQLSomeExpr) whereSqlExpr;
            if (sqlSomeExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlSomeExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (!(whereSqlExpr instanceof SQLBinaryOpExpr) &&
                !(whereSqlExpr instanceof SQLInListExpr) &&
                !(whereSqlExpr instanceof SQLBetweenExpr) &&
                !(whereSqlExpr instanceof SQLIdentifierExpr) &&
                !(whereSqlExpr instanceof SQLMethodInvokeExpr) &&
                !(whereSqlExpr instanceof SQLVariantRefExpr)
        ) {
            /*
                SQLBinaryOpExpr         where语句中的判断条件
                SQLInListExpr           in ...
                SQLBetweenExpr          between...and
                SQLIdentifierExpr       sql语句解析有问题，例如<where>标签中通过include指定了空的<sql>内容
                SQLMethodInvokeExpr     方法调用
                SQLVariantRefExpr       ?
             */
            logger.error("暂未处理的SQLExpr类型 {}", whereSqlExpr.getClass().getName());
        }
    }

    // 解析insert语句
    private void parseInsertStatement(MySqlInsertStatement mySqlInsertStatement, MySqlTableInfo mySqlTableInfo) {
        MySqlStatementEnum mySqlStatementEnum = MySqlStatementEnum.DSE_INSERT;
        if (mySqlInsertStatement.isIgnore()) {
            mySqlStatementEnum = MySqlStatementEnum.DSE_INSERT_IGNORE;
        }
        if (mySqlInsertStatement.getDuplicateKeyUpdate() != null && !mySqlInsertStatement.getDuplicateKeyUpdate().isEmpty()) {
            mySqlStatementEnum = MySqlStatementEnum.DSE_INSERT_OR_UPDATE;
        }

        SQLExprTableSource sqlExprTableSource = mySqlInsertStatement.getTableSource();
        String tableName = sqlExprTableSource.getTableName();
        // 记录表名
        recordTableName(tableName, mySqlTableInfo, mySqlStatementEnum);

        if (mySqlInsertStatement.getQuery() != null) {
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(mySqlInsertStatement.getQuery().getQuery(), mySqlTableInfo);
        }
    }

    // 解析replace语句
    private void parseReplaceStatement(SQLReplaceStatement sqlReplaceStatement, MySqlTableInfo mySqlTableInfo) {
        SQLExprTableSource sqlExprTableSource = sqlReplaceStatement.getTableSource();
        String tableName = sqlExprTableSource.getTableName();

        // 记录表名
        recordTableName(tableName, mySqlTableInfo, MySqlStatementEnum.DSE_REPLACE);

        SQLQueryExpr sqlQueryExpr = sqlReplaceStatement.getQuery();
        if (sqlQueryExpr != null && sqlQueryExpr.getSubQuery() != null) {
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(sqlQueryExpr.getSubQuery().getQuery(), mySqlTableInfo);
        }
    }

    // 解析update语句
    private void parseUpdateStatement(SQLUpdateStatement sqlUpdateStatement, MySqlTableInfo mySqlTableInfo) {
        // 记录update相关的表名
        MySqlTableInfo updateMySqlTableInfo = new MySqlTableInfo();
        // 记录update时使用别名进行了set的相关表名
        MySqlTableInfo updateUseAliasMySqlTableInfo = new MySqlTableInfo();

        SQLTableSource sqlTableSource = sqlUpdateStatement.getTableSource();
        // 处理涉及的表名，update相关
        handleSQLTableSource(sqlTableSource, updateMySqlTableInfo, MySqlStatementEnum.DSE_UPDATE);

        for (SQLUpdateSetItem sqlUpdateSetItem : sqlUpdateStatement.getItems()) {
            SQLExpr columnExpr = sqlUpdateSetItem.getColumn();
            if (columnExpr instanceof SQLPropertyExpr) {
                // update的set字段有使用别名的情况
                SQLPropertyExpr columnPropertyExpr = (SQLPropertyExpr) columnExpr;
                String tableAlias = columnPropertyExpr.getOwnerName();
                SQLTableSource setSqlTableSource = sqlTableSource.findTableSource(tableAlias);
                // 处理涉及的表名
                handleSQLTableSource(setSqlTableSource, updateUseAliasMySqlTableInfo, MySqlStatementEnum.DSE_UPDATE);
            } else if (!(columnExpr instanceof SQLIdentifierExpr) &&
                    !(columnExpr instanceof SQLListExpr)) {
                /*
                    SQLIdentifierExpr   update的set字段未使用别名的情况
                    SQLListExpr         (col1, col2, col3) = (?, ?, ?)
                 */
                logger.error("暂未处理的SQLExpr类型 {}", columnExpr.getClass().getName());
            }
        }

        List<String> updateUseAliasTableList = updateUseAliasMySqlTableInfo.getUpdateTableList();
        if (updateUseAliasMySqlTableInfo.getUpdateTableList().isEmpty()) {
            // update未使用别名，将update相关表名添加到结果中
            MySqlTableInfo.copyUpdateTableList(updateMySqlTableInfo, mySqlTableInfo);
        } else {
            // update有使用别名
            for (String updateTableList : updateMySqlTableInfo.getUpdateTableList()) {
                if (updateUseAliasTableList.contains(updateTableList)) {
                    // 当前update相关的表名有通过别名进行set，将被更新的表名添加到结果中
                    mySqlTableInfo.addUpdateTable(updateTableList);
                    continue;
                }
                // 当前update相关的表名未通过别名进行set，将查询使用的表名添加到结果中
                mySqlTableInfo.addSelectTable(updateTableList);
            }
        }

        SQLExpr whereSQLExpr = sqlUpdateStatement.getWhere();
        if (whereSQLExpr != null) {
            // 处理where语句
            handleWhereSqlExpr(whereSQLExpr, mySqlTableInfo);
        }

        /*
            SQLUpdateStatement.getWhere()方法获取的是from字段，在setFrom()方法中赋值
            该方法只在以下类中有调用，MySql不涉及，因此不处理sqlUpdateStatement.getFrom()
            OscarStatementParser.java
            PGSQLStatementParser.java
            SQLServerStatementParser.java
         */
    }

    // 解析delete语句
    private void parseDeleteStatement(SQLDeleteStatement sqlDeleteStatement, MySqlTableInfo mySqlTableInfo) {
        // 记录delete相关的表名或别名
        MySqlTableInfo deleteMySqlTableInfo = new MySqlTableInfo();
        // 记录from相关的表名
        MySqlTableInfo fromMySqlTableInfo = new MySqlTableInfo();

        SQLTableSource sqlTableSource = sqlDeleteStatement.getTableSource();
        // 处理delete涉及的表名或别名
        if (sqlTableSource instanceof SQLExprTableSource) {
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            SQLExpr tableSourceExpr = sqlExprTableSource.getExpr();
            if (tableSourceExpr instanceof SQLIdentifierExpr) {
                // 使用别名，且delete时为"delete alias"形式
                SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) tableSourceExpr;
                String tableNameOrAlias = sqlIdentifierExpr.getName();
                // 记录表名或别名
                recordTableName(tableNameOrAlias, deleteMySqlTableInfo, MySqlStatementEnum.DSE_DELETE);
            } else if (tableSourceExpr instanceof SQLPropertyExpr) {
                // 使用别名，且delete时为"delete alias.*"形式
                SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) tableSourceExpr;
                String tableNameOrAlias = sqlPropertyExpr.getOwnerName();
                // 记录表名或别名
                recordTableName(tableNameOrAlias, deleteMySqlTableInfo, MySqlStatementEnum.DSE_DELETE);
            } else if (!(tableSourceExpr instanceof SQLVariantRefExpr)) {
                /*
                    SQLVariantRefExpr       ?
                 */
                logger.error("暂未支持的SQLExpr类型 {}", tableSourceExpr.getClass().getName());
            }
        } else {
            /*
                SQLTableSource sqlTableSource可能为SQLJoinTableSource类型，但MySQL不支持这种写法
                delete from test_balance_log l join test_balance b on l.id=b.id where b.balance = '1.23'
             */
            logger.error("暂未支持的SQLTableSource类型 {}", sqlTableSource.getClass().getName());
        }

        SQLExpr whereSQLExpr = sqlDeleteStatement.getWhere();
        if (whereSQLExpr != null) {
            // 处理where语句，添加表select相关的表名
            handleWhereSqlExpr(whereSQLExpr, mySqlTableInfo);
        }

        SQLTableSource sqlTableSourceFrom = sqlDeleteStatement.getFrom();
        if (sqlTableSourceFrom == null) {
            // delete的from为空，即未使用别名删除的形式
            MySqlTableInfo.copyDeleteTableList(deleteMySqlTableInfo, mySqlTableInfo);
            return;
        }

        // delete的from非空，即使用别名删除的形式
        handleSQLTableSource(sqlTableSourceFrom, fromMySqlTableInfo, MySqlStatementEnum.DSE_SELECT);

        for (String deleteTableNameOrAlias : deleteMySqlTableInfo.getDeleteTableList()) {
            SQLTableSource deleteSqlTableSource = sqlTableSourceFrom.findTableSource(deleteTableNameOrAlias);
            if (deleteSqlTableSource != null) {
                // 处理涉及的表名，记录delete相关表名
                handleSQLTableSource(deleteSqlTableSource, mySqlTableInfo, MySqlStatementEnum.DSE_DELETE);
            }
        }

        // 对于from中有查询的表名，假如不是delete相关的表名，则添加到select相关的表名中
        for (String selectTableName : fromMySqlTableInfo.getSelectTableList()) {
            if (!mySqlTableInfo.getDeleteTableList().contains(selectTableName)) {
                mySqlTableInfo.addSelectTable(selectTableName);
            }
        }
    }

    // 解析alter table语句
    private void parseAlterStatement(SQLAlterTableStatement sqlAlterTableStatement, MySqlTableInfo mySqlTableInfo) {
        // 处理涉及的表名
        handleSQLTableSource(sqlAlterTableStatement.getTableSource(), mySqlTableInfo, MySqlStatementEnum.DSE_ALTER);
    }

    // 解析truncate table语句
    private void parseTruncateStatement(SQLTruncateStatement sqlTruncateStatement, MySqlTableInfo mySqlTableInfo) {
        for (SQLExprTableSource sqlExprTableSource : sqlTruncateStatement.getTableSources()) {
            // 处理涉及的表名
            handleSQLTableSource(sqlExprTableSource, mySqlTableInfo, MySqlStatementEnum.DSE_TRUNCATE);
        }
    }

    // 解析create table语句
    private void parseCreateStatement(MySqlCreateTableStatement mySqlCreateTableStatement, MySqlTableInfo mySqlTableInfo) {
        // 处理涉及的表名
        handleSQLTableSource(mySqlCreateTableStatement.getTableSource(), mySqlTableInfo, MySqlStatementEnum.DSE_CREATE);
    }

    // 解析drop table语句
    private void parseDropStatement(SQLDropTableStatement sqlDropTableStatement, MySqlTableInfo mySqlTableInfo) {
        for (SQLExprTableSource sqlExprTableSource : sqlDropTableStatement.getTableSources()) {
            // 处理涉及的表名
            handleSQLTableSource(sqlExprTableSource, mySqlTableInfo, MySqlStatementEnum.DSE_DROP);
        }
    }

    // 处理涉及的表名
    private void handleSQLTableSource(SQLTableSource sqlTableSource, MySqlTableInfo mySqlTableInfo, MySqlStatementEnum mySqlStatementEnum) {
        if (sqlTableSource == null) {
            return;
        }

        if (sqlTableSource instanceof SQLExprTableSource) {
            // 一般的表名形式
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            String tableName = sqlExprTableSource.getTableName();
            // 记录表名
            recordTableName(tableName, mySqlTableInfo, mySqlStatementEnum);
            return;
        }

        if (sqlTableSource instanceof SQLJoinTableSource) {
            // join查询的形式
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource leftSQLTableSource = sqlJoinTableSource.getLeft();
            if (leftSQLTableSource != null) {
                // 处理涉及的表名
                handleSQLTableSource(leftSQLTableSource, mySqlTableInfo, mySqlStatementEnum);
            }
            SQLTableSource rightSQLTableSource = sqlJoinTableSource.getRight();
            if (rightSQLTableSource != null) {
                // 处理涉及的表名
                handleSQLTableSource(rightSQLTableSource, mySqlTableInfo, mySqlStatementEnum);
            }
            return;
        }

        if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // 使用子查询的形式
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) sqlTableSource;
            if (sqlSubqueryTableSource.getSelect() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlSubqueryTableSource.getSelect().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (sqlTableSource instanceof SQLUnionQueryTableSource) {
            // 子查询中union的形式
            SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) sqlTableSource;
            if (sqlUnionQueryTableSource.getUnion() != null && sqlUnionQueryTableSource.getUnion().getChildren() != null) {
                for (SQLSelectQuery sqlSelectQuery : sqlUnionQueryTableSource.getUnion().getChildren()) {
                    // 处理SQLSelectQuery对象
                    handleSQLSelectQuery(sqlSelectQuery, mySqlTableInfo);
                }
            }
            return;
        }

        logger.error("暂未处理的SQLTableSource类型 {}", sqlTableSource.getClass().getName());
    }

    // 记录表名
    private void recordTableName(String tableName, MySqlTableInfo mySqlTableInfo, MySqlStatementEnum mySqlStatementEnum) {
        switch (mySqlStatementEnum) {
            case DSE_SELECT:
                mySqlTableInfo.addSelectTable(tableName);
                break;
            case DSE_SELECT_4_UPDATE:
                mySqlTableInfo.addSelect4UpdateTable(tableName);
                break;
            case DSE_INSERT:
                mySqlTableInfo.addInsertTable(tableName);
                break;
            case DSE_INSERT_IGNORE:
                mySqlTableInfo.addInsertIgnoreTable(tableName);
                break;
            case DSE_INSERT_OR_UPDATE:
                mySqlTableInfo.addInsertOrUpdateTable(tableName);
                break;
            case DSE_REPLACE:
                mySqlTableInfo.addReplaceIntoTable(tableName);
                break;
            case DSE_UPDATE:
                mySqlTableInfo.addUpdateTable(tableName);
                break;
            case DSE_DELETE:
                mySqlTableInfo.addDeleteTable(tableName);
                break;
            case DSE_ALTER:
                mySqlTableInfo.addAlterTable(tableName);
                break;
            case DSE_TRUNCATE:
                mySqlTableInfo.addTruncateTable(tableName);
                break;
            case DSE_CREATE:
                mySqlTableInfo.addCreateTable(tableName);
                break;
            case DSE_DROP:
                mySqlTableInfo.addDropTable(tableName);
                break;
            default:
                logger.error("非法的语句 {}", mySqlStatementEnum);
                break;
        }
    }
}
