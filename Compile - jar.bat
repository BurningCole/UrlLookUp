javac -d out -cp libs/* -sourcepath src src/*.java
jar cfm UrlLookUp.jar Manifest.txt -C out .
pause
