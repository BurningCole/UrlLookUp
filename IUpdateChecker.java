import java.util.List;

public interface IUpdateChecker{
	public void startScan(String outputFile);
	public boolean testUrlFor(String url,String string,String exclude)throws Exception;
	public List<UrlUpdate> getResults();
}