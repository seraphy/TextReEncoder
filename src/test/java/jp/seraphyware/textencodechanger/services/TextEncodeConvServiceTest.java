package jp.seraphyware.textencodechanger.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
    
    /**
     * クラス準備
     * @throws Exception 
     */
    @org.testng.annotations.BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * クラスの後始末
     * @throws Exception 
     */
    @org.testng.annotations.AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * テスト準備
     * @throws Exception 
     */
    @org.testng.annotations.BeforeMethod
    public void setUpMethod() throws Exception {
    }

    /**
     * テストの後始末
     * @throws Exception 
     */
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
                map(EncodingType::getCharset).
                forEach(System.out::println);
    }

    /**
     * サポートするテキストの文字コード推定のテスト
     * @throws Exception 失敗
     */
    @Test
    public void testPresumeEncoding() throws Exception {
        check(EncodingType.UTF8, EncodingType.UTF8);
        check(EncodingType.UTF8_BOM, EncodingType.UTF8_BOM);
        check(EncodingType.Windows31J, EncodingType.Windows31J);
        check(EncodingType.EUC_JP, EncodingType.EUC_JP);
        check(EncodingType.UTF16_BOM_LE, EncodingType.UTF16_BOM_LE);
        check(EncodingType.UTF16_BOM_BE, EncodingType.UTF16_BOM_BE);
        
        // UTF-16LE/BEは変換エラーになりにくく、どのようなコードでも受け入れるので
        // 推定に使うのは難しい
    }
    
    /**
     * 文字コードのテストが正しく判定されるか検査する
     * @param encoding
     * @param resultEncodingType
     * @throws IOException 
     */
    private void check(EncodingType encoding,
            EncodingType resultEncodingType) throws IOException {
       
        // UTF-8, csWindows-31J, EUC_JPに変換した場合、
        // それぞれ他方の文字コードでは表現できない文字を含むメッセージ
        String message = "Hello,これは日本語です";

        byte[] data = encoding.encode(CharBuffer.wrap(message)).array();

        ByteBuffer byteBuf = ByteBuffer.wrap(data);

        EncodingType ret = serivce.presumeEncoding(byteBuf);

        Assert.assertEquals(ret, resultEncodingType);
    }
}
