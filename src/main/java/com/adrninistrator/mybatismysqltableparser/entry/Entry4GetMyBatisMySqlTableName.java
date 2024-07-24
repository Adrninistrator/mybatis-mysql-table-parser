package com.adrninistrator.mybatismysqltableparser.entry;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description: 入口类，用于获取MyBatis XML中涉及的全部MySQL表名
 */
public class Entry4GetMyBatisMySqlTableName extends AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(Entry4GetMyBatisMySqlTableName.class);

    /**
     * 获取指定目录中MyBatis XML中涉及的全部MySQL表名，并写入指定文件
     * 生成的文件内容仅包含相关的数据库表名
     *
     * @param dirPath        需要解析的目录路径
     * @param outputFilePath 指定生成文件的路径
     */
    public void getTableName(String dirPath, String outputFilePath) {
        if (StringUtils.isBlank(dirPath) || StringUtils.isBlank(outputFilePath)) {
            logger.warn("传入参数不允许为空");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
            // 处理目录
            Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = handleDirectory(dirPath);

            // 用于保存所有的表名
            Set<String> allTableSet = new HashSet<>();

            for (Map.Entry<String, MyBatisMySqlInfo> entry : myBatisSqlInfoMap.entrySet()) {
                MyBatisMySqlInfo myBatisSqlInfo = entry.getValue();
                Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap = myBatisSqlInfo.getMySqlTableColumnInfoMap();
                for (Map.Entry<String, MySqlTableColumnInfo> entry1 : mySqlTableColumnInfoMap.entrySet()) {
                    MySqlTableColumnInfo mySqlTableColumnInfo = entry1.getValue();
                    // 添加所有的表名列表
                    allTableSet.addAll(mySqlTableColumnInfo.getSelectTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getSelect4UpdateTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getInsertTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getInsertIgnoreTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getInsertOrUpdateTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getReplaceTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getUpdateTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getDeleteTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getAlterTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getTruncateTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getCreateTableList());
                    allTableSet.addAll(mySqlTableColumnInfo.getDropTableList());
                }
            }

            // 在文件中记录表名信息
            recordTableInfo(writer, allTableSet);
        } catch (Exception e) {
            logger.warn("获取指定目录中MyBatis XML中涉及的全部MySQL表名出现异常 ", e);
        }
    }

    // 在文件中记录表名信息
    private void recordTableInfo(BufferedWriter writer, Set<String> allTableSet) throws IOException {
        if (allTableSet.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<String> tableList = new ArrayList<>(allTableSet);
        Collections.sort(tableList);
        for (String table : tableList) {
            stringBuilder.append(table).append(MyBatisTableParserConstants.NEW_LINE);
        }
        writer.write(stringBuilder.toString());
    }
}