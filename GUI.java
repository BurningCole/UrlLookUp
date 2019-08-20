import javafx.application.Application;
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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region ;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;

public class GUI extends Application {
	
	@Override 
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Test GUI");
		openMenu(primaryStage);
		primaryStage.show();
	}
	
	/**
	* opens the starting menu
	*/
	private void openMenu(Stage primaryStage){
		//initiate buttons
		Button scanBtn = new Button("Scan urls");//scan button
		scanBtn.setDefaultButton(true);
		Button addBtn = new Button("Add new urls");//goes to add menu
		Button removeBtn = new Button("Remove urls");
		Button quitBtn =new Button("quit");
		
		//put in array for quick altering of values
		Button[] buttons = new Button[4];
		buttons[0]=scanBtn;
		buttons[1]=addBtn;
		buttons[2]=removeBtn;
		buttons[3]=quitBtn;
		
		//initiate pane
		Pane pane= new Pane();
		pane.getChildren().addAll(scanBtn,addBtn,removeBtn,quitBtn);
		Scene scene= new Scene(pane, 300, 350);
		
		//set button values
		int i=0;
		for(Button btn:buttons){
			//link va;lues to width
			btn.prefWidthProperty().bind(pane.widthProperty().divide(2));//buttons are half width
			btn.prefHeightProperty().bind(pane.widthProperty().divide(6));//buttons are in a ratio of 1:3
			
			btn.layoutXProperty().bind(pane.widthProperty().divide(2).subtract(btn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
			
			btn.layoutYProperty().bind(pane.widthProperty().multiply(3*i+1).divide(12));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			i++;
		}
		
		
		//button handling
		scanBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				System.out.println("Doing scan!\n beep beep!");
				
				HandleScan(primaryStage);//do actual scan
			}
		});
		
		addBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				addBtn.setText("Not Done");
				addBtn.setDisable(true);
				System.out.println("Going to Add!\n beep beep!");
			}
		});
		
		removeBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				removeBtn.setText("Not Done");
				removeBtn.setDisable(true);
				System.out.println("Going to Remove!\n beep beep!");
			} 
		});
		
	
		quitBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				primaryStage.close();
			}
		});
		
		//set stage scene to new scene
		primaryStage.setScene(scene);
	}
	static int values=0;
	static Region prevPane;
	
	private void HandleScan(Stage primaryStage){
		Scene oldScene=primaryStage.getScene();
		values=0;
		//final Integer values=0;
		ArrayList<CheckBox> checkboxes = new ArrayList<CheckBox>();
		prevPane=new Region();
		
		DbBasic db = getDataBase();
		ScrollPane sPane = new ScrollPane();
		sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sPane.setPrefSize(500,900);
		
		Pane internalPane=new Pane();
		sPane.setContent(internalPane);
		internalPane.prefWidthProperty().bind(sPane.widthProperty());
		
		Scene scene= new Scene(sPane, 500, 900);
		
		primaryStage.setScene(scene);
		
		//create lookup
		IUpdateChecker lookup=new UrlLookUp(db);
		
		List<UrlUpdate> updates=lookup.getResults();
		if( updates instanceof ListExpression){
			System.out.println("Listproperty");
			((ListExpression)updates).sizeProperty().addListener(new ChangeListener(){
				@Override public void changed(ObservableValue o,Object oldVal, Object newVal){
					System.out.println("Update-"+updates.size());
					for(;values<updates.size();values++){
						UrlUpdate curUpdate=updates.get(values);
						Platform.runLater(new Runnable() {
							@Override public void run() {
								prevPane=addScanResult(curUpdate,internalPane,prevPane,checkboxes);
							}
						});
					}
				}
			});
		}else System.out.println("Not listproperty");
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
					System.out.println("clearing");
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
				Button FinishedBtn =new Button("Verify");
				FinishedBtn.setOnAction(new EventHandler<ActionEvent>() { 
					public void handle(ActionEvent e)
					{ 
						//calculate sql and run
						DbBasic dataBase = getDataBase();
						for(int i=0;i<updates.size();i++){
							if(checkboxes.get(i).isSelected())//switch to if check checked
								switch(updates.get(i).getType()){
									case UrlUpdate.NORMAL:	//normal update
									dataBase.runSQL(updates.get(i).getSQLStatement());
									break;
									default:				//other unhandled update
									break;
								}
						}
						dataBase.close();
						
						//reload scene
						primaryStage.setScene(oldScene);
						HandleScan(primaryStage);
					}
				});
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
	
	private TitledPane addScanResult(UrlUpdate curUpdate,Pane internalPane, Region prevPane,List checkboxes){
		//add new segment
		Pane segPane = new Pane();//pane to put url labels in
		
		
		//name,url links, check/
		int urls=0;
		Button link=null;
		if(curUpdate.urls()!=null){
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
		}
		TitledPane segment=new TitledPane(curUpdate.name(),segPane);//
		segment.layoutYProperty().bind(prevPane.layoutYProperty().add(prevPane.heightProperty()));
		segment.setExpanded(false);
		prevPane=segment;
		
		CheckBox check = new CheckBox();
		
		segment.prefWidthProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(2)));// 7/8 width
		segment.maxWidthProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(2)));// 7/8 width
		
		check.layoutYProperty().bind(segment.layoutYProperty().add(segment.heightProperty().subtract(check.heightProperty()).divide(2)));
		check.layoutXProperty().bind(internalPane.widthProperty().subtract(check.widthProperty().multiply(7).divide(4)));
		checkboxes.add(check);
		
		internalPane.getChildren().addAll(segment,check);
		return segment;
	}
	
	private String getDataFileLoc(){
		//find update file
		Class<?> c = UrlLookUp.class;
		//remove unnesasary parts of string that are returned
		String fileLoc=c.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:","");
		fileLoc=fileLoc.replace("UrlLookUp.jar","");
		//add data folder to location
		fileLoc=fileLoc+"data/";
		return fileLoc;
	}
	
	private DbBasic getDataBase(){
		String fileLoc=getDataFileLoc();
		System.out.println(fileLoc);
		DbBasic db=new DbBasic(fileLoc+"Urls.db");
		return db;
	}
	
	public static void startGUI(String[] args){
		launch(args);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}