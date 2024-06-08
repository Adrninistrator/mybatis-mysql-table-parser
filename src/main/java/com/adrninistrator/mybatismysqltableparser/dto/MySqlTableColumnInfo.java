package com.adrninistrator.mybatismysqltableparser.dto;

import com.adrninistrator.mybatismysqltableparser.common.enums.MySqlStatementEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author adrninistrator
 * @date 2022/12/21
 * @description: MySQL的sql语句中的表与字段信息
 */
public class MySqlTableColumnInfo {

    // MyBatis XML中sql语句的元素名称
    private final String xmlElementName;

    /*
        已经保存过的表名Map
        key
            sql语句类型
        value
            已经保存过的表名
     */
    private final Map<String, Set<String>> storedTableMap = new HashMap<>();

    // 所有语句的表名集合
    private final Set<String> allTableSet = new HashSet<>();

    // delete语句对应的表别名集合
    private final Set<String> deleteTableAliasSet = new HashSet<>();

    // select语句的表名列表
    private final List<String> selectTableList = new ArrayList<>();

    // select for update语句的表名列表
    private final List<String> select4UpdateTableList = new ArrayList<>();

    // insert语句的表名列表
    private final List<String> insertTableList = new ArrayList<>();

    // insert ignore into语句的表名列表
    private final List<String> insertIgnoreTableList = new ArrayList<>();

    // insert into on duplicate key update语句的表名列表
    private final List<String> insertOrUpdateTableList = new ArrayList<>();

    // replace into语句的表名列表
    private final List<String> replaceTableList = new ArrayList<>();

    // update语句的表名列表
    private final List<String> updateTableList = new ArrayList<>();

    // delete语句的表名列表
    private final List<String> deleteTableList = new ArrayList<>();

    // alter table语句的表名列表
    private final List<String> alterTableList = new ArrayList<>();

    // truncate table语句的表名列表
    private final List<String> truncateTableList = new ArrayList<>();

    // create table语句的表名列表
    private final List<String> createTableList = new ArrayList<>();

    // drop table语句的表名列表
    private final List<String> dropTableList = new ArrayList<>();

    // sql语句中，set相关的字段信息（update语句）列表
    private final List<MySqlSetColumnInfo> mySqlSetColumnInfoList = new ArrayList<>();

    // sql语句中，where相关的字段信息列表
    private final List<MySqlWhereColumnInfo> mySqlWhereColumnInfoList = new ArrayList<>();

    // sql语句中，select相关的字段信息列表
    private final List<MySqlSelectColumnInfo> mySqlSelectColumnInfoList = new ArrayList<>();

    // 使用MySQL时执行写操作的数据库表信息
    private MySqlWriteTableInfo mySqlWriteTableInfo;

    // 解析失败
    private boolean parseFail = false;

    public MySqlTableColumnInfo() {
        this.xmlElementName = "";
    }

    public MySqlTableColumnInfo(String xmlElementName) {
        this.xmlElementName = xmlElementName;
    }

    /**
     * 拷贝where字段列表
     *
     * @param src 源对象
     */
    public void copyWhereColumnList(MySqlTableColumnInfo src) {
        this.mySqlWhereColumnInfoList.addAll(src.mySqlWhereColumnInfoList);
    }

    /**
     * 拷贝查询字段列表
     *
     * @param src 源对象
     */
    public void copySelectColumnList(MySqlTableColumnInfo src) {
        this.mySqlSelectColumnInfoList.addAll(src.mySqlSelectColumnInfoList);
    }

    /**
     * 拷贝查询字段列表，使用指定的字段别名
     *
     * @param src           源对象
     * @param dbColumnAlias 字段别名
     */
    public void copySelectColumnListWithAlias(MySqlTableColumnInfo src, String dbColumnAlias) {
        for (MySqlSelectColumnInfo srcMySqlSelectColumnInfo : src.mySqlSelectColumnInfoList) {
            MySqlSelectColumnInfo destMySqlSelectColumnInfo = new MySqlSelectColumnInfo(srcMySqlSelectColumnInfo.getDbTableName(), srcMySqlSelectColumnInfo.getDbColumnName(),
                    dbColumnAlias);
            this.mySqlSelectColumnInfoList.add(destMySqlSelectColumnInfo);
        }
    }

