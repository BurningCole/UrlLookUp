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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class UpdateGUI{
	int values=0;
	ArrayList<WebsiteVal> results = new ArrayList<WebsiteVal>();
	Stage primaryStage;
	Scene oldScene;
	ScrollPane sPane = new ScrollPane();
	
	public UpdateGUI(Stage primaryStage){
		this.primaryStage=primaryStage;
		oldScene=primaryStage.getScene();
		
		sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sPane.setPrefSize(500,900);
		Scene scene= new Scene(sPane, 500, 900);
		primaryStage.setScene(scene);
	}
	public static class OrderTypes{
		public final static int UPDATED = 0;
		public final static int ID = 1;
		public final static int NAME = 2;
		public final static int URL = 3;
		public final static int ASC = 0;
		public final static int DESC = 4;
		public final static String[] orderStrings = {"updated","urls.id","urls.alias","url"};
	}
	
	//Do default scan and load to sPane
	public void HandleScan(){
		HandleScan(OrderTypes.UPDATED+OrderTypes.ASC);
	}
	
	public void HandleScan(int order){
		//final Integer values=0;
		String orderStr=OrderTypes.orderStrings[order%OrderTypes.DESC];
		if(order>=OrderTypes.DESC){
			orderStr=orderStr+" DESC";
		}else{
			orderStr=orderStr+" ASC";
		}
		
		DbBasic db = GUI.getDataBase();
		
		Pane internalPane=new Pane();
		sPane.setContent(internalPane);
		
		internalPane.prefWidthProperty().bind(sPane.widthProperty());
		
		//create lookup
		results.clear();
		
		ResultSet rs=db.doQuery("SELECT urls.id, websites.webId, websites.url || urls.url AS url, urls.alias FROM urls INNER JOIN websites ON urls.webId=websites.webId ORDER BY "+orderStr);
		
		WebsiteVal prevWebsite=null;
		try{
			while(rs.next()){
				prevWebsite=addScanResult(rs,prevWebsite,internalPane);
			}
		}catch(SQLException e){
			GUI.getLogger().severe("error loading values");
		}
		db.close();
				
		//back button
		Button backBtn =new Button("Back");
		backBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				primaryStage.setScene(oldScene);
			}
		});
		backBtn.layoutXProperty().bind(primaryStage.widthProperty().multiply(2).divide(3).subtract(backBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		backBtn.layoutYProperty().bind(backBtn.heightProperty().add(prevWebsite.pane.heightProperty().add(prevWebsite.pane.layoutYProperty())));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		//run sql & reload button
		Button FinishedBtn =new Button("reload");
		FinishedBtn.setOnAction(HandleRescan);
		FinishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(3).subtract(FinishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		FinishedBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		internalPane.getChildren().addAll(backBtn,FinishedBtn);
		internalPane.prefHeightProperty().bind(backBtn.heightProperty().multiply(2).add(backBtn.layoutYProperty()));
		//internalPane.setWidth(sPane.getWidth());
		internalPane.prefWidthProperty().bind(sPane.widthProperty());
	}
	
	private EventHandler<ActionEvent> HandleRescan = new EventHandler<ActionEvent>(){
		@Override
		public void handle(ActionEvent e)
		{ 
			HandleScan();
		}
	};
	
	private class WebsiteVal{
		//url validity
		static final int VALID=0;
		static final int MISSING_EXCLUDE=1;
		static final int ERROR=2;
		static final int UNKNOWN=-1;
		
		//changes
		static final int NONE=0;
		static final int ALIAS=1;
		static final int WEBID=2;
		static final int URL=4;
		static final int DELETED=8;
		
		int valid = UNKNOWN;
		int id;
		String url;
		String alias;
		int changes=NONE;
		Region pane=null;
		
		int webId;
		
		public WebsiteVal(int id, int webId, String url, String alias){
			this.id=id;
			this.url=url;
			this.alias=alias;
			this.webId=webId;
		}
		public Region getTitledPane(){
			return pane;
		}
		public String getUrl(){
			return url;
		}
		
		public String getAlias(){
			return alias;
		}
		
		public int checkValidity(){
			try{
				DbBasic dataBase = GUI.getDataBase();
				ResultSet rs=dataBase.doQuery("SELECT accept,exclude FROM websites WHERE id="+webId);
				if(rs.next()){
					UrlLookUpLine check = new UrlLookUpLine(url,rs.getString(1),rs.getString(2));
					check.run();
					int result=check.result();
					switch(result){
					case 1:
					case 0:
						valid=VALID;
						return VALID;
					case -1:
						valid=MISSING_EXCLUDE;
						return MISSING_EXCLUDE;
					case -2:
						valid=ERROR;
						return ERROR;
					}
				}
			}catch(SQLException e){
				GUI.getLogger().severe("unable to check validity for website id "+id);
			}
			return UNKNOWN;
		}
		
		public boolean IsValid(){
			if(valid==UNKNOWN){
				checkValidity();
			}
			return valid==VALID;
		}
		
		public void changeURL(String newURL){
			url=newURL;
			changes=changes|URL;
		}
		public void changeAlias(String newAlias){
			alias=newAlias;
			changes=changes|ALIAS;
		}
		
		public void update(){
			DbBasic database = GUI.getDataBase();
			database.runSQL(getUpdateSQL());
			database.close();
		}
		
		public void delete(){
			System.out.println("deleted id "+id);
			DbBasic database = GUI.getDataBase();
			database.runSQL("DELETE FROM urls WHERE id = "+id);
			database.close();
		}
		
		public String getUpdateSQL(){
			String urlpart="";
			boolean found=false;
			if((changes & URL)!=0){
				DbBasic dataBase = GUI.getDataBase();
				ResultSet rs=dataBase.doQuery("SELECT url,webId FROM websites");
				try{
					while(rs.next()){
						if(url.startsWith(rs.getString("url"))){
							found=true;
							urlpart=url.substring(
								rs.getString("url").length()
							);
							if(webId == rs.getInt("webId")) break;
							changes=changes|WEBID;
							webId = rs.getInt("webId");
							break;
						}
					}
					rs.close();
				}catch(SQLException ex){
					GUI.getLogger().severe("Error loading websites: "+url);
					ex.printStackTrace();
					return "";
				}
				dataBase.close();
			}
			String updateString="";
			if(found){
				if((changes & WEBID)!=0){
					updateString =updateString+" webId="+webId+", ";
				}
				if((changes & URL)!=0){
					updateString =updateString+" url=\""+urlpart.replace("\"","\"\"")+"\", ";
				}
			}else{
				GUI.getLogger().warning("not valid website: "+url);
			}
			if((changes & ALIAS)!=0){
				updateString =updateString+" alias=\""+alias.replace("\"","\"\"")+"\", ";
			}
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
			return "UPDATE urls SET "+updateString+"updated='"+DATE_FORMAT.format(new Date())+"' WHERE id = "+id+";";
		}
	}
	
	private WebsiteVal addScanResult(ResultSet rs, WebsiteVal prevWebsite, Pane internalPane){
		try{
			WebsiteVal newSite=new WebsiteVal(rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getString(4));
			//add new segment
			Pane segPane = new Pane();//pane to put url labels in
			
			Label alias=new Label("Assigned Name");
			TextField aliasEditField = new TextField(newSite.getAlias());

			Label url= new Label("Current URL");
			TextField urlEditField = new TextField(newSite.getUrl());
			
			//name,url links
			int urls=0;
			Button link=null;
			
			//set up pane seperation
			TitledPane segment=new TitledPane(newSite.getAlias(),segPane);
			segment.setTextFill(Color.MIDNIGHTBLUE);
			if(prevWebsite==null){
				segment.layoutYProperty().bind(internalPane.layoutYProperty());
			}else{
				segment.layoutYProperty().bind(prevWebsite.pane.layoutYProperty().add(prevWebsite.pane.heightProperty()));
			}
			segment.setExpanded(false);
			newSite.pane=segment;
			
			alias.layoutXProperty().bind(segPane.widthProperty().subtract(alias.widthProperty()).divide(2));		
			url.layoutXProperty().bind(segPane.widthProperty().subtract(url.widthProperty()).divide(2));
			
			aliasEditField.prefWidthProperty().bind(segPane.widthProperty());
			urlEditField.prefWidthProperty().bind(aliasEditField.widthProperty());
			
			aliasEditField.layoutYProperty().bind(alias.layoutYProperty().add(alias.heightProperty()));
			url.layoutYProperty().bind(aliasEditField.layoutYProperty().add(aliasEditField.heightProperty().multiply(3).divide(2)));
			urlEditField.layoutYProperty().bind(url.layoutYProperty().add(url.heightProperty()));
			
			
			//Update button
			Button UpdateBtn =new Button("Update");
			UpdateBtn.setOnAction(new EventHandler<ActionEvent>() { 
				public void handle(ActionEvent e)
				{ 
					newSite.update();
				}
			});
			UpdateBtn.layoutXProperty().bind(segPane.widthProperty().divide(7));//button offset to middle and then shifted by half the width
			UpdateBtn.layoutYProperty().bind(urlEditField.layoutYProperty().add(urlEditField.heightProperty().multiply(3).divide(2)));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			UpdateBtn.prefWidthProperty().bind(segPane.widthProperty().divide(7).multiply(2));
			
			Button RemoveBtn =new Button("Remove Value");
			RemoveBtn.setOnAction(new EventHandler<ActionEvent>() { 
				public void handle(ActionEvent e)
				{ 
					newSite.delete();
				}
			});
			RemoveBtn.layoutXProperty().bind(segPane.widthProperty().divide(7).multiply(4));//button offset to middle and then shifted by half the width
			RemoveBtn.layoutYProperty().bind(UpdateBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			RemoveBtn.prefWidthProperty().bind(UpdateBtn.widthProperty());
			
			
			
			//alias edit
			aliasEditField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
					newSite.changeAlias(newValue);
				}
			});
			segPane.getChildren().add(alias);
			segPane.getChildren().add(aliasEditField);
			
			//urledit
			urlEditField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
					newSite.changeURL(newValue);
				}
			});
			segPane.getChildren().add(url);
			segPane.getChildren().add(urlEditField);
			
			segment.prefWidthProperty().bind(internalPane.widthProperty());
			segment.maxWidthProperty().bind(internalPane.widthProperty());
			
			segPane.prefWidthProperty().bind(segment.widthProperty());
			segPane.maxWidthProperty().bind(segment.widthProperty());
			
			segPane.getChildren().add(UpdateBtn);
			segPane.getChildren().add(RemoveBtn);
			
			segPane.prefHeightProperty().bind(RemoveBtn.layoutYProperty().add(RemoveBtn.heightProperty().multiply(3).divide(2)));
			
			results.add(newSite);
			
			internalPane.getChildren().addAll(segment);
			
			return newSite;
		}catch(SQLException e){
			GUI.getLogger().severe("website values corrupted");
			return prevWebsite;
		}
	}
}