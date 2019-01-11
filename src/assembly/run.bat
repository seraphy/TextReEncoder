if "%JAVA_HOME%" == "" set JAVA_HOME=C:\Java\jdk1.8.0_121

set PATH=%JAVA_HOME%\bin;%SystemRoot%\System32;%SystemRoot%
java -jar ${project.artifactId}-${project.version}.jar
