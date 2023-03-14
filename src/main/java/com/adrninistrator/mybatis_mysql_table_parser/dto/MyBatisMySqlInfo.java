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
        完整sql语句Map
        key
            xml中的id/Mapper中的方法名
        value
            完整sql语句列表，可能存在多条
     */
    private Map<String, List<String>> fullSqlMap;

    /*
        MySQL的sql语句中的表信息Map
        key
            xml中的id/Mapper中的方法名
        value
            MySQL的sql语句中的表信息
     */
    private Map<String, MySqlTableInfo> mySqlTableInfoMap;

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
}
