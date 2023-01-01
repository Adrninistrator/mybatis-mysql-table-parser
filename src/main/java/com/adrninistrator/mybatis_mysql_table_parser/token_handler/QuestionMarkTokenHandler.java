package com.adrninistrator.mybatis_mysql_table_parser.token_handler;

import copy.org.apache.ibatis.parsing.TokenHandler;

/**
 * @author adrninistrator
 * @date 2022/12/18
 * @description:
 */
public class QuestionMarkTokenHandler implements TokenHandler {
    private static final QuestionMarkTokenHandler instance = new QuestionMarkTokenHandler();

    public static QuestionMarkTokenHandler getInstance() {
        return instance;
    }

    @Override
    public String handleToken(String content) {
        return "?";
    }
}
