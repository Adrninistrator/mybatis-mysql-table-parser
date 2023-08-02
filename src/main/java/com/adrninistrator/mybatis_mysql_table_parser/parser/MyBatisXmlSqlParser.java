package com.adrninistrator.mybatis_mysql_table_parser.parser;

import com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisMySqlInfo;
import com.adrninistrator.mybatis_mysql_table_parser.token_handler.EmptyTokenHandler;
import com.adrninistrator.mybatis_mysql_table_parser.token_handler.QuestionMarkTokenHandler;
import com.adrninistrator.mybatis_mysql_table_parser.xml.NoOpEntityResolver;
import copy.org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author adrninistrator
 * @date 2021/8/25
 * @description: 解析MyBatis的XML文件中的sql语句
 */
public class MyBatisXmlSqlParser {
    private static final Logger logger = LoggerFactory.getLogger(MyBatisXmlSqlParser.class);

    // MySQL只有一个单词的DML语句，需要为小写且以空格结尾
    private static final String[] MYSQL_DML_SINGLE_STATEMENT = new String[]{
            "select ",
            "update ",
            "delete "
    };

    // MySQL有多个单词的DML或DDL语句，需要为小写且以空格结尾
    private static final String[] MYSQL_DML_DDL_MULTI_STATEMENT = new String[]{
            "insert into ",
            "insert ignore into ",
            "replace insert ",
            "alter table ",
            "truncate table ",
            "create table ",
            "drop table ",
    };

    /*
        sql用于对字段操作的语句
        前面是对字段判断的语句，最后是查询字段时使用逗号进行分隔
     */
    private static final String[] SQL_COLUMN_OPERATE_STATEMENT = new String[]{
            "=",
            "<",
            ">",
            "!",
            "not ",
            "in (",
            "in(",
            "like ",
            "like'",
            ","
    };

    private static final String[] SQL_AND_OR = new String[]{
            "and ",
            "or "
    };

    private final SAXBuilder saxBuilder;

    private final GenericTokenParser hashtagValueParser;
    private final GenericTokenParser dollarValueParser;
    private final GenericTokenParser commentMultiLineParser;
    private final GenericTokenParser commentSingleLine1Parser;
    private final GenericTokenParser commentSingleLine2Parser;

    public MyBatisXmlSqlParser() {
        saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // 不读取DTD
        saxBuilder.setEntityResolver(new NoOpEntityResolver());

        // 将#{}替换为?
        hashtagValueParser = new GenericTokenParser("#{", "}", QuestionMarkTokenHandler.getInstance());
        // 将${}替换为?
        dollarValueParser = new GenericTokenParser("${", "}", QuestionMarkTokenHandler.getInstance());
        // 将sql语句中的多行注释替换掉
        commentMultiLineParser = new GenericTokenParser("/*", "*/", EmptyTokenHandler.getInstance());
        // 将sql语句中的单行注释替换掉，以/r结束
        commentSingleLine1Parser = new GenericTokenParser("-- ", "\r", EmptyTokenHandler.getInstance());
        // 将sql语句中的单行注释替换掉，以/n结束
        commentSingleLine2Parser = new GenericTokenParser("-- ", "\n", EmptyTokenHandler.getInstance());
    }

