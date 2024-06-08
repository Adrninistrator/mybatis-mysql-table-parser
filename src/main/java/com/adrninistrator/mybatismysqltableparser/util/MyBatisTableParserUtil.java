package com.adrninistrator.mybatismysqltableparser.util;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatismysqltableparser.dto.ParameterName;
import com.adrninistrator.mybatismysqltableparser.dto.ParameterNameAndType;
import com.adrninistrator.mybatismysqltableparser.dto.TableAndColumnName;
import com.adrninistrator.mybatismysqltableparser.tokenhandler.DollarParameterTokenHandler;
import com.adrninistrator.mybatismysqltableparser.tokenhandler.HashtagParameterTokenHandler;
import com.adrninistrator.mybatismysqltableparser.visitor.SQLExprTableSourceVisitor;
import com.alibaba.druid.sql.ast.SQLCurrentTimeExpr;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLSequenceExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
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
 * @date 2023/10/7
 * @description:
 */
public class MyBatisTableParserUtil {
    private static final Logger logger = LoggerFactory.getLogger(MyBatisTableParserUtil.class);

    private static final ThreadLocal<String> THREAD_LOCAL_CURRENT_XML_FILE_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> THREAD_LOCAL_CURRENT_SQL_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> THREAD_LOCAL_CURRENT_SQL = new ThreadLocal<>();

    /**
     * 记录当前处理的XML文件名
     *
     * @param sql
     */
    public static void recordCurrentXmlFileName(String sql) {
        THREAD_LOCAL_CURRENT_XML_FILE_NAME.set(sql);
    }

    /**
     * 获取当前处理的XML文件名
     *
     * @return
     */
    public static String getCurrentXmlFileName() {
        return StringUtils.defaultString(THREAD_LOCAL_CURRENT_XML_FILE_NAME.get(), "");
    }


    /**
     * 记录当前处理的SQL语句ID
     *
     * @param sql
     */
    public static void recordCurrentSqlID(String sql) {
        THREAD_LOCAL_CURRENT_SQL_ID.set(sql);
    }

    /**
     * 获取当前处理的SQL语句ID
     *
     * @return
     */
    public static String getCurrentSqlID() {
        return StringUtils.defaultString(THREAD_LOCAL_CURRENT_SQL_ID.get(), "");
    }

    /**
     * 记录当前处理的sql语句
     *
     * @param sql
     */
    public static void recordCurrentSql(String sql) {
        THREAD_LOCAL_CURRENT_SQL.set(sql);
    }

    /**
     * 获取当前处理的sql语句
     *
     * @return
     */
    public static String getCurrentSql() {
        return StringUtils.defaultString(THREAD_LOCAL_CURRENT_SQL.get(), "");
    }

    /**
     * 清理当前处理的信息
     */
    public static void clearThreadLocal() {
        THREAD_LOCAL_CURRENT_XML_FILE_NAME.remove();
        THREAD_LOCAL_CURRENT_SQL_ID.remove();
        THREAD_LOCAL_CURRENT_SQL.remove();
    }

    /**
     * 根据文件路径获取文件名
     *
     * @param filePath
     * @return
     */
    public static String getFileNameFromPath(String filePath) {
        String fileName = StringUtils.substringAfterLast(filePath, "/");
        if (StringUtils.isNotBlank(fileName)) {
            return fileName;
        }
        fileName = StringUtils.substringAfterLast(filePath, "\\");
        if (StringUtils.isNotBlank(fileName)) {
            return fileName;
        }
        return filePath;
    }

    /**
     * 从SQLTableSource中获取表名
     *
     * @param sqlTableSource
     * @return
     */
    public static String getTableNameFromTableSource(SQLTableSource sqlTableSource) {
        SQLExprTableSourceVisitor sqlExprTableSourceVisitor = new SQLExprTableSourceVisitor();
        sqlTableSource.accept(sqlExprTableSourceVisitor);
        return sqlExprTableSourceVisitor.getTableName();
    }

