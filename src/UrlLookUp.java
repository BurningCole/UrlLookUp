import java.net.*;
import java.io.*;
import java.awt.Desktop;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UrlLookUp implements IUpdateChecker{
	private String fileName;
	private boolean isDB;
	private String accept;
	private String exclude;
	private JLabel text1,text2;
	private List<UrlUpdate> Updates;
	private DbBasic db;
	
	//create arrays
	private int maxThreads=64;
	private Thread[] lookups = new Thread[maxThreads];
	private UrlLookUpLine[] Line=new UrlLookUpLine[maxThreads];
	private String[] names = new String[maxThreads];
	private int[] actualIDs= new int[maxThreads];
	
	private StringBuilder current;
	private StringBuilder total;
	private int finished=0;
	
	/**
	* FileName: name of file that contains urls
	* Accept: what string marks link
	* Exclude: what string marks end of area to check (speeds it up a little)
	*/
	public UrlLookUp(String FileName, String Accept, String Exclude){
		fileName=FileName;
		accept=Accept;
		exclude=Exclude;
		isDB=false;
		ObservableList<UrlUpdate> observableList = FXCollections.observableArrayList();
		Updates=new SimpleListProperty(observableList);
	}
	
	public UrlLookUp(DbBasic DataBaseConn){
		db=DataBaseConn;
		accept="=\"back";
		exclude="=\"clearfix";
		isDB=true;
		ObservableList<UrlUpdate> observableList = FXCollections.observableArrayList();
		Updates=new SimpleListProperty(observableList);
	}
	
	private void addResult(int id){
		int result=Line[id%maxThreads].result();
		//if success
		UrlUpdate update;
		switch(result){
			case 1:
				ArrayList<String> urls = Line[id%maxThreads].getAllUrls();
				
				String newUrl=Line[id%maxThreads].getUrl();
				
				ResultSet rs=db.doQuery("SELECT url FROM websites");
				try{
					while(rs.next()){
						if(newUrl.startsWith(rs.getString("url"))){
							newUrl=newUrl.substring(
								rs.getString("url").length()
							);
							break;
						}
					}
					update=new UrlUpdate(
						actualIDs[id%maxThreads],	//id of website
						UrlUpdate.NORMAL,			//type
						urls,						//all update urls
						newUrl,						//new url part
						names[id%maxThreads]		//url name reference
					);
					Updates.add(update);
					
				}catch(SQLException e){
					e.printStackTrace();
				}
				break;
			//if error
			case -1:
				
				update=new UrlUpdate(
					actualIDs[id%maxThreads],		//id of website
					UrlUpdate.MISSING_EXCLUDE,		//type
					new ArrayList<String>(),		//all update urls
					Line[id%maxThreads].getUrl(),	//new url part
					names[id%maxThreads]			//url name reference
				);
				Updates.add(update);
				
				break;
			case -2:
				update=new UrlUpdate(
					actualIDs[id%maxThreads],		//id of website
					UrlUpdate.SOCK_ERROR,			//type
					new ArrayList<String>(),		//all update urls
					Line[id%maxThreads].getUrl(),	//new url part
					names[id%maxThreads]			//url name reference
				);
				Updates.add(update);
				
				break;
			case -3:
				update=new UrlUpdate(
					actualIDs[id%maxThreads],		//id of website
					UrlUpdate.READ_ERROR,			//type
					new ArrayList<String>(),		//all update urls
					Line[id%maxThreads].getUrl(),	//new url part
					names[id%maxThreads]			//url name reference
				);
				Updates.add(update);
				break;
		}
	}
	
	private int waitForResult(){
		int id=0;
		while(lookups[id]!=null&&lookups[id].isAlive()){
			
			id++;
			if(id>=maxThreads){
				try{
					Thread.sleep(250);
				}catch(InterruptedException e){
					
				}
				id=0;
			}
		}
		addResult(id);
		GUIUpdate();
		return id;
	}
	
	private void waitForResult(int id){
		boolean waited=false;
		while(lookups[id%maxThreads].isAlive()){
			try{
				waited=true;
				lookups[id%maxThreads].join(250);
				GUIUpdate();
			}catch(InterruptedException e){
				System.out.println("Thread joining error");
			}
		}
		//get result
		addResult(id);
	}
	
	/**
	* gets results in a string format
	*/
	public List<UrlUpdate> getResults(){
		return Updates;
	}
	
	/**
	* starts scan of input file
	*/
	public void startScan(){
		System.out.println("\n--START SCAN---");
		//create editable string for loading bars
		current=new StringBuilder("[");
		for(int i=0;i<maxThreads;i++)
			current.append("=");
		current.append("]");
		total=new StringBuilder(current);
		
		//set up swing fields
		text1=new JLabel(current.toString());
		text2=new JLabel(total.toString());
		JFrame frame= new JFrame("Output");
		frame.setSize(maxThreads*7+16,70);
		frame.setResizable(false);
		frame.getContentPane().add(text1);
		frame.getContentPane().add(text2);
		frame.setLayout(null);
		frame.setVisible(true);
		text1.setBounds(0,0,maxThreads*7+16,20);
		text2.setBounds(0,20,maxThreads*7+16,20);
		
		//start reading file
		try{
			//get output file
			BufferedReader br=null;
			ResultSet rs=null;
			if(isDB){
				rs=db.doQuery("SELECT urls.id, websites.url, urls.url, urls.alias, websites.accept, websites.exclude FROM urls INNER JOIN websites ON urls.webId=websites.webId ORDER BY updated");
			}else{
				br = new BufferedReader(new FileReader(fileName));
			}
			int read=0,i=0;
		
			
			//iterate through each line
			if(isDB){
				try{
				while(rs.next()){
					if(read>=maxThreads){
						i=waitForResult();
					}else{
						i=read;
					}
					//get next url
					names[i]=rs.getString(4);
					String url=rs.getString(2)+rs.getString(3);
					//create lookup for line
					Line[i]=new UrlLookUpLine(url,rs.getString(5),rs.getString(6));
					lookups[i]=new Thread(Line[i%maxThreads]);
					actualIDs[i]=rs.getInt(1);
					//start lookup
					lookups[i].start();
					//update swing gui
					GUIUpdate();
					read++;
				}
				}catch(SQLException e){
					e.printStackTrace();
					return;
				}
			}else
			for(String line; (line = br.readLine()) != null; read++){
				
				//if thread array capacity reached
				if(read>=maxThreads){
					i=waitForResult();
				}else{
					i=read;
				}
				//get next url
				String[] lineSplit=line.split(">");
				names[i]=lineSplit[0];
				actualIDs[i]=read;
				if(lineSplit.length!=2){
					continue;
				}
				//create lookup for line
				Line[i]=new UrlLookUpLine(lineSplit[1],accept,exclude);
				lookups[i]=new Thread(Line[read%maxThreads]);
				//start lookup
				lookups[i].start();
				
				//update swing gui
				GUIUpdate();
			}
			System.out.println("read: "+read);
			for(int j=0;j<maxThreads;j++){
				waitForResult(j);
			}
			//close input file
			if(!isDB)
				br.close();
		}catch(FileNotFoundException e){
			System.out.println("\nFile didn't exist");
		}catch(IOException e){
			System.out.println("\nCouldn't access file");
		}
		System.out.println("\n---DONE SCAN---");
		frame.dispose();
	}
	
	// update GUI
	private void GUIUpdate(){
		for(int j=0;j<maxThreads;j++){
			if(lookups[j]==null||!lookups[j].isAlive())
				if(current.charAt(j+1)=='#'){
					total.setCharAt(finished--,'=');
					current.setCharAt(j+1,'=');
				}
			else{
				if(current.charAt(j+1)=='='){
					total.setCharAt(++finished,'#');
					current.setCharAt(j+1,'#');
				}
			}
		}
		text1.setText(current.toString());
		text2.setText(total.toString());
	}
	
	/**
	* checks a single url(url) for string(string) until it finds a
	* different string(exclude)
	* (replaced with different class)
	*/
	public boolean testUrlFor(String url,String string,String exclude)throws Exception{
		try{
			URL website = new URL(url);
			HttpURLConnection con = (HttpURLConnection) website.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				if(line.contains(string)){
					String[] tokensVal = line.split("\"");
					return true;
				}else if(line.contains(exclude)){
					break;
				}
			}
			reader.close();
		}catch(Exception e){
			System.out.println("Error accsessing"+url);
		}
		return false;
	}
}