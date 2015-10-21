package jp.seraphyware.textencodechanger.ui;

import java.util.Objects;
import javafx.scene.Parent;

/**
 * FXMLをロードする抽象実装
 *
 * @author seraphy
 */
public abstract class AbstractFXMLController {

    /**
     * ルート
     */
    private Parent root;

    /**
     * ルートを生成する.
     */
    protected abstract void makeRoot();

    /**
     * ルートを取得する.
     * まだ作成されてなければ作成する.
     * @return 
     */
    public Parent getRoot() {
        if (root == null) {
            makeRoot();
            assert root != null;
        }
        return root;
    }

    /**
     * ルートを設定する.
     * @param root 
     */
    protected final void setRoot(Parent root) {
        Objects.requireNonNull(root);
        this.root = root;
    }
}
