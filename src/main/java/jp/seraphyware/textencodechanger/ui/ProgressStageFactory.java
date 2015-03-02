package jp.seraphyware.textencodechanger.ui;

import javafx.concurrent.Task;
import javafx.stage.Stage;

/**
 * プログレスダイアログを作成するファクトリ.
 *
 * @author seraphy
 */
@FunctionalInterface
public interface ProgressStageFactory {

    /**
     * プログレスダイアログ用のステージを作成する.
     *
     * @param bgTask タスク
     * @return ステージ
     */
    Stage createProgressStage(Task<?> bgTask);

}
