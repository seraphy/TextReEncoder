package jp.seraphyware.textencodechanger.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * エラーダイアログのコントローラ.
 *
 * @author seraphy
 */
@Component
@Scope("prototype")
@FXMLController
public class ErrorDialogController implements Initializable {

    /**
     * OKボタン.
     */
    @FXML
    private Button btnOK;

    /**
     * テキストエリア.
     */
    @FXML
    private TextArea textArea;

    /**
     * テキストプロパティ.
     * @return テキストプロパティ
     */
    public final StringProperty textProperty() {
        return textArea.textProperty();
    }

    /**
     * Initializes the controller class.
     *
     * @param url ロードするFXMLのURL
     * @param rb リソースバンドル
     */
    @Override
    public final void initialize(final URL url, final ResourceBundle rb) {
        btnOK.setOnAction(p -> {
            Node node = (Node) p.getSource();
            Stage stg = (Stage) node.getScene().getWindow();
            stg.close();
        });
    }
}
