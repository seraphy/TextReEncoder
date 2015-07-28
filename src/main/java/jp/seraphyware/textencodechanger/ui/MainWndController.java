package jp.seraphyware.textencodechanger.ui;

import jp.seraphyware.textencodechanger.services.TransferType;
import jp.seraphyware.textencodechanger.services.TextEncodeConvService;
import jp.seraphyware.textencodechanger.services.BackgroundTaskService;
import jp.seraphyware.textencodechanger.services.FileWalkService;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jp.seraphyware.textencodechanger.services.EncodingType;
import jp.seraphyware.textencodechanger.services.FileWalkService.FileInfo;
import jp.seraphyware.textencodechanger.services.FileWalkerCallable;
import jp.seraphyware.textencodechanger.services.FileWalkerProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * メインウィンドウのコントローラ.
 *
 * @author seraphy
 */
@Component
@FXMLController
public class MainWndController implements Initializable {

    /**
     * ロガー.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * プログレスダイアログを作成するファクトリ.
     */
    @Autowired
    private ProgressStageFactory progressStageFactory;

    /**
     * エラーダイアログを作成するファクトリ.
     */
    @Autowired
    private ErrorDialogFactory errorDialogFactory;

    /**
     * ファイルツリーをトラバーサルするサービス.
     */
    @Autowired
    private FileWalkService fileWalkService;

    /**
     * テキストの文字コード変換のサービス.
     */
    @Autowired
    private TextEncodeConvService encodeConvService;

    /**
     * バックグラウンドでタスクを実行するためのサービス.
     */
    @Autowired
    private BackgroundTaskService bgTaskSerive;

    /**
     * モデル.
     */
    private final MainWndModel model = new MainWndModel();

    /**
     * シーンコンテナ.
     */
    @FXML
    private Parent parent;

    /**
     * 入力元テキストボックス.
     */
    @FXML
    private TextField txtInput;

    /**
     * サブフォルダのチェックボックス.
     */
    @FXML
    private CheckBox chkRecursive;

    /**
     * 出力先テキストボックス.
     */
    @FXML
    private TextField txtOutput;

    /**
     * ファイル名パターンのテキストボックス.
     */
    @FXML
    private TextField txtPattern;

    /**
     * 転送モードの選択ドロップダウン.
     */
    @FXML
    private ComboBox<TransferType> comboTransferType;

    /**
     * バックアップ要否のチェックボックス.
     */
    @FXML
    private CheckBox chkBackup;

    /**
     * 出力先のフォルダ選択ボタン.
     */
    @FXML
    private Button btnBrowseOutputDir;

    /**
     * 文字コードの選択ドロップダウン.
     */
    @FXML
    private ComboBox<EncodingType> comboEncoding;

    /**
     * ファイルのテーブルビュー.
     */
    @FXML
    private TableView<MainWndModel.FileItem> tblFiles;

    /**
     * 選択状態カラム.
     */
    @FXML
    private TableColumn<MainWndModel.FileItem, Boolean> colSelect;

    /**
     * ファイル名カラム.
     */
    @FXML
    private TableColumn<MainWndModel.FileItem, String> colName;

    /**
     * ファイルの文字コードカラム.
     */
    @FXML
    private TableColumn<MainWndModel.FileItem, String> colEncoding;

    /**
     * チェック開始ボタン.
     */
    @FXML
    private Button btnCheck;

    /**
     * 変換開始ボタン.
     */
    @FXML
    private Button btnConvert;

    /**
     * 入力元用ディレクトリ選択ダイアログ.
     */
    private final DirectoryChooser dcInput = new DirectoryChooser();

    /**
     * 出力先用ディレクトリ選択ダイアログ.
     */
    private final DirectoryChooser dcOutput = new DirectoryChooser();

    /**
     * 完了通知ダイアログボッススのファクトリ.
     */
    private IntFunction<Alert> createConvertCompleteDialog;

