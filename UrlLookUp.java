import java.net.*;
import java.io.*;
import java.awt.Desktop;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

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
		Updates=new ArrayList<UrlUpdate>();
	}
	
	public UrlLookUp(DbBasic DataBaseConn){
		db=DataBaseConn;
		accept="=\"back";
		exclude="=\"clearfix";
		isDB=true;
		Updates=new ArrayList<UrlUpdate>();
	}
	
	private void waitForResult(int id){
		boolean waited=false;
		while(lookups[id%maxThreads].isAlive()){
			try{
				waited=true;
				lookups[id%maxThreads].join(1000);
				GUIUpdate();
			}catch(InterruptedException e){
				System.out.println("Thread joining error");
			}
		}
		if(waited)
			System.out.println("read "+(id+1)+" Lines");
		//get result
		int result=Line[id%maxThreads].result();
		//if success
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
					UrlUpdate update=new UrlUpdate(
						actualIDs[id%maxThreads],	//id of website
						UrlUpdate.NORMAL,				//type
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
				
				UrlUpdate update=new UrlUpdate(
					actualIDs[id%maxThreads],	//id of website
					UrlUpdate.MISSING_EXCLUDE,		//type
					null,						//all update urls
					null,						//new url part
					names[id%maxThreads]		//url name reference
				);
				Updates.add(update);
				
				break;
			case -2:
				
				break;
			case -3:
				
				break;
			}
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
				rs=db.doQuery("SELECT urls.id, websites.url, urls.url, urls.alias, websites.accept, websites.exclude FROM urls INNER JOIN websites ON urls.webId=websites.webId");
			}else{
				br = new BufferedReader(new FileReader(fileName));
			}
			int read=0,i=0;
		
			
			//iterate through each line
			if(isDB){
				try{
				while(rs.next()){
					if(read>=maxThreads){
						//if no lookup in space
						if(lookups[read%maxThreads]==null){
							System.out.println("Line: "+(read+1)+" not a correct format");
						}else{
							waitForResult(i);
						}
						i++;
					}
					//get next url
					names[read%maxThreads]=rs.getString(4);
					String url=rs.getString(2)+rs.getString(3);
					//create lookup for line
					Line[read%maxThreads]=new UrlLookUpLine(url,rs.getString(5),rs.getString(6));
					lookups[read%maxThreads]=new Thread(Line[read%maxThreads]);
					actualIDs[read%maxThreads]=rs.getInt(1);
					//start lookup
					lookups[read%maxThreads].start();
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
					//if no lookup in space
					if(lookups[read%maxThreads]==null){
						System.out.println("Line: "+(read+1)+" not a correct format");
					}else{
						waitForResult(i);
					}
					i++;
				}
				//get next url
				String[] lineSplit=line.split(">");
				names[read%maxThreads]=lineSplit[0];
				actualIDs[read%maxThreads]=read;
				if(lineSplit.length!=2){
					continue;
				}
				//create lookup for line
				Line[read%maxThreads]=new UrlLookUpLine(lineSplit[1],accept,exclude);
				lookups[read%maxThreads]=new Thread(Line[read%maxThreads]);
				//start lookup
				lookups[read%maxThreads].start();
				
				//update swing gui
				GUIUpdate();
			}
			//close input file
			if(!isDB)
				br.close();
			for(;i<read;i++){
				if(lookups[i%maxThreads]==null){
						System.out.println("Line: "+(i+1)+" not a correct format");
				}else{
					waitForResult(i);
				}
			}
		}catch(FileNotFoundException e){
			System.out.println("\nFile didn't exist");
		}catch(IOException e){
			System.out.println("\nCouldn't access file");
		}
		System.out.println("\n---DONE---");
		frame.dispose();
	}
	
	// update GUI
	private void GUIUpdate(){
		for(int j=0;j<maxThreads;j++){
			if(lookups[j%maxThreads]==null||!lookups[j%maxThreads].isAlive())
				if(current.charAt(j%maxThreads+1)=='#'){
					total.setCharAt(finished--,'=');
					current.setCharAt(j%maxThreads+1,'=');
				}
			else{
				if(current.charAt(j%maxThreads+1)=='='){
					total.setCharAt(++finished,'#');
					current.setCharAt(j%maxThreads+1,'#');
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