    /**
     * 从SQLTableSource中获取表名，支持别名
     *
     * @param sqlTableSource
     * @param tableAlias
     * @return
     */
    public static String getTableNameFromTableSource(SQLTableSource sqlTableSource, String tableAlias) {
        if (sqlTableSource == null) {
            logger.error("传入的SQLTableSource为空 [{}] [{}] [{}] [{}]", tableAlias, getCurrentXmlFileName(), getCurrentSqlID(), getCurrentSql());
            return "";
        }
        if (StringUtils.isBlank(tableAlias)) {
            // 不使用表别名
            return getTableNameFromTableSource(sqlTableSource);
        }
        // 使用表别名
        SQLTableSource sqlTableSourceUseAlias = sqlTableSource.findTableSource(tableAlias);
        if (sqlTableSourceUseAlias == null) {
            logger.error("从SQLTableSource中根据表别名未获取到对应的表 [{}] [{}] [{}] [{}] [{}]", sqlTableSource, tableAlias, getCurrentXmlFileName(), getCurrentSqlID(), getCurrentSql());
            return "";
        }
        return getTableNameFromTableSource(sqlTableSourceUseAlias);
    }

    /**
     * 根据字段获取对应的数据库表名及字段名
     *
     * @param columnExpr
     * @param sqlTableSource
     * @return
     */
    public static List<TableAndColumnName> genTableAndColumnName(SQLExpr columnExpr, SQLTableSource sqlTableSource) {
        String tableName;
        String columnName;
        if (columnExpr instanceof SQLIdentifierExpr) {
            // 直接使用字段名的情况
            tableName = getTableNameFromTableSource(sqlTableSource);
            columnName = ((SQLIdentifierExpr) columnExpr).getName();
            return Collections.singletonList(new TableAndColumnName(tableName, columnName));
        } else if (columnExpr instanceof SQLPropertyExpr) {
            // 使用别名+字段名的情况: alias.column
            SQLPropertyExpr sqlPropertyExprLeft = (SQLPropertyExpr) columnExpr;
            tableName = getTableNameFromTableSource(sqlTableSource, sqlPropertyExprLeft.getOwnerName());
            columnName = sqlPropertyExprLeft.getName();
            return Collections.singletonList(new TableAndColumnName(tableName, columnName));
        }
        if (columnExpr instanceof SQLVariantRefExpr) {
            // 使用${}的情况
            SQLVariantRefExpr sqlVariantRefExpr = (SQLVariantRefExpr) columnExpr;
            tableName = getTableNameFromTableSource(sqlTableSource, null);
            columnName = sqlVariantRefExpr.getName();
            return Collections.singletonList(new TableAndColumnName(tableName, columnName));
        }
        if (columnExpr instanceof SQLCastExpr) {
            // 使用CAST的情况
            SQLCastExpr sqlCastExpr = (SQLCastExpr) columnExpr;
            return genTableAndColumnName(sqlCastExpr.getExpr(), sqlTableSource);
        }
        if (columnExpr instanceof SQLCaseExpr) {
            // 使用CASE的情况（在where的字段中使用CASE=变量时）
            List<TableAndColumnName> tableAndColumnNameList = new ArrayList<>();
            SQLCaseExpr sqlCaseExpr = (SQLCaseExpr) columnExpr;
            // 处理WHEN...THEN...语句
            for (SQLCaseExpr.Item item : sqlCaseExpr.getItems()) {
                // 处理select对应的表字段
                List<TableAndColumnName> tableAndColumnNameListTmp = genTableAndColumnName(item.getValueExpr(), sqlTableSource);
                for (TableAndColumnName tableAndColumnName : tableAndColumnNameListTmp) {
                    if (!tableAndColumnNameList.contains(tableAndColumnName)) {
                        tableAndColumnNameList.add(tableAndColumnName);
                    }
                }
            }
            // 处理ELSE语句
            SQLExpr caseElseExpr = sqlCaseExpr.getElseExpr();
            if (caseElseExpr != null) {
                // 处理select对应的表字段
                List<TableAndColumnName> tableAndColumnNameListTmp = genTableAndColumnName(caseElseExpr, sqlTableSource);
                for (TableAndColumnName tableAndColumnName : tableAndColumnNameListTmp) {
                    if (!tableAndColumnNameList.contains(tableAndColumnName)) {
                        tableAndColumnNameList.add(tableAndColumnName);
                    }
                }
            }
            return tableAndColumnNameList;
        }
        if (!(columnExpr instanceof SQLTextLiteralExpr) &&
                !(columnExpr instanceof SQLBinaryOpExpr) &&
                !(columnExpr instanceof SQLMethodInvokeExpr) &&
                !(columnExpr instanceof SQLCurrentTimeExpr)) {
            logger.error("暂未处理的字段类型 {} [{}] [{}] [{}] [{}]", columnExpr.getClass().getName(), columnExpr, getCurrentXmlFileName(), getCurrentSqlID(), getCurrentSql());
        }
        return Collections.emptyList();
    }

