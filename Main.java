import java.sql.*;
public class Main{
	public static void main(String[] args)throws Exception{
		/*
		//find update file
		Class<?> c = UrlLookUp.class;
		//remove unnesasary parts of string that are returned
		String fileLoc=c.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:","");
		fileLoc=fileLoc.replace("UrlLookUp.jar","");
		//add data folder to location
		fileLoc=fileLoc+"data/";
		System.out.println(fileLoc);
		DbBasic db=new DbBasic(fileLoc+"Urls.db");
		
		//create lookup
		IUpdateChecker lookup=new UrlLookUp(db);
		//start scan
		lookup.startScan(fileLoc+"updates.txt");
		
		db.close();
		*/
		GUI.startGUI(args);
	}
}