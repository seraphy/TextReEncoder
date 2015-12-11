package jp.seraphyware.textencodechanger.ui;

import jp.seraphyware.textencodechanger.ui.common.CenteredOverrunHyperlinkTableCell;
import jp.seraphyware.textencodechanger.services.TaskBridge;
import jp.seraphyware.textencodechanger.services.TransferType;
import jp.seraphyware.textencodechanger.services.TextEncodeConvService;
import jp.seraphyware.textencodechanger.services.BackgroundTaskService;
import jp.seraphyware.textencodechanger.services.FileWalkService;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.inject.Inject;
import javax.inject.Provider;
import jp.seraphyware.textencodechanger.services.EncodingType;
import jp.seraphyware.textencodechanger.services.FileReplaceService;
import jp.seraphyware.textencodechanger.services.FileReplaceService.ContentConverter;
import jp.seraphyware.textencodechanger.services.FileReplaceService.ContentReader;
import jp.seraphyware.textencodechanger.services.FileReplaceService.FileContentConverter;
import jp.seraphyware.textencodechanger.services.FileWalkService.FileInfo;
import jp.seraphyware.textencodechanger.services.FileWalkerCallable;
import jp.seraphyware.textencodechanger.services.OverwriteMode;
import jp.seraphyware.textencodechanger.services.SearchCondition;
import jp.seraphyware.textencodechanger.services.TextTermConvService;
import jp.seraphyware.textencodechanger.services.TextTermType;
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
public class MainWndController extends SimpleWindowController implements Initializable {

    /**
     * ロガー.
     */
    protected static final Logger log = LoggerFactory.getLogger(MainWndController.class);

    /**
     * リソース.
     */
    private ResourceBundle res;
    
    /**
     * プログレスダイアログを作成するファクトリ.
     */
    @Inject
    private Provider<ProgressController> progressCtrlProv;

    /**
     * テキストプレビューダイアログを取得するためのプロバイダー.
     */
    @Inject
    private Provider<TextPreviewController> textPrevCtrlProv;

    /**
     * ファイルツリーをトラバーサルするサービス.
     */
    @Autowired
    private FileWalkService fileWalkService;

    /**
     * テキストの文字コード変換のサービス.
     */
    @Autowired
    protected TextEncodeConvService encodeConvService;

    /**
     * テキストの文字コード変換のサービス.
     */
    @Autowired
    protected FileReplaceService fileReplaceService;

    /**
     * 行末タイプの変換サービス.
     */
    @Autowired(required = true)
    private TextTermConvService termConvSrv;
    
    /**
     * バックグラウンドでタスクを実行するためのサービス.
     */
    @Autowired
    private BackgroundTaskService bgTaskSerive;

    /**
     * モデル.
     */
    protected final MainWndModel model = new MainWndModel();
    
    /**
     * 検索条件
     */
    private final SearchCondition searchCondition = new SearchCondition();
    
    /**
     * 最後に使用した検索条件
     */
    private final SearchCondition lastUseSearchCondition = new SearchCondition();

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
     * 上書きモードの選択ドロップダウン.
     */
    @FXML
    private ComboBox<OverwriteMode> comboOverwriteMode;

    /**
     * 出力先のフォルダ選択ボタン.
     */
    @FXML
    private Button btnBrowseOutputDir;
    
    /**
     * サーチボックス
     */
    @FXML
    private TextField txtSearch;

    /**
     * 文字コードの選択ドロップダウン.
     */
    @FXML
    private ComboBox<EncodingType> comboEncoding;

    /**
     * 行末タイプの選択ドロップダウン.
     */
    @FXML
    private ComboBox<TextTermType> comboTermType;

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
    private TableColumn<MainWndModel.FileItem, EncodingType> colEncoding;

    /**
     * ファイルの行末タイプカラム.
     */
    @FXML
    private TableColumn<MainWndModel.FileItem, TextTermType> colTermType;

    /**
     * ファイルのサイズ.
     */
    @FXML
    private TableColumn<MainWndModel.FileItem, Long> colSize;

        /**
     * ファイルの最終更新日.
     */
    @FXML
    private TableColumn<MainWndModel.FileItem, FileTime> colLastModified;

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
     * フィルタ済みリスト.
     */
    private FilteredList<MainWndModel.FileItem> filteredItems;
    
    /**
     * ソート連携リスト.
     */
    private SortedList<MainWndModel.FileItem> sortedItems;
    

    @Override
    public void onCloseRequest(WindowEvent event) {
        closeWindow();
    }
    
