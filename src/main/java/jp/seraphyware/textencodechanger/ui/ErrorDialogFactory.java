package jp.seraphyware.textencodechanger.ui;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * エラーダイアログを作成するファクトリ.
 *
 * @author seraphy
 */
@FunctionalInterface
public interface ErrorDialogFactory {

    /**
     * エラーダイアログを作成する.
     *
     * @param parent 親ウィンドウ
     * @param title タイトル
     * @param ex 例外
     * @return ステージ
     */
    Stage createErrorDialog(Window parent, String title, Throwable ex);

}