    /**
     * 根据字段对应的值获取对应的参数名称及使用方式
     *
     * @param valueExpr
     * @return
     */
    public static ParameterNameAndType genParameterNameAndType(SQLVariantRefExpr valueExpr) {
        String parameterName;
        String parameterType;
        String rightName = valueExpr.getName();
        if (rightName.startsWith(DollarParameterTokenHandler.LEFT_FLAG)) {
            parameterName = StringUtils.substringBetween(rightName, DollarParameterTokenHandler.LEFT_FLAG, DollarParameterTokenHandler.RIGHT_FLAG);
            parameterType = DollarParameterTokenHandler.DOLLAR;
        } else if (rightName.startsWith(HashtagParameterTokenHandler.LEFT_FLAG)) {
            parameterName = StringUtils.substringBetween(rightName, HashtagParameterTokenHandler.LEFT_FLAG, HashtagParameterTokenHandler.RIGHT_FLAG);
            parameterType = HashtagParameterTokenHandler.HASHTAG;
        } else if (rightName.startsWith(MyBatisTableParserConstants.FLAG_AT)) {
            parameterName = rightName;
            parameterType = MyBatisTableParserConstants.FLAG_AT;
        } else {
            logger.error("暂未处理的变量形式 {} [{}] [{}] [{}]", rightName, getCurrentXmlFileName(), getCurrentSqlID(), getCurrentSql());
            parameterName = "";
            parameterType = "";
        }
        return new ParameterNameAndType(parameterName, parameterType);
    }

    /**
     * 查找指定的对象的指定类型的父节点
     *
     * @param sqlObject
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T findSuperSQLObject(SQLObject sqlObject, Class<T> clazz) {
        SQLObject currentSQLObject = sqlObject.getParent();
        while (currentSQLObject != null) {
            if (clazz.equals(currentSQLObject.getClass())) {
                return (T) currentSQLObject;
            }
            currentSQLObject = currentSQLObject.getParent();
        }
        return null;
    }

    /**
     * 查找指定的对象的父节点中的SQLTableSource
     *
     * @param sqlObject
     * @return
     */
    public static SQLTableSource findSQLTableSourceInSuper(SQLObject sqlObject) {
        // select语句的父节点（包含insert的情况）
        MySqlSelectQueryBlock mySqlSelectQueryBlock = findSuperSQLObject(sqlObject, MySqlSelectQueryBlock.class);
        if (mySqlSelectQueryBlock != null) {
            return mySqlSelectQueryBlock.getFrom();
        }
        // update语句的父节点
        MySqlUpdateStatement mySqlUpdateStatement = findSuperSQLObject(sqlObject, MySqlUpdateStatement.class);
        if (mySqlUpdateStatement != null) {
            return mySqlUpdateStatement.getTableSource();
        }
        // delete语句的父节点
        MySqlDeleteStatement mySqlDeleteStatement = findSuperSQLObject(sqlObject, MySqlDeleteStatement.class);
        if (mySqlDeleteStatement != null) {
            return mySqlDeleteStatement.getTableSource();
        }
        return null;
    }

    /**
     * 生成MyBatis Mapper方法参数使用argx形式的名称，x从0开始
     *
     * @param argSeq
     * @return
     */
    public static String genMyBatisMapperArgNameUseArg(int argSeq) {
        return "arg" + argSeq;
    }

    /**
     * 生成MyBatis Mapper方法参数使用paramx形式的名称，x从1开始
     *
     * @param argSeq
     * @return
     */
    public static String genMyBatisMapperArgNameUseParam(int argSeq) {
        return "param" + (argSeq + 1);
    }

