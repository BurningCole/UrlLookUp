import javafx.application.Platform;
import javafx.beans.binding.ListExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class ScanGUI{
	int values=0;
	Region prevPane;
	ArrayList<CheckBox> checkboxes = new ArrayList<CheckBox>();
	List<UrlUpdate> updates;
	Stage primaryStage;
	Scene oldScene;
	ScrollPane sPane = new ScrollPane();
	
	public ScanGUI(Stage primaryStage){
		this.primaryStage=primaryStage;
		oldScene=primaryStage.getScene();
		
		sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sPane.setPrefSize(500,900);
		Scene scene= new Scene(sPane, 500, 900);
		primaryStage.setScene(scene);
	}
	
	public void HandleScan(){
		
		values=0;
		//final Integer values=0;
		prevPane=new Region();
		
		DbBasic db = GUI.getDataBase();
		
		Pane internalPane=new Pane();
		sPane.setContent(internalPane);
		
		internalPane.prefWidthProperty().bind(sPane.widthProperty());
		
		//create lookup
		IUpdateChecker lookup=new UrlLookUp(db);
		
		updates=lookup.getResults();
		if( updates instanceof ListExpression){
			((ListExpression)updates).sizeProperty().addListener(new ChangeListener(){
				@Override public void changed(ObservableValue o,Object oldVal, Object newVal){
					for(;values<updates.size();values++){
						UrlUpdate curUpdate=updates.get(values);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								prevPane=addScanResult(curUpdate,internalPane,prevPane,checkboxes);
							}
						});
					}
				}
			});
		}
		//start scan
		
		Task<Void> scan = new Task<Void>(){
			@Override 
			public Void call() throws InterruptedException {
				lookup.startScan();
				return null;
			}
		};
		scan.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t)
			{
				db.close();
				
				if(!(updates instanceof ListExpression)){
					for(;values<updates.size();values++){//add all aditional values
						//get update
						UrlUpdate curUpdate=updates.get(values);
						prevPane=addScanResult(curUpdate,internalPane,prevPane,checkboxes);
						internalPane.prefHeightProperty().bind(prevPane.heightProperty().multiply(2).add(prevPane.layoutYProperty()));
					}
				}
				
				if(updates.size()==0){
					//tell user no updates found
				}
				
				//back button
				Button backBtn =new Button("Back");
				backBtn.setOnAction(new EventHandler<ActionEvent>() { 
					public void handle(ActionEvent e)
					{ 
						primaryStage.setScene(oldScene);
					}
				});
				backBtn.layoutXProperty().bind(primaryStage.widthProperty().multiply(2).divide(3).subtract(backBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
				backBtn.layoutYProperty().bind(backBtn.heightProperty().add(prevPane.heightProperty().add(prevPane.layoutYProperty())));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
				
				//run sql & reload button
				Button FinishedBtn =new Button("mark+refresh");
				FinishedBtn.setOnAction(HandleRescan);
				FinishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(3).subtract(FinishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
				FinishedBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
				
				internalPane.getChildren().addAll(backBtn,FinishedBtn);
				internalPane.prefHeightProperty().bind(backBtn.heightProperty().multiply(2).add(backBtn.layoutYProperty()));
				//internalPane.setWidth(sPane.getWidth());
				internalPane.prefWidthProperty().bind(sPane.widthProperty());
			}
		});
		new Thread(scan).start();
	}
	
	private EventHandler<ActionEvent> HandleRescan = new EventHandler<ActionEvent>(){
		@Override
		public void handle(ActionEvent e)
		{ 
			//calculate sql and run
			DbBasic dataBase = GUI.getDataBase();
			for(int i=0;i<updates.size();i++){
				if(checkboxes.get(i).isSelected())//switch to if check checked
					switch(updates.get(i).getType()){
						case UrlUpdate.NORMAL:	//normal update
							dataBase.runSQL(updates.get(i).getSQLStatement());
							break;
						case UrlUpdate.MISSING_EXCLUDE:
							if(updates.get(i).urls().size()!=0){
								String SQLUpdate=updates.get(i).getSQLStatement();
								List<String> data =updates.get(i).urls();
								String change="";
								for(int j=0;j<data.size();j++){
									if(j>0){
										change+=",";
									}
									change+=data.get(j);
								}
								SQLUpdate = SQLUpdate.replace("^",change);
								dataBase.runSQL(
									SQLUpdate
								);
							}
							break;
						default:				//other unhandled update
							break;
					}
			}
			dataBase.close();
			
			//reload scene
			HandleScan();
		}
	};
	
	private TitledPane addScanResult(UrlUpdate curUpdate,Pane internalPane, Region prevPane,List checkboxes){
		//add new segment
		Pane segPane = new Pane();//pane to put url labels in
		
		
		//name,url links, check/
		int urls=0;
		Button link=null;
		if(curUpdate.urls()!=null&&curUpdate.getType()==UrlUpdate.NORMAL){
			for(String website:curUpdate.urls()){
				//add single url part
				link = new Button(website);
				link.prefWidthProperty().bind(segPane.widthProperty());
				link.maxWidthProperty().bind(segPane.widthProperty());
				link.layoutYProperty().bind(link.heightProperty().multiply(urls));
				link.setOnAction(new EventHandler<ActionEvent>() { 
					public void handle(ActionEvent e)
					{ 
						try{
							String finalWebsite;
							if(!(website.startsWith("http")||website.startsWith("Http"))){
								finalWebsite="http://"+website;
							}else{
								finalWebsite=website;
							}
						java.awt.Desktop.getDesktop().browse(new java.net.URI(finalWebsite));
						}catch(Exception ex){
							System.out.println("Error");
						}
					}
				});
				urls++;
				segPane.getChildren().add(link);
			}
			if(link!=null)
			segPane.prefHeightProperty().bind(link.heightProperty().multiply(urls));
		}else
			if(curUpdate.getType()==UrlUpdate.MISSING_EXCLUDE){
				TextField urlEditField = new TextField(curUpdate.getUrl());
				urlEditField.prefWidthProperty().bind(segPane.widthProperty());
				urlEditField.maxWidthProperty().bind(segPane.widthProperty());
				urlEditField.textProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
						curUpdate.urls().clear();
						DbBasic db = GUI.getDataBase();
						ResultSet rs=db.doQuery("SELECT url,webId FROM websites");
						try{
							while(rs.next()){
								if(newValue.startsWith(rs.getString("url"))){
									String newUrl=newValue.substring(
										rs.getString("url").length()
									);
									if(!curUpdate.getUrl().startsWith(rs.getString("url")))
										curUpdate.urls().add("webId = "+rs.getString("webId")+" ");
									if(!curUpdate.getUrl().endsWith(newUrl))
										curUpdate.urls().add("URL = '"+newUrl+"' ");
									break;
								}
							}
						}catch(SQLException e){
							e.printStackTrace();
						}
						db.close();
					}
				});
				
				segPane.getChildren().add(urlEditField);
				segPane.prefHeightProperty().bind(urlEditField.heightProperty());
				
			}
		TitledPane segment=new TitledPane(curUpdate.name(),segPane);
		segment.layoutYProperty().bind(prevPane.layoutYProperty().add(prevPane.heightProperty()));
		segment.setExpanded(false);
		prevPane=segment;
		
		CheckBox check = new CheckBox();
		
		segment.prefWidthProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(2)));// 7/8 width
		segment.maxWidthProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(2)));// 7/8 width
		
		segPane.prefWidthProperty().bind(segment.widthProperty());
		segPane.maxWidthProperty().bind(segment.widthProperty());
		
		check.layoutYProperty().bind(segment.layoutYProperty().add(segment.heightProperty().subtract(check.heightProperty()).divide(2)));
		check.layoutXProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(7).divide(4)));
		checkboxes.add(check);
		
		internalPane.getChildren().addAll(segment,check);
		return segment;
	}
}