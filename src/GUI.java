import javafx.application.Application;
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.logging.*;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import java.sql.*;

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
				ScanGUI scan = new ScanGUI(primaryStage);
				scan.HandleScan();//do actual scan
			}
		});
		
		addBtn.setOnAction(new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				System.out.println("Going to Add!\n beep beep!");
				AddGUI add = new AddGUI(primaryStage);
				add.HandleAddMenu();
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
	private static String DataFileLoc = null;
	
	private static String getDataFileLoc(){
		if(DataFileLoc!=null)
			return DataFileLoc;
		//find update file
		Class<?> c = UrlLookUp.class;
		//remove unnesasary parts of string that are returned
		String fileLoc=c.getProtectionDomain().getCodeSource().getLocation().toString();
		fileLoc=fileLoc.replace("file:/","");
		fileLoc=fileLoc.replace("file:","");
		fileLoc=fileLoc.replace("UrlLookUp.jar","");
		//add data folder to location
		fileLoc=fileLoc+"data/";
		DataFileLoc = fileLoc;
		return fileLoc;
	}
	
	public static DbBasic getDataBase(){
		String fileLoc=getDataFileLoc();
		DbBasic db=new DbBasic(fileLoc+"Urls.db");
		return db;
	}
	
	public static void startGUI(String[] args){
		launch(args);
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	private static Logger logger = null;
	private static FileHandler fh;   
	 
	
	private static Logger loadLogger(){
		if(logger!=null)
			return logger;
		try{
			logger = Logger.getLogger("Primary");
			fh = new FileHandler(getDataFileLoc()+"logFile.log");  
			fh.setFormatter(new SimpleFormatter());		
			logger.addHandler(fh);
		}catch(Exception e){
			System.out.println("loggerError\n"+getDataFileLoc());
			e.printStackTrace();
			System.exit(0);
		}
		logger.info("logger Started: "+getDataFileLoc()+"logFile.log");
		return logger;
	}
	
	public static Logger getLogger(){
		if(logger==null)
			loadLogger();
		return logger;
	}
	
	public static void logInfo(String log){
		if(logger==null)
			loadLogger();
		logger.info(log);
	}
}