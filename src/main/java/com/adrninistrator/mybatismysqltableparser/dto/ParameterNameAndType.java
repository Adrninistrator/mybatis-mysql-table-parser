package com.adrninistrator.mybatismysqltableparser.dto;

/**
 * @author adrninistrator
 * @date 2023/10/7
 * @description: 参数的名称及类型
 */
public class ParameterNameAndType {

    // 参数名称
    private final String parameterName;

    // 参数的使用方式（#/$）
    private final String parameterType;

    public ParameterNameAndType(String parameterName, String parameterType) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    @Override
    public String toString() {
        return "ParameterNameAndType{" +
                "parameterName='" + parameterName + '\'' +
                ", parameterType='" + parameterType + '\'' +
                '}';
    }
}
