package com.adrninistrator.mybatis_mysql_table_parser.entry;

import com.adrninistrator.mybatis_mysql_table_parser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatis_mysql_table_parser.common.enums.MySqlStatementEnum;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisMySqlInfo;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MySqlTableInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description: 入口类，用于获取MyBatis XML中涉及的MySQL表名的详细信息，包含MyBatis Mapper信息
 */
public class Entry4GetMyBatisMySqlTableDetailInfo extends AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(Entry4GetMyBatisMySqlTableDetailInfo.class);

    /**
     * 获取指定目录中MyBatis XML中涉及的表名详细信息，并写入指定文件
     * 生成的文件内容使用"\t"分隔，内容分别为MyBatis Mapper类名、MyBatis Mapper方法名、sql语句类型、相关的数据库表名
     *
     * @param dirPath        需要解析的目录路径
     * @param outputFilePath 指定生成文件的路径
     */
    public void getDetailInfo(String dirPath, String outputFilePath) {
        if (StringUtils.isBlank(dirPath)) {
            logger.error("传入参数不允许为空");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
            String fileHeader = "# MyBatis-Mapper类名" + MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR +
                    "MyBatis-Mapper方法名" + MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR +
                    "sql语句类型" + MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR +
                    "表名" + MyBatisTableParserConstants.NEW_LINE;
            writer.write(fileHeader);

            // 处理目录
            Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = handleDirectory(dirPath);

            List<String> mapperClassNameList = new ArrayList<>(myBatisSqlInfoMap.keySet());
            Collections.sort(mapperClassNameList);
            for (String mapperClassName : mapperClassNameList) {
                MyBatisMySqlInfo myBatisSqlInfo = myBatisSqlInfoMap.get(mapperClassName);
                Map<String, MySqlTableInfo> mySqlTableInfoMap = myBatisSqlInfo.getMySqlTableInfoMap();
                List<String> mapperMethodNameList = new ArrayList<>(mySqlTableInfoMap.keySet());
                Collections.sort(mapperMethodNameList);
                for (String mapperMethodName : mapperMethodNameList) {
                    MySqlTableInfo mySqlTableInfo = mySqlTableInfoMap.get(mapperMethodName);
                    // 在文件中记录表名信息
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_SELECT, mySqlTableInfo.getSelectTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_SELECT_4_UPDATE, mySqlTableInfo.getSelect4UpdateTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_INSERT, mySqlTableInfo.getInsertTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_INSERT_IGNORE, mySqlTableInfo.getInsertIgnoreTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_INSERT_OR_UPDATE, mySqlTableInfo.getInsertOrUpdateTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_REPLACE, mySqlTableInfo.getReplaceTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_UPDATE, mySqlTableInfo.getUpdateTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_DELETE, mySqlTableInfo.getDeleteTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_ALTER, mySqlTableInfo.getAlterTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_TRUNCATE, mySqlTableInfo.getTruncateTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_CREATE, mySqlTableInfo.getCreateTableList());
                    recordTableInfo(writer, mapperClassName, mapperMethodName, MySqlStatementEnum.DSSE_DROP, mySqlTableInfo.getDropTableList());
                }
            }
        } catch (Exception e) {
            logger.error("解析sql语句出现异常 ", e);
        }
    }

    // 在文件中记录表名信息
    private void recordTableInfo(BufferedWriter writer, String mapperClassName, String mapperMethodName, MySqlStatementEnum mySqlStatementEnum, List<String> tableList) throws IOException {
        if (tableList.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Collections.sort(tableList);
        for (String table : tableList) {
            stringBuilder.append(mapperClassName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mapperMethodName).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(mySqlStatementEnum.getType()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(table).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }
}
