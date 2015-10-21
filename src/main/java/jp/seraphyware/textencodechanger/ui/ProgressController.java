package jp.seraphyware.textencodechanger.ui;

import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * プログレスダイアログのコントローラ
 * 
 * @author seraphy
 */
@Component
@Scope("prototype")
public class ProgressController extends SimpleWindowController {

    private static final Logger logger = LoggerFactory.getLogger(ProgressController.class);
    
    @FXML
    private Label txtStatus;
    
    @FXML
    private ProgressIndicator progress;

    /**
     * タイトル.
     * ステージが作成されるまえにジョブとバインドし、作成後に連結するための中間ハブ.
     */
    private final StringProperty titleProperty = new SimpleStringProperty();
    
    /**
     * ジョブ
     */
    private Task<?> bgTask;
    
    /**
     * モーダルダイアログ用のステージを構成する.
     * @return 
     */
    @Override
    protected Stage createStage() {
        Stage stg = super.createStage();
        stg.initModality(Modality.WINDOW_MODAL);
        stg.initStyle(StageStyle.UTILITY);

        stg.titleProperty().bind(titleProperty);
        return stg;
    }

    /**
     * キャンセルボタン押下時.
     */
    @FXML
    protected void onCancel() {
        logger.info("★requestCancel");
        if (bgTask != null) {
            bgTask.cancel();
        }
    }
    
    /**
     * プログレスダイアログとタスクを関連づける.
     * 関連づけることによりタスクの終了によりプログレスウィンドウを閉じるなどの処理ができる.
     * @param bgTask 
     */
    public void connect(Task<?> bgTask) {
        Objects.requireNonNull(bgTask);
        assert this.bgTask == null;
        this.bgTask = bgTask;
        
        titleProperty.bind(bgTask.titleProperty());
        txtStatus.textProperty().bind(bgTask.messageProperty());
        progress.progressProperty().bind(bgTask.progressProperty());
        
        // タスクが完了または失敗またはキャンセルされた場合にダイアログを自動的に閉じる
        bgTask.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, p -> {
            logger.info("★successed");
            closeWindow();
        });
        bgTask.addEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, p -> {
            logger.info("★failed");
            closeWindow();
        });
        bgTask.addEventFilter(WorkerStateEvent.WORKER_STATE_CANCELLED, p -> {
            logger.info("★cancelled");
            closeWindow();
        });
    }
}