    /**
     * 解析MyBatis的XML文件中的sql语句
     *
     * @param inputStream
     * @param filePath
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public MyBatisMySqlInfo parseMybatisXmlSql(InputStream inputStream, String filePath) throws IOException, JDOMException {
        Document document = saxBuilder.build(inputStream);

        Element root = document.getRootElement();
        if (!"mapper".equals(root.getName())) {
            logger.info("跳过非Mybatis XML 1: {}", filePath);
            return null;
        }

        String namespace = root.getAttributeValue("namespace");
        if (StringUtils.isBlank(namespace)) {
            logger.info("跳过非Mybatis XML 2: {}", filePath);
            return null;
        }

        // 以上用于跳过非Mybatis mapper的XML文件
        logger.info("开始处理Mybatis XML: {}", filePath);

        Map<String, String> sqlElementMap = new HashMap<>();

        // 对MyBatis XML文件进行预处理
        preHandleMybatisXml(root, sqlElementMap);

        // 获取MyBatis XML文件中的sql语句
        return handleMybatisXml(root, namespace, sqlElementMap);
    }

    // 对MyBatis XML文件进行预处理
    private void preHandleMybatisXml(Element root, Map<String, String> sqlElementMap) {
        // 获取sql元素的sql语句
        for (Element element : root.getChildren()) {
            try {
                if (!"sql".equals(element.getName())) {
                    continue;
                }
                String id = element.getAttributeValue("id");
                List<String> sqlFragmentList = new ArrayList<>();
                // 获取sql元素的content中的sql语句
                getSqlFromElementContent(element, sqlFragmentList, sqlElementMap);

                // 将sql片段列表拼接为一条完整sql语句
                List<String> fullSqlList = appendSqlFragment(sqlFragmentList, true);
                if (fullSqlList.isEmpty()) {
                    logger.error("未获取sql元素的sql语句 {}", id);
                    continue;
                }
                sqlElementMap.put(id, fullSqlList.get(0));
            } catch (Exception e) {
                logger.error("解析sql语句出现异常 ", e);
            }
        }
    }

    // 获取MyBatis XML文件中的sql语句
    private MyBatisMySqlInfo handleMybatisXml(Element root, String namespace, Map<String, String> sqlElementMap) {
        Map<String, List<String>> sqlMap = new HashMap<>();
        MyBatisMySqlInfo myBatisSqlInfo = new MyBatisMySqlInfo();
        myBatisSqlInfo.setMapperInterfaceName(namespace);
        myBatisSqlInfo.setFullSqlMap(sqlMap);

        boolean resultMapHandled = false;
        for (Element element : root.getChildren()) {
            String elementName = element.getName();
            if ("resultMap".equals(elementName) && !resultMapHandled) {
                // 处理resultMap元素，不判断resultMap元素的id，因为存在id非BaseResultMap的情况
                handleResultMapElement(myBatisSqlInfo, element);
                resultMapHandled = true;
            } else if (StringUtils.equalsAny(elementName, "select", "insert", "update", "delete")) {
                try {
                    // 处理sql语句
                    String sqlId = element.getAttributeValue("id");
                    List<String> sqlFragmentList = new ArrayList<>();

                    // 处理selectKey元素，order=BEFORE
                    boolean existsSelectKey = handleSelectKeyElement(element, sqlFragmentList, sqlElementMap, true);

                    // 获取sql元素的content中的sql语句
                    getSqlFromElementContent(element, sqlFragmentList, sqlElementMap);

                    if (existsSelectKey) {
                        // 处理selectKey元素，order=AFTER
                        handleSelectKeyElement(element, sqlFragmentList, sqlElementMap, false);
                    }

                    // 将sql片段列表拼接为多条完整sql语句
                    List<String> fullSqlList = appendSqlFragment(sqlFragmentList, false);
                    sqlMap.put(sqlId, fullSqlList);
                } catch (Exception e) {
                    logger.error("解析sql语句出现异常 ", e);
                }
            }
        }
        return myBatisSqlInfo;
    }

    // 处理resultMap元素
    private void handleResultMapElement(MyBatisMySqlInfo myBatisSqlInfo, Element element) {
        // 记录对应的Entity类名
        myBatisSqlInfo.setEntityClassName(element.getAttributeValue("type"));

        Map<String, String> entityAndTableColumnNameMap = new HashMap<>();
        Map<String, String> tableAndEntityColumnNameMap = new HashMap<>();
        myBatisSqlInfo.setEntityAndTableColumnNameMap(entityAndTableColumnNameMap);
        myBatisSqlInfo.setTableAndEntityColumnNameMap(tableAndEntityColumnNameMap);
        for (Element childElement : element.getChildren()) {
            if (!StringUtils.equalsAny(childElement.getName(), "id", "result")) {
                continue;
            }
            // 处理id、result元素
            String column = childElement.getAttributeValue("column");
            String property = childElement.getAttributeValue("property");
            if (StringUtils.isNoneBlank(column, property)) {
                // 记录Entity类字段名与对应的数据库表字段名
                entityAndTableColumnNameMap.put(property, column);

                // 记录数据库表字段名与对应的entity类字段名
                tableAndEntityColumnNameMap.put(column, property);
            }
        }
    }

    // 获取sql元素的content中的sql语句
    private void getSqlFromElementContent(Element currentElement, List<String> sqlFragmentList, Map<String, String> sqlElementMap) {
        for (Content content : currentElement.getContent()) {
            if (content instanceof Text) {
                // 处理一个sql元素中的文本，包含CDATA类型的处理
                Text text = (Text) content;
                // 向sql片段列表添加sql语句，文本
                addSqlStatement(sqlFragmentList, text.getText());
                continue;
            }

            if (content instanceof Element) {
                // 处理一个sql元素中的Element
                Element element = (Element) content;
                String elementName = element.getName();

                if (StringUtils.equalsAny(elementName, "foreach")) {
                    // 处理foreach元素
                    handleForeachElement(sqlFragmentList, element, sqlElementMap);
                    continue;
                }

                if (StringUtils.equalsAny(elementName, "if", "choose", "when", "otherwise")) {
                    // 获取sql元素的content中的sql语句
                    getSqlFromElementContent(element, sqlFragmentList, sqlElementMap);
                    continue;
                }

                if (StringUtils.equals(elementName, "where")) {
                    // 处理where元素
                    handleWhereElement(sqlFragmentList, elementName, element, sqlElementMap);
                    continue;
                }

                if (StringUtils.equals(elementName, "set")) {
                    // 向sql片段列表添加sql语句，set
                    addSqlStatement(sqlFragmentList, elementName);
                    // 获取sql元素的content中的sql语句
                    getSqlFromElementContent(element, sqlFragmentList, sqlElementMap);
                    // 处理set元素最后的逗号
                    handleLastComma4SetElement(sqlFragmentList);
                    continue;
                }

                if (StringUtils.equals(elementName, "include")) {
                    // 处理include元素
                    handleIncludeElement(sqlFragmentList, element, sqlElementMap);
                    continue;
                }

                if (StringUtils.equals(elementName, "trim")) {
                    // 处理trim元素
                    handleTrimElement(element, sqlFragmentList, sqlElementMap);
                    continue;
                }

                if (!StringUtils.equalsAny(elementName, "selectKey", "bind")) {
                    // 在这里不处理selectKey、bind
                    logger.error("暂未处理的MyBatis类型 {}", elementName);
                    continue;
                }
                continue;
            }

            if (!(content instanceof Comment)) {
                // 不处理注释
                logger.error("暂未处理的XML类型 {}", content.getClass().getName());
            }
        }
    }

    /**
     * 处理selectKey元素
     *
     * @param currentElement
     * @param sqlFragmentList
     * @param sqlElementMap
     * @param before          true: 处理order为BEFORE的情况 false: 处理order为AFTER的情况
     * @return true: 有处理过selectKey元素 false: 未处理过selectKey元素
     */
    private boolean handleSelectKeyElement(Element currentElement, List<String> sqlFragmentList, Map<String, String> sqlElementMap, boolean before) {
        int selectKeyNum = 0;
        for (Content content : currentElement.getContent()) {
            if (!(content instanceof Element)) {
                continue;
            }
            Element element = (Element) content;
            String elementName = element.getName();
            if (!"selectKey".equals(elementName)) {
                continue;
            }

            // 处理selectKey元素
            selectKeyNum++;

            String order = element.getAttributeValue("order");
            if ((before && !"BEFORE".equals(order)) || (!before && !"AFTER".equals(order))) {
                // 当前的selectKey元素的order与需要处理的不同，不处理
                continue;
            }

            List<String> selectKeySqlFragmentList = new ArrayList<>();
            handleTrimElement(element, selectKeySqlFragmentList, sqlElementMap);
            if (selectKeySqlFragmentList.isEmpty()) {
                continue;
            }

            if (before) {
                // 在其他sql之前执行
                sqlFragmentList.addAll(0, selectKeySqlFragmentList);
                continue;
            }
            // 在其他sql之后执行
            sqlFragmentList.addAll(selectKeySqlFragmentList);
        }

        return selectKeyNum > 0;
    }

