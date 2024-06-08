/**
 * @author adrninistrator
 * @date 2024/3/3
 * @description:
 */
package com.adrninistrator.mybatismysqltableparser.visitor;

/*
  当前包中的visitor都实现MySqlASTVisitor，不实现SQLASTVisitor
  避免出现以下异常：
  java.lang.ClassCastException: com.adrninistrator.mybatis_mysql_table_parser.visitor.SQLExprTableSourceMultiVisitor
  cannot be cast to com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor
 */