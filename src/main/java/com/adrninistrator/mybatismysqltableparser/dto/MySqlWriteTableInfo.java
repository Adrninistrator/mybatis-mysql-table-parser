package com.adrninistrator.mybatismysqltableparser.dto;

import com.adrninistrator.mybatismysqltableparser.common.enums.MySqlStatementEnum;

/**
 * @author adrninistrator
 * @date 2023/3/9
 * @description: 使用MySQL时执行写操作的数据库表信息
 */
public class MySqlWriteTableInfo {
    // MySQL支持在一条sql语句中更新多个表，不考虑这种情况
    // sql语句类型
    private final MySqlStatementEnum mySqlStatementEnum;

    // 数据库表名
    private final String tableName;

    public MySqlWriteTableInfo(MySqlStatementEnum mySqlStatementEnum, String tableName) {
        this.mySqlStatementEnum = mySqlStatementEnum;
        this.tableName = tableName;
    }

    public MySqlStatementEnum getMySqlStatementEnum() {
        return mySqlStatementEnum;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return mySqlStatementEnum.getType() + "-" + tableName;
    }
}
