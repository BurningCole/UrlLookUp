public interface IUpdateChecker{
	public void startScan(String outputFile);
	public boolean testUrlFor(String url,String string,String exclude)throws Exception;
	public String[] getResults();
}