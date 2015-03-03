概要
======================
これはJavaFX8によるGUIの「テキストファイル文字コード変換ツール」です。

(JavaFX8 + Spring4 + Log4j2 の組み合わせの技術習得を兼ねて作成した。)



## 処理内容

任意のフォルダを走査し、指定したパターンに合致する名前のテキストファイルを
読み込み、その文字コードを推定し、テーブルに結果を一覧表示します。

その後、テーブルで必要なファイルを選択し、指定した文字コードで変換して保存します。

![screen-capture1.png](./images/screen-capture1.png)


## リリース

https://github.com/seraphy/TextEncodeChanger/releases


## ビルド方法

NetBeans8のプロジェクトです。

GUIのデザインにはScene Builder2を使用しています。

実行およびビルドにはJava8が必要です。
