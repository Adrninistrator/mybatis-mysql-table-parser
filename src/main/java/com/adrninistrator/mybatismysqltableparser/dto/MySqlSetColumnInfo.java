package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.Objects;

/**
 * @author adrninistrator
 * @date 2023/10/6
 * @description: MySQL的sql语句中，set相关的字段信息（update语句）
 */
public class MySqlSetColumnInfo {

    // 数据库表名
    private final String dbTableName;

    // 数据库字段名
    private final String dbColumnName;

    // 数据库字段赋值的参数名
    private final String parameterName;

    public MySqlSetColumnInfo(String dbTableName, String dbColumnName, String parameterName) {
        this.dbTableName = dbTableName;
        this.dbColumnName = dbColumnName;
        this.parameterName = parameterName;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    public String getParameterName() {
        return parameterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MySqlSetColumnInfo that = (MySqlSetColumnInfo) o;
        return dbTableName.equals(that.dbTableName) && dbColumnName.equals(that.dbColumnName) && parameterName.equals(that.parameterName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbTableName, dbColumnName, parameterName);
    }

    @Override
    public String toString() {
        return "MySqlSetColumnInfo{" +
                "dbTableName='" + dbTableName + '\'' +
                ", dbColumnName='" + dbColumnName + '\'' +
                ", parameterName='" + parameterName + '\'' +
                '}';
    }
}
