import java.io.*;

public final class AddToDb{
	public static void main(String[] args){
		try{
			BufferedReader in = new BufferedReader(new FileReader("Urls.txt"));
			BufferedWriter out = new BufferedWriter(new FileWriter("create.sql"));
			int id=1;
			String line;
			out.write("create TABLE websites(\n");
			out.write("webId INTEGER NOT NULL,\n");
			out.write("url VARCHAR(75),\n");
			out.write("PRIMARY KEY(webId)\n");
			out.write(");\n");
			
			out.write("INSERT INTO websites VALUES (0,\"https://mangakakalot.com/chapter/\");\n");
			out.write("INSERT INTO websites VALUES (1,\"https://manganelo.com/chapter/\");\n");
			
			out.write("create TABLE urls(\n");
			out.write("id INTEGER NOT NULL,\n");
			out.write("webId INTEGER NOT NULL,\n");
			out.write("url VARCHAR(150) NOT NULL,\n");
			out.write("alias VARCHAR(100),\n");
			out.write("PRIMARY KEY(id),\n");
			out.write("FOREIGN KEY(webId) references websites(webId)\n);\n");
			while((line = in.readLine()) != null){
				line=line.replace("\"","\"\"");
				String[] lines=line.split(">");
				int webId;
				String newUrl;
				if(lines[1].contains("https://mangakakalot.com/chapter/")){
					newUrl=lines[1].replace("https://mangakakalot.com/chapter/","");
					webId=0;
				}else{
					newUrl=lines[1].replace("https://manganelo.com/chapter/","");
					webId=1;
				}
				
				
				out.write("INSERT INTO urls VALUES ("+id+", "+webId+", \""+newUrl+"\", \""+lines[0]+"\");");
				out.newLine();
				id++;
			}
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}