package com.adrninistrator.mybatismysqltableparser.dto;

import java.util.List;

/**
 * @author adrninistrator
 * @date 2025/6/13
 * @description: MyBatis XML中的resultMap
 */
public class MyBatisResultMap {

    // resultMap的id
    private String id;

    // resultMap对应的Entity类型
    private String entityType;

    // resultMap继承的resultMap
    private String extendsResultMap;

    // resultMap对应的每个id、result的内容
    private List<MyBatisResultMapResult> resultMapResultList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getExtendsResultMap() {
        return extendsResultMap;
    }

    public void setExtendsResultMap(String extendsResultMap) {
        this.extendsResultMap = extendsResultMap;
    }

    public List<MyBatisResultMapResult> getResultMapResultList() {
        return resultMapResultList;
    }

    public void setResultMapResultList(List<MyBatisResultMapResult> resultMapResultList) {
        this.resultMapResultList = resultMapResultList;
    }
}
