package jp.seraphyware.textencodechanger.ui;

import java.util.Objects;
import jp.seraphyware.textencodechanger.services.TransferType;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
         * 文字コード.
         * @return 文字コード
         */
        public SimpleObjectProperty<EncodingType> encodingProperty() {
            return encodingProperty;
        }

        /**
         * ファイル.
         * @return ファイル.
         */
        public SimpleStringProperty fileProperty() {
            return fileProperty;
        }

        /**
         * 選択状態.
         * @return 選択状態.
         */
        public SimpleBooleanProperty selectProperty() {
            return selectProperty;
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
    private final ObservableList<FileItem> fileItems =
            FXCollections.observableArrayList(
                (FileItem i) -> new Observable[]{i.selectProperty()});


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
