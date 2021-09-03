import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

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
	private String error;
	
	public UrlUpdate(int ID,int Type,List<String> Urls,String UrlSubPart,String Name){
		this.ID=ID;
		this.type=Type;
		this.urls=Urls;
		this.urlSubPart=UrlSubPart;
		this.name=Name;
		switch(Type){
			case NORMAL:
				error = null;
				break;
			case MISSING_EXCLUDE:
				error = "Page not valid lookup";
				break;
			case SOCK_ERROR:
				error = "Issue to do with socket";
				break;
			case READ_ERROR:
				error = "Could not get webpage";
				break;
			default:
				error = "Should never be seen";
				break;
		}
	}
	
	public String getSQLStatement(){
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		switch(type){
			case NORMAL:
				return "UPDATE urls SET url='"+urlSubPart+"', updated='"+DATE_FORMAT.format(new Date())+"' WHERE id = "+ID+";";
			case MISSING_EXCLUDE:
				return "UPDATE urls SET ^, updated='"+DATE_FORMAT.format(new Date())+"' WHERE id = "+ID+";";
			default:
				return "--OTHER ERROR";
		}
	}
	public String getDeleteStatement(){
		return "DELETE FROM urls WHERE id = "+ID+";";
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
	
	public String getError(){
		return error;
	}
	
	public void setUrl(String url){
		urlSubPart=url;
	}
}