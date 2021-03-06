import java.net.*;
import java.io.*;
import java.awt.Desktop;
import javax.swing.*;
import java.util.ArrayList;

public class UrlLookUp implements IUpdateChecker{
	private String fileName;
	private boolean isDB;
	private String accept;
	private String exclude;
	private JLabel text1,text2;
	
	//create arrays
	private int maxThreads=64;
	private Thread[] lookups = new Thread[maxThreads];
	private UrlLookUpLine[] Line=new UrlLookUpLine[maxThreads];
	private String[] names = new String[maxThreads];
	
	private StringBuilder current;
	private StringBuilder total;
	private int finished=0;
	
	BufferedWriter sqlfile;
	
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
	}
	/*public UrlLookUp(DbBasic DataBaseConn){
		fileName=FileName;
		accept=Accept;
		exclude=Exclude;
		isDB=true;
	}*/
	
	private void waitForResult(Thread thread, UrlLookUpLine line, String name, int id,BufferedWriter output){
		while(thread.isAlive()){
			try{
				thread.join(1000);
				GUIUpdate();
			}catch(InterruptedException e){
				System.out.println("Thread joining error");
			}
		}
		System.out.println("read "+id+" Lines");
		//get result
		int result=line.result();
		//if success
		try{
			if(result==1){
				output.write("Url Line: "+id+" ("+name+")");
				output.newLine();
				ArrayList<String> urls = line.getAllUrls();
				for (String url:urls){
					output.write("\t"+url);
					output.newLine();
					
				}
				output.newLine();
				
				sqlfile.write("UPDATE urls SET url='"+line.getUrl()+"' WHERE id = "+id+";");
				sqlfile.newLine();
			//if error
			}else if(result==-1){
				output.write("Error on line: "+id+" ("+name+")");
				output.newLine();
				output.write(line.getUrl());
				output.newLine();
				output.newLine();
				
				sqlfile.write("--UPDATE urls SET url='#Change this#' WHERE id = "+id+";");
				sqlfile.newLine();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	* starts scan of input file
	* outputFile: what file to put results in
	*/
	public void startScan(String outputFile){
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
		try(
			BufferedReader br = new BufferedReader(new FileReader(fileName))){
			//get output file
			BufferedWriter text = new BufferedWriter(new FileWriter(outputFile));
			sqlfile=new BufferedWriter(new FileWriter(outputFile.replace(".txt",".sql")));
			int read=0,i=0;
		
			
			//iterate through each line
			for(String line; (line = br.readLine()) != null; read++){
				
				//if thread array capacity reached
				if(read>=maxThreads){
					//if no lookup in space
					if(lookups[read%maxThreads]==null){
						System.out.println("Line: "+(read+1)+" not a correct format");
						text.write("Url Line: "+(read+1)+" not a correct format");
						text.newLine();
						text.newLine();
					}else{
						waitForResult(lookups[read%maxThreads],Line[i%maxThreads],names[i%maxThreads],i+1,text);
					}
					i++;
				}
				//get next url
				String[] lineSplit=line.split(">");
				names[read%maxThreads]=lineSplit[0];
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
			br.close();
			for(;i<read;i++){
				if(lookups[read%maxThreads]==null){
						System.out.println("Line: "+(read+1)+" not a correct format");
						text.write("Url Line: "+(read+1)+" not a correct format");
						text.newLine();
						text.newLine();
				}else{
					waitForResult(lookups[read%maxThreads],Line[i%maxThreads],names[i%maxThreads],i+1,text);
				}
			}
			text.close();
			sqlfile.close();
			
			if (Desktop.isDesktopSupported()){
				Desktop.getDesktop().edit(new File(outputFile));
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
					System.out.println(tokensVal[1]);
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