    // 处理trim元素
    private void handleTrimElement(Element element, List<String> sqlFragmentList, Map<String, String> sqlElementMap) {
        // 处理前缀
        String prefix = element.getAttributeValue("prefix");
        if (prefix != null) {
            // 向sql片段列表添加sql语句，trim的prefix
            addSqlStatement(sqlFragmentList, prefix);
        }

        String prefixOverrides = element.getAttributeValue("prefixOverrides");
        String suffixOverrides = element.getAttributeValue("suffixOverrides");

        // 创建trim元素对应的sql片段列表
        List<String> trimSqlFragmentList = new ArrayList<>();

        // 获取trim元素的content中的sql语句
        getSqlFromElementContent(element, trimSqlFragmentList, sqlElementMap);

        if (!trimSqlFragmentList.isEmpty()) {
            // 处理需要删除的前缀
            if (StringUtils.isNotEmpty(prefixOverrides)) {
                // prefixOverrides可以通过"|"指定多个，需要逐个处理
                List<String> prefixOverrideList = parseOverrides(prefixOverrides);
                for (String currentPrefixOverride : prefixOverrideList) {
                    // 以下需要对sql进行格式化，否则sql语句前面可能带有\r\n\t等字符
                    String firstSql = formatSql(trimSqlFragmentList.get(0));
                    if (StringUtils.startsWithIgnoreCase(firstSql, currentPrefixOverride)) {
                        trimSqlFragmentList.set(0, firstSql.substring(currentPrefixOverride.length()));
                        break;
                    }
                }
            }

            // 处理需要删除的后缀
            if (StringUtils.isNotEmpty(suffixOverrides)) {
                int lastIndex = trimSqlFragmentList.size() - 1;
                // suffixOverrides可以通过"|"指定多个，需要逐个处理
                List<String> suffixOverrideList = parseOverrides(suffixOverrides);
                for (String currentSuffixOverride : suffixOverrideList) {
                    // 以下需要对sql进行格式化，否则sql语句前面可能带有\r\n\t等字符
                    String lastSql = formatSql(trimSqlFragmentList.get(lastIndex));
                    if (StringUtils.endsWithIgnoreCase(lastSql, currentSuffixOverride)) {
                        trimSqlFragmentList.set(lastIndex, lastSql.substring(0, lastSql.length() - currentSuffixOverride.length()));
                        break;
                    }
                }
            }
        }

        sqlFragmentList.addAll(trimSqlFragmentList);

        // 处理后缀
        String suffix = element.getAttributeValue("suffix");
        if (suffix != null) {
            // 向sql片段列表添加sql语句，trim的suffix
            addSqlStatement(sqlFragmentList, suffix);
        }
    }

