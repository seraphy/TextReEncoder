package jp.seraphyware.textencodechanger;

import java.awt.SplashScreen;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import jp.seraphyware.textencodechanger.ui.MainWndController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
/**
 * アプリケーションのエントリ.
 *
 * @author seraphy
 */
@SpringBootApplication
public class MainApp extends Application {

    /**
     * ロガー.
     */
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    /**
     * Springのコンテキスト.
     */
    private ConfigurableApplicationContext context;

    /**
     * JavaFXアプリケーションの初期化時に呼び出される.
     * @throws Exception 
     */
    @Override
    public void init() throws Exception {
        log.info("★MainApp::init");

        // Springのコンテキストを作成する.
        // JavaFXのlaunchにより、JavaFXスレッド上でコンテキストを構築する.
        String[] args = getParameters().getRaw().toArray(new String[0]);
        context = SpringApplication.run(MainApp.class, args);
    }

    /**
     * JavaFXアプリケーションの開始時に呼び出される.
     *
     * @param stage
     * @throws Exception なんらかの失敗
     */
    @Override
    public void start(final Stage stage) throws Exception {
        log.info("★MainApp::start");

        // メインウィンドウの表示
        MainWndController mainWindow = context.getBean(MainWndController.class);
        Stage stg = mainWindow.getStage();
        stg.show();
        
        // スプラッシュが表示されていれば非表示にする
        SwingUtilities.invokeLater(() -> {
            try {
                SplashScreen splashScreen = SplashScreen.getSplashScreen();
                if (splashScreen != null) {
                    // スプラッシュを閉じる
                    splashScreen.close();
                }
            }
            catch (Exception ex) {
                // スプラッシュ関連のエラーは致命的ではないので継続してよい
                ex.printStackTrace(System.err);
            }
        });
    }

    /**
     * JavaFXアプリケーションの停止時に呼び出される.
     *
     * @throws Exception なんらかの失敗
     */
    @Override
    public void stop() throws Exception {
        log.info("★MainApp::stop");
        // コンテキストを終了する.
        context.close();
    }
}
