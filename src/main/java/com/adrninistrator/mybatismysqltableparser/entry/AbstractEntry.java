package com.adrninistrator.mybatismysqltableparser.entry;

import com.adrninistrator.mybatismysqltableparser.common.MyBatisTableParserConstants;
import com.adrninistrator.mybatismysqltableparser.dto.MyBatisMySqlInfo;
import com.adrninistrator.mybatismysqltableparser.dto.MyBatisXmlElement4Statement;
import com.adrninistrator.mybatismysqltableparser.dto.MySqlTableColumnInfo;
import com.adrninistrator.mybatismysqltableparser.parser.MyBatisXmlSqlParser;
import com.adrninistrator.mybatismysqltableparser.parser.MySqlTableColumnParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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

    protected final MySqlTableColumnParser mySqlTableColumnParser;

    protected AbstractEntry() {
        myBatisXmlSqlParser = new MyBatisXmlSqlParser();
        mySqlTableColumnParser = new MySqlTableColumnParser();
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
    protected MyBatisMySqlInfo handleXmlFile(String xmlFilePath) {
        return handleXmlFile(xmlFilePath, null);
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(String xmlFilePath, Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap) {
        try (InputStream inputStream = new FileInputStream(xmlFilePath)) {
            return handleXmlFile(inputStream, xmlFilePath, myBatisSqlInfoMap);
        } catch (Exception e) {
            logger.error("处理xml文件出现异常 {} ", xmlFilePath, e);
            return null;
        }
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(InputStream inputStream, String xmlFilePath) {
        return handleXmlFile(inputStream, xmlFilePath, null);
    }

    // 处理xml文件
    protected MyBatisMySqlInfo handleXmlFile(InputStream inputStream, String xmlFilePath, Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap) {
        logger.debug("处理xml文件 {}", xmlFilePath);
        try {
            // 解析MyBatis的XML文件中的sql语句
            MyBatisMySqlInfo myBatisSqlInfo = myBatisXmlSqlParser.parseMybatisXmlSql(inputStream, xmlFilePath);
            if (myBatisSqlInfo == null) {
                return null;
            }

            Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap = new HashMap<>();

            Map<String, MyBatisXmlElement4Statement> statementMap = myBatisSqlInfo.getStatementMap();
            List<String> sqlIdList = new ArrayList<>(statementMap.keySet());
            Collections.sort(sqlIdList);
            for (String sqlId : sqlIdList) {
                MyBatisXmlElement4Statement statement = statementMap.get(sqlId);
                List<String> fullSqlList = statement.getFullSqlList();
                for (String fullSql : fullSqlList) {
                    // 解析sql语句中使用的表名
                    MySqlTableColumnInfo mySqlTableColumnInfo = mySqlTableColumnParser.parseTablesInSql(xmlFilePath, statement.getXmlElementName(), sqlId, fullSql);
                    mySqlTableColumnInfoMap.put(sqlId, mySqlTableColumnInfo);
                    if (mySqlTableColumnInfo.isParseFail()) {
                        logger.error("解析失败 {} {} [{}]", xmlFilePath, sqlId, fullSql);
                    }
                }
            }

            myBatisSqlInfo.setMySqlTableColumnInfoMap(mySqlTableColumnInfoMap);

            // 获取当前xml文件可能处理的数据库表名
            String possibleTableName = getPossibleTableName4MySqlTableColumnInfo(xmlFilePath, mySqlTableColumnInfoMap);
            myBatisSqlInfo.setPossibleTableName(possibleTableName);

            if (myBatisSqlInfoMap != null) {
                // 记录MyBatis的sql信息
                myBatisSqlInfoMap.put(myBatisSqlInfo.getMapperInterfaceName(), myBatisSqlInfo);
            }

            return myBatisSqlInfo;
        } catch (Exception e) {
            String flag = e.getClass().getName().startsWith("org.jdom2.") ? "" : "预期外的异常";
            logger.error("处理xml文件出现异常2 {} {} ", flag, xmlFilePath, e);
            return null;
        }
    }

    // 获取当前xml文件可能处理的数据库表名
    protected String getPossibleTableName4MySqlTableColumnInfo(String xmlFilePath, Map<String, MySqlTableColumnInfo> mySqlTableColumnInfoMap) {
        // 首先尝试通过MyBatis Mapper XML中默认的ID获取数据库表名
        for (String mybatisMapperDefaultId : MyBatisTableParserConstants.MYBATIS_MAPPER_DEFAULT_ID) {
            MySqlTableColumnInfo mySqlTableColumnInfo = mySqlTableColumnInfoMap.get(mybatisMapperDefaultId);
            if (mySqlTableColumnInfo == null) {
                continue;
            }
            Set<String> allTableSet = mySqlTableColumnInfo.getAllTableSet();
            if (allTableSet.size() == 1) {
                for (String tableName : allTableSet) {
                    return tableName;
                }
            }
        }

        // 尝试通过方法名为insert的插入的sql语句信息获得插入的数据库表名
        MySqlTableColumnInfo insertMySqlTableColumnInfo = mySqlTableColumnInfoMap.get(MyBatisTableParserConstants.XML_ELEMENT_NAME_INSERT);
        if (insertMySqlTableColumnInfo != null) {
            String insertTableName = getWriteTableName4List(xmlFilePath, insertMySqlTableColumnInfo.getInsertTableList());
            if (insertTableName != null) {
                return insertTableName;
            }
        }

        // 记录所有涉及的数据库表名
        Set<String> allTableSet = new HashSet<>();
        // 若未获取到，再尝试通过各类sql语句信息获得对应的数据库表名
        for (Map.Entry<String, MySqlTableColumnInfo> entry : mySqlTableColumnInfoMap.entrySet()) {
            logger.debug("当前处理的sql语句的id {}", entry.getKey());
            MySqlTableColumnInfo mySqlTableColumnInfo = entry.getValue();
            String xmlElementName = mySqlTableColumnInfo.getXmlElementName();
            allTableSet.addAll(mySqlTableColumnInfo.getAllTableSet());
            if (StringUtils.equalsAny(xmlElementName, MyBatisTableParserConstants.XML_ELEMENT_NAME_INSERT, MyBatisTableParserConstants.XML_ELEMENT_NAME_UPDATE)) {
                // 处理insert语句，XML中<update>中也可以写insert语句
                String insertTableName = getWriteTableName4List(xmlFilePath, mySqlTableColumnInfo.getInsertTableList());
                if (insertTableName != null) {
                    return insertTableName;
                }
                insertTableName = getWriteTableName4List(xmlFilePath, mySqlTableColumnInfo.getInsertIgnoreTableList());
                if (insertTableName != null) {
                    return insertTableName;
                }
                insertTableName = getWriteTableName4List(xmlFilePath, mySqlTableColumnInfo.getInsertOrUpdateTableList());
                if (insertTableName != null) {
                    return insertTableName;
                }
            } else if (StringUtils.equalsAny(xmlElementName, MyBatisTableParserConstants.XML_ELEMENT_NAME_DELETE, MyBatisTableParserConstants.XML_ELEMENT_NAME_UPDATE)) {
                // 处理delete语句，XML中<update>中也可以写delete语句
                String deleteTableName = getWriteTableName4List(xmlFilePath, mySqlTableColumnInfo.getDeleteTableList());
                if (deleteTableName != null) {
                    return deleteTableName;
                }
            } else if (MyBatisTableParserConstants.XML_ELEMENT_NAME_UPDATE.equals(xmlElementName)) {
                String updateTableName = getWriteTableName4List(xmlFilePath, mySqlTableColumnInfo.getUpdateTableList());
                if (updateTableName != null) {
                    return updateTableName;
                }
            }
        }

        if (allTableSet.size() == 1) {
            // 假如当前Mapper文件只涉及一个数据库表，则使用
            for (String tableName : allTableSet) {
                return tableName;
            }
        }
        return "";
    }

    // 从写操作数据库表名列表中获取表名
    private String getWriteTableName4List(String xmlFilePath, List<String> tableList) {
        if (tableList.size() == 1) {
            return tableList.get(0);
        }
        if (tableList.size() > 1) {
            logger.error("修改操作语句中修改了多个数据库表 {} {}", xmlFilePath, StringUtils.join(tableList, " "));
        }
        return null;
    }
}
