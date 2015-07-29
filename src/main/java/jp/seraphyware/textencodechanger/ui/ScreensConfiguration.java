package jp.seraphyware.textencodechanger.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 画面に関する構成用ビーン.
 *
 * @author seraphy
 */
@Configuration
@Lazy
public class ScreensConfiguration {

    /**
     * ロガー.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * プログレスダイアログの幅(固定値).
     */
    private static final int PROGRESS_WIDTH = 350;

    /**
     * アプリケーションコンテキスト.
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * プライマリステージ.
     */
    private Stage primaryStage;

    /**
     * プライマリステージを設定する.
     *
     * @param primaryStage ステージ
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final void setPrimaryStage(final Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * メインウィンドウを作成して返す.
     *
     * @return メインウィンドウの構成済みステージ
     * @throws IOException ロードに失敗
     */
    @Bean
    @SuppressWarnings("checkstyle:designforextension")
    public Stage mainWindow() throws IOException {
        loadStage(primaryStage, MainWndController.class);
        return primaryStage;
    }


    /**
     * プログレスダイアログを作成するファクトリを返す.
     *
     * @return プログレスダイアログのファクトリ
     */
    @Bean
    @SuppressWarnings("checkstyle:designforextension")
    public ProgressStageFactory progressStageFactory() {
        return bgTask -> createProgressStage(bgTask);
    }

    /**
     * プログレスダイアログを作成する. タスクのタイトル、プログレスインジケータ、メッセージが接続される.
     * キャンセルボタンはタスクへのCancelリクエストと接続される.
     *
     * @param <T> タスクの返却型
     * @param bgTask タスク
     * @return ステージ
     */
    private <T> Stage createProgressStage(final Task<T> bgTask) {
        Stage progStg = new Stage(StageStyle.UTILITY);
        progStg.setWidth(PROGRESS_WIDTH);

        progStg.setTitle("wait...");
        progStg.initModality(Modality.APPLICATION_MODAL);

        Button btnCancel = new Button("cancel");
        btnCancel.setOnAction(p -> {
            log.info("★requestCancel");
            bgTask.cancel();
        });
        btnCancel.setCancelButton(true);
        progStg.setOnCloseRequest(p -> {
            p.consume();
        });

        Label dirLabel = new Label();
        dirLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS); // 真ん中を省略表示
        dirLabel.textProperty().bind(bgTask.messageProperty());

        ProgressIndicator progInd = new ProgressIndicator();
        progInd.progressProperty().bind(bgTask.progressProperty());

        progStg.titleProperty().bind(bgTask.titleProperty());

        VBox box = new VBox();
        Scene progScene = new Scene(box);

        box.getChildren().add(dirLabel);
        box.getChildren().add(progInd);
        box.getChildren().add(btnCancel);
        progStg.setScene(progScene);

        // タスクが完了または失敗またはキャンセルされた場合にダイアログを自動的に閉じる
        bgTask.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, p -> {
            log.info("★successed");
            progStg.close();
        });
        bgTask.addEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, p -> {
            log.info("★failed");
            progStg.close();
        });
        bgTask.addEventFilter(WorkerStateEvent.WORKER_STATE_CANCELLED, p -> {
            log.info("★cancelled");
            progStg.close();
        });

        return progStg;
    }

    /**
     * プレビューダイアログを作成するファクトリ
     * @return * プレビューダイアログを作成するファクトリ
     */
    @Bean
    @SuppressWarnings("checkstyle:designforextension")
    public TextPreviewDialogFactory textPreviewDialogFactory() {
        return (parent, title, text, fileName, encoding) -> 
                textPreviewDialog(parent, title, text, fileName, encoding);
    }

    /**
     * プレビューダイアログ
     * @param parent
     * @param title
     * @param text
     * @param fileName
     * @param encoding
     * @return 
     */
    private Stage textPreviewDialog(
            final Window parent,
            final String title,
            final String text,
            final String fileName,
            final String encoding
    ) {
        try {
            Stage stg = new Stage(StageStyle.UTILITY);
            stg.initOwner(parent);

            TextPreviewController controller
                    = loadStage(stg, TextPreviewController.class);
            stg.setTitle(title);

            controller.textProperty().set(text);
            controller.fileNameProperty().set(fileName);
            controller.encodingProperty().set(encoding);

            stg.setOnCloseRequest(p -> stg.close());

            return stg;

        } catch (IOException iex) {
            throw new UncheckedIOException(iex);
        }
    }
    
    
    /**
     * エラーダイアログを作成するファクトリを返す.
     *
     * @return エラーダイアログのファクトリ
     */
    @Bean
    @SuppressWarnings("checkstyle:designforextension")
    public ErrorDialogFactory errorDialogFactory() {
        return (parent, title, ex) -> errorDialog(parent, title, ex);
    }

    /**
     * エラーダイアログを作成する.
     *
     * @param parent 親
     * @param title タストル
     * @param ex 例外
     * @return ステージ
     */
    private Stage errorDialog(
            final Window parent,
            final String title,
            final Throwable ex
    ) {
        try {
            Stage stg = new Stage(StageStyle.UTILITY);
            stg.initOwner(parent);

            ErrorDialogController controller
                    = loadStage(stg, ErrorDialogController.class);
            stg.setTitle(title);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            controller.textProperty().set(sw.toString());

            stg.setOnCloseRequest(p -> stg.close());

            return stg;

        } catch (IOException iex) {
            throw new UncheckedIOException(iex);
        }
    }


    /**
     * ステージをコントローラクラスを指定して構築する. コントローラクラスのFXMLControllerアノテーションにFXMLおよびリソース名が
     * 指定されていれば、それを用いて、ステージを構成する. そうでなければクラス名からFXMLとリソースファイル名を推定して構成する.
     *
     * @param <T> コントローラのクラス
     * @param stage 構成するステージ
     * @param ctrlCls コントローラのクラス
     * @return コントローラ
     * @throws IOException ロードに失敗
     */
    private <T> T loadStage(
            final Stage stage,
            final Class<T> ctrlCls
    ) throws IOException {
        Objects.requireNonNull(stage);
        Objects.requireNonNull(ctrlCls);

        String fxmlName = null;
        String resourceName = null;
        FXMLController attr = ctrlCls.getAnnotation(FXMLController.class);
        if (attr != null) {
            fxmlName = attr.value();
            resourceName = attr.resource();
        }

        if (fxmlName == null || fxmlName.length() == 0) {
            fxmlName = ctrlCls.getSimpleName() + ".fxml";
        }
        if (resourceName == null || resourceName.length() == 0) {
            resourceName = ctrlCls.getName().replace(".", "/");
        }

        log.info("★loadStage controller= " + ctrlCls
                + "/fxml=" + fxmlName + "/rb=" + resourceName);

        // FXMLはコントローラクラス相対のリソースから検索する.
        URL url = ctrlCls.getResource(fxmlName);
        Objects.requireNonNull(url, "fxml is not found: " + fxmlName);

        ResourceBundle rb = ResourceBundle.getBundle(resourceName);

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(cls -> applicationContext.getBean(cls));
        loader.setResources(rb);

        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        if (rb.containsKey("window.title")) {
            stage.setTitle(rb.getString("window.title"));
        }

        stage.setScene(scene);

        return loader.getController();
    }
}
