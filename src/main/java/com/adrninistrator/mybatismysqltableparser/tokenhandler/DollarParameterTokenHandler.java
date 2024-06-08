package com.adrninistrator.mybatismysqltableparser.tokenhandler;

/**
 * @author adrninistrator
 * @date 2023/10/5
 * @description: 参数处理父类，使用${}
 */
public class DollarParameterTokenHandler extends AbstractParameterTokenHandler {

    public static final String DOLLAR = "$";
    public static final String LEFT_FLAG = DOLLAR + "{";
    public static final String RIGHT_FLAG = "}";

    private static final DollarParameterTokenHandler instance = new DollarParameterTokenHandler();

    public static DollarParameterTokenHandler getInstance() {
        return instance;
    }

    public DollarParameterTokenHandler() {
        super(LEFT_FLAG, RIGHT_FLAG);
    }
}
