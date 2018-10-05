テキストファイル文字コード変換ツール(TextReEncoder) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
=================================================
指定したディレクトリ以下にあるテキストファイルの文字コードを一括変換するGUIツールです。

JavaFX8を使用しているため、Java8以降のランタイムが必要です。

リリースの種類
------------

- ver1.4は、Java8がインストールされている環境では単一のjarとして実行できます。Java11には対応していません。(JavaFXを使用しているため)
- ver1.5は、Java\8, Java11のいずれでも動作します。ただし、単一jarではなく、libフォルダ上にライブラリが分離されています。(主にJavaFXのモジュールのため)
  - java11で実行するには ```run11.bat``` からの実行が必要です。(jarファイルのダブルクリックでは開きません。また、java11のパスの設定が必要です。)

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

プロジェクトはNetBeans8で作成されました。(現在はNetBeans9でメンテナンスしています)

FXMLファイルに編集には、SceneBuilder2を使用しています。

 
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
