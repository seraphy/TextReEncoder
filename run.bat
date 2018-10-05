set JAVA_HOME=C:\Java\jdk-11
set PATH=%JAVA_HOME%\bin;C:\Windows\System32;C:\Windows
java -p target\lib --add-modules javafx.controls,javafx.fxml,javax.inject -jar target\TextReEncoder-1.5-SNAPSHOT.jar
