package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.List;

/**
 * @author adrninistrator
 * @date 2023/9/24
 * @description: MyBatis XML中SQL语句对应的XML元素中的信息
 */
public class MyBatisXmlElement4Statement {

    // XML元素的名称
    private final String xmlElementName;

    // 多条完整的SQL语句列表
    private final List<String> fullSqlList;

    // resultMap字段值
    private final String resultMap;

    public MyBatisXmlElement4Statement(String xmlElementName, List<String> fullSqlList, String resultMap) {
        this.xmlElementName = xmlElementName;
        this.fullSqlList = fullSqlList;
        this.resultMap = resultMap;
    }

    public String getXmlElementName() {
        return xmlElementName;
    }

    public List<String> getFullSqlList() {
        return fullSqlList;
    }

    public String getResultMap() {
        return resultMap;
    }
}
