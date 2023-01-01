package com.adrninistrator.mybatis_mysql_table_parser.entry;

import com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisSqlInfo;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MySqlTableInfo;
import com.adrninistrator.mybatis_mysql_table_parser.parser.MyBatisXmlSqlParser;
import com.adrninistrator.mybatis_mysql_table_parser.parser.MySqlTableParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2022/12/27
 * @description:
 */
public abstract class AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEntry.class);

    protected final MyBatisXmlSqlParser myBatisXmlSqlParser;

    protected final MySqlTableParser mySqlTableParser;

    /*
        MyBatis的sql信息
        key
            mapper接口类名
        value
            MyBatis的sql信息
     */
    protected final Map<String, MyBatisSqlInfo> myBatisSqlInfoMap;

    protected AbstractEntry() {
        myBatisXmlSqlParser = new MyBatisXmlSqlParser();
        mySqlTableParser = new MySqlTableParser();
        myBatisSqlInfoMap = new HashMap<>();
    }

    // 处理目录
    protected void handleDirectory(String dirPath) {
        logger.debug("处理目录 {}", dirPath);
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 处理目录，递归调用
                handleDirectory(file.getPath());
                continue;
            }

            String filePath = file.getPath();
            if (StringUtils.endsWithIgnoreCase(filePath, ".xml")) {
                // 处理xml文件
                handleXmlFile(filePath);
            }
        }
    }

    // 处理xml文件
    protected MyBatisSqlInfo handleXmlFile(String filePath) {
        logger.debug("处理xml文件 {}", filePath);
        try (InputStream inputStream = new FileInputStream(filePath)) {
            // 解析MyBatis的XML文件中的sql语句
            MyBatisSqlInfo myBatisSqlInfo = myBatisXmlSqlParser.parseMybatisXmlSql(inputStream, filePath);
            if (myBatisSqlInfo == null) {
                return null;
            }

            Map<String, MySqlTableInfo> mySqlTableInfoMap = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : myBatisSqlInfo.getFullSqlMap().entrySet()) {
                String sqlId = entry.getKey();
                List<String> fullSqlList = entry.getValue();
                for (String fullSql : fullSqlList) {
                    // 解析sql语句中使用的表名
                    MySqlTableInfo mySqlTableInfo = mySqlTableParser.parseTablesInSql(fullSql);
                    mySqlTableInfoMap.put(sqlId, mySqlTableInfo);
                    if (mySqlTableInfo.isParseFail()) {
                        logger.error("解析失败\t{}\t{}\t{}", filePath, sqlId, fullSql);
                    }
                }
            }

            myBatisSqlInfo.setMySqlTableInfoMap(mySqlTableInfoMap);

            // 记录MyBatis的sql信息
            myBatisSqlInfoMap.put(myBatisSqlInfo.getMapperInterfaceName(), myBatisSqlInfo);

            return myBatisSqlInfo;
        } catch (Exception e) {
            logger.error("error ", e);
            return null;
        }
    }
}
