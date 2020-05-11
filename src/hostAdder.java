import javafx.application.Platform;
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import java.sql.*;

public class hostAdder{

	Stage primaryStage;
	Scene oldScene;
	Pane pane = new Pane();
	TextField urlEditField = new TextField();
	TextField acceptEditField = new TextField();
	TextField excludeEditField = new TextField();
	
	public hostAdder(Stage primaryStage){
		this.primaryStage=primaryStage;
		oldScene=primaryStage.getScene();
		
		pane.setPrefSize(500,300);
		Scene scene= new Scene(pane, 500, 300);
		primaryStage.setScene(scene);
	}
	
	public void HandleAddMenu(String baseUrl){
		
		Label url= new Label("Insert Website Segment of "+baseUrl);
		Label accept=new Label("Insert class/tag of next chapter links on webpage");
		Label exclude=new Label("Insert html on valid webpage (not missing chapter page)");
		
		//set part X positions
		url.layoutXProperty().bind(primaryStage.widthProperty().subtract(url.widthProperty()).divide(2));
		urlEditField.layoutXProperty().bind(primaryStage.widthProperty().subtract(urlEditField.widthProperty()).divide(2));
		
		accept.layoutXProperty().bind(primaryStage.widthProperty().subtract(accept.widthProperty()).divide(2));
		acceptEditField.layoutXProperty().bind(primaryStage.widthProperty().subtract(acceptEditField.widthProperty()).divide(2));
		
		exclude.layoutXProperty().bind(primaryStage.widthProperty().subtract(exclude.widthProperty()).divide(2));
		excludeEditField.layoutXProperty().bind(primaryStage.widthProperty().subtract(excludeEditField.widthProperty()).divide(2));
		
		//set textBox widths
		urlEditField.prefWidthProperty().bind(primaryStage.widthProperty().divide(3).multiply(2));
		acceptEditField.prefWidthProperty().bind(urlEditField.widthProperty());
		excludeEditField.prefWidthProperty().bind(urlEditField.widthProperty());
		
		//set part Y positions
		urlEditField.layoutYProperty().bind(url.layoutYProperty().add(url.heightProperty()));
		
		accept.layoutYProperty().bind(urlEditField.layoutYProperty().add(urlEditField.heightProperty().multiply(2)));
		acceptEditField.layoutYProperty().bind(accept.layoutYProperty().add(accept.heightProperty()));
		
		exclude.layoutYProperty().bind(acceptEditField.layoutYProperty().add(acceptEditField.heightProperty().multiply(2)));
		excludeEditField.layoutYProperty().bind(exclude.layoutYProperty().add(exclude.heightProperty()));
		
		//back button
		Button backBtn =new Button("Cancel");
		backBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				primaryStage.setScene(oldScene);
			}
		});
		backBtn.layoutXProperty().bind(primaryStage.widthProperty().multiply(2).divide(3).subtract(backBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		backBtn.layoutYProperty().bind(excludeEditField.layoutYProperty().add(excludeEditField.heightProperty().multiply(2)));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		//run sql & reload button
		Button FinishedBtn =new Button("Add Value");
		FinishedBtn.setOnAction(HandleAdd);
		FinishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(3).subtract(FinishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		FinishedBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		pane.getChildren().addAll(url,accept,exclude,backBtn,FinishedBtn,urlEditField,acceptEditField,excludeEditField);
	}
	
	private EventHandler<ActionEvent> HandleAdd = new EventHandler<ActionEvent>(){
		@Override
		public void handle(ActionEvent e){
			if(excludeEditField.getText().length()==0||acceptEditField.getText().length()==0||urlEditField.getText().length()==0)
				return;//return if values not put in
			//add
			String sql = "INSERT INTO websites(accept,exclude,url) VALUES (\"<Accept>\",<Exclude>,\"<Url>\");";
			DbBasic dataBase = GUI.getDataBase();
			String url=urlEditField.getText();
			String usedUrl="";
			int webId=0;
			sql=sql.replace("<Accept>",prepArg(acceptEditField.getText()));
			sql=sql.replace("<Exclude>",prepArg(excludeEditField.getText()));
			sql=sql.replace("<Url>",prepArg(urlEditField.getText()));
			dataBase.runSQL(sql);
			//System.out.println(sql);
			dataBase.close();
			GUI.logInfo("Website: "+url+" added");
		}
	};
	
	private String prepArg(String s){
		s=s.replace("\"","\"\"");
		return s;
	}
}