package jp.seraphyware.textencodechanger.ui;

import java.nio.file.attribute.FileTime;
import jp.seraphyware.textencodechanger.services.TransferType;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jp.seraphyware.textencodechanger.services.EncodingType;
import jp.seraphyware.textencodechanger.services.OverwriteMode;

/**
 * メインウィンドウのモデル.
 *
 * @author seraphy
 */
public final class MainWndModel {

    /**
     * ファイル一覧の行モデル.
     */
    public static final class FileItem {

        /**
         * ファイル.
         */
        private final SimpleStringProperty fileProperty =
                new SimpleStringProperty();
        
        /**
         * ファイルサイズ.
         */
        private final SimpleLongProperty sizeProperty =
                new SimpleLongProperty();

        /**
         * 最終更新日.
         */
        private final SimpleObjectProperty<FileTime> lastModifiedProperty =
                new SimpleObjectProperty<>();

        /**
         * 選択状態.
         */
        private final SimpleBooleanProperty selectProperty =
                new SimpleBooleanProperty();

        /**
         * 文字コード.
         */
        private final SimpleObjectProperty<EncodingType> encodingProperty =
                new SimpleObjectProperty<>();
        
        /**
         * 変換済みフラグ.
         */
        private final SimpleBooleanProperty convertedProperty =
                new SimpleBooleanProperty();

        /**
         * 文字コード.
         * @return 文字コード
         */
        public ObjectProperty<EncodingType> encodingProperty() {
            return encodingProperty;
        }

        /**
         * ファイル.
         * @return ファイル.
         */
        public StringProperty fileProperty() {
            return fileProperty;
        }
        
        /**
         * ファイルサイズ.
         * @return 
         */
        public LongProperty sizeProperty() {
            return sizeProperty;
        }
        
        /**
         * 最終更新日.
         * @return 
         */
        public ObjectProperty<FileTime> lastModifiedProperty() {
            return lastModifiedProperty;
        }

        /**
         * 選択状態.
         * @return 選択状態.
         */
        public BooleanProperty selectProperty() {
            return selectProperty;
        }
        
        /**
         * 変換状態.
         * @return 
         */
        public BooleanProperty convertedProperty() {
            return convertedProperty;
        }
    }
    
    /**
     * 出力先ディレクトリ.
     */
    private final SimpleStringProperty outputProperty =
            new SimpleStringProperty(this, "output");

    /**
     * 転送タイプ.
     */
    private final SimpleObjectProperty<TransferType> transferTypeProperty =
            new SimpleObjectProperty<>(this, "transferType", TransferType.COPY);

    /**
     * 上書きモード.
     */
    private final SimpleObjectProperty<OverwriteMode> overwriteModeProperty =
            new SimpleObjectProperty<>(this, "overwriteMode", OverwriteMode.OVERWRITE);

    /**
     * ファイルリスト.
     */
    private final ObservableList<FileItem> fileItems
            = FXCollections.observableArrayList(
                    (FileItem i) -> new Observable[]{
                        i.selectProperty()
                    });


    /**
     * 出力先ディレクトリ.
     * @return 出力先ディレクトリ.
     */
    public StringProperty outputProperty() {
        return outputProperty;
    }

    /**
     * 上書きモード
     * @return 上書きモード
     */
    public ObjectProperty<OverwriteMode> overwriteModeProperty() {
        return overwriteModeProperty;
    }

    /**
     * 転送タイプ.
     * @return 転送タイプ
     */
    public ObjectProperty<TransferType> transferTypeProperty() {
        return transferTypeProperty;
    }

    /**
     * ファイルの一覧.
     * @return ファイルの一覧
     */
    public ObservableList<FileItem> getFileItems() {
        return fileItems;
    }
}
