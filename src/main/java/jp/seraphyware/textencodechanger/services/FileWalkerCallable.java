package jp.seraphyware.textencodechanger.services;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * ファイルの走査結果を返すCallableの拡張.
 * 進行状況の通知を受けるコールバックの設定・取得メソッドを追加している.
 *
 * @author seraphy
 */
public interface FileWalkerCallable
    extends Callable<List<FileWalkService.FileInfo>> {

    /**
     * 進行状況の通知を受けるコールバックの設定.
     *
     * @param callback コールバック、不要ならnull可
     */
    void setProgressCallback(FileWalkerProgress callback);

    /**
     * 進行状況の通知を受けるコールバックの取得.
     *
     * @return コールバック、未設定ならnull
     */
    FileWalkerProgress getProgressCallback();

}
