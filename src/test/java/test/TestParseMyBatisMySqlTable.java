package test;

import com.adrninistrator.mybatismysqltableparser.dto.MyBatisMySqlInfo;
import com.adrninistrator.mybatismysqltableparser.entry.Entry4ParseMyBatisMySqlTable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author adrninistrator
 * @date 2022/12/31
 * @description:
 */
public class TestParseMyBatisMySqlTable {
    private static final Logger logger = LoggerFactory.getLogger(TestParseMyBatisMySqlTable.class);

    @Test
    public void test() {
        Entry4ParseMyBatisMySqlTable entry4ParseMyBatisMySqlTable = new Entry4ParseMyBatisMySqlTable();
        Map<String, MyBatisMySqlInfo> myBatisSqlInfoMap = entry4ParseMyBatisMySqlTable.parseDirectory("D:/test/test_dir");
        MyBatisMySqlInfo myBatisSqlInfo = entry4ParseMyBatisMySqlTable.parseFile("D:/test/test_dir/test.xml");

        logger.info("myBatisSqlInfoMap.size(): {}", myBatisSqlInfoMap.size());
        logger.info("myBatisSqlInfo.getMapperInterfaceName(): {}", myBatisSqlInfo.getMapperInterfaceName());
    }
}
