package jp.seraphyware.textencodechanger;

import javafx.application.Application;

/**
 * エントリポイント
 * @author seraphy
 */
public class Main {
    
    public static void main(String[] args) {
        // JavaFX11の関連jarをクラスパスで通した場合、
        // Application派生クラス内のmainメソッドをエントリポイントにすると
        // Applicationクラスの解決のためにJavaFXモジュールを探索して発見できず
        // エラーで終了してしまう問題があるため、Application派生クラスの外で
        // mainを呼び出すようにする。
        System.setProperty("java.awt.headless", "false");
        Application.launch(MainApp.class, args);
    }
}
