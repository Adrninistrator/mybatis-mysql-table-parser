package com.adrninistrator.mybatismysqltableparser.entry;

import com.adrninistrator.mybatismysqltableparser.dto.MyBatisMySqlInfo;
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
    public Map<String, MyBatisMySqlInfo> parseDirectory(String dirPath) {
        if (StringUtils.isBlank(dirPath)) {
            logger.warn("传入参数不允许为空");
            return null;
        }

        // 处理目录
        return handleDirectory(dirPath);
    }

    /**
     * 解析文件中MyBatis XML中涉及的MySQL表名
     *
     * @param xmlFilePath 需要解析的XML文件路径
     * @return MyBatis的sql信息，说明见AbstractEntry类的myBatisSqlInfoMap字段
     */
    public MyBatisMySqlInfo parseFile(String xmlFilePath) {
        if (StringUtils.isBlank(xmlFilePath)) {
            logger.warn("传入参数不允许为空");
            return null;
        }

        // 处理xml文件
        return handleXmlFile(xmlFilePath);
    }

    /**
     * 解析文件中MyBatis XML中涉及的MySQL表名
     *
     * @param inputStream 需要解析的文件内容对应的InputStream，流使用完毕后需要调用方关闭
     * @param xmlFilePath 需要解析的XML文件路径
     * @return MyBatis的sql信息，说明见AbstractEntry类的myBatisSqlInfoMap字段
     */
    public MyBatisMySqlInfo parseFile(InputStream inputStream, String xmlFilePath) {
        if (StringUtils.isBlank(xmlFilePath)) {
            logger.warn("传入参数不允许为空");
            return null;
        }

        // 处理xml文件
        return handleXmlFile(inputStream, xmlFilePath);
    }
}
