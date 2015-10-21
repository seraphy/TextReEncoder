package jp.seraphyware.textencodechanger.services;

/**
 * 上書きモード
 * 
 * @author seraphy
 */
public enum OverwriteMode {
    
    /**
     * 上書きする
     */
    OVERWRITE,

    /**
     * バックアップを作成する
     */
    CREATE_BACKUP,
   
    /**
     * 上書きせずスキップする
     */
    SKIP
}
