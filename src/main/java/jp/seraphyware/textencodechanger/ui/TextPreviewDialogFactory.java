package jp.seraphyware.textencodechanger.ui;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * プレビューダイアログのファクトリ
 * 
 * @author seraphy
 */
@FunctionalInterface
public interface TextPreviewDialogFactory {
    
    Stage textPreviewDialog(
            final Window parent,
            final String title,
            final String text,
            final String fileName,
            final String encoding
    );
    
}
