package jp.seraphyware.textencodechanger.services;

import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import jp.seraphyware.textencodechanger.UnitTestConfiguration;
import jp.seraphyware.textencodechanger.services.FileWalkService.FileNameMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * FileWalkServiceのTestNGによるテスト
 * 
 * @author seraphy
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UnitTestConfiguration.class})
public class FileWalkServiceTest {
    
    @Autowired
    private FileWalkService service;
    
    /**
     * ファイルパターンのテスト
     */
    @Test
    public void testMakePatterns() {
        Assert.assertTrue(service.makePatterns("").isEmpty());
        Assert.assertTrue(service.makePatterns(" ").isEmpty());
        Assert.assertTrue(service.makePatterns(";").isEmpty());
        Assert.assertTrue(service.makePatterns(" ;  ").isEmpty());
        Assert.assertTrue(service.makePatterns(" ;  ;   ").isEmpty());

        // リテラル
        {
            List<Pattern> pattern1 = service.makePatterns("ABC");
            Assert.assertEquals(pattern1.size(), 1);
            Assert.assertEquals(pattern1.get(0).pattern(), "^ABC$");
        }
        
        // ワイルドカード(*)
        {
            List<Pattern> pattern1 = service.makePatterns("abc*.txt");
            Assert.assertEquals(pattern1.size(), 1);
            Assert.assertEquals(pattern1.get(0).pattern(), "^abc.*\\.txt$");
        }

        // ワイルドカード(?)
        {
            List<Pattern> pattern1 = service.makePatterns("a?b");
            Assert.assertEquals(pattern1.size(), 1);
            Assert.assertEquals(pattern1.get(0).pattern(), "^a.b$");
        }
    }
    
    /**
     * ファイル名マッチングのテスト
     */
    @Test
    public void testCreateFileNameMatcher() {
        List<Pattern> pattern1 = service.makePatterns("AB*;x?z");
        FileNameMatcher matcher = service.createFileNameMatcher(pattern1);
        
        Assert.assertTrue(!matcher.match(Paths.get("")));
        Assert.assertTrue(!matcher.match(Paths.get("123")));
        Assert.assertTrue(!matcher.match(Paths.get("1AB")));
        Assert.assertTrue(!matcher.match(Paths.get("x_z_")));
    
        Assert.assertTrue(matcher.match(Paths.get("AB")));
        Assert.assertTrue(matcher.match(Paths.get("ABCDE")));
        Assert.assertTrue(matcher.match(Paths.get("ab")));
        Assert.assertTrue(matcher.match(Paths.get("abcde")));

        Assert.assertTrue(matcher.match(Paths.get("xyz")));
        Assert.assertTrue(matcher.match(Paths.get("XYZ")));
        Assert.assertTrue(matcher.match(Paths.get("X@z")));
    }
}
