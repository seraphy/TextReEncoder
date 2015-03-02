package jp.seraphyware.textencodechanger.services;

import jp.seraphyware.textencodechanger.services.FileWalkService;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import jp.seraphyware.textencodechanger.services.FileWalkService.FileNameMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * FileWalkServiceのTestNGによるテスト
 * 
 * @author seraphy
 */
@ContextConfiguration("file:src/test/java/testApplicationContext.xml")
public class FileWalkServiceTest extends AbstractTestNGSpringContextTests {
    
    @Autowired
    private FileWalkService service;
    
    public FileWalkServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
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
