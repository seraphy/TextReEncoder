テキストファイル文字コード変換ツール(TextReEncoder) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Build Status](https://travis-ci.com/seraphy/TextReEncoder.svg?branch=master)](https://travis-ci.com/seraphy/TextReEncoder)
=================================================
指定したディレクトリ以下にあるテキストファイルの文字コードを一括変換するGUIツールです。

JavaFX8を使用しているため、Java8以降のランタイムが必要です。

(Linuxなど、OpenJDK系のJDK/JREを使うは愛は、JavaFX8のインストールも必須です。)

JavaFX11のランタイムを同梱しているので、Java11環境でも動作します。


使い方
------

任意のフォルダを指定し「チェック」を押下すると、
そのフォルダにある指定したパターンに合致する名前のテキストファイルを読み込み、
その文字コードを推測し、テーブルに結果を一覧表示します。

![screen capture 1](src/site/resources/images/screen-capture1.png?raw=true "screen capture1")

その後、テーブルで必要なファイルを選択した状態で「変換」ボタンを押下すると、
出力先に指定されたディレクトリに文字コードが変換されて出力されます。

以下の文字コードをサポートします。
- UTF-8
- いわゆるShift_JIS (MS932/csWindows31J)
- EUC-JP
- UTF-16LE
- UTF-16BE

UTFの場合はBOMあり、BOMなしの区別があります。

ビルド方法
----------------
[![Build Status](https://travis-ci.org/seraphy/TextReEncoder.svg)](https://travis-ci.org/seraphy/TextReEncoder)

ビルドにはMaven(3.5.4)が必要です。

Maven Wrapperを入れているため、Mavenをインストールしていない環境であれば、JAVA_HOME環境変数を設定して

```
./mvnw clean package -Plaunch4j
```

のようにしてMavenの準備からビルドまでを一括して行うことができます。

(java8以降でコンパイルします。java8の場合、javafx8がインストール済みの環境でないとビルドできません)

Spring-BootのUberJarで依存jarを同梱する、単一のjarファイルが作成されます。

プロファイルで *launch4j* を指定した場合は、launch4jによるexeラッパーが作成されます。


### 備考

トリッキーですが、コンパイラバージョンはjava8をターゲットにしていますが、JavaFX11のランタイムを依存jarに含めています。

これにより、JRE/JDK上にJavaFX8がある環境ではJavaFX11のjarは単に無視され、JavaFX8として動作します。

JavaFXランタイムが存在しないJava11のJRE/JDKで実行する場合は、同梱しているJavaFX11のランタイムが有効になります。

ただし、Java8環境でJavaFX8がまだインストールされていない場合は、JavaFX11をロードしようとして*クラスバージョンの不一致*で起動できません。

プロジェクトはNetBeans8で作成されました。

現在はNetBeans10でメンテナンスしています。

FXMLファイルに編集には、SceneBuilder3を使用しています。

 
ライセンス
----------
Copyright &copy; 2015-2018 seraphyware

Licensed under the [Apache License, Version 2.0][Apache]

[Apache]: http://www.apache.org/licenses/LICENSE-2.0


○ サーチアイコンにはJXNBLKさんの以下のもの(MITライセンス下)を
https://www.iconfinder.com/icons/293645/search_stroked_icon#size=128

○ CircularアイコンにはFreepikの
http://www.flaticon.com/free-icon/arrows-circle_32220
を少しいじったものを使用しています。
