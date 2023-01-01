package com.adrninistrator.mybatis_mysql_table_parser.token_handler;

import copy.org.apache.ibatis.parsing.TokenHandler;

/**
 * @author adrninistrator
 * @date 2022/12/18
 * @description:
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
