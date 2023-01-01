package com.adrninistrator.mybatis_mysql_table_parser.common.enums;

/**
 * @author adrninistrator
 * @date 2021/9/9
 * @description: MySql语句枚举
 */
public enum MySqlStatementEnum {
    DSE_SELECT("select"),
    DSE_SELECT_4_UPDATE("select_for_update"),
    DSE_INSERT("insert_into"),
    DSE_INSERT_IGNORE("insert_ignore_into"),
    DSE_INSERT_OR_UPDATE("insert_into_on_duplicate_key_update"),
    DSE_REPLACE("replace_into"),
    DSE_UPDATE("update"),
    DSE_DELETE("delete"),
    DSE_ALTER("alter_table"),
    DSE_TRUNCATE("truncate_table"),
    DSE_CREATE("create_table"),
    DSE_DROP("drop_table"),
    DSE_ILLEGAL("-");

    private final String statement;

    MySqlStatementEnum(String statement) {
        this.statement = statement;
    }

    public static MySqlStatementEnum getFromStatement(String type) {
        for (MySqlStatementEnum dbStatementEnum : MySqlStatementEnum.values()) {
            if (dbStatementEnum.getStatement().equals(type)) {
                return dbStatementEnum;
            }
        }
        return MySqlStatementEnum.DSE_ILLEGAL;
    }

    public String getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        return statement;
    }
}
