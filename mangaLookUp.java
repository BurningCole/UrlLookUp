import java.net.*;
import java.io.*;
import java.awt.Desktop;

public class mangaLookUp{
	private String fileName;
	private String accept;
	private String exclude;
	
	public mangaLookUp(String FileName, String Accept, String Exclude){
		fileName=FileName;
		accept=Accept;
		exclude=Exclude;
	}
	
	public void startScan(String outputFile){
		int maxThreads=999;
		try(
			BufferedReader br = new BufferedReader(new FileReader(fileName))){
			BufferedWriter text = new BufferedWriter(new FileWriter(outputFile));
			int read=0;
			Thread[] lookups = new Thread[maxThreads];
			mangaLookUpLine[] Line=new mangaLookUpLine[maxThreads];
			for(String line; (line = br.readLine()) != null; read++){
				Line[read]=new mangaLookUpLine(line.split(">")[1],accept,exclude);
				lookups[read]=new Thread(Line[read]);
				lookups[read].start();
			}
			br.close();
			for(int i=0;i<read;i++){
				if(lookups[i].isAlive()){
					try{
						lookups[i].join();
					}catch(InterruptedException e){}
					System.out.println("read "+(i+1)+" Lines");
				}
				if(Line[i].result()){
					text.write("Manga Line: "+(i+1));
					text.newLine();
					text.write(Line[i].getUrl());
					text.newLine();
				}
			}
			text.close();
			if (Desktop.isDesktopSupported()){
				Desktop.getDesktop().edit(new File(outputFile));
			}
		}catch(FileNotFoundException e){
			System.out.println("\nFile didn't exist");
		}catch(IOException e){
			System.out.println("\nCouldn't access file");
		}
		System.out.println("\n---DONE---");
	}
	
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
	
	public static void main(String[] args)throws Exception{
		Class<?> c = mangaLookUp.class;
		String fileLoc=c.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:","");
		fileLoc=fileLoc.replace("mangaLookUp.jar","");
		fileLoc=fileLoc+"data/";
		System.out.println(fileLoc);
		mangaLookUp lookup=new mangaLookUp(fileLoc+"MangaUrls.txt","=\"back","=\"clearfix");
		lookup.startScan(fileLoc+"updates.txt");
	}
}