package com.adrninistrator.mybatis_mysql_table_parser.entry;

import com.adrninistrator.mybatis_mysql_table_parser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisMySqlInfo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author adrninistrator
 * @date 2022/12/27
 * @description:
 */
public abstract class AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEntry.class);

    protected final MyBatisXmlSqlParser myBatisXmlSqlParser;

    protected final MySqlTableParser mySqlTableParser;


    protected AbstractEntry() {
        myBatisXmlSqlParser = new MyBatisXmlSqlParser();
        mySqlTableParser = new MySqlTableParser();
    }

    /**
     * 处理目录
     *
     * @param dirPath 需要处理的目录路径
     * @return MyBatis的sql信息
     */
    protected Map<String, MyBatisMySqlInfo> handleDirectory(String dirPath) {
        /*
            MyBatis的sql信息
            key
                mapper接口类名
            value
                MyBatis的sql信息
         */
        Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = new HashMap<>();

        doHandleDirectory(dirPath, myBatisSqlInfoMap);
        return myBatisSqlInfoMap;
    }

    // 处理目录
    private void doHandleDirectory(String dirPath, Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap) {
        logger.debug("处理目录 {}", dirPath);
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 处理目录，递归调用
                doHandleDirectory(file.getPath(), myBatisSqlInfoMap);
                continue;
            }

            String filePath = file.getPath();
            if (StringUtils.endsWithIgnoreCase(filePath, ".xml")) {
                // 处理xml文件
                handleXmlFile(filePath, myBatisSqlInfoMap);
            }
        }
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(String filePath) {
        return handleXmlFile(filePath, null);
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(String filePath, Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return handleXmlFile(inputStream, filePath, myBatisSqlInfoMap);
        } catch (Exception e) {
            logger.error("解析sql语句出现异常 ", e);
            return null;
        }
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(InputStream inputStream, String filePath) {
        return handleXmlFile(inputStream, filePath, null);
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(InputStream inputStream, String filePath, Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap) {
        logger.debug("处理xml文件 {}", filePath);
        try {
            // 解析MyBatis的XML文件中的sql语句
            MyBatisMySqlInfo myBatisSqlInfo = myBatisXmlSqlParser.parseMybatisXmlSql(inputStream, filePath);
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

            // 获取当前xml文件可能处理的数据库表名
            String insertTableName = getPossibleTableName4MySqlTableInfo(filePath, mySqlTableInfoMap);
            myBatisSqlInfo.setPossibleTableName(insertTableName);

            if (myBatisSqlInfoMap != null) {
                // 记录MyBatis的sql信息
                myBatisSqlInfoMap.put(myBatisSqlInfo.getMapperInterfaceName(), myBatisSqlInfo);
            }

            return myBatisSqlInfo;
        } catch (Exception e) {
            logger.error("解析sql语句出现异常 ", e);
            return null;
        }
    }

    // 获取当前xml文件可能处理的数据库表名
    protected String getPossibleTableName4MySqlTableInfo(String filePath, Map<String, MySqlTableInfo> mySqlTableInfoMap) {
        // 尝试通过方法名为insert的插入的sql语句信息获得插入的数据库表名
        MySqlTableInfo insertMySqlTableInfo = mySqlTableInfoMap.get(MyBatisTableParserConstants.FLAG_INSERT);
        if (insertMySqlTableInfo != null) {
            String insertTableName = getInsertTableName4List(filePath, insertMySqlTableInfo.getInsertTableList());
            if (insertTableName != null) {
                return insertTableName;
            }
        }

        // 记录所有涉及的数据库表名
        Set<String> allTableNameSet = new HashSet<>();

        // 若未获取到，再尝试通过方法名以insert开头的sql语句信息获得插入的数据库表名
        for (Map.Entry<String, MySqlTableInfo> entry : mySqlTableInfoMap.entrySet()) {
            String methodName = entry.getKey();
            MySqlTableInfo mySqlTableInfo = entry.getValue();
            allTableNameSet.addAll(mySqlTableInfo.getAllTableSet());
            if (!methodName.startsWith(MyBatisTableParserConstants.FLAG_INSERT)) {
                continue;
            }
            String insertTableName = getInsertTableName4List(filePath, mySqlTableInfo.getInsertTableList());
            if (insertTableName != null) {
                return insertTableName;
            }
            insertTableName = getInsertTableName4List(filePath, mySqlTableInfo.getInsertIgnoreTableList());
            if (insertTableName != null) {
                return insertTableName;
            }
            insertTableName = getInsertTableName4List(filePath, mySqlTableInfo.getInsertOrUpdateTableList());
            if (insertTableName != null) {
                return insertTableName;
            }
        }

        if (allTableNameSet.size() == 1) {
            // 假如当前Mapper文件只涉及一个数据库表，则使用
            for (String tableName : allTableNameSet) {
                return tableName;
            }
        }
        return "";
    }

    // 从插入的数据库表名列表中获取表名
    private String getInsertTableName4List(String filePath, List<String> insertTableList) {
        if (insertTableList.size() == 1) {
            return insertTableList.get(0);
        }
        if (insertTableList.size() > 1) {
            logger.error("ihsert语句中插入了多个数据库表 {} {} {}", filePath, MyBatisTableParserConstants.FLAG_INSERT, StringUtils.join(insertTableList, " "));
        }
        return null;
    }
}
