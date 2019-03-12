package jp.seraphyware.textencodechanger;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * テスト用のコンテキスト設定
 * @author seraphy
 */
@Configuration
@ImportResource({
    "classpath:testApplicationContext.xml"
})
public class UnitTestConfiguration {
    
}