    /**
     * 拷贝查询语句相关的表名列表
     *
     * @param src 源对象
     */
    public void copySelectTableList(MySqlTableColumnInfo src) {
        addAllTableList(MySqlStatementEnum.DSSE_SELECT, src.selectTableList, this.selectTableList);
    }

    /**
     * 拷贝更新语句相关的表名列表
     *
     * @param src 源对象
     */
    public void copyUpdateTableList(MySqlTableColumnInfo src) {
        addAllTableList(MySqlStatementEnum.DSSE_UPDATE, src.updateTableList, this.updateTableList);
        this.mySqlWriteTableInfo = src.mySqlWriteTableInfo;
    }

    /**
     * 拷贝删除语句相关的表名列表
     *
     * @param src 源对象
     */
    public void copyDeleteTableList(MySqlTableColumnInfo src) {
        addAllTableList(MySqlStatementEnum.DSSE_DELETE, src.deleteTableList, this.deleteTableList);
        this.mySqlWriteTableInfo = src.mySqlWriteTableInfo;
    }

    /**
     * 添加所有的表名列表，不添加重复项
     *
     * @param src 源对象
     */
    public void addAllTables(MySqlTableColumnInfo src) {
        addAllTableList(MySqlStatementEnum.DSSE_SELECT, src.selectTableList, this.selectTableList);
        addAllTableList(MySqlStatementEnum.DSSE_SELECT_4_UPDATE, src.select4UpdateTableList, this.select4UpdateTableList);
        addAllTableList(MySqlStatementEnum.DSSE_INSERT, src.insertTableList, this.insertTableList);
        addAllTableList(MySqlStatementEnum.DSSE_INSERT_IGNORE, src.insertIgnoreTableList, this.insertIgnoreTableList);
        addAllTableList(MySqlStatementEnum.DSSE_INSERT_OR_UPDATE, src.insertOrUpdateTableList, this.insertOrUpdateTableList);
        addAllTableList(MySqlStatementEnum.DSSE_REPLACE, src.replaceTableList, this.replaceTableList);
        addAllTableList(MySqlStatementEnum.DSSE_UPDATE, src.updateTableList, this.updateTableList);
        addAllTableList(MySqlStatementEnum.DSSE_DELETE, src.deleteTableList, this.deleteTableList);
        addAllTableList(MySqlStatementEnum.DSSE_ALTER, src.alterTableList, this.alterTableList);
        addAllTableList(MySqlStatementEnum.DSSE_TRUNCATE, src.truncateTableList, this.truncateTableList);
        addAllTableList(MySqlStatementEnum.DSSE_CREATE, src.createTableList, this.createTableList);
        addAllTableList(MySqlStatementEnum.DSSE_DROP, src.dropTableList, this.dropTableList);
    }

    // 添加delete语句对应的表别名集合
    public void addDeleteTableAliasSet(String deleteTableAlias) {
        deleteTableAliasSet.add(deleteTableAlias);
    }

    private void addAllTableList(MySqlStatementEnum mySqlStatementEnum, List<String> srcTableList, List<String> destTableList) {
        for (String srcTable : srcTableList) {
            addTable(mySqlStatementEnum, srcTable, destTableList);
        }
    }

    private void addTable(MySqlStatementEnum mySqlStatementEnum, String tableName, List<String> tableList) {
        if (tableName == null) {
            // 表名使用${}形式时，获取到的表名可能为null
            return;
        }

        Set<String> storedTableSet = storedTableMap.computeIfAbsent(mySqlStatementEnum.getType(), k -> new HashSet<>());
        // 避免重复添加表名
        if (storedTableSet.add(tableName)) {
            tableList.add(tableName);
        }

        allTableSet.add(tableName);

        if (mySqlStatementEnum.isWriteDml()) {
            // 当前SQL语句为写操作DML，记录
            mySqlWriteTableInfo = new MySqlWriteTableInfo(mySqlStatementEnum, tableName);
        }
    }

    public boolean isParseFail() {
        return parseFail;
    }

    public void setParseFail(boolean parseFail) {
        this.parseFail = parseFail;
    }

