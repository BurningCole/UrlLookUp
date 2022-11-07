javac --module-path "libs/javafx-sdk-18.0.1/lib" --add-modules javafx.controls,javafx.fxml -d out -cp libs/* -sourcepath src src/*.java
jar cfm UrlLookUp.jar Manifest.txt -C out .
pause
