テキストファイル文字コード変換ツール
======================
指定したディレクトリ以下にあるテキストファイルの文字コードを一括変換するGUIツールです。

JavaFX8を使用しているため、Java8以降のランタイムが必要です。


使い方
------

任意のフォルダを指定し「チェック」を押下すると、
そのフォルダにある指定したパターンに合致する名前のテキストファイルを読み込み、
その文字コードを推測し、テーブルに結果を一覧表示します。

![screen capture 1](src/site/resources/images/screen-capture1.png?raw=true "screen capture1")

その後、テーブルで必要なファイルを選択した状態で「変換」ボタンを押下すると、
出力先に指定されたディレクトリに文字コードが変換されて出力されます。

 
ビルド方法
----------------
[![Build Status](https://travis-ci.org/seraphy/TextEncodeChanger.svg)](https://travis-ci.org/seraphy/TextEncodeChanger)

ビルドにはMavenが必要です。

プロジェクトはNetBeans8で作成されました。

FXMLファイルに編集には、SceneBuilder2を使用しています。

 
ライセンス
----------
Copyright &copy; 2015 seraphyware

Licensed under the [Apache License, Version 2.0][Apache]

[Apache]: http://www.apache.org/licenses/LICENSE-2.0
