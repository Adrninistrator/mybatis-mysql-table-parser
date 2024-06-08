package com.adrninistrator.mybatismysqltableparser.common;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description:
 */
public class MyBatisTableParserConstants {
    // 文件列的分隔符
    public static final String FILE_COLUMN_SEPARATOR = "\t";

    public static final String NEW_LINE = "\n";

    public static final String FLAG_DOT = ".";
    public static final String FLAG_ALL = "*";
    public static final String FLAG_AT = "@";

    public static final String XML_ELEMENT_NAME_SELECT = "select";
    public static final String XML_ELEMENT_NAME_UPDATE = "update";
    public static final String XML_ELEMENT_NAME_INSERT = "insert";
    public static final String XML_ELEMENT_NAME_DELETE = "delete";

    // MyBatis Mapper XML中默认的ID
    public static final String[] MYBATIS_MAPPER_DEFAULT_ID = new String[]{
            "insert",
            "insertSelective",
            "updateByPrimaryKey",
            "updateByPrimaryKeySelective",
            "deleteByPrimaryKey",
            "selectByPrimaryKey"
    };

    private MyBatisTableParserConstants() {
        throw new IllegalStateException("illegal");
    }
}
