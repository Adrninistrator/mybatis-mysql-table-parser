package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.Objects;

/**
 * @author adrninistrator
 * @date 2025/6/13
 * @description: MyBatis XML中的resultMap中的每个id、result的内容
 */
public class MyBatisResultMapResult {

    // Java Entity中的字段名
    private String javaEntityFieldName;

    // 数据库列类型
    private String dbColumnType;

    // 数据库列名称
    private String dbColumnName;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MyBatisResultMapResult)) return false;
        MyBatisResultMapResult that = (MyBatisResultMapResult) object;
        return Objects.equals(javaEntityFieldName, that.javaEntityFieldName) && Objects.equals(dbColumnType, that.dbColumnType) && Objects.equals(dbColumnName, that.dbColumnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaEntityFieldName, dbColumnType, dbColumnName);
    }

    @Override
    public String toString() {
        return "MyBatisResultMapResult{" +
                "javaEntityFieldName='" + javaEntityFieldName + '\'' +
                ", dbColumnType='" + dbColumnType + '\'' +
                ", dbColumnName='" + dbColumnName + '\'' +
                '}';
    }

    public String getJavaEntityFieldName() {
        return javaEntityFieldName;
    }

    public void setJavaEntityFieldName(String javaEntityFieldName) {
        this.javaEntityFieldName = javaEntityFieldName;
    }

    public String getDbColumnType() {
        return dbColumnType;
    }

    public void setDbColumnType(String dbColumnType) {
        this.dbColumnType = dbColumnType;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
        this.dbColumnName = dbColumnName;
    }
}
