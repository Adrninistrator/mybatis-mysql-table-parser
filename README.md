[![Maven Central](https://img.shields.io/maven-central/v/com.github.adrninistrator/mybatis-mysql-table-parser.svg)](https://search.maven.org/artifact/com.github.adrninistrator/mybatis-mysql-table-parser/)

[![Apache License 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](https://github.com/Adrninistrator/mybatis-mysql-table-parser/blob/master/LICENSE)

# 1. 说明

当前项目用于解析MyBatis XML文件中sql语句使用的数据库表名，支持使用MySQL数据库（或兼容MySQL协议的数据库）的情况。

支持获取的sql语句类型为常见的DML及DDL语句，如下所示：

```
select
select for update
insert into
insert ignore into
insert into on duplicate key update
replace into
update
delete
alter table
truncate table
create table
drop table
```

# 2. 添加依赖

假如以组件的形式使用当前项目，首先需要在对应的项目引入本项目组件的依赖：

- Gradle

```
testImplementation 'com.github.adrninistrator:mybatis-mysql-table-parser:0.0.5'
```

- Maven

```xml
<dependency>
  <groupId>com.github.adrninistrator</groupId>
  <artifactId>mybatis-mysql-table-parser</artifactId>
  <version>0.0.5</version>
</dependency>
```

`本项目仅引入了slf4j-api组件，在引入本项目组件的项目中，还需要引入log4j2、logback等日志组件，且保证配置正确，能够在本地正常运行。`

`由于Maven间接依赖的组件版本不会自动使用最大的版本号，因此可能需要在项目中手工指定mybatis-mysql-table-parser依赖组件的版本号，避免因为依赖组件版本不一致导致问题，可通过mybatis-mysql-table-parser的pom文件的dependencies元素查看依赖组件版本`

```
https://repo1.maven.org/maven2/com/github/adrninistrator/mybatis-mysql-table-parser/0.0.5/mybatis-mysql-table-parser-0.0.5.pom
```

# 3. 项目地址

当前项目的代码地址为： [https://github.com/Adrninistrator/mybatis-mysql-table-parser](https://github.com/Adrninistrator/mybatis-mysql-table-parser) 。

# 4. 将表名的详细信息写入文件

执行以下类的`getDetailInfo()`方法，可以获取指定目录中MyBatis XML中涉及的表名详细信息，并写入指定文件：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableDetailInfo
```

可参考`TestEntry4GetMyBatisMySqlTableDetailInfo`类中的代码，如下所示：

```java
Entry4GetMyBatisMySqlTableDetailInfo entry4GetMyBatisMySqlTableDetailInfo = new Entry4GetMyBatisMySqlTableDetailInfo();
entry4GetMyBatisMySqlTableDetailInfo.getDetailInfo("D:/test/test_dir", "result_table_detail_info.txt");
```

生成的文件内容使用"\t"分隔，内容分别为MyBatis Mapper类名、MyBatis Mapper方法名、sql语句类型、相关的数据库表名。

生成文件内容示例如下：

```
# MyBatis-Mapper类名	MyBatis-Mapper方法名	sql语句类型	表名
tester.sql.dao.TestUpdateLimit1Dao	delete	delete	test_update_limit1
tester.sql.dao.TestUpdateLimit1Dao	insert	insert_into	test_update_limit1
tester.sql.dao.TestUpdateLimit1Dao	select	select	test_update_limit1
tester.sql.dao.TestUpdateLimit1Dao	update	update	test_update_limit1
```

# 5. 将表名及sql语句类型写入文件

执行以下类的`getTableInfo()`方法，可以获取指定目录中MyBatis XML中涉及的表名及对应的sql语句类型，并写入指定文件：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableInfo
```

可参考`TestGetMyBatisMySqlTableInfo`类中的代码，如下所示：

```java
Entry4GetMyBatisMySqlTableInfo entry4GetMyBatisMySqlAllTables = new Entry4GetMyBatisMySqlTableInfo();
entry4GetMyBatisMySqlAllTables.getTableInfo("D:/test/test_dir", "result_table_info.txt");
```

生成的文件内容使用"\t"分隔，内容分别为sql语句类型、相关的数据库表名。

生成文件内容示例如下：

```
# sql语句类型	表名
select	test_table1
select	test_table2
select	test_table3
select_for_update	test_balance
select_for_update	test_balance_log
select_for_update	test_select_insert
insert_into	test_balance
insert_into	test_balance_log
insert_into	test_select_insert
```

# 6. 将表名写入文件

执行以下类的`getTableName()`方法，可以获取指定目录中MyBatis XML中涉及的表名，并写入指定文件：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableName
```

可参考`TestGetMyBatisMySqlTableName`类中的代码，如下所示：

```java
Entry4GetMyBatisMySqlTableName entry4GetMyBatisMySqlAllTableName = new Entry4GetMyBatisMySqlTableName();
entry4GetMyBatisMySqlAllTableName.getTableName("D:/test/test_dir", "result_table_name.txt");
```

生成的文件内容为相关的数据库表名。

生成文件内容示例如下：

```
test_table1
test_table2
test_table3
```

# 7. 获取解析结果（在Java代码中使用）

以上的使用方式会将MyBatis XML文件中使用的数据库表名信息写入文件，假如需要在Java代码中获取以上解析结果，可以执行以下类：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableName
```

`parseDirectory()`方法用于解析指定目录中所有MyBatis XML文件中涉及的表名，`parseFile()`方法用于解析指定MyBatis XML文件中涉及的表名。

可参考`TestParseMyBatisMySqlTable`类中的代码，如下所示：

```java
Entry4ParseMyBatisMySqlTable entry4ParseMyBatisMySqlTable = new Entry4ParseMyBatisMySqlTable();
Map<String, MyBatisSqlInfo> myBatisSqlInfoMap = entry4ParseMyBatisMySqlTable.parseDirectory("D:/test/test_dir");
MyBatisSqlInfo myBatisSqlInfo = entry4ParseMyBatisMySqlTable.parseFile("D:/test/test_dir/test.xml");
```
