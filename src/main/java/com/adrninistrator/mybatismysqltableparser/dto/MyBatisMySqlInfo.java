package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.Map;

/**
 * @author adrninistrator
 * @date 2022/12/18
 * @description: 使用MySQL时MyBatis的sql信息
 */
public class MyBatisMySqlInfo {
    // XML文件路径
    private String xmlFilePath;

    // mapper接口类名
    private String mapperInterfaceName;

    /*
        MySQL的完整sql语句Map
        key     xml中的id，即Mapper中的方法名
        value   完整sql语句列表，可能存在多条
     */
    private Map<String, MyBatisXmlElement4Statement> statementMap;

    /*
        MySQL的sql语句中的表与字段信息Map
        key     xml中的id/Mapper中的方法名
        value   MySQL的sql语句中的表与字段信息
     */
    private Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap;

    // mapper对应的可能的数据库表名
    private String possibleTableName;

    // Entity类名
    private String entityClassName;

    /*
        Entity类字段名与对应的数据库字段名Map
        key     entity类字段名
        value   数据库字段名
     */
    private Map<String, String> entityAndColumnNameMap;

    /*
        数据库字段名与对应的entity类字段名Map
        key     entity类字段名
        value   数据库字段名
     */
    private Map<String, String> tableAndEntityColumnNameMap;

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public String getMapperInterfaceName() {
        return mapperInterfaceName;
    }

    public void setMapperInterfaceName(String mapperInterfaceName) {
        this.mapperInterfaceName = mapperInterfaceName;
    }

    public Map<String, MyBatisXmlElement4Statement> getStatementMap() {
        return statementMap;
    }

    public void setStatementMap(Map<String, MyBatisXmlElement4Statement> statementMap) {
        this.statementMap = statementMap;
    }

    public Map<String, MySqlTableColumnInfo> getMySqlTableColumnInfoMap() {
        return mySqlTableColumnInfoMap;
    }

    public void setMySqlTableColumnInfoMap(Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap) {
        this.mySqlTableColumnInfoMap = mySqlTableColumnInfoMap;
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

    public Map<String, String> getEntityAndColumnNameMap() {
        return entityAndColumnNameMap;
    }

    public void setEntityAndColumnNameMap(Map<String, String> entityAndColumnNameMap) {
        this.entityAndColumnNameMap = entityAndColumnNameMap;
    }

    public Map<String, String> getTableAndEntityColumnNameMap() {
        return tableAndEntityColumnNameMap;
    }

    public void setTableAndEntityColumnNameMap(Map<String, String> tableAndEntityColumnNameMap) {
        this.tableAndEntityColumnNameMap = tableAndEntityColumnNameMap;
    }
}
