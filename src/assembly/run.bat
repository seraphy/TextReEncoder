rem JAVA8用起動バッチ
rem PATHがJRE8に通っていればダブルクリックでも起動できます。
if "%JAVA_HOME%" == "" set JAVA_HOME=C:\Java\jdk1.8.0_121

set PATH=%JAVA_HOME%\bin;%SystemRoot%\System32;%SystemRoot%
java -jar TextReEncoder-1.5-SNAPSHOT.jar
