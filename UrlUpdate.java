import java.util.List;

public class UrlUpdate{
	
	static final int NORMAL=0;
	static final int MISSING_EXCLUDE=-1;
	static final int SOCK_ERROR=-2;
	static final int READ_ERROR=-3;
	
	private int ID;
	private int type;
	private List<String> urls;
	private String name;
	private String urlSubPart;
	
	
	public UrlUpdate(int ID,int Type,List<String> Urls,String UrlSubPart,String Name){
		this.ID=ID;
		this.type=Type;
		this.urls=Urls;
		this.urlSubPart=UrlSubPart;
		this.name=Name;
	}
	public String getSQLStatement(){
		switch(type){
			case NORMAL:
				return "UPDATE urls SET url='"+urlSubPart+"' WHERE id = "+ID+";";
			case MISSING_EXCLUDE:
				return "UPDATE urls SET ^ WHERE id = "+ID+";";
			default:
				return "--OTHER ERROR";
		}
	}
	public String name(){
		return name;
	}
	public List<String> urls(){
		return urls;
	}
	
	public int getType(){
		return type;
	}
	
	public String getUrl(){
		return urlSubPart;
	}
	
	public void setUrl(String url){
		urlSubPart=url;
	}
}
/*
case 0:
output.write("Manga Line: "+actualIDs[id%maxThreads]+" ("+names[id%maxThreads]+")");
output.newLine();
ArrayList<String> urls = Line[id%maxThreads].getAllUrls();
for (String url:urls){
output.write("\t"+url);
output.newLine();

}
output.newLine();

String newUrl=Line[id%maxThreads].getUrl();
ResultSet rs=db.doQuery("SELECT url FROM websites");
try{
while(rs.next()){
System.out.println(newUrl+":"+rs.getString("url"));
if(newUrl.startsWith(rs.getString("url"))){
System.out.println("Match");
newUrl=newUrl.substring(
rs.getString("url").length()
);
break;
}
}
sqlfile.write("UPDATE urls SET url='"+newUrl+"' WHERE id = "+actualIDs[id%maxThreads]+";");
sqlfile.newLine();
}catch(SQLException e){
e.printStackTrace();
}
break;
//if error
case -1:
output.write("Error on ID: "+actualIDs[id%maxThreads]+" ("+names[id%maxThreads]+") ... no exclude");
output.newLine();
output.write(Line[id%maxThreads].getUrl());
output.newLine();
output.newLine();

sqlfile.write("--UPDATE urls SET url='#Change this#' WHERE id = "+actualIDs[id%maxThreads]+";");
sqlfile.newLine();
break;
case -2:
output.write("Error on ID: "+actualIDs[id%maxThreads]+" ("+names[id%maxThreads]+") ... Socket error");
output.newLine();
output.write(Line[id%maxThreads].getUrl());
output.newLine();
output.newLine();

sqlfile.write("--UPDATE urls SET url='#Change this#' WHERE id = "+actualIDs[id%maxThreads]+";");
sqlfile.newLine();
break;
case -3:
output.write("Error on ID: "+actualIDs[id%maxThreads]+" ("+names[id%maxThreads]+") ... couldn't read website");
output.newLine();
output.write(Line[id%maxThreads].getUrl());
output.newLine();
output.newLine();

sqlfile.write("--UPDATE urls SET url='#Change this#' WHERE id = "+actualIDs[id%maxThreads]+";");
sqlfile.newLine();
break;
*/