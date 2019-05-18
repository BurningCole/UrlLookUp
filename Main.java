public class Main{
	public static void main(String[] args)throws Exception{
		//find update file
		Class<?> c = mangaLookUp.class;
		//remove unnesasary parts of string that are returned
		String fileLoc=c.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:","");
		fileLoc=fileLoc.replace("mangaLookUp.jar","");
		//add data folder to location
		fileLoc=fileLoc+"data/";
		System.out.println(fileLoc);
		
		//create lookup
		IUpdateChecker lookup=new mangaLookUp(fileLoc+"MangaUrls.txt","=\"back","=\"clearfix");
		//start scan
		lookup.startScan(fileLoc+"updates.txt");
	}
}