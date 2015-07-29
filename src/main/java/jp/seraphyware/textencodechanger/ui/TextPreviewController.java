package jp.seraphyware.textencodechanger.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * プレビュー
 *
 * @author seraphy
 */
@Component
@Scope("prototype")
@FXMLController
public class TextPreviewController implements Initializable {

    @FXML
    private Button btnOK;

    @FXML
    private Label lblFileName;
    
    @FXML
    private Label lblEncoding;
    
    @FXML
    private TextArea textArea;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnOK.setOnAction(p -> {
            Node node = (Node) p.getSource();
            Stage stg = (Stage) node.getScene().getWindow();
            stg.close();
        });
    }    
    
    public StringProperty fileNameProperty() {
        return lblFileName.textProperty();
    }
    
    public StringProperty encodingProperty() {
        return lblEncoding.textProperty();
    }
    
    public StringProperty textProperty() {
        return textArea.textProperty();
    }
}
