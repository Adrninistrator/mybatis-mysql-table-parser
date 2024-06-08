package com.adrninistrator.mybatismysqltableparser.tokenhandler;

import copy.org.apache.ibatis.parsing.TokenHandler;

/**
 * @author adrninistrator
 * @date 2022/12/18
 * @description: 参数处理父类，修改为问号"?"
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
