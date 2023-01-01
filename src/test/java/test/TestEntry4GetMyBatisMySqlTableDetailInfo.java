package test;

import com.adrninistrator.mybatis_mysql_table_parser.entry.Entry4GetMyBatisMySqlTableDetailInfo;
import org.junit.Test;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description:
 */
public class TestEntry4GetMyBatisMySqlTableDetailInfo {
    @Test
    public void test() {
        Entry4GetMyBatisMySqlTableDetailInfo entry4GetMyBatisMySqlTableDetailInfo = new Entry4GetMyBatisMySqlTableDetailInfo();
        entry4GetMyBatisMySqlTableDetailInfo.getDetailInfo("D:/test/test_dir", "result_table_detail_info.txt");
    }
}
