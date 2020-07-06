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
import javafx.scene.control.ComboBox;
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
	ArrayList<Region> toDraw = new ArrayList<Region>();
	Stage primaryStage;
	Scene oldScene;
	ScrollPane sPane = new ScrollPane();
	Pane resultPane=new Pane();
	
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
	
	int curOrder=OrderTypes.UPDATED+OrderTypes.ASC;
	
	//Do default scan and load to sPane
	public void HandleScan(){
		HandleScan(curOrder);
	}
	
	public void HandleScan(int order){
		
		Pane internalPane=new Pane();
		sPane.setContent(internalPane);
		
		internalPane.prefWidthProperty().bind(sPane.widthProperty());
		
		Label sortBy=new Label("Sort By:");
		
		ComboBox sortBox = new ComboBox();
		ComboBox orderBox = new ComboBox();
		
		sortBox.getItems().addAll(
			"Updated",
			"ID",
			"Name/Alias",
			"URL"
		);
		orderBox.getItems().addAll(
			"Ascending",
			"Decending"
		);
		
		sortBox.getSelectionModel().selectFirst();
		orderBox.getSelectionModel().selectFirst();
		
		sortBox.valueProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue ov, String oldVal, String newVal) {
				curOrder-=sortBox.getItems().indexOf(oldVal);
				curOrder+=sortBox.getItems().indexOf(newVal);
				System.out.println(sortBox.getItems().indexOf(newVal));
				System.out.println(sortBox.getItems().indexOf(oldVal));
				loadResults(curOrder);
		}});
		orderBox.valueProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue ov, String oldVal, String newVal) {
				curOrder-=orderBox.getItems().indexOf(oldVal)*OrderTypes.DESC;
				curOrder+=orderBox.getItems().indexOf(newVal)*OrderTypes.DESC;
				System.out.println(orderBox.getItems().indexOf(newVal));
				System.out.println(orderBox.getItems().indexOf(oldVal));
				System.out.println(curOrder);
				loadResults(curOrder);
		}});

		sortBy.layoutYProperty().bind(internalPane.layoutYProperty().add(sortBox.heightProperty()));
		sortBy.layoutXProperty().bind(internalPane.layoutYProperty().add(sortBox.heightProperty()));
		
		sortBox.layoutYProperty().bind(sortBy.layoutYProperty());
		sortBox.layoutXProperty().bind(sortBy.layoutXProperty().multiply(2).add(sortBy.widthProperty()));
		
		orderBox.layoutYProperty().bind(sortBy.layoutYProperty());
		orderBox.layoutXProperty().bind(sortBox.layoutXProperty().add(sortBox.widthProperty()));
		
		loadResults(order);
		
		resultPane.layoutYProperty().bind(sortBox.layoutYProperty().add(sortBox.heightProperty()));
		resultPane.prefWidthProperty().bind(internalPane.widthProperty());
			
		//back button
		Button backBtn =new Button("Back");
		backBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				primaryStage.setScene(oldScene);
			}
		});
		backBtn.layoutXProperty().bind(primaryStage.widthProperty().multiply(2).divide(3).subtract(backBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		backBtn.layoutYProperty().bind(resultPane.heightProperty().add(resultPane.layoutYProperty()).add(backBtn.heightProperty()));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		//run sql & reload button
		Button FinishedBtn =new Button("reload");
		FinishedBtn.setOnAction(HandleRescan);
		FinishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(3).subtract(FinishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		FinishedBtn.layoutYProperty().bind(backBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		internalPane.prefHeightProperty().bind(backBtn.heightProperty().multiply(2).add(backBtn.layoutYProperty()));
		//internalPane.setWidth(sPane.getWidth());
		internalPane.prefWidthProperty().bind(sPane.widthProperty());
		internalPane.getChildren().addAll(sortBy,sortBox,orderBox,resultPane,backBtn,FinishedBtn);
		
		/*Task<Void> scan = new Task<Void>(){
			@Override 
			public Void call() throws InterruptedException {
				GUI.logInfo("Scan Started");
				loadResults(order);
				GUI.logInfo("Scan Finished");
				return null;
			}
		};
		
		
		scan.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t)
			{
				for(int i=0;i<toDraw.size();i++){
					if(i==0){
						toDraw.get(i).layoutYProperty().bind(resultPane.layoutYProperty());
					}else{
						toDraw.get(i).layoutYProperty().bind(toDraw.get(i-1).layoutYProperty().add(toDraw.get(i-1).heightProperty()));
					}
					toDraw.get(i).prefWidthProperty().bind(resultPane.widthProperty());
					resultPane.getChildren().add(toDraw.get(i));
					
				}
				
				//Region firstpane=toDraw.get(0);
				//Region lastpane=toDraw.get(toDraw.size()-1);
				System.out.println(resultPane.heightProperty().getValue());
				System.out.println(resultPane.getLayoutY());
			}
		});
		new Thread(scan).start();*/
		
	}
	
	private void loadResults(int order){
		//create lookup
		results.clear();
		toDraw.clear();
		
		resultPane.getChildren().clear();
		
		
		DbBasic db = GUI.getDataBase();
		
		//final Integer values=0;
		String orderStr=OrderTypes.orderStrings[order%OrderTypes.DESC];
		if(order>=OrderTypes.DESC){
			orderStr=orderStr+" DESC";
		}else{
			orderStr=orderStr+" ASC";
		}
		
		ResultSet rs=db.doQuery("SELECT urls.id, websites.webId, websites.url || urls.url AS url, urls.alias, urls.updated FROM urls INNER JOIN websites ON urls.webId=websites.webId ORDER BY "+orderStr);
		System.out.println("SELECT urls.id, websites.webId, websites.url || urls.url AS url, urls.alias, urls.updated FROM urls INNER JOIN websites ON urls.webId=websites.webId ORDER BY "+orderStr);
		WebsiteVal prevWebsite=null;
		String curBlock="----------------";
		Pane block=new Pane();
		try{
			while(rs.next()){
				switch(order%OrderTypes.DESC){
					case OrderTypes.UPDATED:
						if(rs.getString(5)==null&&!curBlock.equals("UNKNOWN")){
							curBlock="UNKNOWN";
							block=createBlock(curBlock);
							prevWebsite=null;
						}else
						if(rs.getString(5)!=null&&!curBlock.equals(rs.getString(5))){
							curBlock=rs.getString(5);
							block=createBlock(curBlock);
							prevWebsite=null;
						}
						break;
					case OrderTypes.ID:
						if(!((rs.getInt(1)/100)+"XX").equals(curBlock)){
							curBlock=(rs.getInt(1)/100)+"XX";
							block=createBlock(curBlock);
							prevWebsite=null;
						}
						break;
					case OrderTypes.NAME:
						if(!rs.getString(4).startsWith(curBlock)){
							curBlock=rs.getString(4).substring(0,1);
							block=createBlock(curBlock);
							prevWebsite=null;
						}
						break;
					case OrderTypes.URL:
						if(!rs.getString(3).startsWith(curBlock)){
							String[] split=rs.getString(3).split("/");
							curBlock=split[0]+"//"+split[2];
							block=block=createBlock(curBlock);
							prevWebsite=null;
						}
						break;
				}
				prevWebsite=addScanResult(rs,prevWebsite,block);
			}
		}catch(SQLException e){
			GUI.getLogger().severe("error loading values");
		}
		db.close();

	}
	
	private Pane createBlock(String curBlock){
		Pane block=new Pane();
		TitledPane segment=new TitledPane(curBlock,block);
		int size=toDraw.size();
		toDraw.add(segment);
		segment.setExpanded(false);
		
		if(size==0){
			segment.layoutYProperty().bind(resultPane.layoutYProperty());
		}else{
			segment.layoutYProperty().bind(toDraw.get(size-1).layoutYProperty().add(toDraw.get(size-1).heightProperty()));
		}
		segment.prefWidthProperty().bind(resultPane.widthProperty());
		resultPane.getChildren().add(segment);
		resultPane.prefHeightProperty().bind(segment.heightProperty().add(segment.layoutYProperty()));
		return block;
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
			GUI.getLogger().info("Updated: "+alias);
			changes=NONE;
		}
		
		public void delete(){
			System.out.println("deleted id "+id);
			DbBasic database = GUI.getDataBase();
			database.runSQL("DELETE FROM urls WHERE id = "+id);
			database.close();
			GUI.getLogger().info("Deleted: "+alias);
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
			
			
			String title;
			switch(curOrder%OrderTypes.DESC){
				case OrderTypes.URL:
					title=newSite.getUrl();
					break;
				case OrderTypes.ID:
					title=newSite.id+": "+newSite.getAlias();
				default:
					title=newSite.getAlias();
					break;
			}
			
			//set up pane seperation
			TitledPane segment=new TitledPane(title,segPane);
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
					UpdateBtn.setDisable(true);
				}
			});
			UpdateBtn.layoutXProperty().bind(segPane.widthProperty().divide(13));//button offset to middle and then shifted by half the width
			UpdateBtn.layoutYProperty().bind(urlEditField.layoutYProperty().add(urlEditField.heightProperty().multiply(3).divide(2)));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			UpdateBtn.prefWidthProperty().bind(segPane.widthProperty().multiply(3).divide(13));
			UpdateBtn.setDisable(true);
			
			Button LinkBtn =new Button("Link");
			LinkBtn.setOnAction(new EventHandler<ActionEvent>() { 
				public void handle(ActionEvent e)
				{ 
					try{
						java.awt.Desktop.getDesktop().browse(new java.net.URI(newSite.getUrl()));
					}catch(Exception ex){
						GUI.getLogger().warning("website \""+newSite.getUrl()+"\" conection error");
					}
					
				}
			});
			LinkBtn.layoutXProperty().bind(segPane.widthProperty().multiply(5).divide(13));//button offset to middle and then shifted by half the width
			LinkBtn.layoutYProperty().bind(UpdateBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			LinkBtn.prefWidthProperty().bind(UpdateBtn.widthProperty());
			
			Button RemoveBtn =new Button("Remove Value");
			RemoveBtn.setOnAction(new EventHandler<ActionEvent>() { 
				public void handle(ActionEvent e)
				{ 
					newSite.delete();
					segment.setExpanded(false);
					segment.setCollapsible(false);
					segment.setText("DELETED");
					
				}
			});
			RemoveBtn.layoutXProperty().bind(segPane.widthProperty().multiply(9).divide(13));//button offset to middle and then shifted by half the width
			RemoveBtn.layoutYProperty().bind(UpdateBtn.layoutYProperty());//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			RemoveBtn.prefWidthProperty().bind(UpdateBtn.widthProperty());
			
			//alias edit
			aliasEditField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
					newSite.changeAlias(newValue);
					UpdateBtn.setDisable(false);
				}
			});
			segPane.getChildren().add(alias);
			segPane.getChildren().add(aliasEditField);
			
			//urledit
			urlEditField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
					newSite.changeURL(newValue);
					UpdateBtn.setDisable(false);
				}
			});
			
			segPane.getChildren().add(url);
			segPane.getChildren().add(urlEditField);
			
			segment.prefWidthProperty().bind(internalPane.widthProperty().multiply(9).divide(10));
			segment.maxWidthProperty().bind(internalPane.widthProperty().multiply(9).divide(10));
			segment.layoutXProperty().bind(internalPane.widthProperty().subtract(segment.widthProperty()).divide(3));
			
			segPane.prefWidthProperty().bind(segment.widthProperty());
			segPane.maxWidthProperty().bind(segment.widthProperty());
			
			segPane.getChildren().add(UpdateBtn);
			segPane.getChildren().add(RemoveBtn);
			segPane.getChildren().add(LinkBtn);
			
			segPane.prefHeightProperty().bind(RemoveBtn.layoutYProperty().add(RemoveBtn.heightProperty().multiply(3).divide(2)));
			
			internalPane.getChildren().add(segment);
			internalPane.prefHeightProperty().bind(segment.layoutYProperty().add(segment.heightProperty().subtract(internalPane.layoutYProperty())));
			
			results.add(newSite);
			
			return newSite;
		}catch(SQLException e){
			GUI.getLogger().severe("website values corrupted");
			return prevWebsite;
		}catch(Exception e){
			e.printStackTrace();
			return prevWebsite;
		}
	}
}