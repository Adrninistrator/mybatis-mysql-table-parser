package test;

import com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableInfo;
import org.junit.Test;

/**
 * @author adrninistrator
 * @date 2022/12/31
 * @description:
 */
public class TestGetMyBatisMySqlTableInfo {
    @Test
    public void test() {
        Entry4GetMyBatisMySqlTableInfo entry4GetMyBatisMySqlAllTables = new Entry4GetMyBatisMySqlTableInfo();
        entry4GetMyBatisMySqlAllTables.getTableInfo("D:/test/test_dir", "result_table_info.txt");
    }
}
