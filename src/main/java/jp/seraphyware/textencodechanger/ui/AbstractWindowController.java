package jp.seraphyware.textencodechanger.ui;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * FXMLをロードしてステージを構成するコントローラクラスの抽象実装.
 *
 * @author seraphy
 */
public abstract class AbstractWindowController
    extends AbstractFXMLController {

    /**
     * 親ウィンドウ
     */
    private Window owner;

    /**
     * サイズをシーンに合わせるか？
     */
    private boolean sizeToScene = true;

    /**
     * このコントローラに関連づけられているステージ
     */
    private Stage stage;

    /**
     * シーン
     */
    private Scene scene;

    /**
     * クローズイベントに対するハンドラ
     */
    private final EventHandler<WindowEvent> closeRequestHandler = event -> {
        onCloseRequest(event);
        event.consume();
    };

    /**
     * デフォルトコンストラクタ
     */
    protected AbstractWindowController() {
        this(null, true);
    }

    /**
     * コンストラクタ
     * @param owner
     * @param sizeToScene 
     */
    protected AbstractWindowController(Window owner, boolean sizeToScene) {
        this.owner = owner;
        this.sizeToScene = sizeToScene;
    }

    /**
     * クローズイベントの処理
     * @param event 
     */
    public abstract void onCloseRequest(WindowEvent event);

    /**
     * 親ウィンドウを設定する.
     * @param owner 
     */
    public void setOwner(Window owner) {
        this.owner = owner;
    }

    /**
     * 親ウィンドウを取得する.
     * @return 
     */
    public Window getOwner() {
        return owner;
    }

    /**
     * シーンのサイズに合わせるか設定する.
     * @param sizeToScene 
     */
    public void setSizeToScene(boolean sizeToScene) {
        this.sizeToScene = sizeToScene;
    }

    /**
     * シーンのサイズに合わせるか？.
     * @return 
     */
    public boolean isSizeToScene() {
        return sizeToScene;
    }

    /**
     * ステージを設定する.
     * @param stage 
     */
    public void setStage(Stage stage) {
        if (this.stage != null) {
            throw new IllegalStateException();
        }
        this.stage = stage;
    }

    /**
     * ステージを取得する.
     * まだなければ作成する.
     * @return 
     */
    public Stage getStage() {
        if (stage == null) {
            stage = createStage();
            stage.setScene(getScene());

            if (sizeToScene) {
                // 初回のみ
                stage.sizeToScene();
            }
        }
        return stage;
    }

    /**
     * ステージを作成する.
     * @return 
     */
    protected Stage createStage() {
        Stage stg = new Stage();
        stg.initOwner(owner);
        stg.setOnCloseRequest(closeRequestHandler);

        // アイコンの設定 (最適なサイズが選択される)
        // (AbstractWindowController.classと同じパッケージ上からアイコンを取得)
        Class<?> cls = AbstractWindowController.class;
        stg.getIcons().addAll(Arrays.asList(
                new Image(cls.getResourceAsStream("circular32.png")),
                new Image(cls.getResourceAsStream("circular64.png"))));

        return stg;
    }

    /**
     * シーンを取得する.
     * シーンがまだなければ作成する.
     * @return 
     */
    public Scene getScene() {
        assert Platform.isFxApplicationThread();

        if (scene == null) {
            scene = new Scene(getRoot());
        }

        return scene;
    }

    /**
     * ステージを表示する.
     */
    public void openWindow() {
        assert Platform.isFxApplicationThread();

        getStage().show();
        getStage().toFront();
    }

    /**
     * ステージを閉じる.
     */
    public void closeWindow() {
        assert Platform.isFxApplicationThread();
        if (stage != null) {
            // ステージがあれば閉じる(なければ何もしない)
            stage.close();
        }
    }
}
