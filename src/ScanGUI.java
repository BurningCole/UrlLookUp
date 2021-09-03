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
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class ScanGUI{
	int values=0;
	Region prevPane;
	ArrayList<ScanStruct> results = new ArrayList<ScanStruct>();
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
		results.clear();
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
								prevPane=addScanResult(curUpdate,internalPane,prevPane,results);
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
				GUI.getLogger().info("Scan Started");
				lookup.startScan();
				return null;
			}
		};
		scan.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t)
			{
				GUI.getLogger().info("Scan Finished");
				db.close();
				
				if(!(updates instanceof ListExpression)){
					for(;values<updates.size();values++){//add all aditional values
						//get update
						UrlUpdate curUpdate=updates.get(values);
						prevPane=addScanResult(curUpdate,internalPane,prevPane,results);
						internalPane.prefHeightProperty().bind(prevPane.heightProperty().multiply(2).add(prevPane.layoutYProperty()));
					}
				}
				
				if(updates.size()==0){
					//tell user no updates found
				}
				GUI.getLogger().info("Found "+updates.size()+" updates/changes/errors");
				
				//back button
				Button backBtn =new Button("Back");
				backBtn.setOnAction(new EventHandler<ActionEvent>() { 
					public void handle(ActionEvent e)
					{ 
						primaryStage.setScene(oldScene);
					}
				});
				backBtn.layoutXProperty().bind(primaryStage.widthProperty().multiply(3).divide(4).subtract(backBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
				backBtn.layoutYProperty().bind(backBtn.heightProperty().add(prevPane.heightProperty().add(prevPane.layoutYProperty())));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
				
				//run sql & reload button
				Button reloadBtn =new Button("mark+refresh");
				reloadBtn.setOnAction(new EventHandler<ActionEvent>() { 
					public void handle(ActionEvent e){ 
						//reload scene
						HandleMarking();
						HandleScan();
					}
				});
				reloadBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(4).subtract(reloadBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
				reloadBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
				
				Button finishedBtn =new Button("mark+back");
				finishedBtn.setOnAction(new EventHandler<ActionEvent>() { 
					public void handle(ActionEvent e){ 
						//reload scene
						HandleMarking();
						primaryStage.setScene(oldScene);
					}
				});
				finishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(2).subtract(finishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
				finishedBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
				
				
				internalPane.getChildren().addAll(backBtn,finishedBtn,reloadBtn);
				internalPane.prefHeightProperty().bind(backBtn.heightProperty().multiply(2).add(backBtn.layoutYProperty()));
				//internalPane.setWidth(sPane.getWidth());
			}
		});
		new Thread(scan).start();
	}
	
	private void HandleMarking(){
		//calculate sql and run
		DbBasic dataBase = GUI.getDataBase();
		for(int i=0;i<updates.size();i++){
			if(results.get(i).getChecked()){//switch to if check checked
				UrlUpdate update=results.get(i).getUpdate();
				//GUI.getLogger().info("Update: "+update.name());
				switch(update.getType()){
					case UrlUpdate.NORMAL:	//normal update
						dataBase.runSQL(update.getSQLStatement());
						break;
					case UrlUpdate.MISSING_EXCLUDE:
						ResultSet rs=dataBase.doQuery("SELECT url,webId FROM websites");
						String SQLUpdate=update.getSQLStatement();
						int website=-1;
						String url="";
						try{
							while(rs.next()){
								if(update.getUrl().startsWith(rs.getString("url"))){
									url=update.getUrl().substring(
										rs.getString("url").length()
									);
									website=rs.getInt("webId");
									break;
								}
							}
							if(website==-1){
								GUI.getLogger().info("Unknown url: "+url);
								//new hostAdder(primaryStage).HandleAddMenu(url);
							}
						}catch(SQLException ex){
							ex.printStackTrace();
							GUI.getLogger().warning("SQL error on "+url+"SELECT url,webId FROM websites");
						}
						
						if(website!=-1){
							String change="webId = "+website+",URL = '"+url+"' ";
							SQLUpdate = SQLUpdate.replace("^",change);
							dataBase.runSQL(
								SQLUpdate
							);
							
						}else{
							GUI.getLogger().warning("Update failed "+update.getUrl());
						}
						break;
					default:				//other unhandled update
						GUI.getLogger().info("Unimplemented update type id: "+update.getType());
						GUI.getLogger().warning("Update failed "+update.getUrl());
						break;
				}
			}else if(results.get(i).getDeleteChecked()){
				UrlUpdate update = results.get(i).getUpdate();
				dataBase.runSQL(update.getDeleteStatement());
				GUI.getLogger().info("Deleted: "+update.name()+" : "+update.getUrl());
			}
		}
		dataBase.close();
	}

	private class ScanStruct{
		TitledPane pane;
		CheckBox checkbox;
		CheckBox deleteBox;
		UrlUpdate update;
		
		public ScanStruct(TitledPane p,CheckBox c, CheckBox d, UrlUpdate u){
			pane=p;
			checkbox=c;
			deleteBox=d;
			update=u;
		}
		public TitledPane getTitledPane(){
			return pane;
		}
		public boolean getChecked(){
			return checkbox.isSelected();
		}
		public boolean getDeleteChecked(){
			return deleteBox.isSelected();
		}
		public UrlUpdate getUpdate(){
			return update;
		}
	}
	
	private Button generateButton(String website,Pane container,int index){
		Button link = new Button(website);
		link.prefWidthProperty().bind(container.widthProperty());
		link.maxWidthProperty().bind(container.widthProperty());
		link.layoutYProperty().bind(link.heightProperty().multiply(index));
		link.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				String finalWebsite="Unknown";
				try{
					if(!(website.startsWith("http")||website.startsWith("Http"))){
						finalWebsite="http://"+website;
					}else{
						finalWebsite=website;
					}
				java.awt.Desktop.getDesktop().browse(new java.net.URI(finalWebsite));
				}catch(Exception ex){
					System.out.println("Error");
					GUI.getLogger().warning("website \""+finalWebsite+"\" conection error");
				}
			}
		});
		container.getChildren().add(link);
		return link;
	}
	
	private TitledPane addScanResult(UrlUpdate curUpdate,Pane internalPane, Region prevPane,List results){
		//add new segment
		Pane segPane = new Pane();//pane to put url labels in
		
		
		//name,url links, check/
		int urls=0;
		
		Color segColour=Color.RED;
		Button link = null;
		if(curUpdate.urls()!=null&&curUpdate.getType()==UrlUpdate.NORMAL){		
			segColour=Color.MIDNIGHTBLUE;
			for(String website:curUpdate.urls()){
				link = generateButton(website,segPane,urls);
				urls++;
			}
			if(link!=null)
				segPane.prefHeightProperty().bind(link.heightProperty().multiply(urls));
		}else{
			String errorUrl = curUpdate.getUrl();
			List<String> correctUrls = curUpdate.urls();
			correctUrls.remove(errorUrl);
			for(String website:correctUrls){
				link = generateButton(website,segPane,urls);
				urls++;
			}
			Label issue = new Label(curUpdate.getError());
			issue.prefWidthProperty().bind(segPane.widthProperty());
			issue.maxWidthProperty().bind(segPane.widthProperty());
			if(link!=null){
				issue.layoutYProperty().bind(link.heightProperty().multiply(urls));
			}
			segColour=Color.CRIMSON;
			TextField urlEditField = new TextField(errorUrl);
			urlEditField.prefWidthProperty().bind(segPane.widthProperty());
			urlEditField.maxWidthProperty().bind(segPane.widthProperty());
			urlEditField.layoutYProperty().bind(issue.heightProperty().add(issue.layoutYProperty()));
			
			urlEditField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
					curUpdate.setUrl(newValue);
				}
			});
			
			segPane.getChildren().add(issue);
			segPane.getChildren().add(urlEditField);
			segPane.prefHeightProperty().bind(urlEditField.heightProperty().add(urlEditField.layoutYProperty()));
		}
		TitledPane segment=new TitledPane(curUpdate.name(),segPane);
		segment.setTextFill(segColour);
		segment.layoutYProperty().bind(prevPane.layoutYProperty().add(prevPane.heightProperty()));
		segment.setExpanded(false);
		prevPane=segment;
		
		CheckBox check = new CheckBox();
		CheckBox delete = new CheckBox();
		
		delete.setBackground(new Background(new BackgroundFill(Color.CRIMSON,CornerRadii.EMPTY,Insets.EMPTY)));
		
		segment.prefWidthProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(3)));// 7/8 width
		segment.maxWidthProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(3)));// 7/8 width
		
		segPane.prefWidthProperty().bind(segment.widthProperty());
		segPane.maxWidthProperty().bind(segment.widthProperty());
		
		check.layoutYProperty().bind(segment.layoutYProperty().add(segment.heightProperty().subtract(check.heightProperty()).divide(2)));
		check.layoutXProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(3)));
		
		delete.layoutYProperty().bind(segment.layoutYProperty().add(segment.heightProperty().subtract(delete.heightProperty()).divide(2)));
		delete.layoutXProperty().bind(internalPane.widthProperty().subtract(delete.widthProperty().multiply(7).divide(4)));
		
		results.add(new ScanStruct(segment,check,delete,curUpdate));
		
		internalPane.getChildren().addAll(segment,check,delete);
		return segment;
	}
}