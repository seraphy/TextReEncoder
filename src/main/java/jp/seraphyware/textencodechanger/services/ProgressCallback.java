package jp.seraphyware.textencodechanger.services;

/**
 * ファイルの走査の進行状況の通知を受けるコールバック用インターフェイス.
 *
 * @author seraphy
 */
public interface ProgressCallback {

    /**
     * タイトルを設定します.
     *
     * @param title タイトル
     */
    void updateTitle(String title);

    /**
     * メッセージを設定します.
     *
     * @param message メッセージ
     */
    void updateMessage(String message);

    /**
     * プログレスを設定します.
     * 
     * @param workDone
     * @param max 
     */
    void updateProgress(double workDone, double max);
}
