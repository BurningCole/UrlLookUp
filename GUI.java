import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 

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
	
	private void HandleScan(Stage primaryStage){
		Scene oldScene=primaryStage.getScene();
		
		int values=0;
		ArrayList<TitledPane> segments = new ArrayList<TitledPane>();
		
		DbBasic db = getDataBase();
		ScrollPane sPane = new ScrollPane();
		sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sPane.setPrefSize(500,900);
		
		Pane internalPane=new Pane();
		sPane.setContent(internalPane);
		
		Scene scene= new Scene(sPane, 500, 900);
		
		primaryStage.setScene(scene);
		
		//create lookup
		IUpdateChecker lookup=new UrlLookUp(db);
		List<UrlUpdate> updates=lookup.getResults();
		/*if( updates instanceof ListExpression){
			(ListExpression)updates.sizeProperty().addListener(new ChangeListener(){
			@Override public void changed(ObservableValue o,Object oldVal, Object newVal){
				for(;values<updates.size();values++){
					Pane valuePane= new Pane();
					
					internalPane.getChildren().add(valuePane);
				}
			}
		}*/
		//start scan
		//lookup.startScan(getDataFileLoc()+"updates.txt");
		ArrayList<String> s1 = new ArrayList<String>();
		s1.add("test");
		updates.add(new UrlUpdate(0,UrlUpdate.NORMAL,s1,"","Name 1"));
		for(;values<updates.size();values++){//add all aditional values
			//get update
			UrlUpdate curUpdate=updates.get(values);
			//add new segment
			Pane segPlane = new Pane();
			TitledPane segment=new TitledPane(curUpdate.name(),segPlane);
			//name,url links, check/
			segments.add(segment);
			internalPane.getChildren().add(segment);
			
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
		backBtn.layoutYProperty().bind(primaryStage.widthProperty().divide(12));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		//run sql & reload button
		Button FinishedBtn =new Button("Verify");
		FinishedBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				//calculate sql
				String sqlQuery="";
				for(int i=0;i<updates.size();i++){
					//if(false)//switch to if check checked
						switch(updates.get(i).getType()){
							case UrlUpdate.NORMAL:	//normal update
							sqlQuery+=updates.get(i).getSQLStatement();
							break;
							default:				//other unhandled update
							break;
						}
				}
				//run sql
				//DbBasic dataBase = getDataBase();
				//dataBase.doQuery(sqlQuery);
				//dataBase.close();
				
				//reload scene
				primaryStage.setScene(oldScene);
				HandleScan(primaryStage);
			}
		});
		FinishedBtn.layoutXProperty().bind(primaryStage.widthProperty().divide(3).subtract(FinishedBtn.widthProperty().divide(2)));//button offset to middle and then shifted by half the width
		FinishedBtn.layoutYProperty().bind(primaryStage.widthProperty().divide(12));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
		
		internalPane.getChildren().addAll(backBtn,FinishedBtn);
		db.close();
		
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