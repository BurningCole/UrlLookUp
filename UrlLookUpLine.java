import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class UrlLookUpLine implements Runnable{
	private String url;
	private ArrayList<String> urlMem=new ArrayList<String>();
	private String accept;
	private String prevUrl="";
	private String exclude;
	private int attempts=0;
	private int hasUpdate=0;
	
	/**
	* create an instance to run with:
	* (URL) the target url of webpage,
	* (Accept) the string that marks link to connected webpage and
	* (Exclude)the string that will always be on webpage after acceptance string
	*/
	public UrlLookUpLine(String Url,String Accept,String Exclude){
		url=Url;
		prevUrl=url;
		accept=Accept;
		exclude=Exclude;
	}
	
	/**
	* returns 1 if the accept string was found on website,
	* returns 0 if the accept string was not found on website,
	* returns -1 if an error was encounted
	*/
	public int result(){
		return hasUpdate;
	}
	
	/**
	* ruturns arraylist of all URL results
	*/
	public ArrayList<String> getAllUrls(){
		return urlMem;
	}
	
	/**
	* gets last url if it exists, otherwise returns null
	*/
	public String getUrl(){
		if(urlMem.size()!=0)
			return urlMem.get(urlMem.size()-1);
		else
			return null;
	}
	
	/**
	* gets the nth URL 
	* returns null if address value is not valid
	*/
	public String getUrl(int address){
		if(address<urlMem.size()&&address>=0)
			return urlMem.get(address);
		else
			return null;
	}
	
	private String getLinkURL(String line){
		String[] links=line.split("<a");
		for(String link:links){
			if(link.contains(accept)){
				String[] tokensVal = link.split("\"");
				//search for http links
				for(String token:tokensVal)
					if(token.contains("http")){
						return token;
					}
			}
		}
		return null;
	} 
	
	/**
	* start of thread called by thread.start()
	* will do the actual search
	*/
	public void run(){
		try{
			//set up HTTP connection
			URL website = new URL(url);
			HttpURLConnection con = (HttpURLConnection) website.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			
			//read http file that is returned
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			boolean hasExclude=false;
			attempts=0;
			
			//iterate through http file lines
			while ((line = reader.readLine()) != null)
			{
				//if line has accept value
				if(line.contains(accept)){
					String tmpUrl=getLinkURL(line);
					if(tmpUrl!=null){
						url=tmpUrl;
					}
					
					//mark as having an update
					hasUpdate=1;
					urlMem.add(url);
					//if url has changed
					if(!url.equals(prevUrl)){
						//System.out.println("from:\t"+prevUrl+"\nto:\t"+url);//print out result
						prevUrl=url;
						//try running it again to get aditional linked urls	
						//(maybe change from recursive to stop any stack overflow?)
						run();
					}
					hasExclude=true;
					break;
				}else if(line.contains(exclude)){//if line contains exclude then stop searching
					hasExclude=true;
					break;
				}
			}
			//if reached end and exclude not found
			if(!hasExclude){
				// print out error info
				System.out.println(url+" missing exclude...");
				urlMem.add("Error: "+url+" missing exclude...\n");
				hasUpdate=-1;
			}
			//close reader
			reader.close();
		}catch(SocketTimeoutException e){
			//if it crashed attempt 5 more times
			if(attempts<5){
				attempts++;
				run();
				return;
			}
			System.out.println("Error accsessing:\n"+url);
			urlMem.add("Error: "+url+" website connection error (sock timeout)...\n");
			hasUpdate=-1;
			e.printStackTrace();
		}catch(IOException e){
			//if it crashed attempt 5 more times
			if(attempts<5){
				attempts++;
				run();
				return;
			}
			System.out.println("Error accsessing:\n"+url);
			urlMem.add("Error: "+url+" IO error...\n");
			hasUpdate=-1;
			e.printStackTrace();
		}
	}
}