    /**
     * mybatis.jar
     * org.apache.ibatis.scripting.xmltags.TrimSqlNode
     * parseOverrides()
     * 删除了toUpperCase的处理，在比较时忽略大小写
     *
     * @param overrides
     * @return
     */
    private List<String> parseOverrides(String overrides) {
        if (overrides != null) {
            final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
            final List<String> list = new ArrayList<>(parser.countTokens());
            while (parser.hasMoreTokens()) {
                list.add(parser.nextToken());
            }
            return list;
        }
        return Collections.emptyList();
    }

    // 处理set元素最后的逗号
    private void handleLastComma4SetElement(List<String> sqlFragmentList) {
        if (sqlFragmentList.isEmpty()) {
            return;
        }
        // MyBatis的<set>元素会自动删除最后的半角逗号,，因此需要处理
        int lastIndex = sqlFragmentList.size() - 1;
        // 替换sql语句中的标志
        String lastSql = replaceFlagInSql(sqlFragmentList.get(lastIndex));
        if (lastSql.endsWith(",")) {
            sqlFragmentList.set(lastIndex, lastSql.substring(0, lastSql.length() - 1));
        }
    }

    // 处理foreach元素
    private void handleForeachElement(List<String> sqlFragmentList, Element element, Map<String, String> sqlElementMap) {
        // 处理前缀
        String open = element.getAttributeValue("open");
        if (open != null) {
            // 向sql片段列表添加sql语句，foreach的open
            addSqlStatement(sqlFragmentList, open);
        }

        // 获取foreach元素的content中的sql语句
        getSqlFromElementContent(element, sqlFragmentList, sqlElementMap);

        // 处理后缀
        String close = element.getAttributeValue("close");
        if (close != null) {
            // 向sql片段列表添加sql语句，foreach的close
            addSqlStatement(sqlFragmentList, close);
        }
    }

    // 处理where元素
    private void handleWhereElement(List<String> sqlFragmentList, String elementName, Element element, Map<String, String> sqlElementMap) {
        // 向sql片段列表添加sql语句，where
        addSqlStatement(sqlFragmentList, elementName);

        List<String> whereSqlFragmentList = new ArrayList<>();
        // 获取where元素的content中的sql语句
        getSqlFromElementContent(element, whereSqlFragmentList, sqlElementMap);
        if (!whereSqlFragmentList.isEmpty()) {
            // MyBatis会删除where元素中开头的and或or，这里需要处理
            // 替换sql语句中的标志
            String firstSqlFragment = replaceFlagInSql(whereSqlFragmentList.get(0));
            for (String andOrOr : SQL_AND_OR) {
                if (StringUtils.startsWithIgnoreCase(firstSqlFragment, andOrOr)) {
                    whereSqlFragmentList.set(0, firstSqlFragment.substring(andOrOr.length()));
                }
            }

            sqlFragmentList.addAll(whereSqlFragmentList);
        }
    }

