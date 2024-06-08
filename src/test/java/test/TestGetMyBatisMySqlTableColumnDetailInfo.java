package test;

import com.adrninistrator.mybatismysqltableparser.entry.Entry4GetMyBatisMySqlTableColumnDetailInfo;
import org.junit.Test;

/**
 * @author adrninistrator
 * @date 2023/1/1
 * @description:
 */
public class TestGetMyBatisMySqlTableColumnDetailInfo {
    @Test
    public void test() {
        Entry4GetMyBatisMySqlTableColumnDetailInfo entry4GetMyBatisMySqlTableDetailInfo = new Entry4GetMyBatisMySqlTableColumnDetailInfo();
        entry4GetMyBatisMySqlTableDetailInfo.getDetailInfo("D:/test/test_dir", ".");
    }
}
