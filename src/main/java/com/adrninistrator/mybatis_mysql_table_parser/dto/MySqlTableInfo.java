package com.adrninistrator.mybatis_mysql_table_parser.dto;

import com.adrninistrator.mybatis_mysql_table_parser.common.enums.MySqlStatementEnum;
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
 * @description: MySQL的sql语句中的表信息
 */
public class MySqlTableInfo {
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
    private List<String> updateTableList = new ArrayList<>();

    // delete语句的表名列表
    private List<String> deleteTableList = new ArrayList<>();

    // alter table语句的表名列表
    private final List<String> alterTableList = new ArrayList<>();

    // truncate table语句的表名列表
    private final List<String> truncateTableList = new ArrayList<>();

    // create table语句的表名列表
    private final List<String> createTableList = new ArrayList<>();

    // drop table语句的表名列表
    private final List<String> dropTableList = new ArrayList<>();

    // 使用MySQL时执行写操作的数据库表信息
    private MySQLWriteTableInfo mySQLWriteTableInfo;

    // 解析失败
    private boolean parseFail = false;

    /**
     * 拷贝更新语句相关的表名列表
     *
     * @param src  源对象
     * @param dest 目标对象
     */
    public static void copyUpdateTableList(MySqlTableInfo src, MySqlTableInfo dest) {
        dest.updateTableList = src.updateTableList;
        dest.mySQLWriteTableInfo = src.mySQLWriteTableInfo;
    }

    /**
     * 拷贝删除语句相关的表名列表
     *
     * @param src  源对象
     * @param dest 目标对象
     */
    public static void copyDeleteTableList(MySqlTableInfo src, MySqlTableInfo dest) {
        dest.deleteTableList = src.deleteTableList;
        dest.mySQLWriteTableInfo = src.mySQLWriteTableInfo;
    }

    /**
     * 添加所有的表名列表，不添加重复项
     *
     * @param dest 目标对象
     */
    public void addAllTables(MySqlTableInfo dest) {
        Map<String, Set<String>> usedStoredTableMap = dest.storedTableMap;
        addAllTableList(MySqlStatementEnum.DSSE_SELECT, selectTableList, dest.selectTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_SELECT_4_UPDATE, select4UpdateTableList, dest.select4UpdateTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_INSERT, insertTableList, dest.insertTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_INSERT_IGNORE, insertIgnoreTableList, dest.insertIgnoreTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_INSERT_OR_UPDATE, insertOrUpdateTableList, dest.insertOrUpdateTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_REPLACE, replaceTableList, dest.replaceTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_UPDATE, updateTableList, dest.updateTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_DELETE, deleteTableList, dest.deleteTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_ALTER, alterTableList, dest.alterTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_TRUNCATE, truncateTableList, dest.truncateTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_CREATE, createTableList, dest.createTableList, usedStoredTableMap);
        addAllTableList(MySqlStatementEnum.DSSE_DROP, dropTableList, dest.dropTableList, usedStoredTableMap);
    }

    private void addAllTableList(MySqlStatementEnum mySqlStatementEnum, List<String> srcTableList, List<String> destTableList, Map<String, Set<String>> usedStoredTableMap) {
        for (String srcTable : srcTableList) {
            addTable(mySqlStatementEnum, srcTable, destTableList, usedStoredTableMap);
        }
    }

    private void addTable(MySqlStatementEnum mySqlStatementEnum, String tableName, List<String> tableList, Map<String, Set<String>> usedStoredTableMap) {
        Set<String> storedTableSet = usedStoredTableMap.computeIfAbsent(mySqlStatementEnum.getType(), k -> new HashSet<>());
        // 避免重复添加表名
        if (storedTableSet.add(tableName)) {
            tableList.add(tableName);
        }

        allTableSet.add(tableName);

        if (mySqlStatementEnum.isWriteDml()) {
            // 当前SQL语句为写操作DML，记录
            mySQLWriteTableInfo = new MySQLWriteTableInfo(mySqlStatementEnum, tableName);
        }
    }

    public boolean isParseFail() {
        return parseFail;
    }

    public void setParseFail(boolean parseFail) {
        this.parseFail = parseFail;
    }

    public void addSelectTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_SELECT, tableName, selectTableList, storedTableMap);
    }

    public void addSelect4UpdateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_SELECT_4_UPDATE, tableName, select4UpdateTableList, storedTableMap);
    }

    public void addInsertTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_INSERT, tableName, insertTableList, storedTableMap);
    }

    public void addInsertIgnoreTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_INSERT_IGNORE, tableName, insertIgnoreTableList, storedTableMap);
    }

    public void addInsertOrUpdateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_INSERT_OR_UPDATE, tableName, insertOrUpdateTableList, storedTableMap);
    }

    public void addReplaceIntoTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_REPLACE, tableName, replaceTableList, storedTableMap);
    }

    public void addUpdateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_UPDATE, tableName, updateTableList, storedTableMap);
    }

    public void addDeleteTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_DELETE, tableName, deleteTableList, storedTableMap);
    }

    public void addAlterTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_ALTER, tableName, alterTableList, storedTableMap);
    }

    public void addTruncateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_TRUNCATE, tableName, truncateTableList, storedTableMap);
    }

    public void addCreateTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_CREATE, tableName, createTableList, storedTableMap);
    }

    public void addDropTable(String tableName) {
        addTable(MySqlStatementEnum.DSSE_DROP, tableName, dropTableList, storedTableMap);
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
        addTableList4ToString(stringBuilder, "select", selectTableList);
        addTableList4ToString(stringBuilder, "select4Update", select4UpdateTableList);
        addTableList4ToString(stringBuilder, "insert", insertTableList);
        addTableList4ToString(stringBuilder, "insertIgnore", insertIgnoreTableList);
        addTableList4ToString(stringBuilder, "insertOrUpdate", insertOrUpdateTableList);
        addTableList4ToString(stringBuilder, "replaceInto", replaceTableList);
        addTableList4ToString(stringBuilder, "update", updateTableList);
        addTableList4ToString(stringBuilder, "delete", deleteTableList);
        addTableList4ToString(stringBuilder, "alter", alterTableList);
        addTableList4ToString(stringBuilder, "truncate", truncateTableList);
        addTableList4ToString(stringBuilder, "create", createTableList);
        addTableList4ToString(stringBuilder, "drop", dropTableList);
        if (mySQLWriteTableInfo != null) {
            stringBuilder.append(" write: ").append(mySQLWriteTableInfo);
        }
        return stringBuilder.toString();
    }

    public Set<String> getAllTableSet() {
        return allTableSet;
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

    public MySQLWriteTableInfo getMySQLWriteTableInfo() {
        return mySQLWriteTableInfo;
    }
}
