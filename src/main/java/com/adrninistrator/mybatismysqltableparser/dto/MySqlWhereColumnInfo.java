package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.Objects;

/**
 * @author adrninistrator
 * @date 2023/10/6
 * @description: MySQL的sql语句中，where相关的字段信息
 */
public class MySqlWhereColumnInfo {

    // 数据库表名
    private final String dbTableName;

    // 数据库字段名
    private final String dbColumnName;

    // 数据库字段进行比较的方式
    private final String operation;

    // 数据库字段用于比较的参数名
    private final String parameterName;

    // 数据库字段用于比较的参数的使用方式，#/$
    private final String parameterType;

    public MySqlWhereColumnInfo(String dbTableName, String dbColumnName, String operation, String parameterName, String parameterType) {
        this.dbTableName = dbTableName;
        this.dbColumnName = dbColumnName;
        this.operation = operation;
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    public String getOperation() {
        return operation;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MySqlWhereColumnInfo that = (MySqlWhereColumnInfo) o;
        return dbTableName.equals(that.dbTableName) && dbColumnName.equals(that.dbColumnName) && operation.equals(that.operation) && parameterName.equals(that.parameterName) && parameterType.equals(that.parameterType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbTableName, dbColumnName, operation, parameterName, parameterType);
    }

    @Override
    public String toString() {
        return "MySqlWhereColumnInfo{" +
                "dbTableName='" + dbTableName + '\'' +
                ", dbColumnName='" + dbColumnName + '\'' +
                ", operation='" + operation + '\'' +
                ", parameterName='" + parameterName + '\'' +
                ", parameterType='" + parameterType + '\'' +
                '}';
    }
}
