テキストファイル文字コード変換ツール(TextReEncoder) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
=================================================
指定したディレクトリ以下にあるテキストファイルの文字コードを一括変換するGUIツールです。

JavaFX8を使用しているため、Java8以降のランタイムが必要です。

リリースの種類
------------

- ver1.4は、Java8がインストールされている環境では単一のjarとして実行できます。Java11には対応していません。(JavaFXを使用しているため)
- ver1.5は、Java8, Java11用にバイナリビルドが分かれています。
  - Launch4jによるexeラッパーを用意してあり、Java8用はJava8のみ探索します。Java11用はJava11以降を探索します。
    - Java8は各種ライブラリもひとまとめにした単一のexeファイルです。Java11ではexeは起動スタブであり、JavaFXをはじめとする各種ライブラリをlibフォルダに格納しています。
    - いずれもexeのあるフォルダ下にjreフォルダがあれば、レジストリによる探索前に、そのjavaが使われます。
    - Launch4jのヘッドをカスタマイズしており、JRE/JDKが発見できない場合はユーザーにJAVA_HOMEを問い合わせるフォルダ選択ダイアログが表示されます。
      - https://github.com/seraphy/Launch4jHead
  - jar形式を使いJava11環境で実行するには ```run11.bat``` からの実行が必要です。(jarファイルのダブルクリックでは開きません。また、java11のパスの設定が必要です。)
    - jar形式(実行可能jar)をダブルクリックで実行するには、jarがJava8に関連づられていなければなりません。(Java11以降に関連づられている場合はJavaFXが利用できず起動できません。)

ver1.4/1.5に機能的な差異はありません。

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
./mvnw clean package -Pjava11
```

のようにしてMavenの準備からビルドまでを一括して行うことができます。

Java8, Java11用は、それぞれプロファイルによってビルドを切り替えます。

プロジェクトはNetBeans8で作成されました。(現在はNetBeans10でメンテナンスしています)

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
