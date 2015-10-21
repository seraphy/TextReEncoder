package jp.seraphyware.textencodechanger.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * FXMLとリソースをクラスの現在位置から自動的にをロードする実装
 * @author seraphy
 */
public class SimpleWindowController extends AbstractWindowController {

    /**
     * コントローラクラスの命名規則による末尾の文字例.
     */
    private static final String controllerSuffix = "Controller";
    
    /**
     * ロガー.
     */
    private static final Logger logger = LoggerFactory.getLogger(SimpleWindowController.class);

    /**
     * Springのコンテキスト.
     */
    @Autowired
    private ApplicationContext appContext;

    /**
     * リソースバンドル
     */
    private ResourceBundle resource;
    
    /**
     * コントローラオブジェクトが初期化されるときにFXMLをロードする.
     */
    @PostConstruct
    @Override
    protected void makeRoot() {
       
        FXMLLoader ldr = new FXMLLoader();

        String resourceName = getClass().getCanonicalName();
        if (resourceName.endsWith(controllerSuffix)) {
            resourceName = resourceName.substring(0,
                    resourceName.length() - controllerSuffix.length());
        }

        resource = ResourceBundle.getBundle(resourceName);
        ldr.setResources(resource);

        ldr.setControllerFactory(cls -> {
            if (cls.equals(getClass())) {
                return this;
            }

            logger.info("get instance from context: cls=" + cls);
            return appContext.getBean(cls);
        });
        
        // クラス名の末尾からControllerを取り除く
        String clsName = getClass().getSimpleName();
        if (clsName.endsWith(controllerSuffix)) {
            clsName = clsName.substring(0, clsName.length() - controllerSuffix.length());
        }
        
        URL fxmlUrl = getClass().getResource(clsName + ".fxml");
        assert fxmlUrl != null;

        ldr.setLocation(fxmlUrl);
       
        try {
            Parent parent = ldr.load();
            setRoot(parent);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * 閉じるイベントの処理.
     * @param event 
     */
    @Override
    public void onCloseRequest(WindowEvent event) {
        closeWindow();
    }

    /**
     * ステージを構成する.
     * リソースにタイトルの指定があればタイトルを設定する.
     * @return 
     */
    @Override
    protected Stage createStage() {
        Stage stg = super.createStage();
        if (resource.containsKey("window.title")) {
            stg.setTitle(resource.getString("window.title"));
        }
        return stg;
    }
}
