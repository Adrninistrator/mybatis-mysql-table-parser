package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.Objects;

/**
 * @author adrninistrator
 * @date 2023/10/12
 * @description:
 */
public class MySqlSelectColumnInfo {

    // 数据库表名
    private final String dbTableName;

    // 数据库字段名
    private final String dbColumnName;

    // 数据库字段别名
    private final String dbColumnAlias;

    public MySqlSelectColumnInfo(String dbTableName, String dbColumnName, String dbColumnAlias) {
        this.dbTableName = dbTableName;
        this.dbColumnName = dbColumnName;
        this.dbColumnAlias = dbColumnAlias;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    public String getDbColumnAlias() {
        return dbColumnAlias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MySqlSelectColumnInfo that = (MySqlSelectColumnInfo) o;
        return Objects.equals(dbTableName, that.dbTableName) && Objects.equals(dbColumnName, that.dbColumnName) && Objects.equals(dbColumnAlias, that.dbColumnAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbTableName, dbColumnName, dbColumnAlias);
    }

    @Override
    public String toString() {
        return "MySqlSelectColumnInfo{" +
                "dbTableName='" + dbTableName + '\'' +
                ", dbColumnName='" + dbColumnName + '\'' +
                ", dbColumnAlias='" + dbColumnAlias + '\'' +
                '}';
    }
}