    // 处理include元素
    private void handleIncludeElement(List<String> sqlFragmentList, Element element, Map<String, String> sqlElementMap) {
        String sqlValue = sqlElementMap.get(element.getAttributeValue("refid"));
        if (sqlValue != null) {
            sqlFragmentList.add(sqlValue);
        }
    }

    // 向sql片段列表添加sql语句
    private void addSqlStatement(List<String> sqlFragmentList, String sqlStatement) {
        if (sqlStatement.trim().isEmpty()) {
            // 避免添加全空的sql语句
            return;
        }
        sqlFragmentList.add(sqlStatement);
    }

    /**
     * 将sql片段列表拼接为完整sql语句
     *
     * @param sqlFragmentList sql片段列表
     * @param oneSql          true: 拼接为一条sql语句 false: 拼接为多条sql语句
     * @return
     */
    private List<String> appendSqlFragment(List<String> sqlFragmentList, boolean oneSql) {
        List<String> fullSqlList = new ArrayList<>();
        boolean first = true;
        StringBuilder fullSql = new StringBuilder();
        String lastSqlFragment = "";
        for (int i = 0; i < sqlFragmentList.size(); i++) {
            // 对sql语句进行格式化
            String formattedSqlFragment = formatSql(sqlFragmentList.get(i));
            // 需要拼接为多条sql语句，且当前已拼接过sql语句时，判断当前已格式化过的sql语句片段是否为新的一条sql语句
            if (!oneSql && fullSql.length() > 0 && isAnotherSql(formattedSqlFragment)) {
                // 找到下一条新的sql语句
                fullSqlList.add(fullSql.toString());
                first = true;
                fullSql = new StringBuilder();
                lastSqlFragment = "";
            }

            if (!first && !lastSqlFragment.endsWith(" ") && !formattedSqlFragment.startsWith(" ")) {
                // 假如需要，则在sql片段前面增加空格
                formattedSqlFragment = " " + formattedSqlFragment;
            }

            fullSql.append(formattedSqlFragment);
            lastSqlFragment = formattedSqlFragment;
            first = false;
        }

        if (fullSql.length() > 0) {
            fullSqlList.add(fullSql.toString());
        }
        return fullSqlList;
    }

    // 对sql语句进行格式化
    private String formatSql(String sql) {
        String sql1 = hashtagValueParser.parse(sql);
        String sql2 = dollarValueParser.parse(sql1);
        String sql3 = commentMultiLineParser.parse(sql2);
        String sql4 = commentSingleLine1Parser.parse(sql3);
        String sql5 = commentSingleLine2Parser.parse(sql4);
        // 替换sql语句中的标志
        return replaceFlagInSql(sql5);
    }

    // 替换sql语句中的标志
    private String replaceFlagInSql(String sql) {
        return sql.replaceAll("[\r\n\t]", " ")
                .replaceAll("[ ][ ]*", " ").trim();
    }

    /**
     * 判断当前已格式化过的sql语句片段是否为新的一条sql语句
     *
     * @param formattedSqlFragment
     * @return
     */
    private boolean isAnotherSql(String formattedSqlFragment) {
        String lowerFormattedSql = formattedSqlFragment.toLowerCase(Locale.ROOT);
        // 判断MySQL只有一个单词的DML语句
        for (String mysqlDmlStatement : MYSQL_DML_SINGLE_STATEMENT) {
            if (!lowerFormattedSql.startsWith(mysqlDmlStatement)) {
                continue;
            }

            // 当前sql语句片段以dml语句开头
            boolean ignore = false;
            for (String sqlColumnOperateStatement : SQL_COLUMN_OPERATE_STATEMENT) {
                if (lowerFormattedSql.startsWith(mysqlDmlStatement + sqlColumnOperateStatement)) {
                    // 当前sql语句片段以dml语句开头，但是是对字段的操作，不是新的sql语句
                    ignore = true;
                    break;
                }
            }
            if (ignore) {
                continue;
            }
            // 当前sql语句片段是新的sql语句
            return true;
        }

        // 判断MySQL有多个单词的DML或DDL语句
        for (String mysqlDmlStatement : MYSQL_DML_DDL_MULTI_STATEMENT) {
            if (lowerFormattedSql.startsWith(mysqlDmlStatement)) {
                // 当前sql语句片段是新的sql语句
                return true;
            }
        }

        // 当前sql语句片段不是新的sql语句
        return false;
    }
}
