package jp.seraphyware.textencodechanger;

/**
 * JavaFXアプリを起動するためだけのエントリポイント。
 * mainメソッドのクラスをロードするときに javafx.application.Application を
 * 継承していると、モジュールロードを試行してクラスロードに失敗するため、別クラスにする必要がある。
 * https://torutk.hatenablog.jp/entry/2018/12/01/215113
 * http://mail.openjdk.java.net/pipermail/openjfx-dev/2018-June/021980.html
 */
public class Main {
    
    public static void main(String[] args) {
        javafx.application.Application.launch(MainApp.class, args);
    }
}
