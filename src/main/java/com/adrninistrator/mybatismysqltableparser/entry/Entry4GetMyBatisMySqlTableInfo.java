package com.adrninistrator.mybatismysqltableparser.entry;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatismysqltableparser.common.enums.MySqlStatementEnum;
import com.adrninistrator.mybatismysqltableparser.dto.MyBatisMySqlInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlTableColumnInfo;
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
            logger.warn("传入参数不允许为空");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
            String fileHeader = "# sql语句类型" + MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR + "数据库表名" + MyBatisTableParserConstants.NEW_LINE;
            writer.write(fileHeader);

            // 处理目录
            Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = handleDirectory(dirPath);

            // 用于保存所有sql语句类型的表名
            MySqlTableColumnInfo allMySqlTableColumnInfo = new MySqlTableColumnInfo();

            for (Map.Entry<String, MyBatisMySqlInfo> entry : myBatisSqlInfoMap.entrySet()) {
                MyBatisMySqlInfo myBatisSqlInfo = entry.getValue();
                Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap = myBatisSqlInfo.getMySqlTableColumnInfoMap();
                for (Map.Entry<String, MySqlTableColumnInfo> entry1 : mySqlTableColumnInfoMap.entrySet()) {
                    MySqlTableColumnInfo mySqlTableColumnInfo = entry1.getValue();
                    // 添加所有的表名列表，不添加重复项
                    allMySqlTableColumnInfo.addAllTables(mySqlTableColumnInfo);
                }
            }

            recordTableInfo(writer, MySqlStatementEnum.DSSE_SELECT, allMySqlTableColumnInfo.getSelectTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_SELECT_4_UPDATE, allMySqlTableColumnInfo.getSelect4UpdateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_INSERT, allMySqlTableColumnInfo.getInsertTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_INSERT_IGNORE, allMySqlTableColumnInfo.getInsertIgnoreTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_INSERT_OR_UPDATE, allMySqlTableColumnInfo.getInsertOrUpdateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_REPLACE, allMySqlTableColumnInfo.getReplaceTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_UPDATE, allMySqlTableColumnInfo.getUpdateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_DELETE, allMySqlTableColumnInfo.getDeleteTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_ALTER, allMySqlTableColumnInfo.getAlterTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_TRUNCATE, allMySqlTableColumnInfo.getTruncateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_CREATE, allMySqlTableColumnInfo.getCreateTableList());
            recordTableInfo(writer, MySqlStatementEnum.DSSE_DROP, allMySqlTableColumnInfo.getDropTableList());
        } catch (Exception e) {
            logger.warn("获取指定目录中MyBatis XML中涉及的全部MySQL表名及对应的sql语句出现异常 ", e);
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
            stringBuilder.append(mySqlStatementEnum.getType()).append(MyBatisTableParserConstants.FILE_COLUMN_SEPARATOR)
                    .append(table).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }
}