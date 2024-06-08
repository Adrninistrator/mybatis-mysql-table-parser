package com.adrninistrator.mybatismysqltableparser.dto;

/**
 * @author adrninistrator
 * @date 2023/11/6
 * @description: 参数名称相关信息
 */
public class ParameterName {

    // 参数对象名称
    private final String paramObjName;

    // 参数名称，不包含参数对象名称
    private final String paramName;

    public ParameterName(String paramObjName, String paramName) {
        this.paramObjName = paramObjName;
        this.paramName = paramName;
    }

    public String getParamObjName() {
        return paramObjName;
    }

    public String getParamName() {
        return paramName;
    }

    @Override
    public String toString() {
        return "ParameterName{" +
                "paramObjName='" + paramObjName + '\'' +
                ", paramName='" + paramName + '\'' +
                '}';
    }
}
