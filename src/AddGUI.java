import javafx.application.Platform;
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AddGUI{
	
	TextField urlEditField = new TextField();
	TextField aliasEditField = new TextField();
	Stage primaryStage;
	Scene oldScene;
	Pane pane = new Pane();
	
	public AddGUI(Stage primaryStage){
		this.primaryStage=primaryStage;
		oldScene=primaryStage.getScene();
		
		pane.setPrefSize(500,300);
		Scene scene= new Scene(pane, 500, 300);
		primaryStage.setScene(scene);
	}
	
	public void HandleAddMenu(){
		
		Label alias=new Label("Insert Name To Assign");
		Label url= new Label("Insert Entire URL");
		
		alias.layoutXProperty().bind(primaryStage.widthProperty().subtract(alias.widthProperty()).divide(2));
		aliasEditField.layoutXProperty().bind(primaryStage.widthProperty().subtract(aliasEditField.widthProperty()).divide(2));
		
		url.layoutXProperty().bind(primaryStage.widthProperty().subtract(url.widthProperty()).divide(2));
		urlEditField.layoutXProperty().bind(primaryStage.widthProperty().subtract(urlEditField.widthProperty()).divide(2));
		
		aliasEditField.prefWidthProperty().bind(primaryStage.widthProperty().divide(3).multiply(2));
		urlEditField.prefWidthProperty().bind(aliasEditField.widthProperty());
		
		aliasEditField.layoutYProperty().bind(alias.layoutYProperty().add(alias.heightProperty()));
		
		url.layoutYProperty().bind(aliasEditField.layoutYProperty().add(aliasEditField.heightProperty().multiply(2)));
		urlEditField.layoutYProperty().bind(url.layoutYProperty().add(url.heightProperty()));
		
		//back button
		Button backBtn =new Button("Back");
		backBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				primaryStage.setScene(oldScene);
			}
		});
		backBtn.layoutXProperty().bind(primaryStage.widthProperty().multiply(2).divide(3).subtract(backBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		backBtn.layoutYProperty().bind(urlEditField.layoutYProperty().add(urlEditField.heightProperty().multiply(2)));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		//run sql & reload button
		Button FinishedBtn =new Button("Add Value");
		FinishedBtn.setOnAction(HandleAdd);
		FinishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(3).subtract(FinishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		FinishedBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		pane.getChildren().addAll(url,backBtn,FinishedBtn,aliasEditField,urlEditField,alias);
	}
	
	private EventHandler<ActionEvent> HandleAdd = new EventHandler<ActionEvent>(){
		@Override
		public void handle(ActionEvent e){
			if(aliasEditField.getText().length()==0||urlEditField.getText().length()==0)
				return;//return if values not put in
			//add
			String sql = "INSERT INTO urls(alias,webId,url,updated) VALUES (\"<Alias>\",<WebId>,\"<Url>\",\"<Date>\");";
			DbBasic dataBase = GUI.getDataBase();
			String url=urlEditField.getText();
			GUI.logInfo("Adding url: "+url);
			String usedUrl="";
			int webId=0;
			ResultSet rs=dataBase.doQuery("SELECT url,webId FROM websites");
			try{
				SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
				while(rs.next()){
					if(url.startsWith(rs.getString("url"))){
						webId = rs.getInt("webId");
						usedUrl=url.substring(
								rs.getString("url").length()
							);
						break;
					}
				}
				rs.close();
				if(usedUrl.length()==0){
					//open host adder
					GUI.logInfo("Unknown url");
					new hostAdder(primaryStage).HandleAddMenu(url);
					return;
				}
				if(usedUrl.length()>0){
					sql=sql.replace("<Alias>",prepArg(aliasEditField.getText()));
					sql=sql.replace("<WebId>",String.valueOf(webId));
					sql=sql.replace("<Url>",prepArg(usedUrl));
					sql=sql.replace("<Date>",DATE_FORMAT.format(new Date()));
					dataBase.runSQL(sql);
					//System.out.println(sql);
					urlEditField.setText("");
					aliasEditField.setText("");
				}
			}catch(SQLException ex){
				ex.printStackTrace();
				return;
			}
			dataBase.close();
		}
	};
	
	private String prepArg(String s){
		s=s.replace("\"","\"\"");
		return s;
	}
}