    public void addSelectTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_SELECT, tableName, selectTableList);
    }

    public void addSelect4UpdateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_SELECT_4_UPDATE, tableName, select4UpdateTableList);
    }

    public void addInsertTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_INSERT, tableName, insertTableList);
    }

    public void addInsertIgnoreTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_INSERT_IGNORE, tableName, insertIgnoreTableList);
    }

    public void addInsertOrUpdateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_INSERT_OR_UPDATE, tableName, insertOrUpdateTableList);
    }

    public void addReplaceIntoTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_REPLACE, tableName, replaceTableList);
    }

    public void addUpdateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_UPDATE, tableName, updateTableList);
    }

    public void addDeleteTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_DELETE, tableName, deleteTableList);
    }

    public void addAlterTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_ALTER, tableName, alterTableList);
    }

    public void addTruncateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_TRUNCATE, tableName, truncateTableList);
    }

    public void addCreateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_CREATE, tableName, createTableList);
    }

    public void addDropTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_DROP, tableName, dropTableList);
    }

    public void addMySqlSetColumnInfo(MySqlSetColumnInfo mySqlSetColumnInfo) {
        if (!mySqlSetColumnInfoList.contains(mySqlSetColumnInfo)) {
            mySqlSetColumnInfoList.add(mySqlSetColumnInfo);
        }
    }

    public void addMySqlWhereColumnInfo(MySqlWhereColumnInfo mySqlWhereColumnInfo) {
        if (!mySqlWhereColumnInfoList.contains(mySqlWhereColumnInfo)) {
            mySqlWhereColumnInfoList.add(mySqlWhereColumnInfo);
        }
    }

    public boolean addMySqlSelectColumnInfo(MySqlSelectColumnInfo mySqlSelectColumnInfo) {
        if (!mySqlSelectColumnInfoList.contains(mySqlSelectColumnInfo)) {
            mySqlSelectColumnInfoList.add(mySqlSelectColumnInfo);
            return true;
        }
        return false;
    }

    private void addTableList4ToString(StringBuilder stringBuilder, String listName, List<String> list) {
        if (list.isEmpty()) {
            return;
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.append(" ");
        }
        stringBuilder.append(listName).append(": ").append(StringUtils.join(list, ", "));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_SELECT.getInitials(), selectTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_SELECT_4_UPDATE.getInitials(), select4UpdateTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_INSERT.getInitials(), insertTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_INSERT_IGNORE.getInitials(), insertIgnoreTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_INSERT_OR_UPDATE.getInitials(), insertOrUpdateTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_REPLACE.getInitials(), replaceTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_UPDATE.getInitials(), updateTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_DELETE.getInitials(), deleteTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_ALTER.getInitials(), alterTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_TRUNCATE.getInitials(), truncateTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_CREATE.getInitials(), createTableList);
        addTableList4ToString(stringBuilder, MySqlStatementEnum.DSSE_DROP.getInitials(), dropTableList);
        if (mySqlWriteTableInfo != null) {
            stringBuilder.append(" write: ").append(mySqlWriteTableInfo);
        }
        return stringBuilder.toString();
    }

    public String getXmlElementName() {
        return xmlElementName;
    }

    public Set<String> getAllTableSet() {
        return allTableSet;
    }

    public Set<String> getDeleteTableAliasSet() {
        return deleteTableAliasSet;
    }

    public List<String> getSelectTableList() {
        return selectTableList;
    }

    public List<String> getSelect4UpdateTableList() {
        return select4UpdateTableList;
    }

    public List<String> getInsertTableList() {
        return insertTableList;
    }

    public List<String> getInsertIgnoreTableList() {
        return insertIgnoreTableList;
    }

    public List<String> getInsertOrUpdateTableList() {
        return insertOrUpdateTableList;
    }

    public List<String> getReplaceTableList() {
        return replaceTableList;
    }

    public List<String> getUpdateTableList() {
        return updateTableList;
    }

    public List<String> getDeleteTableList() {
        return deleteTableList;
    }

    public List<String> getAlterTableList() {
        return alterTableList;
    }

    public List<String> getTruncateTableList() {
        return truncateTableList;
    }

    public List<String> getCreateTableList() {
        return createTableList;
    }

    public List<String> getDropTableList() {
        return dropTableList;
    }

    public List<MySqlSetColumnInfo> getMySqlSetColumnInfoList() {
        return mySqlSetColumnInfoList;
    }

    public List<MySqlWhereColumnInfo> getMySqlWhereColumnInfoList() {
        return mySqlWhereColumnInfoList;
    }

    public List<MySqlSelectColumnInfo> getMySqlSelectColumnInfoList() {
        return mySqlSelectColumnInfoList;
    }

    public MySqlWriteTableInfo getMySqlWriteTableInfo() {
        return mySqlWriteTableInfo;
    }
}
