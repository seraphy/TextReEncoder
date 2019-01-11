if "%JAVA_HOME%" == "" set JAVA_HOME=C:\Java\jdk-11

set PATH=%JAVA_HOME%\bin;%SystemRoot%\System32;%SystemRoot%
java -p lib --add-modules javafx.controls,javafx.fxml,javax.inject -jar ${project.artifactId}-${project.version}.jar
