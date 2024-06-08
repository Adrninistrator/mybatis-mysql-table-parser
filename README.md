[![Maven Central](https://img.shields.io/maven-central/v/com.github.adrninistrator/mybatis-mysql-table-parser.svg)](https://search.maven.org/artifact/com.github.adrninistrator/mybatis-mysql-table-parser/)

[![Apache License 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](https://github.com/Adrninistrator/mybatis-mysql-table-parser/blob/master/LICENSE)

# 1. 说明

当前项目用于解析MyBatis XML文件中sql语句使用的数据库表名，以及where和set子句中的字段名与对应的变量名，支持使用MySQL数据库（或兼容MySQL协议的数据库）的情况。

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
testImplementation 'com.github.adrninistrator:mybatis-mysql-table-parser:0.0.7'
```

- Maven

```xml
<dependency>
  <groupId>com.github.adrninistrator</groupId>
  <artifactId>mybatis-mysql-table-parser</artifactId>
  <version>0.0.7</version>
</dependency>
```

`本项目仅引入了slf4j-api组件，在引入本项目组件的项目中，还需要引入log4j2、logback等日志组件，且保证配置正确，能够在本地正常运行。`

`由于Maven间接依赖的组件版本不会自动使用最大的版本号，因此可能需要在项目中手工指定mybatis-mysql-table-parser依赖组件的版本号，避免因为依赖组件版本不一致导致问题，可通过mybatis-mysql-table-parser的pom文件的dependencies元素查看依赖组件版本`

```
https://repo1.maven.org/maven2/com/github/adrninistrator/mybatis-mysql-table-parser/0.0.7/mybatis-mysql-table-parser-0.0.7.pom
```

# 3. 项目地址

当前项目的代码地址为： [https://github.com/Adrninistrator/mybatis-mysql-table-parser](https://github.com/Adrninistrator/mybatis-mysql-table-parser) 。

# 4. 将表名与字段名的详细信息写入文件

执行以下类的`getDetailInfo()`方法，可以获取指定目录中MyBatis XML中涉及的表名及字段名的详细信息，并写入指定的目录中：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableColumnDetailInfo
```

可参考`TestGetMyBatisMySqlTableColumnDetailInfo`类中的代码，如下所示：

```java
Entry4GetMyBatisMySqlTableColumnDetailInfo entry4GetMyBatisMySqlTableDetailInfo = new Entry4GetMyBatisMySqlTableColumnDetailInfo();
entry4GetMyBatisMySqlTableDetailInfo.getDetailInfo("D:/test/test_dir", ".");
```

## 4.1. result_table_info.md

当前文件中保存的是sql语句中数据库表的详细信息，生成的文件内容使用"\t"分隔

生成文件内容示例如下：

```
# MyBatis-Mapper类名	MyBatis-Mapper方法名	sql语句类型	数据库表名	XML文件路径
tester.sql.dao.InnodbLocksDao	select	select	INNODB_LOCKS	src\main\resources\test\sql\InnodbLocksDaoMapper.xml
tester.sql.dao.TestDao	insert	insert_into	test_balance	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	insertIgnore	insert_ignore_into	test_balance	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	insertLog	insert_into	test_balance_log	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	select	select	test_balance	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	selectList	select	test_balance	src\main\resources\test\sql\TestDaoMapper.xml
```

## result_select_column.md

当前文件中保存的是sql语句的select的字段信息，生成的文件内容使用"\t"分隔

生成文件内容示例如下：

```
# MyBatis-Mapper类名	MyBatis-Mapper方法名	数据库表名	数据库字段名	数据库字段别名	XML文件路径
tester.sql.dao.TestDao	select	test_balance	balance		src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	select	test_balance	id		src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	selectList	test_balance	id		src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	selectMap	test_balance	balance		src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	selectMap	test_balance	id		src\main\resources\test\sql\TestDaoMapper.xml
```

## 4.2. result_where_column.md

当前文件中保存的是sql语句的where子句中的字段信息，生成的文件内容使用"\t"分隔

生成文件内容示例如下：

```
# MyBatis-Mapper类名	MyBatis-Mapper方法名	数据库表名	数据库字段名	数据库字段进行比较的方式	数据库字段用于比较的变量名	数据库字段用于比较的变量的使用方式	XML文件路径
tester.sql.dao.TestDao	select	test_balance	id	=	id	#	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	selectTrim	test_balance	id	>	id_min	#	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	selectTrim	test_balance	id	<	id_max	#	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	select_slave	test_balance	id	=	id	#	src\main\resources\test\sql\TestDaoMapper.xml
tester.sql.dao.TestDao	update	test_balance	id	=	id	#	src\main\resources\test\sql\TestDaoMapper.xml
```

## 4.3. result_set_column.md

当前文件中保存的是sql语句的update set子句中的字段信息，生成的文件内容使用"\t"分隔

生成文件内容示例如下：

```
# MyBatis-Mapper类名	MyBatis-Mapper方法名	数据库表名	数据库字段名	数据库字段赋值的变量名	XML文件路径
tester.sql.dao.TestSelectInsertMapper	updateByPrimaryKey	test_select_insert	create_time	createTime	src\main\resources\test\sql\TestSelectInsertDao.xml
tester.sql.dao.TestSelectInsertMapper	updateByPrimaryKey	test_select_insert	seq	seq	src\main\resources\test\sql\TestSelectInsertDao.xml
tester.sql.dao.TestSelectInsertMapper	updateByPrimaryKey	test_select_insert	status	status	src\main\resources\test\sql\TestSelectInsertDao.xml
tester.sql.dao.TestSelectInsertMapper	updateByPrimaryKey	test_select_insert	update_time	updateTime	src\main\resources\test\sql\TestSelectInsertDao.xml
```

# 5. 将表名及sql语句类型写入文件

执行以下类的`getTableInfo()`方法，可以获取指定目录中MyBatis XML中涉及的表名及对应的sql语句类型，并写入指定文件：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableInfo
```

可参考`TestGetMyBatisMySqlTableInfo`类中的代码，如下所示：

```java
Entry4GetMyBatisMySqlTableInfo entry4GetMyBatisMySqlAllTables = new Entry4GetMyBatisMySqlTableInfo();
entry4GetMyBatisMySqlAllTables.getTableInfo("D:/test/test_dir", "result_table_info.md");
```

生成的文件内容使用"\t"分隔，内容分别为：sql语句类型、相关的数据库表名。

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
entry4GetMyBatisMySqlAllTableName.getTableName("D:/test/test_dir", "result_table_name.md");
```

生成的文件内容为相关的数据库表名。

生成文件内容示例如下：

```
test_table1
test_table2
test_table3
```

# 7. 获取解析结果（在Java代码中使用）

## 7.1. 使用方式

以上的使用方式会将MyBatis XML文件中使用的数据库表名信息写入文件，假如需要在Java代码中获取以上解析结果，可以执行以下类：

```java
com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4ParseMyBatisMySqlTable
```

`parseDirectory()`方法用于解析指定目录中所有MyBatis XML文件中涉及的表名，`parseFile()`方法用于解析指定MyBatis XML文件中涉及的表名。

可参考`TestParseMyBatisMySqlTable`类中的代码，如下所示：

```java
Entry4ParseMyBatisMySqlTable entry4ParseMyBatisMySqlTable = new Entry4ParseMyBatisMySqlTable();
Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = entry4ParseMyBatisMySqlTable.parseDirectory("D:/test/test_dir");
MyBatisMySqlInfo myBatisSqlInfo = entry4ParseMyBatisMySqlTable.parseFile("D:/test/test_dir/test.xml");
```

## 7.2. 结果说明

在Java代码中使用获取解析结果功能时，除了支持获取以上会写入到文件的信息外，还可以获取到未写入到文件中的其他信息

获取结果可参考`com.adrninistrator.mybatis_mysql_table_parser.dto.MyBatisMySqlInfo`类

能够获取的信息如下：

- mapper接口类名
- MySQL的完整sql语句Map
- MySQL的sql语句中的表与字段信息Map
- mapper对应的可能的数据库表名
- Entity类名
- Entity类字段名与对应的数据库字段名Map
- 数据库字段名与对应的entity类字段名Map
