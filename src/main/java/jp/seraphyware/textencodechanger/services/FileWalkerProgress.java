package jp.seraphyware.textencodechanger.services;

/**
 * ファイルの走査の進行状況の通知を受けるコールバック用インターフェイス.
 *
 * @author seraphy
 */
public interface FileWalkerProgress {

    /**
     * タイトルを設定します.
     *
     * @param title タイトル
     */
    void setTitle(String title);

    /**
     * メッセージを設定します.
     *
     * @param message メッセージ
     */
    void setMessage(String message);

}
