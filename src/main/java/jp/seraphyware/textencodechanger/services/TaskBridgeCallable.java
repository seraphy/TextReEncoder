package jp.seraphyware.textencodechanger.services;

/**
 * JavaFXのTaskをブリッジするTaskBridgeCallableで使われるインターフェイス.
 * 
 * @param <V> タスクが返す戻り型
 * @author seraphy
 */
@FunctionalInterface
public interface TaskBridgeCallable<V> {

    /**
     * 進行状態を通知するためのコールバックを引数にとり、
     * タスクを実行する.
     * @param callback 進行状態のコールバック
     * @return タスクの完了
     * @throws Exception 失敗
     */
    V call(ProgressCallback callback) throws Exception;
}
