package com.adrninistrator.mybatismysqltableparser.visitor;

import com.adrninistrator.mybatismysqltableparser.dto.MySqlSelectColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlTableColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlWhereColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.ParameterNameAndType;
import com.adrninistrator.mybatismysqltableparser.dto.TableAndColumnName;
import com.adrninistrator.mybatismysqltableparser.parser.MySqlTableColumnParser;
import com.adrninistrator.mybatismysqltableparser.util.MyBatisTableParserUtil;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author adrninistrator
 * @date 2023/10/7
 * @description: 处理SQLBinaryOpExpr的visitor，用于获取字段关系
 */
public class SQLBinaryOpExprVisitor implements MySqlASTVisitor {

    private static final Logger logger = LoggerFactory.getLogger(SQLBinaryOpExprVisitor.class);

    private final MySqlTableColumnInfo mySqlTableColumnInfo;

    public SQLBinaryOpExprVisitor(MySqlTableColumnInfo mySqlTableColumnInfo) {
        this.mySqlTableColumnInfo = mySqlTableColumnInfo;
    }

    @Override
    public boolean visit(SQLBinaryOpExpr x) {
        SQLBinaryOperator sqlBinaryOperator = x.getOperator();
        if (!sqlBinaryOperator.isRelational()) {
            // where中的字段操作不是比较时，返回
            return true;
        }

        // 查找当前对象的父节点中的SQLTableSource
        SQLTableSource sqlTableSource = MyBatisTableParserUtil.findSQLTableSourceInSuper(x);
        if (sqlTableSource == null) {
            logger.warn("未获取到表来源 {}", x);
            return true;
        }

        SQLExpr sqlExprLeft = x.getLeft();
        SQLExpr sqlExprRight = x.getRight();

        // where左侧或右侧的变量，通常是右侧的
        SQLVariantRefExpr sqlExprVariant;
        // where另一侧的表达式
        SQLExpr sqlExprAnother;

        if (sqlExprLeft instanceof SQLVariantRefExpr
                && (sqlExprRight instanceof SQLIdentifierExpr || sqlExprRight instanceof SQLPropertyExpr)) {
            // where左侧为变量，右侧为字段名称（可能会写成where 'a' = #{xx} 的形式
            sqlExprVariant = (SQLVariantRefExpr) sqlExprLeft;
            sqlExprAnother = sqlExprRight;
        } else if (sqlExprRight instanceof SQLVariantRefExpr) {
            // where右侧为变量
            sqlExprVariant = (SQLVariantRefExpr) sqlExprRight;
            sqlExprAnother = sqlExprLeft;
        } else {
            return true;
        }

        if (sqlExprAnother instanceof SQLQueryExpr) {
            // 处理where中某个查询语句的结果等于指定变量的情况
            // 处理where中的查询语句
            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) sqlExprAnother;
            MySqlTableColumnParser mySqlTableColumnParser = new MySqlTableColumnParser();
            MySqlTableColumnInfo tmpMySqlTableColumnInfo = new MySqlTableColumnInfo();
            // 处理SQLSelectQuery对象
            mySqlTableColumnParser.handleSQLSelectQuery(sqlQueryExpr.getSubQuery().getQuery(), tmpMySqlTableColumnInfo);

            // 处理where中的变量
            ParameterNameAndType parameterNameAndType = MyBatisTableParserUtil.genParameterNameAndType(sqlExprVariant);
            for (MySqlSelectColumnInfo mySqlSelectColumnInfo : tmpMySqlTableColumnInfo.getMySqlSelectColumnInfoList()) {
                mySqlTableColumnInfo.addMySqlWhereColumnInfo(new MySqlWhereColumnInfo(mySqlSelectColumnInfo.getDbTableName(), mySqlSelectColumnInfo.getDbColumnName(),
                        x.getOperator().getName(), parameterNameAndType.getParameterName(), parameterNameAndType.getParameterType()));
            }
            return true;
        }

        // 处理where中的字段
        List<TableAndColumnName> tableAndColumnNameList = MyBatisTableParserUtil.genTableAndColumnName(sqlExprAnother, sqlTableSource);
        // 处理where中的变量
        ParameterNameAndType parameterNameAndType = MyBatisTableParserUtil.genParameterNameAndType(sqlExprVariant);
        for (TableAndColumnName tableAndColumnName : tableAndColumnNameList) {
            mySqlTableColumnInfo.addMySqlWhereColumnInfo(new MySqlWhereColumnInfo(tableAndColumnName.getTableName(), tableAndColumnName.getColumnName(), x.getOperator().getName(),
                    parameterNameAndType.getParameterName(), parameterNameAndType.getParameterType()));
        }
        return true;
    }
}
