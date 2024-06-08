package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.Objects;

/**
 * @author adrninistrator
 * @date 2023/10/7
 * @description: 数据库表名及字段名
 */
public class TableAndColumnName {

    // 数据库表名
    private final String tableName;

    // 数据库字段名
    private final String columnName;

    public TableAndColumnName(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableAndColumnName that = (TableAndColumnName) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, columnName);
    }

    @Override
    public String toString() {
        return "TableAndColumnName{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
