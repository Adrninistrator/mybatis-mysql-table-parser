package com.adrninistrator.mybatismysqltableparser.tokenhandler;

/**
 * @author adrninistrator
 * @date 2023/10/5
 * @description: 参数处理父类，使用#{}
 */
public class HashtagParameterTokenHandler extends AbstractParameterTokenHandler {

    public static final String HASHTAG = "#";
    public static final String LEFT_FLAG = HASHTAG + "{";
    public static final String RIGHT_FLAG = "}";

    private static final HashtagParameterTokenHandler instance = new HashtagParameterTokenHandler();

    public static HashtagParameterTokenHandler getInstance() {
        return instance;
    }

    public HashtagParameterTokenHandler() {
        super(LEFT_FLAG, RIGHT_FLAG);
    }
}
