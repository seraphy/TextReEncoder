package jp.seraphyware.textencodechanger.ui.common;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * テキストの真ん中を省略表示するハイパーリンク式のテーブルセル用クラスの定義.
 *
 * @param <T>
 * @param <S>
 */
public class CenteredOverrunHyperlinkTableCell<T, S> extends TableCell<T, S> {

    private final HBox hbox = new HBox();
    
    private final Label label = new Label();
    
    private final Hyperlink hyperlink = new Hyperlink();
    
    private InvalidationListener linkDisableInvalidateListener;
    
    private ObservableValue<Boolean> linkDisableProperty;

    /**
     * コンストラクタ.
     */
    public CenteredOverrunHyperlinkTableCell() {
        this(null);
    }

    /**
     * 省略文字を指定して構築するコンストラクタ.
     * @param ellipsisString 省略文字.
     */
    public CenteredOverrunHyperlinkTableCell(final String ellipsisString) {
        super();
        hyperlink.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        if (ellipsisString != null) {
            hyperlink.setEllipsisString(ellipsisString);
        }
        
        this.getStyleClass().add("centered-overrun-hyperlink-table-cell");
        
        hbox.getChildren().addAll(label, hyperlink);
    }

    /**
     * 文字列表現.
     * @param item アイテム
     * @param empty 空であるか？
     */
    @Override
    protected void updateItem(final S item, final boolean empty) {
        super.updateItem(item, empty);

        if (linkDisableProperty != null &&
                linkDisableInvalidateListener != null) {
            linkDisableProperty.removeListener(linkDisableInvalidateListener);
            linkDisableInvalidateListener = null;
            linkDisableProperty = null;
        }
        
	@SuppressWarnings("unchecked")
        TableRow<T> row = getTableRow();
        if (empty || row == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        String text;
        if (item == null) {
            text = "";
        } else {
            text = item.toString();
        }

        label.setText(text);

        hyperlink.setText(text);
        hyperlink.setVisited(false);

        EventHandler<ActionEvent> eh = getActionEventHandler();
        hyperlink.setOnAction(eh);

        linkDisableInvalidateListener = (self) -> {
            boolean disabled = false;
            if (linkDisableProperty != null) {
                disabled = linkDisableProperty.getValue();
            }

            label.setVisible(disabled);
            label.setManaged(disabled);
            
            hyperlink.setVisible(!disabled);
            hyperlink.setManaged(!disabled);
        };

        setGraphic(hbox);

        linkDisableProperty = getLinkDisabledProperty();
        if (linkDisableProperty != null) {
            linkDisableProperty.addListener(linkDisableInvalidateListener);
        }

        // 初回変更通知を行う.
        linkDisableInvalidateListener.invalidated(linkDisableProperty);
    }
    private final ObjectProperty<Callback<Integer, EventHandler<ActionEvent>>> actionEventHandlerCallback = new SimpleObjectProperty<>(this, "actionEventHandlerCallback");

    /**
     * ハイパーリンクアクションを返すコールバックのプロパティ
     * @return 
     */
    public final ObjectProperty<Callback<Integer, EventHandler<ActionEvent>>> actionEventHandlerCallbackProperty() {
        return actionEventHandlerCallback;
    }

    /**
     * ハイパーリンクアクションを返すコールバックを取得する.
     * @return 
     */
    private Callback<Integer, EventHandler<ActionEvent>> getActionEventHandlerCallback() {
        return actionEventHandlerCallbackProperty().get();
    }

    /**
     * 現在の行に対するハイパーリンクアクションを返す.
     * なければnullを返す.
     * @return ハイパーリンクのアクション、もしくはnull
     */
    private EventHandler<ActionEvent> getActionEventHandler() {
        return getActionEventHandlerCallback() != null ? getActionEventHandlerCallback().call(getIndex()) : null;
    }
    private final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> linkDisabledStateCallback = new SimpleObjectProperty<>(this, "linkDisabledStateCallback");

    /**
     * ハイパーリンクを無効とするかを示すプロパティを返すコールバック
     * @return 
     */
    public final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> linkDisabledStateCallbackProperty() {
        return linkDisabledStateCallback;
    }

    /**
     * ハイパーリンクを無効とするかを示すプロパティを返すコールバックを設定する.
     * @param value 
     */
    public final void setLinkDisabledStateCallback(Callback<Integer, ObservableValue<Boolean>> value) {
        linkDisabledStateCallbackProperty().set(value);
    }

    /**
     * ハイパーリンクを無効とするかを示すプロパティを返すコールバックを取得する.
     * @return 
     */
    public final Callback<Integer, ObservableValue<Boolean>> getLinkDisabledStateCallback() {
        return linkDisabledStateCallbackProperty().get();
    }

    /**
     * 現在の行に対するハイパーリンクを無効とするかを示すプロパティを返す.
     * なければnullを返す.
     * @return 
     */
    private ObservableValue<Boolean> getLinkDisabledProperty() {
        return getLinkDisabledStateCallback() != null ? getLinkDisabledStateCallback().call(getIndex()) : null;
    }
}
