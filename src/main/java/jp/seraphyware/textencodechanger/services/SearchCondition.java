
package jp.seraphyware.textencodechanger.services;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 検索条件
 * 
 * @author seraphy
 */
public class SearchCondition {
    /**
     * 入力元ディレクトリ.
     */
    private final SimpleStringProperty inputProerty = new SimpleStringProperty(this, "input");
    /**
     * 再帰的に検査するか？.
     */
    private final SimpleBooleanProperty recursiveProperty = new SimpleBooleanProperty(this, "recursive", true);
    /**
     * ファイル名パターン.
     */
    private final SimpleStringProperty patternProperty = new SimpleStringProperty(this, "pattern");

    /**
     * 指定したインスタンスに内容をコピーする
     * @param output
     */
    public void copyTo(SearchCondition output) {
        Objects.requireNonNull(output);
        output.inputProerty().set(inputProerty.get());
        output.recursiveProperty().set(recursiveProperty.get());
        output.patternProperty().set(patternProperty.get());
    }

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
     * ファイル名のパターン.
     * @return ファイル名のパターン
     */
    public StringProperty patternProperty() {
        return patternProperty;
    }
    
}
