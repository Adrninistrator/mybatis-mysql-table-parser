package com.adrninistrator.mybatis_mysql_table_parser.entry;

import com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisSqlInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * @author adrninistrator
 * @date 2022/12/27
 * @description: 入口类，用于解析MyBatis XML中涉及的MySQL表名
 */
public class Entry4ParseMyBatisMySqlTable extends AbstractEntry {
    private static final Logger logger = LoggerFactory.getLogger(Entry4ParseMyBatisMySqlTable.class);

    /**
     * 解析目录中MyBatis XML中涉及的MySQL表名
     *
     * @param dirPath 需要解析的目录路径
     * @return MyBatis的sql信息，说明见AbstractEntry类的myBatisSqlInfoMap字段
     */
    public Map<String, MyBatisSqlInfo> parseDirectory(String dirPath) {
        if (StringUtils.isBlank(dirPath)) {
            logger.error("传入参数不允许为空");
            return null;
        }

        // 处理目录
        handleDirectory(dirPath);

        return myBatisSqlInfoMap;
    }

    /**
     * 解析文件中MyBatis XML中涉及的MySQL表名
     *
     * @param filePath 需要解析的文件路径
     * @return MyBatis的sql信息，说明见AbstractEntry类的myBatisSqlInfoMap字段
     */
    public MyBatisSqlInfo parseFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            logger.error("传入参数不允许为空");
            return null;
        }

        // 处理xml文件
        return handleXmlFile(filePath);
    }

    /**
     * 解析文件中MyBatis XML中涉及的MySQL表名
     *
     * @param inputStream 需要解析的文件内容对应的InputStream
     * @param filePath    需要解析的文件路径，仅用于打印日志
     * @return MyBatis的sql信息，说明见AbstractEntry类的myBatisSqlInfoMap字段
     */
    public MyBatisSqlInfo parseFile(InputStream inputStream, String filePath) {
        if (StringUtils.isBlank(filePath)) {
            logger.error("传入参数不允许为空");
            return null;
        }

        // 处理xml文件
        return handleXmlFile(inputStream, filePath);
    }
}