    /**
     * テキストの真ん中を省略表示するテーブルセル用クラスの定義.
     *
     * @param <T>
     * @param <S>
     */
    private static class CenteredOverrunTableCell<T, S>
            extends TableCell<T, S> {

        /**
         * コンストラクタ.
         */
        public CenteredOverrunTableCell() {
            this(null);
        }

        /**
         * 省略文字を指定して構築するコンストラクタ.
         * @param ellipsisString 省略文字.
         */
        public CenteredOverrunTableCell(final String ellipsisString) {
            super();
            setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            if (ellipsisString != null) {
                setEllipsisString(ellipsisString);
            }
        }

        /**
         * 文字列表現.
         * @param item アイテム
         * @param empty 空であるか？
         */
        @Override
        protected void updateItem(final S item, final boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                setText("");
            } else {
                setText(item.toString());
            }
        }
    }

    /**
     * 初期化.
     *
     * @param url FXMLのロード元
     * @param rb リソースバンドル
     */
    @Override
    public final void initialize(final URL url, final ResourceBundle rb) {
        // 画面とモデルをバインドする.
        txtInput.textProperty().bindBidirectional(model.inputProerty());
        chkRecursive.selectedProperty().bindBidirectional(
                model.recursiveProperty());
        txtOutput.textProperty().bindBidirectional(
                model.outputProperty());
        txtPattern.textProperty().bindBidirectional(
                model.patternProperty());
        chkBackup.selectedProperty().bindBidirectional(
                model.createBackupProperty());

        txtOutput.disableProperty().bind(
                model.transferTypeProperty().isEqualTo(TransferType.REPLACE));
        btnBrowseOutputDir.disableProperty().bind(txtOutput.disableProperty());

        // 「チェック」ボタンは、入力元フォルダまたはパターンのいずれかが入力されてないと不可
        btnCheck.disableProperty().bind(
                Bindings.or(
                        model.inputProerty().isEmpty(),
                        model.patternProperty().isEmpty()
                )
        );

        // 「変換」ボタンは、出力先が設定されず且つ上書きでない場合、または選択がない場合は不可
        btnConvert.disableProperty().bind(Bindings.or(
                model.inputProerty().isEmpty(),
                Bindings.and(
                        model.outputProperty().isEmpty(),
                        model.transferTypeProperty().
                        isNotEqualTo(TransferType.REPLACE)
                )
        ));

        // テーブルの列とデータのバインド.
        tblFiles.setPlaceholder(new Text(rb.getString("emptyRow")));
        tblFiles.setItems(model.getFileItems());

        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colSelect.setCellValueFactory(new PropertyValueFactory<>("select"));

        colName.setCellFactory(p -> new CenteredOverrunTableCell<>("..."));
        colName.setCellValueFactory(new PropertyValueFactory<>("file"));

        colEncoding.setCellValueFactory(new PropertyValueFactory<>("encoding"));

        // 文字コード選択コンボボックスを設定する.
        comboEncoding.setItems(FXCollections.observableArrayList(
                encodeConvService.getEncodings()));
        model.transferTypeProperty().bind(
                comboTransferType.getSelectionModel().selectedItemProperty());
        comboEncoding.getSelectionModel().select(0);

        // 転送モードを表示するためのコンバータを設定する.
        comboTransferType.setConverter(new StringConverter<TransferType>() {
            @Override
            public String toString(final TransferType object) {
                return rb.getString("comboTransferType." + object.name());
            }

            @Override
            public TransferType fromString(final String string) {
                return TransferType.valueOf(string);
            }
        });

        // 転送モードを設定し、初期値として2番目(Copy)を選択する.
        comboTransferType.setItems(
                FXCollections.observableArrayList(TransferType.values()));
        comboTransferType.getSelectionModel().select(1);

        // デフォルトパターンを設定する.
        txtPattern.textProperty().set(rb.getString("defaultPattern"));

        // フォルダ選択ダイアログのタイトルをリソースより取得する.
        dcInput.setTitle(rb.getString("chooseDir.input.caption"));
        dcOutput.setTitle(rb.getString("chooseDir.input.caption"));

        // 入力元フォルダ情報が変更された場合はチェックリストを初期化する.
        model.inputProerty().addListener(e -> clearFiles());
        model.recursiveProperty().addListener(e -> clearFiles());

        // 完了ダイアログのファクトリを作成する.
        createConvertCompleteDialog = (count) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("convert.finished.title"));
            alert.setHeaderText(rb.getString("convert.finished.header"));
            alert.setContentText(String.format(rb.getString(
                    "convert.finished.contentsFmt"), count));
            return alert;
        };

        // フォーカスを入力元フォルダフィールドに設定する.
        Platform.runLater(() -> txtInput.requestFocus());
    }

    /**
     * ソースファイル一覧の対象が変更されえる画面上の変更があった場合、 検出済みのファイル一覧をクリアする.
     */
    private void clearFiles() {
        model.getFileItems().clear();
    }

    /**
     * ソースディレクトリの参照ボタン.
     *
     * @param event イベント
     */
    @FXML
    public final void handleBrowseInputDirAction(final ActionEvent event) {
        String srcDir = txtInput.textProperty().get();
        if (srcDir != null && !srcDir.isEmpty()) {
            dcInput.setInitialDirectory(new File(srcDir));
        }
        File selectedDir = dcInput.showDialog(parent.getScene().getWindow());
        if (selectedDir != null) {
            txtInput.setText(selectedDir.getAbsolutePath());
            txtInput.requestFocus();
        }
    }

    /**
     * 出力先ディレクトリの参照ボタン.
     *
     * @param event イベント
     */
    @FXML
    public final void handleBrowseOutputDirAction(final ActionEvent event) {
        String outDir = txtOutput.textProperty().get();
        if (outDir == null || outDir.isEmpty()) {
            outDir = txtInput.textProperty().get();
        }
        if (outDir != null && !outDir.isEmpty()) {
            dcOutput.setInitialDirectory(new File(outDir));
        }
        File selectedDir = dcOutput.showDialog(parent.getScene().getWindow());
        if (selectedDir != null) {
            txtOutput.setText(selectedDir.getAbsolutePath());
            txtOutput.requestFocus();
        }
    }

    /**
     * ソースディレクトリの走査とファイルの文字コードの推定処理の開始ボタン.
     *
     * @param event イベント
     */
    @FXML
    public final void handleCheckAction(final ActionEvent event) {
        model.getFileItems().clear();

        String srcDirStr = model.inputProerty().get();
        boolean recursive = model.recursiveProperty().get();
        String patterns = model.patternProperty().get();

        EncodingType selEncoding = comboEncoding.getValue();

        List<Pattern> regexps = fileWalkService.makePatterns(patterns);
        Path srcDir = Paths.get(srcDirStr);

        // ワーカーの作成
        FileWalkerCallable fileWalker =
                fileWalkService.createCallable(srcDir, recursive, regexps);

        // ワーカーを、JavaFX UIスレッドとの連携用タスクと接続する.
        Task<List<FileInfo>> bgTask = new Task<List<FileInfo>>() {
            @Override
            protected List<FileInfo> call() throws Exception {
                FileWalkerProgress progressCallback =
                        new FileWalkerProgress() {
                    @Override
                    public void setMessage(final String message) {
                        updateMessage(message);
                    }
                    @Override
                    public void setTitle(final String title) {
                        updateTitle(title);
                    }
                };
                fileWalker.setProgressCallback(progressCallback);
                return fileWalker.call();
            }
        };

        // タスクを実行する.
        bgTaskSerive.execute(bgTask);

        // プログレスダイアログ
        Stage progStg = progressStageFactory.createProgressStage(bgTask);
        if (!bgTask.isDone()) {
            progStg.showAndWait();
        } else {
            // 表示する前に終了していたので直ちに閉じておく
            progStg.close();
        }

        // 走査結果をテーブルデータに変換して表示する.
        try {
            if (!bgTask.isCancelled()) {
                // 走査結果の取得
                List<FileInfo> files = bgTask.get();

                tblFiles.setItems(FXCollections.emptyObservableList());
                ObservableList<MainWndModel.FileItem> items =
                        model.getFileItems();
                files.stream().map((fileInfo) -> {
                    MainWndModel.FileItem item = new MainWndModel.FileItem();

                    // 走査結果のファイルのパスを入力ディレクトリからの相対パスにする
                    Path filePath = fileInfo.getPath();
                    Path relativePath = srcDir.relativize(filePath);

                    item.selectProperty().set(
                            !selEncoding.equals(fileInfo.getEncoding()));
                    item.fileProperty().set(relativePath.toString());
                    item.encodingProperty().set(fileInfo.getEncoding());
                    return item;

                }).forEach((item) -> {
                    items.add(item);
                });
                tblFiles.setItems(items);

            } else {
                // キャンセルされていた場合
                log.info("★中断済み");
                model.getFileItems().clear();
            }

        } catch (Exception ex) {
            log.error("ワーカーの失敗: " + ex, ex);

            // エラーダイアログの表示
            Stage errorDlg = errorDialogFactory.createErrorDialog(
                    parent.getScene().getWindow(), "ERROR", ex);
            errorDlg.showAndWait();
        }
    }

    /**
     * ソースファイルリストで選択されているファイルについて文字コードを変換する.
     *
     * @param event イベント
     */
    @FXML
    public final void handleConvertAction(final ActionEvent event) {
        log.info("★convert");

        String srcDir = model.inputProerty().get();
        String destDir = model.outputProperty().get();
        TransferType transferType = model.transferTypeProperty().get();
        boolean reqBak = model.createBackupProperty().get();

        EncodingType destEncoding = comboEncoding.getValue();

        Task<Integer> bgTask = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                updateTitle("converting...");

                TextEncodeConvService.FileEncodingConverter converter
                        = encodeConvService.createFileEncodingConverter(
                                srcDir,
                                destDir,
                                transferType,
                                reqBak,
                                destEncoding);

                int count = 0;

                for (MainWndModel.FileItem fileItem : model.getFileItems()) {
                    if (!fileItem.selectProperty().get()) {
                        continue;
                    }

                    String relativePath = fileItem.fileProperty().get();
                    EncodingType srcEncoding = fileItem.encodingProperty().get();

                    if (srcEncoding == null) {
                        log.warn("エンコードが不明のため対象外: " + fileItem);
                        continue;
                    }

                    updateMessage(relativePath);
                    converter.convert(relativePath, srcEncoding);

                    // 変換完了を示す
                    Platform.runLater(() -> {
                        // bindingでチェックボックスを変更すると、テーブルカラムも変更されるため
                        // JavaFxのスレッドでの操作とする.
                        fileItem.selectProperty().set(false);
                        fileItem.encodingProperty().set(destEncoding);
                    });
                    count++;
                }
                return count;
            }
        };

        bgTaskSerive.execute(bgTask);

        Stage progStg = progressStageFactory.createProgressStage(bgTask);
        if (!bgTask.isDone()) {
            progStg.showAndWait();

        } else {
            progStg.close();
        }

        try {
            int count = bgTask.get();
            if (count > 0) {
                // 完了通知
                Alert alert = createConvertCompleteDialog.apply(count);
                alert.showAndWait();
            }

        } catch (Exception ex) {
            Stage errorDlg = errorDialogFactory.createErrorDialog(
                    parent.getScene().getWindow(), "ERROR", ex);
            errorDlg.showAndWait();
        }
    }
}

