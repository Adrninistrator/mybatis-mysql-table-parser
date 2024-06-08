package test;

import com.adrninistrator.mybatismysqltableparser.entry.Entry4GetMyBatisMySqlTableName;
import org.junit.Test;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description:
 */
public class TestGetMyBatisMySqlTableName {
    @Test
    public void test() {
        Entry4GetMyBatisMySqlTableName entry4GetMyBatisMySqlAllTableName = new Entry4GetMyBatisMySqlTableName();
        entry4GetMyBatisMySqlAllTableName.getTableName("D:/test/test_dir", "result_table_name.md");
    }
}
