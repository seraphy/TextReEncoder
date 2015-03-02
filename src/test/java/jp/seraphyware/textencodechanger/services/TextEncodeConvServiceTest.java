package jp.seraphyware.textencodechanger.services;

import jp.seraphyware.textencodechanger.services.TextEncodeConvService;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TextEncodeConvServiceのTestNGによるテスト
 * 
 * @author seraphy
 */
@ContextConfiguration("file:src/test/java/testApplicationContext.xml")
public class TextEncodeConvServiceTest extends AbstractTestNGSpringContextTests {
    
    @Autowired
    private TextEncodeConvService serivce;
    
    public TextEncodeConvServiceTest() {
    }

    @org.testng.annotations.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.testng.annotations.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.testng.annotations.BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @org.testng.annotations.AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * サポートする文字コード一覧の取得テスト
     * @throws Exception 失敗
     */
    @Test
    public void testGetEncodins() throws Exception {
        serivce.getEncodings().stream().
                map((encoding) -> Charset.forName(encoding)).
                forEach((cs) -> {
                    System.out.println("cs=" + cs);
                });
    }

    /**
     * テキストの文字コード推定のテスト
     * @throws Exception 失敗
     */
    @Test
    public void testPresumeEncoding() throws Exception {
        for (String encoding : serivce.getEncodings()) {
            // UTF-8, csWindows-31J, EUC_JPに変換した場合、
            // それぞれ他方の文字コードでは表現できない文字を含むメッセージ
            String message = "Hello,これは日本語です";

            byte[] data = message.getBytes(encoding);
            ByteBuffer byteBuf = ByteBuffer.wrap(data);
            
            String ret = serivce.presumeEncoding(byteBuf);
            
            Assert.assertEquals(ret, encoding);
        }
    }
}
