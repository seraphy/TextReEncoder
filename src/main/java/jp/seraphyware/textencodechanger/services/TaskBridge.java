package jp.seraphyware.textencodechanger.services;

import java.util.Objects;
import javafx.concurrent.Task;

/**
 * JavaFXのタスクをラムダ式でブリッジするためのブリッジクラス.
 * 
 * @param <V> タスクが返す戻り型
 * @author seraphy
 */
public class TaskBridge<V> extends Task<V> implements ProgressCallback {
    private final TaskBridgeCallable<V> callable;

    /**
     * コンストラクタ
     * @param callable タスクで実行されるCallable
     */
    public TaskBridge(TaskBridgeCallable<V> callable) {
        Objects.requireNonNull(callable);
        this.callable = callable;
    }

    /**
     * タスクを実行する
     * @return
     * @throws Exception 
     */
    @Override
    protected V call() throws Exception {
        return callable.call(this);
    }

    /**
     * メッセージを更新するために非JavaFXスレッドから呼び出す
     * @param message 
     */
    @Override
    public void updateMessage(String message) {
        super.updateMessage(message);
    }

    /**
     * プログレスを更新するために非JavaFXスレッドから呼び出す
     * @param workDone
     * @param max 
     */
    @Override
    public void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
    }

    /**
     * 
     * @param title 
     */
    @Override
    public void updateTitle(String title) {
        super.updateTitle(title);
    }
}