    /**
     * 初期化.
     *
     * @param url FXMLのロード元
     * @param rb リソースバンドル
     */
    @Override
    public final void initialize(URL url, ResourceBundle rb) {
        this.res = rb;
        
        // 画面とモデルをバインドする.
        txtInput.textProperty().bindBidirectional(searchCondition.inputProerty());
        chkRecursive.selectedProperty().bindBidirectional(
                searchCondition.recursiveProperty());
        txtOutput.textProperty().bindBidirectional(
                model.outputProperty());
        txtPattern.textProperty().bindBidirectional(
                searchCondition.patternProperty());

        txtOutput.disableProperty().bind(
                model.transferTypeProperty().isEqualTo(TransferType.REPLACE));
        btnBrowseOutputDir.disableProperty().bind(txtOutput.disableProperty());

        // 「チェック」ボタンは、入力元フォルダまたはパターンのいずれかが入力されてないと不可
        btnCheck.disableProperty().bind(
                Bindings.or(
                        searchCondition.inputProerty().isEmpty(),
                        searchCondition.patternProperty().isEmpty()
                )
        );

        // チェックされているファイル数
        LongBinding checkedCountBinding = new LongBinding() {
            {
                bind(model.getFileItems());
            }
            @Override
            protected long computeValue() {
                return model.getFileItems().stream().filter(
                        item -> item.selectProperty().get())
                        .collect(Collectors.counting());
            }
        };

        // 「変換」ボタンは、ファイル選択済みでないか、あるいは、出力先が設定されず、
        // 且つ上書きでない場合は不可
        btnConvert.disableProperty().bind(Bindings.or(
                checkedCountBinding.lessThan(1), // チェック数が1未満の場合
                Bindings.and( // または、上書きでない場合で出力先が指定ない場合
                        model.outputProperty().isEmpty(),
                        model.transferTypeProperty().
                            isNotEqualTo(TransferType.REPLACE)
                )
        ));

        // テーブルの列とデータのバインド.
        tblFiles.setPlaceholder(new Text(rb.getString("emptyRow")));

        // サーチボックスが変更された場合に設定するフィルタのプリディケータ
        ObjectBinding<Predicate<MainWndModel.FileItem>> predicateBiding =
                new ObjectBinding<Predicate<MainWndModel.FileItem>>() {
                    
            {
                bind(txtSearch.textProperty());
            }

            @Override
            protected Predicate<MainWndModel.FileItem> computeValue() {
                String search = txtSearch.getText();
                if (search != null && search.trim().length() == 0) {
                    return item -> true;
                }
                return (item) -> item.fileProperty().get().contains(search);
            }
        };
        
        // フィルタ済みリスト
        filteredItems = new FilteredList<>(model.getFileItems());
        filteredItems.predicateProperty().bind(predicateBiding);
        
        // ソート済みリストとテーブルを連結する
        sortedItems = new SortedList<>(filteredItems);
        sortedItems.comparatorProperty().bind(tblFiles.comparatorProperty());
        tblFiles.setItems(sortedItems);

        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colSelect.setCellValueFactory(new PropertyValueFactory<>("select"));

        colName.setCellFactory(p -> {
            CenteredOverrunHyperlinkTableCell<MainWndModel.FileItem, String> cell =
                    new CenteredOverrunHyperlinkTableCell<>("...");
            cell.actionEventHandlerCallbackProperty().set(
                    (Callback<Integer, EventHandler<ActionEvent>>) (Integer idx) -> {
                        return (evt) -> {
                            openFile(sortedItems.get(idx));
                        };
            });
            cell.linkDisabledStateCallbackProperty().set(
                    (Callback<Integer, ObservableValue<Boolean>>) (Integer idx) -> {
                        return sortedItems.get(idx).convertedProperty();
                    });
            return cell;
        });
        colName.setCellValueFactory(new PropertyValueFactory<>("file"));
        
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colLastModified.setCellValueFactory(new PropertyValueFactory<>("lastModified"));

        colEncoding.setCellValueFactory(new PropertyValueFactory<>("encoding"));
        colEncoding.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<EncodingType>() {
            @Override
            public String toString(EncodingType object) {
                return object == null ? "" : object.getDisplayString();
            }

            @Override
            public EncodingType fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }));
        
        colTermType.setCellValueFactory(p -> p.getValue().termTypeProperty());

        // 文字コード選択コンボボックスを設定する.
        comboEncoding.setItems(FXCollections.observableArrayList(
                encodeConvService.getEncodings()));
        comboEncoding.getSelectionModel().select(0);

        // 転送モードを表示するためのコンバータを設定する.
        comboTransferType.setConverter(new StringConverter<TransferType>() {
            @Override
            public String toString(final TransferType object) {
                return res.getString("comboTransferType." + object.name());
            }

            @Override
            public TransferType fromString(final String string) {
                return TransferType.valueOf(string);
            }
        });

        // 転送モードを設定する.
        comboTransferType.setItems(
                FXCollections.observableArrayList(TransferType.values()));
        comboTransferType.getSelectionModel().select(
                model.transferTypeProperty().get());
        model.transferTypeProperty().bind(
                comboTransferType.getSelectionModel().selectedItemProperty());
        
        // 上書きモードを表示するためのコンバータを設定する.
        comboOverwriteMode.setConverter(new StringConverter<OverwriteMode>() {
            @Override
            public String toString(OverwriteMode object) {
                return res.getString("comboOverwriteMode." + object.name());
            }
            @Override
            public OverwriteMode fromString(String string) {
                return OverwriteMode.valueOf(string);
            }
        });
                
        // 上書きモードを設定する
        comboOverwriteMode.setItems(
                FXCollections.observableArrayList(OverwriteMode.values()));
        comboOverwriteMode.getSelectionModel().select(
                model.overwriteModeProperty().get());
        model.overwriteModeProperty().bind(
                comboOverwriteMode.getSelectionModel().selectedItemProperty());

        // 行末モードを設定する
        comboTermType.setItems(
                FXCollections.observableArrayList(TextTermType.values()));
        comboTermType.setConverter(new StringConverter<TextTermType>() {
            @Override
            public String toString(TextTermType object) {
                if (object == TextTermType.UNKNOWN) {
                    return rb.getString("dontcare");
                }
                return object.name();
            }

            @Override
            public TextTermType fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        comboTermType.getSelectionModel().select(
                model.textTermTypeProperty().get());
        model.textTermTypeProperty().bind(comboTermType.getSelectionModel().
                selectedItemProperty());
        
        // デフォルトパターンを設定する.
        txtPattern.textProperty().set(rb.getString("defaultPattern"));

        // フォルダ選択ダイアログのタイトルをリソースより取得する.
        dcInput.setTitle(rb.getString("chooseDir.input.caption"));
        dcOutput.setTitle(rb.getString("chooseDir.input.caption"));

        // 入力元フォルダ情報が変更された場合はチェックリストを初期化する.
        searchCondition.inputProerty().addListener(e -> clearFiles());
        searchCondition.recursiveProperty().addListener(e -> clearFiles());

        // 結果アイテム数
        IntegerBinding sizeBinding = new IntegerBinding() {
            {
                bind(model.getFileItems());
            }
            @Override
            protected int computeValue() {
                return model.getFileItems().size();
            }
        };

        // 結果がある場合のみサーチを有効化する
        txtSearch.disableProperty().bind(sizeBinding.isEqualTo(0));
        
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
     * ファイルのプレビューダイアログを開く
     * @param fileInfo 
     */
    protected void openFile(MainWndModel.FileItem fileInfo) {
        try {
            String file = fileInfo.fileProperty().get();
            String srcDir = lastUseSearchCondition.inputProerty().get();
            Path filePath = Paths.get(srcDir, file);
            
            TextPreviewController textPrevCtrl = textPrevCtrlProv.get();
            textPrevCtrl.setOwner(getStage());
            textPrevCtrl.textFilePathProperty().set(filePath);

            textPrevCtrl.encodingProperty().set(fileInfo.encodingProperty().get());
            textPrevCtrl.fallbackEncodingProperty().set(comboEncoding.getValue());
            
            Stage stg = textPrevCtrl.getStage();
            textPrevCtrl.showTextFile();

            stg.showAndWait();
            
            EncodingType encType = textPrevCtrl.getResult();
            if (encType != null) {
                fileInfo.encodingProperty().set(encType);
            }

        } catch (Throwable ex) {
            log.error("ワーカーの失敗: " + ex, ex);

            // エラーダイアログの表示
            ErrorDialogUtils.showException(getStage(), ex);
        }
    }
    
    /**
     * ソースファイル一覧の対象が変更されえる画面上の変更があった場合、 検出済みのファイル一覧をクリアする.
     */
    private void clearFiles() {
        model.getFileItems().clear();
        txtSearch.clear();
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
            File dir = new File(srcDir);
            if (dir.isDirectory()) {
                dcInput.setInitialDirectory(dir);
            }
        }
        File selectedDir = dcInput.showDialog(getStage());
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
        File selectedDir = dcOutput.showDialog(getStage());
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

        String srcDirStr = searchCondition.inputProerty().get();
        boolean recursive = searchCondition.recursiveProperty().get();
        String patterns = searchCondition.patternProperty().get();
        searchCondition.copyTo(lastUseSearchCondition);

        EncodingType selEncoding = comboEncoding.getValue();
        TextTermType selTermType = comboTermType.getValue();

        List<Pattern> regexps = fileWalkService.makePatterns(patterns);
        Path srcDir = Paths.get(srcDirStr);

        // ワーカーの作成
        FileWalkerCallable fileWalker =
                fileWalkService.createCallable(srcDir, recursive, regexps);

        // ワーカーを、JavaFX UIスレッドとの連携用タスクと接続する.
        Task<List<FileInfo>> bgTask = new TaskBridge<>((progressCallback) -> {
                fileWalker.setProgressCallback(progressCallback);
                return fileWalker.call();
        });

        // タスクを実行する.
        bgTaskSerive.execute(bgTask);

        // プログレスダイアログ
        ProgressController progressCtrl = progressCtrlProv.get();
        progressCtrl.setOwner(getStage());
        progressCtrl.connect(bgTask);
        Stage progStg = progressCtrl.getStage();
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
                List<MainWndModel.FileItem> items = files.stream()
                        .map((fileInfo) -> {
                    MainWndModel.FileItem item = new MainWndModel.FileItem();

                    // 走査結果のファイルのパスを入力ディレクトリからの相対パスにする
                    Path filePath = fileInfo.getPath();
                    Path relativePath = srcDir.relativize(filePath);

                    // パスの表示は相対パスとする.
                    item.fileProperty().set(relativePath.toString());

                    // ファイルの属性を取得する
                    BasicFileAttributeView attrView =
                            Files.getFileAttributeView(filePath,
                                    BasicFileAttributeView.class);
                    if (attrView != null) {
                        try {
                            BasicFileAttributes attr = attrView.readAttributes();
                            
                            item.sizeProperty().set(attr.size());
                            item.lastModifiedProperty().set(
                                    attr.lastModifiedTime());
                        }
                        catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                    
                    // 推定文字コード
                    item.encodingProperty().set(fileInfo.getEncoding());
                    
                    // 推定行末コード
                    item.termTypeProperty().set(fileInfo.getTermType());

                    // ファイルの推定文字コードとターゲットの文字コードが
                    // 一致しなければ、あるいは、改行コードが一致しなければ
                    // 自動的に選択状態とする.
                    // (ただし指定もしくは実ファイルのいずれかの改行コードが
                    // Unknownの場合は改行コードは不問とする.)
                    item.selectProperty().set(
                            !selEncoding.equals(fileInfo.getEncoding()) ||
                            (selTermType != TextTermType.UNKNOWN &&
                             fileInfo.getTermType() != TextTermType.UNKNOWN &&
                             !selTermType.equals(fileInfo.getTermType()))
                    );

                    return item;
                }).collect(Collectors.toList());

                model.getFileItems().addAll(items);

            } else {
                // キャンセルされていた場合
                log.info("★中断済み");
                model.getFileItems().clear();
            }

        } catch (RuntimeException | InterruptedException | ExecutionException ex) {
            log.error("ワーカーの失敗: " + ex, ex);

            // エラーダイアログの表示
             ErrorDialogUtils.showException(getStage(), ex);
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

        String srcDir = lastUseSearchCondition.inputProerty().get();
        if (srcDir == null || srcDir.isEmpty()) {
            return;
        }

        String destDir = model.outputProperty().get();
        TransferType transferType = model.transferTypeProperty().get();
        OverwriteMode overwriteMode = model.overwriteModeProperty().get();

        EncodingType destEncoding = comboEncoding.getValue();
        TextTermType termType = comboTermType.getValue();

        Task<Integer> bgTask = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                updateTitle("converting...");

                Function<CharBuffer, CharBuffer> termConv = (charBuf) ->
                        termConvSrv.changeTermType(charBuf, termType);

                ContentConverter encConv = (chars) -> encodeConvService.
                        writeBytes(termConv.apply(chars), destEncoding);
                FileContentConverter converter
                        = fileReplaceService.createFileContentConverter(
                                srcDir,
                                destDir,
                                transferType,
                                overwriteMode,
                                encConv);

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
                    
                    ContentReader decConv = (data) ->
                            encodeConvService.readText(data, srcEncoding);
                    boolean success = converter.convert(relativePath, decConv);
                    if (success) {
                        // 変換完了を示す
                        Platform.runLater(() -> {
                            // bindingでチェックボックスを変更すると、テーブルカラムも変更されるため
                            // JavaFxのスレッドでの操作とする.
                            fileItem.selectProperty().set(false);
                            
                            // 変更した文字コードに表示を切り替える
                            fileItem.encodingProperty().set(destEncoding);

                            // 変更した改行コードに表示を切り替える.
                            if (termType != TextTermType.UNKNOWN) {
                                fileItem.termTypeProperty().set(termType);
                            }
                            
                            // 文字コードが変更されているので元ファイルは開かないように
                            // ディセーブルにする.
                            fileItem.convertedProperty().set(true);
                        });
                        count++;
                    }
                }
                return count;
            }
        };

        bgTaskSerive.execute(bgTask);

        ProgressController progressCtrl = progressCtrlProv.get();
        progressCtrl.connect(bgTask);
        Stage progStg = progressCtrl.getStage();
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

        } catch (RuntimeException | InterruptedException | ExecutionException ex) {
            ErrorDialogUtils.showException(getStage(), ex);
        }
    }
}

