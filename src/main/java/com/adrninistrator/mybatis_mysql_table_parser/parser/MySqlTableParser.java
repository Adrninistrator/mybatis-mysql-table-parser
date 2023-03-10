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
 * @description: ??????sql????????????????????????
 */
public class MySqlTableParser {
    private static final Logger logger = LoggerFactory.getLogger(MySqlTableParser.class);

    /**
     * ??????sql????????????????????????
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
                // ??????select??????
                parseSelectStatement((SQLSelectStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof MySqlInsertStatement) {
                // ??????insert into??????
                parseInsertStatement((MySqlInsertStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLReplaceStatement) {
                // ??????replace into??????
                parseReplaceStatement((SQLReplaceStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLUpdateStatement) {
                // ??????update??????
                parseUpdateStatement((SQLUpdateStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLDeleteStatement) {
                // ??????delete??????
                parseDeleteStatement((SQLDeleteStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLAlterTableStatement) {
                // ??????alter table??????
                parseAlterStatement((SQLAlterTableStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLTruncateStatement) {
                // ??????truncate table??????
                parseTruncateStatement((SQLTruncateStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof MySqlCreateTableStatement) {
                // ??????create table??????
                parseCreateStatement((MySqlCreateTableStatement) sqlStatement, mySqlTableInfo);
            } else if (sqlStatement instanceof SQLDropTableStatement) {
                // ??????drop table??????
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
                    SQLSetStatement                         MySQL??????????????????????????????SET??????
                    MySqlSetTransactionStatement            MySQL SET TRANSACTION??????
                    SQLCallStatement                        ??????????????????
                    SQLMergeStatement                       MERGE INTO??????
                    SQLBlockStatement                       ??????BEGIN...END??????????????????
                    MySqlDeclareStatement                   declare??????
                    SQLShowTablesStatement                  SHOW TABLES LIKE??????
                    MySqlShowProfilesStatement              SHOW PROFILES??????
                 */
                logger.error("???????????????SQLStatement?????? {}", sqlStatement.getClass().getName());
            }
        } catch (Exception e) {
            logger.error("error {} ", fullSql, e);
            mySqlTableInfo.setParseFail(true);
        }
        return mySqlTableInfo;
    }

    // ??????select??????
    private void parseSelectStatement(SQLSelectStatement sqlSelectStatement, MySqlTableInfo mySqlTableInfo) {
        SQLSelect sqlSelect = sqlSelectStatement.getSelect();
        if (sqlSelect.getQuery() != null) {
            // ??????SQLSelectQuery??????
            handleSQLSelectQuery(sqlSelect.getQuery(), mySqlTableInfo);
        }
    }

    // ??????SQLSelectQuery??????
    private void handleSQLSelectQuery(SQLSelectQuery sqlSelectQuery, MySqlTableInfo mySqlTableInfo) {
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            MySqlSelectQueryBlock mySqlSelectQueryBlock = null;
            if (sqlSelectQuery instanceof MySqlSelectQueryBlock) {
                mySqlSelectQueryBlock = (MySqlSelectQueryBlock) sqlSelectQuery;
            }
            // ?????????????????????
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
            if (sqlTableSource != null) {
                // ?????????select??????select for update
                MySqlStatementEnum mySqlStatementEnum = MySqlStatementEnum.DSSE_SELECT;
                if (mySqlSelectQueryBlock != null && mySqlSelectQueryBlock.isForUpdate()) {
                    mySqlStatementEnum = MySqlStatementEnum.DSSE_SELECT_4_UPDATE;
                }

                // ?????????????????????
                handleSQLTableSource(sqlTableSource, mySqlTableInfo, mySqlStatementEnum);
            }

            SQLExpr whereSQLExpr = sqlSelectQueryBlock.getWhere();
            if (whereSQLExpr != null) {
                // ??????where??????
                handleWhereSqlExpr(whereSQLExpr, mySqlTableInfo);
            }
            return;
        }

        if (sqlSelectQuery instanceof SQLUnionQuery) {
            // union???????????????
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            for (SQLSelectQuery childSqlSelectQuery : sqlUnionQuery.getChildren()) {
                // ??????SQLSelectQuery??????
                handleSQLSelectQuery(childSqlSelectQuery, mySqlTableInfo);
            }
            return;
        }

        if (!(sqlSelectQuery instanceof SQLValuesTableSource)) {
            /*
                SQLValuesTableSource    values next value for xxx
             */
            logger.error("???????????????SQLSelectQuery?????? {}", sqlSelectQuery.getClass().getName());
        }
    }

    // ??????where??????
    private void handleWhereSqlExpr(SQLExpr whereSqlExpr, MySqlTableInfo mySqlTableInfo) {
        if (whereSqlExpr == null) {
            return;
        }

        if (whereSqlExpr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) whereSqlExpr;
            if (sqlInSubQueryExpr.getSubQuery() != null) {
                // ??????SQLSelectQuery??????
                handleSQLSelectQuery(sqlInSubQueryExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLExistsExpr) {
            SQLExistsExpr sqlExistsExpr = (SQLExistsExpr) whereSqlExpr;
            if (sqlExistsExpr.getSubQuery() != null) {
                // ??????SQLSelectQuery??????
                handleSQLSelectQuery(sqlExistsExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLAllExpr) {
            SQLAllExpr sqlAllExpr = (SQLAllExpr) whereSqlExpr;
            if (sqlAllExpr.getSubQuery() != null) {
                // ??????SQLSelectQuery??????
                handleSQLSelectQuery(sqlAllExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLAnyExpr) {
            SQLAnyExpr sqlAnyExpr = (SQLAnyExpr) whereSqlExpr;
            if (sqlAnyExpr.getSubQuery() != null) {
                // ??????SQLSelectQuery??????
                handleSQLSelectQuery(sqlAnyExpr.getSubQuery().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLSomeExpr) {
            SQLSomeExpr sqlSomeExpr = (SQLSomeExpr) whereSqlExpr;
            if (sqlSomeExpr.getSubQuery() != null) {
                // ??????SQLSelectQuery??????
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
                SQLBinaryOpExpr         where????????????????????????
                SQLInListExpr           in ...
                SQLBetweenExpr          between...and
                SQLIdentifierExpr       sql??????????????????????????????<where>???????????????include???????????????<sql>??????
                SQLMethodInvokeExpr     ????????????
                SQLVariantRefExpr       ?
             */
            logger.error("???????????????SQLExpr?????? {}", whereSqlExpr.getClass().getName());
        }
    }

    // ??????insert??????
    private void parseInsertStatement(MySqlInsertStatement mySqlInsertStatement, MySqlTableInfo mySqlTableInfo) {
        MySqlStatementEnum mySqlStatementEnum = MySqlStatementEnum.DSSE_INSERT;
        if (mySqlInsertStatement.isIgnore()) {
            mySqlStatementEnum = MySqlStatementEnum.DSSE_INSERT_IGNORE;
        }
        if (mySqlInsertStatement.getDuplicateKeyUpdate() != null && !mySqlInsertStatement.getDuplicateKeyUpdate().isEmpty()) {
            mySqlStatementEnum = MySqlStatementEnum.DSSE_INSERT_OR_UPDATE;
        }

        SQLExprTableSource sqlExprTableSource = mySqlInsertStatement.getTableSource();
        String tableName = sqlExprTableSource.getTableName();
        // ????????????
        recordTableName(tableName, mySqlTableInfo, mySqlStatementEnum);

        if (mySqlInsertStatement.getQuery() != null) {
            // ??????SQLSelectQuery??????
            handleSQLSelectQuery(mySqlInsertStatement.getQuery().getQuery(), mySqlTableInfo);
        }
    }

    // ??????replace??????
    private void parseReplaceStatement(SQLReplaceStatement sqlReplaceStatement, MySqlTableInfo mySqlTableInfo) {
        SQLExprTableSource sqlExprTableSource = sqlReplaceStatement.getTableSource();
        String tableName = sqlExprTableSource.getTableName();

        // ????????????
        recordTableName(tableName, mySqlTableInfo, MySqlStatementEnum.DSSE_REPLACE);

        SQLQueryExpr sqlQueryExpr = sqlReplaceStatement.getQuery();
        if (sqlQueryExpr != null && sqlQueryExpr.getSubQuery() != null) {
            // ??????SQLSelectQuery??????
            handleSQLSelectQuery(sqlQueryExpr.getSubQuery().getQuery(), mySqlTableInfo);
        }
    }

    // ??????update??????
    private void parseUpdateStatement(SQLUpdateStatement sqlUpdateStatement, MySqlTableInfo mySqlTableInfo) {
        // ??????update???????????????
        MySqlTableInfo updateMySqlTableInfo = new MySqlTableInfo();
        // ??????update????????????????????????set???????????????
        MySqlTableInfo updateUseAliasMySqlTableInfo = new MySqlTableInfo();

        SQLTableSource sqlTableSource = sqlUpdateStatement.getTableSource();
        // ????????????????????????update??????
        handleSQLTableSource(sqlTableSource, updateMySqlTableInfo, MySqlStatementEnum.DSSE_UPDATE);

        for (SQLUpdateSetItem sqlUpdateSetItem : sqlUpdateStatement.getItems()) {
            SQLExpr columnExpr = sqlUpdateSetItem.getColumn();
            if (columnExpr instanceof SQLPropertyExpr) {
                // update???set??????????????????????????????
                SQLPropertyExpr columnPropertyExpr = (SQLPropertyExpr) columnExpr;
                String tableAlias = columnPropertyExpr.getOwnerName();
                SQLTableSource setSqlTableSource = sqlTableSource.findTableSource(tableAlias);
                // ?????????????????????
                handleSQLTableSource(setSqlTableSource, updateUseAliasMySqlTableInfo, MySqlStatementEnum.DSSE_UPDATE);
            } else if (!(columnExpr instanceof SQLIdentifierExpr) &&
                    !(columnExpr instanceof SQLListExpr)) {
                /*
                    SQLIdentifierExpr   update???set??????????????????????????????
                    SQLListExpr         (col1, col2, col3) = (?, ?, ?)
                 */
                logger.error("???????????????SQLExpr?????? {}", columnExpr.getClass().getName());
            }
        }

        List<String> updateUseAliasTableList = updateUseAliasMySqlTableInfo.getUpdateTableList();
        if (updateUseAliasMySqlTableInfo.getUpdateTableList().isEmpty()) {
            // update?????????????????????update??????????????????????????????
            MySqlTableInfo.copyUpdateTableList(updateMySqlTableInfo, mySqlTableInfo);
        } else {
            // update???????????????
            for (String updateTableList : updateMySqlTableInfo.getUpdateTableList()) {
                if (updateUseAliasTableList.contains(updateTableList)) {
                    // ??????update????????????????????????????????????set??????????????????????????????????????????
                    mySqlTableInfo.addUpdateTable(updateTableList);
                    continue;
                }
                // ??????update????????????????????????????????????set?????????????????????????????????????????????
                mySqlTableInfo.addSelectTable(updateTableList);
            }
        }

        SQLExpr whereSQLExpr = sqlUpdateStatement.getWhere();
        if (whereSQLExpr != null) {
            // ??????where??????
            handleWhereSqlExpr(whereSQLExpr, mySqlTableInfo);
        }

        /*
            SQLUpdateStatement.getWhere()??????????????????from????????????setFrom()???????????????
            ???????????????????????????????????????MySql???????????????????????????sqlUpdateStatement.getFrom()
            OscarStatementParser.java
            PGSQLStatementParser.java
            SQLServerStatementParser.java
         */
    }

    // ??????delete??????
    private void parseDeleteStatement(SQLDeleteStatement sqlDeleteStatement, MySqlTableInfo mySqlTableInfo) {
        // ??????delete????????????????????????
        MySqlTableInfo deleteMySqlTableInfo = new MySqlTableInfo();
        // ??????from???????????????
        MySqlTableInfo fromMySqlTableInfo = new MySqlTableInfo();

        SQLTableSource sqlTableSource = sqlDeleteStatement.getTableSource();
        // ??????delete????????????????????????
        if (sqlTableSource instanceof SQLExprTableSource) {
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            SQLExpr tableSourceExpr = sqlExprTableSource.getExpr();
            if (tableSourceExpr instanceof SQLIdentifierExpr) {
                // ??????????????????delete??????"delete alias"??????
                SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) tableSourceExpr;
                String tableNameOrAlias = sqlIdentifierExpr.getName();
                // ?????????????????????
                recordTableName(tableNameOrAlias, deleteMySqlTableInfo, MySqlStatementEnum.DSSE_DELETE);
            } else if (tableSourceExpr instanceof SQLPropertyExpr) {
                // ??????????????????delete??????"delete alias.*"??????
                SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) tableSourceExpr;
                String tableNameOrAlias = sqlPropertyExpr.getOwnerName();
                // ?????????????????????
                recordTableName(tableNameOrAlias, deleteMySqlTableInfo, MySqlStatementEnum.DSSE_DELETE);
            } else if (!(tableSourceExpr instanceof SQLVariantRefExpr)) {
                /*
                    SQLVariantRefExpr       ?
                 */
                logger.error("???????????????SQLExpr?????? {}", tableSourceExpr.getClass().getName());
            }
        } else {
            /*
                SQLTableSource sqlTableSource?????????SQLJoinTableSource????????????MySQL?????????????????????
                delete from test_balance_log l join test_balance b on l.id=b.id where b.balance = '1.23'
             */
            logger.error("???????????????SQLTableSource?????? {}", sqlTableSource.getClass().getName());
        }

        SQLExpr whereSQLExpr = sqlDeleteStatement.getWhere();
        if (whereSQLExpr != null) {
            // ??????where??????????????????select???????????????
            handleWhereSqlExpr(whereSQLExpr, mySqlTableInfo);
        }

        SQLTableSource sqlTableSourceFrom = sqlDeleteStatement.getFrom();
        if (sqlTableSourceFrom == null) {
            // delete???from??????????????????????????????????????????
            MySqlTableInfo.copyDeleteTableList(deleteMySqlTableInfo, mySqlTableInfo);
            return;
        }

        // delete???from???????????????????????????????????????
        handleSQLTableSource(sqlTableSourceFrom, fromMySqlTableInfo, MySqlStatementEnum.DSSE_SELECT);

        for (String deleteTableNameOrAlias : deleteMySqlTableInfo.getDeleteTableList()) {
            SQLTableSource deleteSqlTableSource = sqlTableSourceFrom.findTableSource(deleteTableNameOrAlias);
            if (deleteSqlTableSource != null) {
                // ??????????????????????????????delete????????????
                handleSQLTableSource(deleteSqlTableSource, mySqlTableInfo, MySqlStatementEnum.DSSE_DELETE);
            }
        }

        // ??????from????????????????????????????????????delete??????????????????????????????select??????????????????
        for (String selectTableName : fromMySqlTableInfo.getSelectTableList()) {
            if (!mySqlTableInfo.getDeleteTableList().contains(selectTableName)) {
                mySqlTableInfo.addSelectTable(selectTableName);
            }
        }
    }

    // ??????alter table??????
    private void parseAlterStatement(SQLAlterTableStatement sqlAlterTableStatement, MySqlTableInfo mySqlTableInfo) {
        // ?????????????????????
        handleSQLTableSource(sqlAlterTableStatement.getTableSource(), mySqlTableInfo, MySqlStatementEnum.DSSE_ALTER);
    }

    // ??????truncate table??????
    private void parseTruncateStatement(SQLTruncateStatement sqlTruncateStatement, MySqlTableInfo mySqlTableInfo) {
        for (SQLExprTableSource sqlExprTableSource : sqlTruncateStatement.getTableSources()) {
            // ?????????????????????
            handleSQLTableSource(sqlExprTableSource, mySqlTableInfo, MySqlStatementEnum.DSSE_TRUNCATE);
        }
    }

    // ??????create table??????
    private void parseCreateStatement(MySqlCreateTableStatement mySqlCreateTableStatement, MySqlTableInfo mySqlTableInfo) {
        // ?????????????????????
        handleSQLTableSource(mySqlCreateTableStatement.getTableSource(), mySqlTableInfo, MySqlStatementEnum.DSSE_CREATE);
    }

    // ??????drop table??????
    private void parseDropStatement(SQLDropTableStatement sqlDropTableStatement, MySqlTableInfo mySqlTableInfo) {
        for (SQLExprTableSource sqlExprTableSource : sqlDropTableStatement.getTableSources()) {
            // ?????????????????????
            handleSQLTableSource(sqlExprTableSource, mySqlTableInfo, MySqlStatementEnum.DSSE_DROP);
        }
    }

    // ?????????????????????
    private void handleSQLTableSource(SQLTableSource sqlTableSource, MySqlTableInfo mySqlTableInfo, MySqlStatementEnum mySqlStatementEnum) {
        if (sqlTableSource == null) {
            return;
        }

        if (sqlTableSource instanceof SQLExprTableSource) {
            // ?????????????????????
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            String tableName = sqlExprTableSource.getTableName();
            // ????????????
            recordTableName(tableName, mySqlTableInfo, mySqlStatementEnum);
            return;
        }

        if (sqlTableSource instanceof SQLJoinTableSource) {
            // join???????????????
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource leftSQLTableSource = sqlJoinTableSource.getLeft();
            if (leftSQLTableSource != null) {
                // ?????????????????????
                handleSQLTableSource(leftSQLTableSource, mySqlTableInfo, mySqlStatementEnum);
            }
            SQLTableSource rightSQLTableSource = sqlJoinTableSource.getRight();
            if (rightSQLTableSource != null) {
                // ?????????????????????
                handleSQLTableSource(rightSQLTableSource, mySqlTableInfo, mySqlStatementEnum);
            }
            return;
        }

        if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // ????????????????????????
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) sqlTableSource;
            if (sqlSubqueryTableSource.getSelect() != null) {
                // ??????SQLSelectQuery??????
                handleSQLSelectQuery(sqlSubqueryTableSource.getSelect().getQuery(), mySqlTableInfo);
            }
            return;
        }

        if (sqlTableSource instanceof SQLUnionQueryTableSource) {
            // ????????????union?????????
            SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) sqlTableSource;
            if (sqlUnionQueryTableSource.getUnion() != null && sqlUnionQueryTableSource.getUnion().getChildren() != null) {
                for (SQLSelectQuery sqlSelectQuery : sqlUnionQueryTableSource.getUnion().getChildren()) {
                    // ??????SQLSelectQuery??????
                    handleSQLSelectQuery(sqlSelectQuery, mySqlTableInfo);
                }
            }
            return;
        }

        logger.error("???????????????SQLTableSource?????? {}", sqlTableSource.getClass().getName());
    }

    // ????????????
    private void recordTableName(String tableName, MySqlTableInfo mySqlTableInfo, MySqlStatementEnum mySqlStatementEnum) {
        switch (mySqlStatementEnum) {
            case DSSE_SELECT:
                mySqlTableInfo.addSelectTable(tableName);
                break;
            case DSSE_SELECT_4_UPDATE:
                mySqlTableInfo.addSelect4UpdateTable(tableName);
                break;
            case DSSE_INSERT:
                mySqlTableInfo.addInsertTable(tableName);
                break;
            case DSSE_INSERT_IGNORE:
                mySqlTableInfo.addInsertIgnoreTable(tableName);
                break;
            case DSSE_INSERT_OR_UPDATE:
                mySqlTableInfo.addInsertOrUpdateTable(tableName);
                break;
            case DSSE_REPLACE:
                mySqlTableInfo.addReplaceIntoTable(tableName);
                break;
            case DSSE_UPDATE:
                mySqlTableInfo.addUpdateTable(tableName);
                break;
            case DSSE_DELETE:
                mySqlTableInfo.addDeleteTable(tableName);
                break;
            case DSSE_ALTER:
                mySqlTableInfo.addAlterTable(tableName);
                break;
            case DSSE_TRUNCATE:
                mySqlTableInfo.addTruncateTable(tableName);
                break;
            case DSSE_CREATE:
                mySqlTableInfo.addCreateTable(tableName);
                break;
            case DSSE_DROP:
                mySqlTableInfo.addDropTable(tableName);
                break;
            default:
                logger.error("??????????????? {}", mySqlStatementEnum);
                break;
        }
    }
}
