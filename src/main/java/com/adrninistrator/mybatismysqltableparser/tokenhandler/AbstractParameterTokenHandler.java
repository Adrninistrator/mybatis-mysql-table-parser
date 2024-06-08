package com.adrninistrator.mybatismysqltableparser.tokenhandler;

import copy.org.apache.ibatis.parsing.TokenHandler;

/**
 * @author adrninistrator
 * @date 2023/10/5
 * @description: 参数处理父类
 */
public class AbstractParameterTokenHandler implements TokenHandler {

    private final String leftFlag;
    private final String rightFlag;

    public AbstractParameterTokenHandler(String leftFlag, String rightFlag) {
        this.leftFlag = leftFlag;
        this.rightFlag = rightFlag;
    }

    @Override
    public String handleToken(String content) {
        String parameter;
        int index = content.indexOf(",");
        if (index == -1) {
            // 参数中不存在","，直接使用
            parameter = content;
        } else {
            // 参数中存在","，使用前面的部分
            parameter = content.substring(0, index);
        }
        // 需要将前后的空格等去掉
        return leftFlag + parameter.trim() + rightFlag;
    }
}
