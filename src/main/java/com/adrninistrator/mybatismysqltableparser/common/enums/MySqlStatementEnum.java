package com.adrninistrator.mybatismysqltableparser.common.enums;

/**
 * @author adrninistrator
 * @date 2021/9/9
 * @description: MySql语句枚举
 */
public enum MySqlStatementEnum {
    DSSE_SELECT("select", "select", true, false),
    // select for update当作数据库写操作
    DSSE_SELECT_4_UPDATE("select_for_update", "select4update", true, true),
    DSSE_INSERT("insert_into", "insert", true, true),
    DSSE_INSERT_IGNORE("insert_ignore_into", "insert_ignore", true, true),
    DSSE_INSERT_OR_UPDATE("insert_into_on_duplicate_key_update", "insert_update", true, true),
    DSSE_REPLACE("replace_into", "replace", true, true),
    DSSE_UPDATE("update", "update", true, true),
    DSSE_DELETE("delete", "delete", true, true),
    DSSE_ALTER("alter_table", "alter", false, false),
    DSSE_TRUNCATE("truncate_table", "truncate", false, false),
    DSSE_CREATE("create_table", "create", false, false),
    DSSE_DROP("drop_table", "drop", false, false),
    DSSE_OPTIMIZE("optimize_table", "optimize", false, false),
    DSSE_ILLEGAL("-", "-", false, false);

    // 语句类型
    private final String type;

    // 语句类型缩写
    private final String initials;

    // 是否为DML
    private final boolean dml;

    // 是否为写数据库操作类型DML
    private final boolean writeDml;

    MySqlStatementEnum(String type, String initials, boolean dml, boolean writeDml) {
        this.type = type;
        this.initials = initials;
        this.dml = dml;
        this.writeDml = writeDml;
    }

    public static MySqlStatementEnum getFromInitials(String initials) {
        for (MySqlStatementEnum dbStatementEnum : MySqlStatementEnum.values()) {
            if (dbStatementEnum.getInitials().equals(initials)) {
                return dbStatementEnum;
            }
        }
        return MySqlStatementEnum.DSSE_ILLEGAL;
    }

    public String getType() {
        return type;
    }

    public String getInitials() {
        return initials;
    }

    public boolean isDml() {
        return dml;
    }

    public boolean isWriteDml() {
        return writeDml;
    }

    @Override
    public String toString() {
        return type;
    }
}
