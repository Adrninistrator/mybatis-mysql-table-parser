package com.adrninistrator.mybatis_mysql_table_parser.entry;

import com.adrninistrator.mybatis_mysql_table_parser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatis_mysql_table_parser.common.enums.MySqlStatementEnum;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisSqlInfo;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MySqlTableInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2022/12/27
 * @description: 入口类，用于获取MyBatis XML中涉及的全部MySQL表名，及对应的sql语句类型
 */
public class Entry4GetMyBatisMySqlTableInfo extends AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(Entry4GetMyBatisMySqlTableInfo.class);

    /**
     * 获取指定目录中MyBatis XML中涉及的全部MySQL表名及对应的sql语句，并写入指定文件
     * 生成的文件内容使用"\t"分隔，内容分别为sql语句类型、相关的数据库表名
     *
     * @param dirPath        需要解析的目录路径
     * @param outputFilePath 指定生成文件的路径
     */
    public void getTableInfo(String dirPath, String outputFilePath) {
        if (StringUtils.isBlank(dirPath) || StringUtils.isBlank(outputFilePath)) {
            logger.error("传入参数不允许为空");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
            String fileHeader = "# sql语句类型" + MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR + "表名" + MyBatisTableParserConstants.NEW_LINE;
            writer.write(fileHeader);

            // 处理目录
            handleDirectory(dirPath);

            // 用于保存所有sql语句类型的表名
            MySqlTableInfo allMySqlTableInfo = new MySqlTableInfo();

            for (Map.Entry<String, MyBatisSqlInfo> entry : myBatisSqlInfoMap.entrySet()) {
                MyBatisSqlInfo myBatisSqlInfo = entry.getValue();
                Map<String, MySqlTableInfo> mySqlTableInfoMap = myBatisSqlInfo.getMySqlTableInfoMap();
                for (Map.Entry<String, MySqlTableInfo> entry1 : mySqlTableInfoMap.entrySet()) {
                    MySqlTableInfo mySqlTableInfo = entry1.getValue();
                    // 添加所有的表名列表，不添加重复项
                    MySqlTableInfo.addAllTables(mySqlTableInfo, allMySqlTableInfo);
                }
            }

            recordTableInfo(writer, MySqlStatementEnum.DSE_SELECT, allMySqlTableInfo.getSelectTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_SELECT_4_UPDATE, allMySqlTableInfo.getSelect4UpdateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_INSERT, allMySqlTableInfo.getInsertTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_INSERT_IGNORE, allMySqlTableInfo.getInsertIgnoreTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_INSERT_OR_UPDATE, allMySqlTableInfo.getInsertOrUpdateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_REPLACE, allMySqlTableInfo.getReplaceTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_UPDATE, allMySqlTableInfo.getUpdateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_DELETE, allMySqlTableInfo.getDeleteTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_ALTER, allMySqlTableInfo.getAlterTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_TRUNCATE, allMySqlTableInfo.getTruncateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_CREATE, allMySqlTableInfo.getCreateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSE_DROP, allMySqlTableInfo.getDropTableList());
        } catch (Exception e) {
            logger.error("error ", e);
        }
    }

    // 在文件中记录表名信息
    private void recordTableInfo(BufferedWriter writer, MySqlStatementEnum mySqlStatementEnum, List<String> tableList) throws IOException {
        if (tableList.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Collections.sort(tableList);
        for (String table : tableList) {
            stringBuilder.append(mySqlStatementEnum.getStatement()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(table).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }
}