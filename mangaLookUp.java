import java.net.*;
import java.io.*;
import java.awt.Desktop;
import javax.swing.*;

public class mangaLookUp{
	private String fileName;
	private String accept;
	private String exclude;
	private JLabel text1,text2;
	
	public mangaLookUp(String FileName, String Accept, String Exclude){
		fileName=FileName;
		accept=Accept;
		exclude=Exclude;
	}
	
	public void startScan(String outputFile){
		int maxThreads=64;
		StringBuilder current=new StringBuilder("[");
		for(int i=0;i<maxThreads;i++)
			current.append("=");
		current.append("]");
		int finished=0;
		StringBuilder total=new StringBuilder(current);
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
		try(
			BufferedReader br = new BufferedReader(new FileReader(fileName))){
			BufferedWriter text = new BufferedWriter(new FileWriter(outputFile));
			int read=0,i=0;
			Thread[] lookups = new Thread[maxThreads];
			mangaLookUpLine[] Line=new mangaLookUpLine[maxThreads];
			String[] names = new String[maxThreads];
			for(String line; (line = br.readLine()) != null; read++){
				if(read>=maxThreads){
					if(lookups[read%maxThreads]==null){
						System.out.println("Line: "+(read+1)+" not a correct format");
						text.write("Manga Line: "+(read+1)+" not a correct format");
						text.newLine();
						text.newLine();
					}else{
					if(lookups[read%maxThreads].isAlive()){
						try{
							lookups[i%maxThreads].join();
						}catch(InterruptedException e){
							System.out.println("Thread joining error");
						}
						System.out.println("read "+(i+1)+" Lines");
					}
					if(Line[i%maxThreads].result()){
						text.write("Manga Line: "+(i+1)+" ("+names[i%maxThreads]+")");
						text.newLine();
						text.write(Line[i%maxThreads].getUrl());
						text.newLine();
					}}
					i++;
				}
				String[] lineSplit=line.split(">");
				names[read%maxThreads]=lineSplit[0];
				if(lineSplit.length!=2){
					continue;
				}
				Line[read%maxThreads]=new mangaLookUpLine(lineSplit[1],accept,exclude);
				lookups[read%maxThreads]=new Thread(Line[read%maxThreads]);
				lookups[read%maxThreads].start();
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
			br.close();
			for(;i<read;i++){
				if(lookups[read%maxThreads]==null){
						System.out.println("Line: "+(read+1)+" not a correct format");
						text.write("Manga Line: "+(read+1)+" not a correct format");
						text.newLine();
						text.newLine();
				}else{
				if(lookups[i%maxThreads].isAlive()){
					try{
						lookups[i%maxThreads].join();
					}catch(InterruptedException e){
						System.out.println("Thread joining error");
					}
					System.out.println("read "+(i+1)+" Lines");
				}
				if(Line[i%maxThreads].result()){
					text.write("Manga Line: "+(i+1)+" ("+names[i%maxThreads]+")");
					text.newLine();
					text.write(Line[i%maxThreads].getUrl());
					text.newLine();
				}}
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
		frame.dispose();
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