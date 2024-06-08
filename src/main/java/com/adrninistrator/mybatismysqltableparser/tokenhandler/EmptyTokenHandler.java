package com.adrninistrator.mybatismysqltableparser.tokenhandler;

import copy.org.apache.ibatis.parsing.TokenHandler;

/**
 * @author adrninistrator
 * @date 2022/12/18
 * @description: 生成空字符串的处理类
 */
public class EmptyTokenHandler implements TokenHandler {
    private static final EmptyTokenHandler instance = new EmptyTokenHandler();

    public static EmptyTokenHandler getInstance() {
        return instance;
    }

    @Override
    public String handleToken(String content) {
        return "";
    }
}
