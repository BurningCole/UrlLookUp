import java.sql.*;
import java.io.File;

public class DbBasic {

	private static final String JDBC_DRIVER = "org.sqlite.JDBC";
	private static final String DATABASE_LOCATION = "jdbc:sqlite:";
	protected Connection con	= null;
	public String dbName	= null;
	private ResultSet rs = null;
	
	/**
	* Establish JDBC connection with database
	*/
	private void getConnection( ) {
		try {
			con = DriverManager.getConnection(
					  DATABASE_LOCATION
					+ dbName);

			con.setAutoCommit(false);
		}
		catch ( SQLException sqle ) {
			notify( "Db.getConnection database location ["
					+ DATABASE_LOCATION
					+ "] db name["
					+ dbName
					+ "]", sqle);
			close( );
		}
	}
	
	public void notify( String message, Exception e ) {
		System.out.println( message + " : " + e );
		e.printStackTrace ( );
		System.exit( 0 );
	}
	
	/**
	* Opens database
	* 
	* loads JDBC driver and establishes JDBC connection to database
	*/
	private void open( ) {
		File dbf = new File( dbName );

		if ( dbf.exists( ) == false ) {
			System.out.println(
				 "SQLite database file ["
				+ dbName
				+ "] does not exist");
			System.exit( 0 );
		}
	
		try {
			Class.forName(JDBC_DRIVER);
			getConnection();
		}
		catch (ClassNotFoundException cnfe) {
			notify("Db.Open", cnfe);
		}
	}
	
	/**
	* Close database
	* 
	* Commits any remaining updates to database and
	* closes connection
	*/
	public final void close( ) {
		try {
			con.commit( ); // Commit any updates
			con.close ( );
		}
		catch ( Exception e ) {
			notify( "Db.close", e );
		};
	}

	/**
	* Records a copy of the database name and
	* opens the database for use
	*
	* @param _dbName	String holding the name of the database,
	* 			for example, C:/directory/subdir/mydb.db
	*/
	public DbBasic( String _dbName ) {
		dbName = _dbName;
		open( );
	}
	
	public ResultSet doQuery(String query){
		ResultSet rs=null;
		try{
			Statement statement = con.createStatement();
			rs=statement.executeQuery(query);
		}catch(SQLException e){
			System.out.println("statement:\n"+query+"\nUnresolveable");
			e.printStackTrace();
		}
		return rs;
	}
	
	public void runSQL(String sql){
		try{
			Statement statement = con.createStatement();
			statement.execute(sql);
		}catch(SQLException e){
			System.out.println("SQL:\n"+sql+"\nUnresolveable");
			e.printStackTrace();
		}
	}
	
	public void runScript(String script){
		String[] commands = script.split(";");
		for(String command:commands){
			if(command!="")
			doQuery(command);
		}
	}
}
