package jp.seraphyware.textencodechanger.services;

/**
 * ファイルの複製・移動モード.
 *
 * @author seraphy
 */
public enum TransferType {

    /**
     * 置換.<br>
     */
    REPLACE,

    /**
     * コピー.<br>
     */
    COPY,
    
    /**
     * 移動.<br>
     */
    MOVE
}
