package jp.seraphyware.textencodechanger.ui;

import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jp.seraphyware.textencodechanger.services.EncodingType;
import jp.seraphyware.textencodechanger.services.TextEncodeConvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * プレビュー
 *
 * @author seraphy
 */
@Component
@Scope("prototype")
public class TextPreviewController extends SimpleWindowController
    implements Initializable {

    /**
     * テキストの文字コード変換のサービス.
     */
    @Autowired
    protected TextEncodeConvService encodeConvService;

    /**
     * ファイル名
     */
    @FXML
    private Label lblFileName;
    
    /**
     * 文字コードの選択ドロップダウン.
     */
    @FXML
    private ComboBox<EncodingType> comboEncoding;

    @FXML
    private Label lblEncoding;

    /**
     * テキスト表示エリア
     */
    @FXML
    private TextArea textArea;
    
    /**
     * OKボタン
     */
    @FXML
    private Button btnOK;
    
    /**
     * 文字コード
     */
    private final ObjectProperty<EncodingType> encodingTypeProperty = new SimpleObjectProperty<>();
    
    /**
     * 予備の文字コード
     */
    private final ObjectProperty<EncodingType> fallbackEncodingTypeProperty = new SimpleObjectProperty<>();
    
    /**
     * テキストファイルのパス
     */
    private final ObjectProperty<Path> pathProperty = new SimpleObjectProperty<>();
    
    /**
     * エラー状態を保持するプロパティ
     */
    private final BooleanProperty errorProperty = new SimpleBooleanProperty();

    /**
     * ダイアログの結果、エラー時もしくはCancel時はnull
     */
    private EncodingType result;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assert lblEncoding != null;
        assert lblFileName != null;
        assert comboEncoding != null;
        assert btnOK != null;

        // 文字コード選択コンボボックスを設定する.
        comboEncoding.setItems(FXCollections.observableArrayList(
                encodeConvService.getEncodings()));

        encodingTypeProperty.addListener((self, old, value) -> {
            if (value != null) {
                comboEncoding.getSelectionModel().select(value);
            }
        });
        
        comboEncoding.setOnAction(this::onChangeEncodingType);
        
        // EncodingTypeを文字列化するバインディング
        StringBinding encodingBinding = new StringBinding() {
            {
                bind(encodingTypeProperty);
            }
            
            @Override
            protected String computeValue() {
                EncodingType encType = encodingTypeProperty.get();
                if (encType != null) {
                    return encType.getDisplayString();
                }
                return "(Unknown)";
            }
        };
        lblEncoding.textProperty().bind(encodingBinding);
        
        lblFileName.textProperty().bind(pathProperty.asString());
        
        // ボタンの活性状態
        btnOK.disableProperty().bind(errorProperty);
    }
    
    /**
     * 文字コードのドロップダウンリストが変更されたことを示す.
     * @param evt 
     */
    protected void onChangeEncodingType(ActionEvent evt) {
        showTextFile();
    }
    
    /**
     * モーダルダイアログ用のステージを構成する.
     * @return 
     */
    @Override
    protected Stage createStage() {
        Stage stg = super.createStage();
        stg.initModality(Modality.WINDOW_MODAL);
        stg.initStyle(StageStyle.UTILITY);
        return stg;
    }

    /**
     * 文字コード
     * @return 
     */
    public ObjectProperty<EncodingType> encodingProperty() {
        return encodingTypeProperty;
    }
    
    /**
     * 文字コードの指定がない場合の予備の文字コード
     * @return 
     */
    public ObjectProperty<EncodingType> fallbackEncodingProperty() {
        return fallbackEncodingTypeProperty;
    }
    
    /**
     * テキストファイルのパス.
     * @return 
     */
    public ObjectProperty<Path> textFilePathProperty() {
        return pathProperty;
    }
    
    /**
     * 閉じるボタン
     */
    @FXML
    protected void onClose() {
        result = null;
        closeWindow();
    }
    
    /**
     * OKボタン
     */
    @FXML
    protected void onOK() {
        if (errorProperty.get()) {
            result = null;
        } else {
            result = comboEncoding.getValue();
        }
        closeWindow();
    }

    /**
     * ダイアログで選択された文字コードを取得する.
     * キャンセルした場合、もしくはエラーの場合はnullを返す.
     * @return 文字コード、もしくはnull
     */
    public EncodingType getResult() {
        return result;
    }

    /**
     * テキスト変換する文字コードの取得.
     * @return 
     */
    private EncodingType getEncodingType() {
        EncodingType encType = comboEncoding.getValue();
        if (encType == null) {
            encType = encodingTypeProperty.get();
        }
        if (encType == null) {
            encType = fallbackEncodingTypeProperty.get();
        }
        if (encType == null) {
            encType = EncodingType.UTF8;
        }
        return encType;
    }
    
    /**
     * ファイルの内容をテキストとして変換して表示する.
     */
    public void showTextFile() {
        boolean success = false;
        try {
            Path textFilePath = pathProperty.get();
            byte[] data;
            if (textFilePath != null && Files.isRegularFile(textFilePath)) {
                data = Files.readAllBytes(textFilePath);
            } else {
                data = new byte[0];
            }
            
            EncodingType encoding = getEncodingType();
            CharBuffer textBuf = encodeConvService.readText(data, encoding);
            StringBuilder buf = new StringBuilder();
            buf.append(textBuf);
            
            textArea.setText(buf.toString());
            success = true;

        } catch (CharacterCodingException ex) {
            textArea.setText("ERROR: " + ex);
            
        } catch (Exception ex) {
            ErrorDialogUtils.showException(getStage(), ex);
        }
        
        errorProperty.set(!success);
    }
}