    /**
     * 生成MyBatis Mapper方法参数在SQL语句中可以使用的名称列表
     *
     * @param argNameInSql
     * @param argSeq
     * @return
     */
    public static List<String> genMyBatisMapperPossibleArgNameList(String argNameInSql, int argSeq) {
        /*
            假如参数的@Param注解属性值或参数名称非空，则使用，再加上paramx的形式
            假如以上为空，则使用argx的形式，再加上paramx的形式
         */
        List<String> myBatisMapperPossibleArgNameList = new ArrayList<>(2);
        if (StringUtils.isNotBlank(argNameInSql)) {
            myBatisMapperPossibleArgNameList.add(argNameInSql);
        } else {
            myBatisMapperPossibleArgNameList.add(genMyBatisMapperArgNameUseArg(argSeq));
        }
        myBatisMapperPossibleArgNameList.add(genMyBatisMapperArgNameUseParam(argSeq));
        return myBatisMapperPossibleArgNameList;
    }

    /**
     * 根据参数原始名称（包含参数对象名称），生成参数对象名称+参数名称（不包含参数对象名称）
     *
     * @param paramRawName
     * @return
     */
    public static ParameterName genParameterName(String paramRawName) {
        int lastIndex = paramRawName.lastIndexOf(MyBatisTableParserConstants.FLAG_DOT);
        if (lastIndex == -1) {
            // 变量中不包含对象名称
            return new ParameterName("", paramRawName);
        }
        // 变量中包含对象名称
        String paramObjName = paramRawName.substring(0, lastIndex);
        String paramName = paramRawName.substring(lastIndex + MyBatisTableParserConstants.FLAG_DOT.length());
        return new ParameterName(paramObjName, paramName);
    }

    /**
     * 获取查询语句中select *的表名Set
     *
     * @param mySqlSelectQueryBlock
     * @return
     */
    public static Set<String> getSelectAllTableAliasSet(MySqlSelectQueryBlock mySqlSelectQueryBlock) {
        Set<String> tableAliasSet = new HashSet<>();
        for (SQLSelectItem sqlSelectItem : mySqlSelectQueryBlock.getSelectList()) {
            SQLExpr sqlExpr = sqlSelectItem.getExpr();
            if (sqlExpr instanceof SQLPropertyExpr) {
                SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
                if (MyBatisTableParserConstants.FLAG_ALL.equals(sqlPropertyExpr.getName())) {
                    tableAliasSet.add(sqlPropertyExpr.getOwnerName());
                }
            }
        }
        return tableAliasSet;
    }

    /**
     * 检查SQLExpr是否是处理select的列时忽略的类型
     *
     * @param sqlExpr
     * @return true: 需要忽略 false: 不忽略
     */
    public static boolean checkIgnoreSelectColumnExprType(SQLExpr sqlExpr) {
      /*
                SQLCharExpr             字符串常量
                SQLNCharExpr            N'xxx'
                SQLNumericLiteralExpr   数量常量
                SQLMethodInvokeExpr     sql中的方法
                SQLVariantRefExpr       变量
                SQLBinaryOpExpr         +-等计算
                SQLCurrentTimeExpr      CURDATE
                SQLNullExpr             null
                SQLSequenceExpr         xxx.nextval
                SQLUnaryExpr            -xxx，一般是Oracle语法的SQL语句中的---注释导致
             */
        return (sqlExpr instanceof SQLCharExpr) ||
                (sqlExpr instanceof SQLNCharExpr) ||
                (sqlExpr instanceof SQLNumericLiteralExpr) ||
                (sqlExpr instanceof SQLMethodInvokeExpr) ||
                (sqlExpr instanceof SQLVariantRefExpr) ||
                (sqlExpr instanceof SQLBinaryOpExpr) ||
                (sqlExpr instanceof SQLCurrentTimeExpr) ||
                (sqlExpr instanceof SQLNullExpr) ||
                (sqlExpr instanceof SQLSequenceExpr) ||
                (sqlExpr instanceof SQLUnaryExpr);
    }

    private MyBatisTableParserUtil() {
        throw new IllegalStateException("illegal");
    }
}
