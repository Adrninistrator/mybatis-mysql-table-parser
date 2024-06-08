package com.adrninistrator.mybatismysqltableparser.parser;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatismysqltableparser.common.enums.MySqlStatementEnum;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlSelectColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlSetColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlTableColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.ParameterNameAndType;
import com.adrninistrator.mybatismysqltableparser.dto.TableAndColumnName;
import com.adrninistrator.mybatismysqltableparser.util.MyBatisTableParserUtil;
import com.adrninistrator.mybatismysqltableparser.visitor.SQLBinaryOpExprVisitor;
import com.adrninistrator.mybatismysqltableparser.visitor.SQLExprTableSourceMultiVisitor;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
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
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
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
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlOptimizeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfilesStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.ParserException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author adrninistrator
 * @date 2022/12/20
 * @description: 解析sql语句中使用的表名与字段
 */
public class MySqlTableColumnParser {
    private static final Logger logger = LoggerFactory.getLogger(MySqlTableColumnParser.class);

    /**
     * 解析sql语句中使用的表名
     *
     * @param xmlFilePath    XML文件路径
     * @param xmlElementName XML中元素的名称
     * @param sqlId          sql语句的ID
     * @param fullSql        完整的sql语句
     * @return
     */
    public MySqlTableColumnInfo parseTablesInSql(String xmlFilePath, String xmlElementName, String sqlId, String fullSql) {
        String xmlFileName = MyBatisTableParserUtil.getFileNameFromPath(xmlFilePath);
        MyBatisTableParserUtil.recordCurrentXmlFileName(xmlFileName);
        MyBatisTableParserUtil.recordCurrentSqlID(sqlId);
        MyBatisTableParserUtil.recordCurrentSql(fullSql);
        MySqlTableColumnInfo mySqlTableColumnInfo = new MySqlTableColumnInfo(xmlElementName);
        try {
            MySqlStatementParser parser = new MySqlStatementParser(fullSql);
            SQLStatement sqlStatement = parser.parseStatement();

            if (sqlStatement instanceof SQLSelectStatement) {
                // 解析select语句
                parseSelectStatement((SQLSelectStatement) sqlStatement, mySqlTableColumnInfo);
                // 添加其他select的表名
                addOtherSelectTable(sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof MySqlInsertStatement) {
                // 解析insert into语句
                parseInsertStatement((MySqlInsertStatement) sqlStatement, mySqlTableColumnInfo);
                // 添加其他select的表名
                addOtherSelectTable(sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof SQLReplaceStatement) {
                // 解析replace into语句
                parseReplaceStatement((SQLReplaceStatement) sqlStatement, mySqlTableColumnInfo);
                // 添加其他select的表名
                addOtherSelectTable(sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof SQLUpdateStatement) {
                // 解析update语句
                parseUpdateStatement((SQLUpdateStatement) sqlStatement, mySqlTableColumnInfo);
                // 添加其他select的表名
                addOtherSelectTable(sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof SQLDeleteStatement) {
                SQLDeleteStatement sqlDeleteStatement = (SQLDeleteStatement) sqlStatement;
                // 解析delete语句
                parseDeleteStatement(sqlDeleteStatement, mySqlTableColumnInfo);
                // 添加其他select的表名
                addOtherSelectTable(sqlDeleteStatement, mySqlTableColumnInfo);
                // SQLDeleteStatement的accept0方法中未对from进行accept，这里单独执行一次，添加from中其他select的表名
                SQLTableSource deleteFromSQLTableSource = sqlDeleteStatement.getFrom();
                if (deleteFromSQLTableSource != null) {
                    addOtherSelectTable(deleteFromSQLTableSource, mySqlTableColumnInfo);
                }
            } else if (sqlStatement instanceof SQLAlterTableStatement) {
                // 解析alter table语句
                parseAlterStatement((SQLAlterTableStatement) sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof SQLTruncateStatement) {
                // 解析truncate table语句
                parseTruncateStatement((SQLTruncateStatement) sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof MySqlCreateTableStatement) {
                // 解析create table语句
                parseCreateStatement((MySqlCreateTableStatement) sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof SQLDropTableStatement) {
                // 解析drop table语句
                parseDropStatement((SQLDropTableStatement) sqlStatement, mySqlTableColumnInfo);
            } else if (sqlStatement instanceof MySqlOptimizeStatement) {
                // 解析optimize table语句
                parseOptimizeStatement((MySqlOptimizeStatement) sqlStatement, mySqlTableColumnInfo);
            } else if (!(sqlStatement instanceof SQLSetStatement) &&
                    !(sqlStatement instanceof MySqlSetTransactionStatement) &&
                    !(sqlStatement instanceof SQLCallStatement) &&
                    !(sqlStatement instanceof SQLMergeStatement) &&
                    !(sqlStatement instanceof SQLBlockStatement) &&
                    !(sqlStatement instanceof MySqlDeclareStatement) &&
                    !(sqlStatement instanceof SQLShowTablesStatement) &&
                    !(sqlStatement instanceof MySqlShowProfilesStatement)
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
                logger.error("暂未处理的SQLStatement类型 {}  [{}] [{}] [{}] [{}]", sqlStatement.getClass().getName(), sqlStatement, MyBatisTableParserUtil.getCurrentXmlFileName(),
                        MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
            }
        } catch (ParserException e) {
            logger.error("解析sql语句出现异常 [{}] [{}] [{}] {}", xmlFilePath, sqlId, fullSql, e.getMessage());
            mySqlTableColumnInfo.setParseFail(true);
        } catch (Exception e) {
            logger.error("解析sql语句出现异常2 [{}] [{}] [{}] ", xmlFilePath, sqlId, fullSql, e);
            mySqlTableColumnInfo.setParseFail(true);
        } finally {
            MyBatisTableParserUtil.clearThreadLocal();
        }
        return mySqlTableColumnInfo;
    }

    // 解析select语句
    private void parseSelectStatement(SQLSelectStatement sqlSelectStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        SQLSelect sqlSelect = sqlSelectStatement.getSelect();
        if (sqlSelect.getQuery() != null) {
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(sqlSelect.getQuery(), mySqlTableColumnInfo);
        }
    }

    // 处理SQLSelectQuery对象
    public void handleSQLSelectQuery(SQLSelectQuery sqlSelectQuery, MySqlTableColumnInfo mySqlTableColumnInfo) {
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            MySqlSelectQueryBlock mySqlSelectQueryBlock = null;
            if (sqlSelectQuery instanceof MySqlSelectQueryBlock) {
                mySqlSelectQueryBlock = (MySqlSelectQueryBlock) sqlSelectQuery;
            }
            // 一般的表名形式
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            // 处理select的字段
            handleSelectColumn(sqlSelectQueryBlock, mySqlTableColumnInfo);

            SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
            if (sqlTableSource != null) {
                // 判断是select还是select for update
                MySqlStatementEnum mySqlStatementEnum = MySqlStatementEnum.DSSE_SELECT;
                if (mySqlSelectQueryBlock != null && mySqlSelectQueryBlock.isForUpdate()) {
                    mySqlStatementEnum = MySqlStatementEnum.DSSE_SELECT_4_UPDATE;
                }

                // 处理涉及的表名
                handleSQLTableSource(sqlTableSource, mySqlTableColumnInfo, mySqlStatementEnum);
            }

            SQLExpr whereSQLExpr = sqlSelectQueryBlock.getWhere();
            if (whereSQLExpr != null) {
                // 处理where语句
                handleWhereSqlExpr(whereSQLExpr, mySqlTableColumnInfo);
            }
            return;
        }

        if (sqlSelectQuery instanceof SQLUnionQuery) {
            // union查询的形式
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            for (SQLSelectQuery childSqlSelectQuery : sqlUnionQuery.getChildren()) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(childSqlSelectQuery, mySqlTableColumnInfo);
            }
            return;
        }

        if (!(sqlSelectQuery instanceof SQLValuesTableSource)) {
            /*
                SQLValuesTableSource    values next value for xxx
             */
            logger.error("暂未处理的SQLSelectQuery类型 {} [{}] [{}] [{}] [{}]", sqlSelectQuery.getClass().getName(), sqlSelectQuery, MyBatisTableParserUtil.getCurrentXmlFileName(),
                    MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
        }
    }

    // 处理select的字段
    private void handleSelectColumn(SQLSelectQueryBlock sqlSelectQueryBlock, MySqlTableColumnInfo mySqlTableColumnInfo) {
        for (SQLSelectItem sqlSelectItem : sqlSelectQueryBlock.getSelectList()) {
            String dbColumnAlias = StringUtils.defaultString(sqlSelectItem.getAlias(), "");
            SQLExpr sqlExpr = sqlSelectItem.getExpr();
            if (sqlExpr instanceof SQLIdentifierExpr || sqlExpr instanceof SQLPropertyExpr) {
                // 处理select对应的表字段
                handleSelectTableColumnExpr(sqlExpr, sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
                continue;
            }

            if (sqlExpr instanceof SQLAllColumnExpr) {
                // 记录select * 对应的select的字段
                recordSelectAllSelectColumn(sqlSelectQueryBlock.getFrom(), mySqlTableColumnInfo);
                continue;
            }

            if (sqlExpr instanceof SQLQueryExpr) {
                // select的某个字段是整个查询
                SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) sqlExpr;
                MySqlTableColumnInfo sqlQueryMySqlTableColumnInfo = new MySqlTableColumnInfo();
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlQueryExpr.getSubQuery().getQuery(), sqlQueryMySqlTableColumnInfo);
                // 拷贝查询语句相关的表名列表
                mySqlTableColumnInfo.copySelectTableList(sqlQueryMySqlTableColumnInfo);
                // 拷贝where字段列表
                mySqlTableColumnInfo.copyWhereColumnList(sqlQueryMySqlTableColumnInfo);
                // 拷贝查询字段列表，使用当前查询的字段的别名
                mySqlTableColumnInfo.copySelectColumnListWithAlias(sqlQueryMySqlTableColumnInfo, dbColumnAlias);
                continue;
            }

            if (sqlExpr instanceof SQLCaseExpr) {
                // 查询的内容是CASE语句
                SQLCaseExpr sqlCaseExpr = (SQLCaseExpr) sqlExpr;
                // 处理WHEN...THEN...语句
                for (SQLCaseExpr.Item item : sqlCaseExpr.getItems()) {
                    // 处理select对应的表字段
                    handleSelectTableColumnExpr(item.getValueExpr(), sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
                }
                // 处理ELSE语句
                SQLExpr caseElseExpr = sqlCaseExpr.getElseExpr();
                if (caseElseExpr != null) {
                    // 处理select对应的表字段
                    handleSelectTableColumnExpr(caseElseExpr, sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
                }
                continue;
            }

            if (sqlExpr instanceof SQLCastExpr) {
                SQLExpr sqlExpr2 = ((SQLCastExpr) sqlExpr).getExpr();
                handleSelectTableColumnExpr(sqlExpr2, sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
                continue;
            }

            if (sqlExpr instanceof SQLExistsExpr) {
                SQLExistsExpr sqlExistsExpr = (SQLExistsExpr) sqlExpr;
                MySqlTableColumnInfo sqlQueryMySqlTableColumnInfo = new MySqlTableColumnInfo();
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlExistsExpr.getSubQuery().getQuery(), sqlQueryMySqlTableColumnInfo);
                // 拷贝查询语句相关的表名列表
                mySqlTableColumnInfo.copySelectTableList(sqlQueryMySqlTableColumnInfo);
                // 拷贝where字段列表
                mySqlTableColumnInfo.copyWhereColumnList(sqlQueryMySqlTableColumnInfo);
                continue;
            }

            if (!MyBatisTableParserUtil.checkIgnoreSelectColumnExprType(sqlExpr)) {
                logger.error("暂未处理的SQLExpr类型 {} [{}] [{}] [{}] [{}]", sqlExpr.getClass().getName(), sqlExpr, MyBatisTableParserUtil.getCurrentXmlFileName(),
                        MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
            }
        }
    }

    // 处理select对应的表字段
    private void handleSelectTableColumnExpr(SQLExpr sqlExpr, SQLSelectQueryBlock sqlSelectQueryBlock, String dbColumnAlias, MySqlTableColumnInfo mySqlTableColumnInfo) {
        if (sqlExpr instanceof SQLIdentifierExpr) {
            // select 字段的形式
            SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) sqlExpr;
            String dbColumnName = sqlIdentifierExpr.getName();
            String dbTableName = MyBatisTableParserUtil.getTableNameFromTableSource(sqlSelectQueryBlock.getFrom(), "");
            MySqlSelectColumnInfo mySqlSelectColumnInfo = new MySqlSelectColumnInfo(dbTableName, dbColumnName, dbColumnAlias);
            mySqlTableColumnInfo.addMySqlSelectColumnInfo(mySqlSelectColumnInfo);
            return;
        }

        if (sqlExpr instanceof SQLPropertyExpr) {
            // select 别名.字段的形式
            SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
            String dbColumnName = sqlPropertyExpr.getName();
            if (MyBatisTableParserConstants.FLAG_ALL.equals(dbColumnName)) {
                // 记录select * 对应的select的字段
                recordSelectAllSelectColumn(sqlSelectQueryBlock.getFrom(), mySqlTableColumnInfo);
                return;
            }
            String dbTableAlias = sqlPropertyExpr.getOwnerName();
            String dbTableName = MyBatisTableParserUtil.getTableNameFromTableSource(sqlSelectQueryBlock.getFrom(), dbTableAlias);
            MySqlSelectColumnInfo mySqlSelectColumnInfo = new MySqlSelectColumnInfo(dbTableName, dbColumnName, dbColumnAlias);
            mySqlTableColumnInfo.addMySqlSelectColumnInfo(mySqlSelectColumnInfo);
            return;
        }

        if (sqlExpr instanceof SQLQueryExpr) {
            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) sqlExpr;
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(sqlQueryExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
            return;
        }

        if (sqlExpr instanceof SQLCaseExpr) {
            SQLCaseExpr sqlCaseExpr = (SQLCaseExpr) sqlExpr;
            // 处理WHEN...THEN...语句
            for (SQLCaseExpr.Item item : sqlCaseExpr.getItems()) {
                // 处理select对应的表字段
                handleSelectTableColumnExpr(item.getValueExpr(), sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
            }
            // 处理ELSE语句
            SQLExpr caseElseExpr = sqlCaseExpr.getElseExpr();
            if (caseElseExpr != null) {
                // 处理select对应的表字段
                handleSelectTableColumnExpr(caseElseExpr, sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
            }
            return;
        }

        if (sqlExpr instanceof SQLCastExpr) {
            SQLExpr sqlExpr2 = ((SQLCastExpr) sqlExpr).getExpr();
            handleSelectTableColumnExpr(sqlExpr2, sqlSelectQueryBlock, dbColumnAlias, mySqlTableColumnInfo);
            return;
        }

        if (!MyBatisTableParserUtil.checkIgnoreSelectColumnExprType(sqlExpr)) {
            logger.error("暂未处理的SQLExpr类型 {} [{}] [{}] [{}] [{}]", sqlExpr.getClass().getName(), sqlExpr, MyBatisTableParserUtil.getCurrentXmlFileName(),
                    MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
        }
    }

    // 记录select * 对应的select的字段
    private void recordSelectAllSelectColumn(SQLTableSource fromAllTableSource, MySqlTableColumnInfo mySqlTableColumnInfo) {
        MySqlTableColumnInfo selectAllMySqlTableColumnInfo = new MySqlTableColumnInfo();
        // 处理select * 的SQLTableSource
        handleSelectAllTableSource(fromAllTableSource, selectAllMySqlTableColumnInfo, null);

//        StringBuilder selectAllColumnInfo = new StringBuilder();
        for (MySqlSelectColumnInfo mySqlSelectColumnInfo : selectAllMySqlTableColumnInfo.getMySqlSelectColumnInfoList()) {
            // 当预期的表别名为空，或实际表别名为空，或与实际表别名一致时添加
            if (mySqlTableColumnInfo.addMySqlSelectColumnInfo(mySqlSelectColumnInfo)) {
//                selectAllColumnInfo.append(mySqlSelectColumnInfo).append("\t");
            }
        }
//        if (selectAllColumnInfo.length() > 0) {
//            logger.info("select * 语句select的字段信息 [{}] [{}] [{}] [{}]", MyBatisTableParserUtil.getCurrentXmlFileName(), MyBatisTableParserUtil.getCurrentSqlID(),
//                    MyBatisTableParserUtil.getCurrentSql(), selectAllColumnInfo);
//        }
    }

    // 处理select * 的SQLTableSource
    private void handleSelectAllTableSource(SQLTableSource fromAllTableSource, MySqlTableColumnInfo mySqlTableColumnInfo, Set<String> expectedTableAliasSet) {
//        logger.info("处理select * select的字段 [{}] [{}] {}", MyBatisTableParserUtil.getCurrentXmlFileName(), MyBatisTableParserUtil.getCurrentSqlID(),
//                MyBatisTableParserUtil.getCurrentSql());

        if (fromAllTableSource instanceof SQLSubqueryTableSource) {
            // 子查询
            SQLSubqueryTableSource fromAllSubQuery = (SQLSubqueryTableSource) fromAllTableSource;
            SQLSelectQuery fromAllSubQuerySQLSelectQuery = fromAllSubQuery.getSelect().getQuery();
            // 处理子查询
            handleSQLSelectQuery(fromAllSubQuerySQLSelectQuery, mySqlTableColumnInfo);
        } else if (fromAllTableSource instanceof SQLJoinTableSource) {
            // join查询
            SQLJoinTableSource fromAllSQLJoinTableSource = (SQLJoinTableSource) fromAllTableSource;
            // 递归处理
            Set<String> usedSelectTableAliasSet = new HashSet<>();
            // 获取父节点，若是查询语句，则获取获取查询语句中查询的表名列表
            SQLObject parent = fromAllTableSource.getParent();
            if (parent instanceof MySqlSelectQueryBlock) {
                // 获取查询语句中select *的表名Set
                Set<String> selectTableAliasSet = MyBatisTableParserUtil.getSelectAllTableAliasSet((MySqlSelectQueryBlock) parent);
                usedSelectTableAliasSet.addAll(selectTableAliasSet);
            }

            handleSelectAllTableSource(fromAllSQLJoinTableSource.getLeft(), mySqlTableColumnInfo, usedSelectTableAliasSet);
            handleSelectAllTableSource(fromAllSQLJoinTableSource.getRight(), mySqlTableColumnInfo, usedSelectTableAliasSet);
        } else if (fromAllTableSource instanceof SQLUnionQueryTableSource) {
            // union查询
            SQLUnionQueryTableSource fromAllSQLUnionQueryTableSource = (SQLUnionQueryTableSource) fromAllTableSource;
            SQLUnionQuery sqlUnionQuery = fromAllSQLUnionQueryTableSource.getUnion();

            // 处理union查询
            handleSQLSelectQuery(sqlUnionQuery, mySqlTableColumnInfo);
        } else if (fromAllTableSource instanceof SQLExprTableSource) {
            // select *对应的表来源，记录对应的表名
            SQLExprTableSource fromAllSQLExprTableSource = (SQLExprTableSource) fromAllTableSource;
            if (expectedTableAliasSet == null || expectedTableAliasSet.isEmpty()
                    || expectedTableAliasSet.contains(fromAllSQLExprTableSource.getAlias())) {
                // 若预期的表别名为空，或非空且与当前表别名相同时，记录select的字段信息
                String dbTableName = MyBatisTableParserUtil.getTableNameFromTableSource(fromAllSQLExprTableSource, "");
                MySqlSelectColumnInfo mySqlSelectColumnInfo = new MySqlSelectColumnInfo(dbTableName, MyBatisTableParserConstants.FLAG_ALL, "");
                mySqlTableColumnInfo.addMySqlSelectColumnInfo(mySqlSelectColumnInfo);
            } else {
                List<String> expectedTableAliasList = new ArrayList<>(expectedTableAliasSet);
                Collections.sort(expectedTableAliasList);
                logger.info("select * 预期的表别名与实际的不相同，不记录 [{}] [{}] [{}] [{}] [{}] ", StringUtils.join(expectedTableAliasList, " "), fromAllSQLExprTableSource.getAlias(),
                        MyBatisTableParserUtil.getCurrentXmlFileName(), MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
            }
        } else {
            logger.error("暂未处理的SQLTableSource类型 {} [{}] [{}] [{}] [{}]", fromAllTableSource.getClass().getName(), fromAllTableSource,
                    MyBatisTableParserUtil.getCurrentXmlFileName(), MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
        }
    }

    // 处理where语句
    private void handleWhereSqlExpr(SQLExpr whereSqlExpr, MySqlTableColumnInfo mySqlTableColumnInfo) {
        if (whereSqlExpr == null) {
            return;
        }

        if (whereSqlExpr instanceof SQLBinaryOpExpr) {
            // where语句中的判断条件
            SQLBinaryOpExprVisitor sqlBinaryOpExprVisitor = new SQLBinaryOpExprVisitor(mySqlTableColumnInfo);
            // 处理字段关系
            whereSqlExpr.accept(sqlBinaryOpExprVisitor);
            return;
        }

        if (whereSqlExpr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) whereSqlExpr;
            if (sqlInSubQueryExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlInSubQueryExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLExistsExpr) {
            SQLExistsExpr sqlExistsExpr = (SQLExistsExpr) whereSqlExpr;
            if (sqlExistsExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlExistsExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLAllExpr) {
            SQLAllExpr sqlAllExpr = (SQLAllExpr) whereSqlExpr;
            if (sqlAllExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlAllExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLAnyExpr) {
            SQLAnyExpr sqlAnyExpr = (SQLAnyExpr) whereSqlExpr;
            if (sqlAnyExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlAnyExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
            }
            return;
        }

        if (whereSqlExpr instanceof SQLSomeExpr) {
            SQLSomeExpr sqlSomeExpr = (SQLSomeExpr) whereSqlExpr;
            if (sqlSomeExpr.getSubQuery() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlSomeExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
            }
            return;
        }

        if (!(whereSqlExpr instanceof SQLInListExpr) &&
                !(whereSqlExpr instanceof SQLBetweenExpr) &&
                !(whereSqlExpr instanceof SQLIdentifierExpr) &&
                !(whereSqlExpr instanceof SQLMethodInvokeExpr) &&
                !(whereSqlExpr instanceof SQLVariantRefExpr)
        ) {
            /*
                SQLInListExpr           in ...
                SQLBetweenExpr          between...and
                SQLIdentifierExpr       sql语句解析有问题，例如<where>标签中通过include指定了空的<sql>内容
                SQLMethodInvokeExpr     方法调用
                SQLVariantRefExpr       ?
             */
            logger.error("暂未处理的SQLExpr类型 {} [{}] [{}] [{}] [{}]", whereSqlExpr.getClass().getName(), whereSqlExpr, MyBatisTableParserUtil.getCurrentXmlFileName(),
                    MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
        }
    }

    // 解析insert语句
    private void parseInsertStatement(MySqlInsertStatement mySqlInsertStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        MySqlStatementEnum mySqlStatementEnum = MySqlStatementEnum.DSSE_INSERT;
        if (mySqlInsertStatement.isIgnore()) {
            mySqlStatementEnum = MySqlStatementEnum.DSSE_INSERT_IGNORE;
        }
        if (mySqlInsertStatement.getDuplicateKeyUpdate() != null && !mySqlInsertStatement.getDuplicateKeyUpdate().isEmpty()) {
            mySqlStatementEnum = MySqlStatementEnum.DSSE_INSERT_OR_UPDATE;
        }

        SQLExprTableSource sqlExprTableSource = mySqlInsertStatement.getTableSource();
        String tableName = MyBatisTableParserUtil.getTableNameFromTableSource(sqlExprTableSource);
        // 记录表名
        recordTableName(tableName, mySqlTableColumnInfo, mySqlStatementEnum);

        if (mySqlInsertStatement.getQuery() != null) {
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(mySqlInsertStatement.getQuery().getQuery(), mySqlTableColumnInfo);
        }
    }

    // 解析replace语句
    private void parseReplaceStatement(SQLReplaceStatement sqlReplaceStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        SQLExprTableSource sqlExprTableSource = sqlReplaceStatement.getTableSource();
        String tableName = MyBatisTableParserUtil.getTableNameFromTableSource(sqlExprTableSource);

        // 记录表名
        recordTableName(tableName, mySqlTableColumnInfo, MySqlStatementEnum.DSSE_REPLACE);

        SQLQueryExpr sqlQueryExpr = sqlReplaceStatement.getQuery();
        if (sqlQueryExpr != null && sqlQueryExpr.getSubQuery() != null) {
            // 处理SQLSelectQuery对象
            handleSQLSelectQuery(sqlQueryExpr.getSubQuery().getQuery(), mySqlTableColumnInfo);
        }
    }

    // 解析update语句
    private void parseUpdateStatement(SQLUpdateStatement sqlUpdateStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        // 记录update相关的表名
        MySqlTableColumnInfo updateMySqlTableColumnInfo = new MySqlTableColumnInfo();
        // 记录update时使用别名进行了set的相关表名
        MySqlTableColumnInfo updateUseAliasMySqlTableColumnInfo = new MySqlTableColumnInfo();

        SQLTableSource sqlTableSource = sqlUpdateStatement.getTableSource();
        // 处理涉及的表名，update相关
        handleSQLTableSource(sqlTableSource, updateMySqlTableColumnInfo, MySqlStatementEnum.DSSE_UPDATE);

        for (SQLUpdateSetItem sqlUpdateSetItem : sqlUpdateStatement.getItems()) {
            SQLExpr updateSetValueExpr = sqlUpdateSetItem.getValue();
            if (updateSetValueExpr instanceof SQLVariantRefExpr) {
                // 处理update语句set中的字段赋值
                handleUpdateSetColumn(sqlUpdateSetItem.getColumn(), (SQLVariantRefExpr) updateSetValueExpr, mySqlTableColumnInfo, sqlTableSource);
            }

            SQLExpr updateSetColumnExpr = sqlUpdateSetItem.getColumn();
            if (updateSetColumnExpr instanceof SQLPropertyExpr) {
                // update的set字段有使用别名的情况
                SQLPropertyExpr columnPropertyExpr = (SQLPropertyExpr) updateSetColumnExpr;
                String tableAlias = columnPropertyExpr.getOwnerName();
                SQLTableSource setSqlTableSource = sqlTableSource.findTableSource(tableAlias);
                // 处理涉及的表名
                handleSQLTableSource(setSqlTableSource, updateUseAliasMySqlTableColumnInfo, MySqlStatementEnum.DSSE_UPDATE);
            } else if (!(updateSetColumnExpr instanceof SQLIdentifierExpr) &&
                    !(updateSetColumnExpr instanceof SQLListExpr)) {
                /*
                    SQLIdentifierExpr   update的set字段未使用别名的情况
                    SQLListExpr         (col1, col2, col3) = (?, ?, ?)
                 */
                logger.error("暂未处理的SQLExpr类型 {} [{}] [{}] [{}] [{}]", updateSetColumnExpr.getClass().getName(), updateSetColumnExpr, MyBatisTableParserUtil.getCurrentXmlFileName(),
                        MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
            }
        }

        List<String> updateUseAliasTableList = updateUseAliasMySqlTableColumnInfo.getUpdateTableList();
        if (updateUseAliasMySqlTableColumnInfo.getUpdateTableList().isEmpty()) {
            // update未使用别名，将update相关表名添加到结果中
            mySqlTableColumnInfo.copyUpdateTableList(updateMySqlTableColumnInfo);
        } else {
            // update有使用别名
            for (String updateTableList : updateMySqlTableColumnInfo.getUpdateTableList()) {
                if (updateUseAliasTableList.contains(updateTableList)) {
                    // 当前update相关的表名有通过别名进行set，将被更新的表名添加到结果中
                    mySqlTableColumnInfo.addUpdateTable(updateTableList);
                    continue;
                }
                // 当前update相关的表名未通过别名进行set，将查询使用的表名添加到结果中
                mySqlTableColumnInfo.addSelectTable(updateTableList);
            }
        }

        SQLExpr whereSQLExpr = sqlUpdateStatement.getWhere();
        if (whereSQLExpr != null) {
            // 处理where语句
            handleWhereSqlExpr(whereSQLExpr, mySqlTableColumnInfo);
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
    private void parseDeleteStatement(SQLDeleteStatement sqlDeleteStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        SQLExpr whereSQLExpr = sqlDeleteStatement.getWhere();
        if (whereSQLExpr != null) {
            // 处理where语句，添加表select相关的表名
            handleWhereSqlExpr(whereSQLExpr, mySqlTableColumnInfo);
        }

        SQLTableSource sqlTableSource = sqlDeleteStatement.getTableSource();
        SQLTableSource fromSQLTableSource = sqlDeleteStatement.getFrom();
        // 处理delete涉及的表名或别名
        if (sqlTableSource instanceof SQLExprTableSource) {
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            SQLExpr tableSourceExpr = sqlExprTableSource.getExpr();

            if (tableSourceExpr instanceof SQLIdentifierExpr) {
                // 使用别名，且delete时为"delete alias"形式
                SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) tableSourceExpr;

                // 记录delete的表名
                recordDeleteTableName(sqlIdentifierExpr.getName(), fromSQLTableSource, mySqlTableColumnInfo);
            } else if (tableSourceExpr instanceof SQLPropertyExpr) {
                // 使用别名，且delete时为"delete alias.*"形式
                SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) tableSourceExpr;
                // 记录delete的表名
                recordDeleteTableName(sqlPropertyExpr.getOwnerName(), fromSQLTableSource, mySqlTableColumnInfo);
            } else if (tableSourceExpr instanceof SQLVariantRefExpr) {
                // 变量形式
                SQLVariantRefExpr sqlVariantRefExpr = (SQLVariantRefExpr) tableSourceExpr;
                recordTableName(sqlVariantRefExpr.getName(), mySqlTableColumnInfo, MySqlStatementEnum.DSSE_DELETE);
            } else {
                logger.error("暂未处理的SQLExpr类型 {} [{}] [{}] [{}] [{}]", tableSourceExpr.getClass().getName(), tableSourceExpr, MyBatisTableParserUtil.getCurrentXmlFileName(),
                        MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
            }
            return;
        }
        if (!(sqlTableSource instanceof SQLJoinTableSource)) {
            /*
                SQLTableSource sqlTableSource可能为SQLJoinTableSource类型，但MySQL不支持这种写法
                    delete from test_balance_log l join test_balance b on l.id=b.id where b.balance = '1.23'
             */
            logger.error("暂未处理的SQLTableSource类型 {} [{}] [{}] [{}] [{}]", sqlTableSource.getClass().getName(), sqlTableSource, MyBatisTableParserUtil.getCurrentXmlFileName(),
                    MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
        }
    }

    // 记录delete的表名
    private void recordDeleteTableName(String tableNameOrAlias, SQLTableSource fromSQLTableSource, MySqlTableColumnInfo mySqlTableColumnInfo) {
        if (fromSQLTableSource != null) {
            // 示例：delete t from test_balance t
            String tableName = MyBatisTableParserUtil.getTableNameFromTableSource(fromSQLTableSource, tableNameOrAlias);
            if (!tableNameOrAlias.equals(tableName)) {
                // 记录delete使用的别名
                mySqlTableColumnInfo.addDeleteTableAliasSet(tableNameOrAlias);
                tableNameOrAlias = tableName;
            }
        }
        // 记录表名
        recordTableName(tableNameOrAlias, mySqlTableColumnInfo, MySqlStatementEnum.DSSE_DELETE);
    }

    // 解析alter table语句
    private void parseAlterStatement(SQLAlterTableStatement sqlAlterTableStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        // 处理涉及的表名
        handleSQLTableSource(sqlAlterTableStatement.getTableSource(), mySqlTableColumnInfo, MySqlStatementEnum.DSSE_ALTER);
    }

    // 解析truncate table语句
    private void parseTruncateStatement(SQLTruncateStatement sqlTruncateStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        for (SQLExprTableSource sqlExprTableSource : sqlTruncateStatement.getTableSources()) {
            // 处理涉及的表名
            handleSQLTableSource(sqlExprTableSource, mySqlTableColumnInfo, MySqlStatementEnum.DSSE_TRUNCATE);
        }
    }

    // 解析create table语句
    private void parseCreateStatement(MySqlCreateTableStatement mySqlCreateTableStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        // 处理涉及的表名
        handleSQLTableSource(mySqlCreateTableStatement.getTableSource(), mySqlTableColumnInfo, MySqlStatementEnum.DSSE_CREATE);
    }

    // 解析drop table语句
    private void parseDropStatement(SQLDropTableStatement sqlDropTableStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        for (SQLExprTableSource sqlExprTableSource : sqlDropTableStatement.getTableSources()) {
            // 处理涉及的表名
            handleSQLTableSource(sqlExprTableSource, mySqlTableColumnInfo, MySqlStatementEnum.DSSE_DROP);
        }
    }

    // 解析optimize table语句
    private void parseOptimizeStatement(MySqlOptimizeStatement mySqlOptimizeStatement, MySqlTableColumnInfo mySqlTableColumnInfo) {
        for (SQLExprTableSource sqlExprTableSource : mySqlOptimizeStatement.getTableSources()) {
            // 处理涉及的表名
            handleSQLTableSource(sqlExprTableSource, mySqlTableColumnInfo, MySqlStatementEnum.DSSE_OPTIMIZE);
        }
    }

    // 处理涉及的表名
    private void handleSQLTableSource(SQLTableSource sqlTableSource, MySqlTableColumnInfo mySqlTableColumnInfo, MySqlStatementEnum mySqlStatementEnum) {
        if (sqlTableSource == null) {
            return;
        }

        if (sqlTableSource instanceof SQLExprTableSource) {
            // 一般的表名形式
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            String tableName = MyBatisTableParserUtil.getTableNameFromTableSource(sqlExprTableSource);
            // 记录表名
            recordTableName(tableName, mySqlTableColumnInfo, mySqlStatementEnum);
            return;
        }

        if (sqlTableSource instanceof SQLJoinTableSource) {
            // join查询的形式
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource leftSQLTableSource = sqlJoinTableSource.getLeft();
            if (leftSQLTableSource != null) {
                // 处理涉及的表名
                handleSQLTableSource(leftSQLTableSource, mySqlTableColumnInfo, mySqlStatementEnum);
            }
            SQLTableSource rightSQLTableSource = sqlJoinTableSource.getRight();
            if (rightSQLTableSource != null) {
                // 处理涉及的表名
                handleSQLTableSource(rightSQLTableSource, mySqlTableColumnInfo, mySqlStatementEnum);
            }
            return;
        }

        if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // 使用子查询的形式
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) sqlTableSource;
            if (sqlSubqueryTableSource.getSelect() != null) {
                // 处理SQLSelectQuery对象
                handleSQLSelectQuery(sqlSubqueryTableSource.getSelect().getQuery(), mySqlTableColumnInfo);
            }
            return;
        }

        if (sqlTableSource instanceof SQLUnionQueryTableSource) {
            // 子查询中union的形式
            SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) sqlTableSource;
            if (sqlUnionQueryTableSource.getUnion() != null && sqlUnionQueryTableSource.getUnion().getChildren() != null) {
                for (SQLSelectQuery sqlSelectQuery : sqlUnionQueryTableSource.getUnion().getChildren()) {
                    // 处理SQLSelectQuery对象
                    handleSQLSelectQuery(sqlSelectQuery, mySqlTableColumnInfo);
                }
            }
            return;
        }

        logger.error("暂未处理的SQLTableSource类型 {} [{}] [{}] [{}] [{}]", sqlTableSource.getClass().getName(), sqlTableSource, MyBatisTableParserUtil.getCurrentXmlFileName(),
                MyBatisTableParserUtil.getCurrentSqlID(), MyBatisTableParserUtil.getCurrentSql());
    }

    // 处理update语句set中的字段赋值
    private void handleUpdateSetColumn(SQLExpr updateSetColumnExpr, SQLVariantRefExpr updateSetValueExpr, MySqlTableColumnInfo mySqlTableColumnInfo,
                                       SQLTableSource sqlTableSource) {
        // 处理update set左侧的字段
        List<TableAndColumnName> tableAndColumnNameList = MyBatisTableParserUtil.genTableAndColumnName(updateSetColumnExpr, sqlTableSource);
        // 处理update set右侧的值
        ParameterNameAndType parameterNameAndType = MyBatisTableParserUtil.genParameterNameAndType(updateSetValueExpr);
        for (TableAndColumnName tableAndColumnName : tableAndColumnNameList) {
            mySqlTableColumnInfo.addMySqlSetColumnInfo(new MySqlSetColumnInfo(tableAndColumnName.getTableName(), tableAndColumnName.getColumnName(),
                    parameterNameAndType.getParameterName()));
        }
    }

    // 添加其他select的表名
    private void addOtherSelectTable(SQLObject sqlObject, MySqlTableColumnInfo mySqlTableColumnInfo) {
        SQLExprTableSourceMultiVisitor sqlExprTableSourceMultiVisitor = new SQLExprTableSourceMultiVisitor();
        sqlObject.accept(sqlExprTableSourceMultiVisitor);
        List<String> tableNameList = sqlExprTableSourceMultiVisitor.getTableNameList();
        for (String tableName : tableNameList) {
            if (!mySqlTableColumnInfo.getAllTableSet().contains(tableName) && !mySqlTableColumnInfo.getDeleteTableAliasSet().contains(tableName)) {
                // 仅当表名不在当前已获取到的表名中，且不在delete的别名中时才添加
                mySqlTableColumnInfo.addSelectTable(tableName);
            }
        }
    }

    // 记录表名
    private void recordTableName(String tableName, MySqlTableColumnInfo mySqlTableColumnInfo, MySqlStatementEnum mySqlStatementEnum) {
        switch (mySqlStatementEnum) {
            case DSSE_SELECT:
                mySqlTableColumnInfo.addSelectTable(tableName);
                break;
            case DSSE_SELECT_4_UPDATE:
                mySqlTableColumnInfo.addSelect4UpdateTable(tableName);
                break;
            case DSSE_INSERT:
                mySqlTableColumnInfo.addInsertTable(tableName);
                break;
            case DSSE_INSERT_IGNORE:
                mySqlTableColumnInfo.addInsertIgnoreTable(tableName);
                break;
            case DSSE_INSERT_OR_UPDATE:
                mySqlTableColumnInfo.addInsertOrUpdateTable(tableName);
                break;
            case DSSE_REPLACE:
                mySqlTableColumnInfo.addReplaceIntoTable(tableName);
                break;
            case DSSE_UPDATE:
                mySqlTableColumnInfo.addUpdateTable(tableName);
                break;
            case DSSE_DELETE:
                mySqlTableColumnInfo.addDeleteTable(tableName);
                break;
            case DSSE_ALTER:
                mySqlTableColumnInfo.addAlterTable(tableName);
                break;
            case DSSE_TRUNCATE:
                mySqlTableColumnInfo.addTruncateTable(tableName);
                break;
            case DSSE_CREATE:
                mySqlTableColumnInfo.addCreateTable(tableName);
                break;
            case DSSE_DROP:
                mySqlTableColumnInfo.addDropTable(tableName);
                break;
            default:
                logger.error("非法的语句 {}", mySqlStatementEnum);
                break;
        }
    }
}
