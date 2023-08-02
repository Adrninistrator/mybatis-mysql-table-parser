package com.adrninistrator.mybatis_mysql_table_parser.dto;

import java.util.List;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2022/12/18
 * @description: 使用MySQL时MyBatis的sql信息
 */
public class MyBatisMySqlInfo {
    // mapper接口类名
    private String mapperInterfaceName;

    /*
        MySQL的完整sql语句Map
        key     xml中的id/Mapper中的方法名
        value   完整sql语句列表，可能存在多条
     */
    private Map<String, List<String>> fullSqlMap;

    /*
        MySQL的sql语句中的表信息Map
        key     xml中的id/Mapper中的方法名
        value   MySQL的sql语句中的表信息
     */
    private Map<String, MySqlTableInfo> mySqlTableInfoMap;

    // mapper对应的可能的数据库表名
    private String possibleTableName;

    // Entity类名
    private String entityClassName;

    /*
        Entity类字段名与对应的数据库表字段名Map
        key     entity类字段名
        value   数据库表字段名
     */
    private Map<String, String> entityAndTableColumnNameMap;

    /*
        数据库表字段名与对应的entity类字段名Map
        key     entity类字段名
        value   数据库表字段名
     */
    private Map<String, String> tableAndEntityColumnNameMap;

    public String getMapperInterfaceName() {
        return mapperInterfaceName;
    }

    public void setMapperInterfaceName(String mapperInterfaceName) {
        this.mapperInterfaceName = mapperInterfaceName;
    }

    public Map<String, List<String>> getFullSqlMap() {
        return fullSqlMap;
    }

    public void setFullSqlMap(Map<String, List<String>> fullSqlMap) {
        this.fullSqlMap = fullSqlMap;
    }

    public Map<String, MySqlTableInfo> getMySqlTableInfoMap() {
        return mySqlTableInfoMap;
    }

    public void setMySqlTableInfoMap(Map<String, MySqlTableInfo> mySqlTableInfoMap) {
        this.mySqlTableInfoMap = mySqlTableInfoMap;
    }

    public String getPossibleTableName() {
        return possibleTableName;
    }

    public void setPossibleTableName(String possibleTableName) {
        this.possibleTableName = possibleTableName;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public Map<String, String> getEntityAndTableColumnNameMap() {
        return entityAndTableColumnNameMap;
    }

    public void setEntityAndTableColumnNameMap(Map<String, String> entityAndTableColumnNameMap) {
        this.entityAndTableColumnNameMap = entityAndTableColumnNameMap;
    }

    public Map<String, String> getTableAndEntityColumnNameMap() {
        return tableAndEntityColumnNameMap;
    }

    public void setTableAndEntityColumnNameMap(Map<String, String> tableAndEntityColumnNameMap) {
        this.tableAndEntityColumnNameMap = tableAndEntityColumnNameMap;
    }
}
