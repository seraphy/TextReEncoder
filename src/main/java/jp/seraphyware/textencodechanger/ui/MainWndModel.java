package jp.seraphyware.textencodechanger.ui;

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
        private final SimpleStringProperty encodingProperty =
                new SimpleStringProperty();

        /**
         * 文字コード.
         * @return 文字コード
         */
        public SimpleStringProperty encodingProperty() {
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
     * 入力元ディレクトリ.
     */
    private final SimpleStringProperty inputProerty =
            new SimpleStringProperty();

    /**
     * 再帰的に検査するか？.
     */
    private final SimpleBooleanProperty recursiveProperty =
            new SimpleBooleanProperty();

    /**
     * 出力先ディレクトリ.
     */
    private final SimpleStringProperty outputProperty =
            new SimpleStringProperty();

    /**
     * 転送タイプ.
     */
    private final SimpleObjectProperty<TransferType> transferTypeProperty =
            new SimpleObjectProperty<>();

    /**
     * バックアップの要否.
     */
    private final SimpleBooleanProperty createBackupProperty =
            new SimpleBooleanProperty();

    /**
     * ファイル名パターン.
     */
    private final SimpleStringProperty patternProperty =
            new SimpleStringProperty();

    /**
     * ファイルリスト.
     */
    private final ObservableList<FileItem> fileItems =
            FXCollections.observableArrayList(
                (FileItem i) -> new Observable[]{i.selectProperty()});

    /**
     * 入力ディレクトリ.
     * @return 入力ディレクトリ.
     */
    public StringProperty inputProerty() {
        return inputProerty;
    }

    /**
     * 再帰的に検査するか？.
     * @return 再帰的に検査するか？
     */
    public BooleanProperty recursiveProperty() {
        return recursiveProperty;
    }

    /**
     * 出力先ディレクトリ.
     * @return 出力先ディレクトリ.
     */
    public StringProperty outputProperty() {
        return outputProperty;
    }

    /**
     * ファイル名のパターン.
     * @return ファイル名のパターン
     */
    public StringProperty patternProperty() {
        return patternProperty;
    }

    /**
     * バックアップの要否.
     * @return バックアップの要否
     */
    public BooleanProperty createBackupProperty() {
        return createBackupProperty;
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
