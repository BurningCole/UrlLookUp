import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 

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
			btn.prefWidthProperty().bind(primaryStage.widthProperty().divide(2));//buttons are half width
			btn.prefHeightProperty().bind(primaryStage.widthProperty().divide(6));//buttons are in a ratio of 1:3
			btn.layoutXProperty().bind(primaryStage.widthProperty().divide(4));//button offset to middle 1-(1/2)=1/2, 1/2 multiplied by a half = 1/4
			btn.layoutYProperty().bind(primaryStage.widthProperty().multiply(3*i+1).divide(12));//the multiply adds the extra vertical offset and the divide is the divide gets it to the right size
			i++;
		}
		
		
		//button handling
		scanBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e)
			{ 
				System.out.println("Doing scan!\n beep beep!");
				HandleScan(primaryStage);
			}
		});
		
		addBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				System.out.println("Going to Add!\n beep beep!");
			}
		});
		
		removeBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
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
		DbBasic db = getDataBase();
		
		//create lookup
		IUpdateChecker lookup=new UrlLookUp(db);
		//start scan
		lookup.startScan(getDataFileLoc()+"updates.txt");
		ArrayList<UrlUpdate> updates=lookup.getResults();
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