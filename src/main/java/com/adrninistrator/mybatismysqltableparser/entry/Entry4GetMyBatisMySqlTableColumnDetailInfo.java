package com.adrninistrator.mybatismysqltableparser.entry;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatismysqltableparser.common.enums.MySqlStatementEnum;
import com.adrninistrator.mybatismysqltableparser.dto.MyBatisMySqlInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlSelectColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlSetColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlTableColumnInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlWhereColumnInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description: 入口类，用于获取MyBatis XML中涉及的MySQL表名及列名的详细信息，包含MyBatis Mapper信息
 */
public class Entry4GetMyBatisMySqlTableColumnDetailInfo extends AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(Entry4GetMyBatisMySqlTableColumnDetailInfo.class);

    public static final String FILE_NAME_TABLE_INFO = "result_table_info.md";
    public static final String FILE_NAME_WHERE_COLUMN = "result_where_column.md";
    public static final String FILE_NAME_SET_COLUMN = "result_set_column.md";
    public static final String FILE_NAME_SELECT_COLUMN = "result_select_column.md";

    /**
     * 获取指定目录中MyBatis XML中涉及的表名详细信息，并写入指定文件
     * 生成的文件内容使用"\t"分隔
     * 内容分别为MyBatis Mapper类名、MyBatis Mapper方法名、sql语句类型、相关的数据库表名
     *
     * @param dirPath       需要解析的目录路径
     * @param outputDirPath 指定生成文件的目录路径
     */
    public void getDetailInfo(String dirPath, String outputDirPath) {
        if (StringUtils.isBlank(dirPath) || StringUtils.isBlank(outputDirPath)) {
            logger.warn("传入参数不允许为空");
            return;
        }

        File dir = new File(outputDirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("指定的目录不存在，需要先创建 {}", outputDirPath);
            return;
        }

        String filePath4TableInfo = outputDirPath + File.separator + FILE_NAME_TABLE_INFO;
        String filePath4WhereColumn = outputDirPath + File.separator + FILE_NAME_WHERE_COLUMN;
        String filePath4SetColumn = outputDirPath + File.separator + FILE_NAME_SET_COLUMN;
        String filePath4SelectColumn = outputDirPath + File.separator + FILE_NAME_SELECT_COLUMN;

        try (BufferedWriter writer4TableInfo = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath4TableInfo), StandardCharsets.UTF_8));
             BufferedWriter writer4WhereColumn = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath4WhereColumn), StandardCharsets.UTF_8));
             BufferedWriter writer4SetColumn = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath4SetColumn), StandardCharsets.UTF_8));
             BufferedWriter writer4SelectColumn = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath4SelectColumn), StandardCharsets.UTF_8))
        ) {
            String fileHeader4TableInfo = StringUtils.joinWith(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR,
                    "# MyBatis-Mapper类名", "MyBatis-Mapper方法名", "sql语句类型", "数据库表名", "XML文件路径") + MyBatisTableParserConstants.NEW_LINE;
            writer4TableInfo.write(fileHeader4TableInfo);
            String fileHeader4WhereColumn = StringUtils.joinWith(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR,
                    "# MyBatis-Mapper类名", "MyBatis-Mapper方法名", "数据库表名", "数据库字段名", "数据库字段进行比较的方式", "数据库字段用于比较的变量名", "数据库字段用于比较的变量的使用方式", "XML文件路径") + MyBatisTableParserConstants.NEW_LINE;
            writer4WhereColumn.write(fileHeader4WhereColumn);
            String fileHeader4SetColumn = StringUtils.joinWith(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR,
                    "# MyBatis-Mapper类名", "MyBatis-Mapper方法名", "数据库表名", "数据库字段名", "数据库字段赋值的变量名", "XML文件路径") + MyBatisTableParserConstants.NEW_LINE;
            writer4SetColumn.write(fileHeader4SetColumn);
            String fileHeader4SelectColumn = StringUtils.joinWith(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR,
                    "# MyBatis-Mapper类名", "MyBatis-Mapper方法名", "数据库表名", "数据库字段名", "数据库字段别名", "XML文件路径") + MyBatisTableParserConstants.NEW_LINE;
            writer4SelectColumn.write(fileHeader4SelectColumn);

            // 处理目录
            Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = handleDirectory(dirPath);

            List<String> mapperClassNameList = new ArrayList<>(myBatisSqlInfoMap.keySet());
            Collections.sort(mapperClassNameList);
            for (String mapperClassName : mapperClassNameList) {
                MyBatisMySqlInfo myBatisSqlInfo = myBatisSqlInfoMap.get(mapperClassName);
                String xmlFilePath = myBatisSqlInfo.getXmlFilePath();
                Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap = myBatisSqlInfo.getMySqlTableColumnInfoMap();
                List<String> mapperMethodNameList = new ArrayList<>(mySqlTableColumnInfoMap.keySet());
                Collections.sort(mapperMethodNameList);
                for (String mapperMethodName : mapperMethodNameList) {
                    MySqlTableColumnInfo mySqlTableColumnInfo = mySqlTableColumnInfoMap.get(mapperMethodName);
                    // 在文件中记录表名信息
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_SELECT, mySqlTableColumnInfo.getSelectTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_SELECT_4_UPDATE,
                            mySqlTableColumnInfo.getSelect4UpdateTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_INSERT, mySqlTableColumnInfo.getInsertTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_INSERT_IGNORE, mySqlTableColumnInfo.getInsertIgnoreTableList(),
                            xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_INSERT_OR_UPDATE,
                            mySqlTableColumnInfo.getInsertOrUpdateTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_REPLACE, mySqlTableColumnInfo.getReplaceTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_UPDATE, mySqlTableColumnInfo.getUpdateTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_DELETE, mySqlTableColumnInfo.getDeleteTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_ALTER, mySqlTableColumnInfo.getAlterTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_TRUNCATE, mySqlTableColumnInfo.getTruncateTableList(),
                            xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_CREATE, mySqlTableColumnInfo.getCreateTableList(), xmlFilePath);
                    recordTableInfo(writer4TableInfo, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_DROP, mySqlTableColumnInfo.getDropTableList(), xmlFilePath);

                    // 在文件中记录字段信息
                    recordColumnInfo(writer4WhereColumn, mapperClassName, mapperMethodName, mySqlTableColumnInfo.getMySqlWhereColumnInfoList(), xmlFilePath);

                    // 在文件中记录update赋值字段信息
                    recordSetInfo(writer4SetColumn, mapperClassName, mapperMethodName, mySqlTableColumnInfo.getMySqlSetColumnInfoList(), xmlFilePath);

                    // 在文件中记录select字段信息
                    recordSelectInfo(writer4SelectColumn, mapperClassName, mapperMethodName, mySqlTableColumnInfo.getMySqlSelectColumnInfoList(), xmlFilePath);
                }
            }
        } catch (Exception e) {
            logger.warn("获取指定目录中MyBatis XML中涉及的表名详细信息出现异常 ", e);
        }
    }

    // 在文件中记录表名信息
    private void recordTableInfo(BufferedWriter writer, String mapperClassName, String mapperMethodName, MySqlStatementEnum mySqlStatementEnum, List<String> tableList,
                                 String xmlFilePath) throws IOException {
        if (tableList == null || tableList.isEmpty()) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        Collections.sort(tableList);
        for (String table : tableList) {
            stringBuilder.append(mapperClassName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mapperMethodName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlStatementEnum.getType()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(table).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(xmlFilePath).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }

    // 在文件中记录字段信息
    private void recordColumnInfo(BufferedWriter writer, String mapperClassName, String mapperMethodName, List<MySqlWhereColumnInfo> whereColumnInfoList, String xmlFilePath) throws IOException {
        if (whereColumnInfoList.isEmpty()) {
            return;
        }
        whereColumnInfoList.sort(Comparator.comparing(MySqlWhereColumnInfo::getDbTableName).thenComparing(MySqlWhereColumnInfo::getDbColumnName));
        StringBuilder stringBuilder = new StringBuilder();
        for (MySqlWhereColumnInfo whereColumnInfo : whereColumnInfoList) {
            stringBuilder.append(mapperClassName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mapperMethodName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(whereColumnInfo.getDbTableName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(whereColumnInfo.getDbColumnName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(whereColumnInfo.getOperation()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(whereColumnInfo.getParameterName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(whereColumnInfo.getParameterType()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(xmlFilePath).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }

    // 在文件中记录update赋值字段信息
    private void recordSetInfo(BufferedWriter writer, String mapperClassName, String mapperMethodName, List<MySqlSetColumnInfo> setColumnInfoList, String xmlFilePath) throws IOException {
        if (setColumnInfoList.isEmpty()) {
            return;
        }
        setColumnInfoList.sort(Comparator.comparing(MySqlSetColumnInfo::getDbTableName).thenComparing(MySqlSetColumnInfo::getDbColumnName));
        StringBuilder stringBuilder = new StringBuilder();
        for (MySqlSetColumnInfo mySqlSetColumnInfo : setColumnInfoList) {
            stringBuilder.append(mapperClassName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mapperMethodName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlSetColumnInfo.getDbTableName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlSetColumnInfo.getDbColumnName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlSetColumnInfo.getParameterName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(xmlFilePath).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }

    // 在文件中记录select字段信息
    private void recordSelectInfo(BufferedWriter writer, String mapperClassName, String mapperMethodName, List<MySqlSelectColumnInfo> mySqlSelectColumnInfoList,
                                  String xmlFilePath) throws IOException {
        if (mySqlSelectColumnInfoList.isEmpty()) {
            return;
        }
        mySqlSelectColumnInfoList.sort(Comparator.comparing(MySqlSelectColumnInfo::getDbTableName).thenComparing(MySqlSelectColumnInfo::getDbColumnName));
        StringBuilder stringBuilder = new StringBuilder();
        for (MySqlSelectColumnInfo mySqlSelectColumnInfo : mySqlSelectColumnInfoList) {
            stringBuilder.append(mapperClassName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mapperMethodName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlSelectColumnInfo.getDbTableName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlSelectColumnInfo.getDbColumnName()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlSelectColumnInfo.getDbColumnAlias()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(xmlFilePath).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }
}
