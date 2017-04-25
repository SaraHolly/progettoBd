package basidati;

//import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class DBConnectionPool {

	private	static 	List<Connection> freeDbConnections;
	static{ 
		freeDbConnections = new	LinkedList<Connection>();
		try	{
			Class.forName("com.mysql.jdbc.Driver");
		}catch	(ClassNotFoundException e) {
			System.out.println(	"DB driver not found!"+ e);
		//} catch	(IOException e) {
			//System.out.println(	"DB connection pool error!"	, e);
		}
	}

	private static Connection createDBConnection() throws SQLException	{
		Connection newConnection = null;
		//String ip = "localhost";
		//String port = "3306";
		//String db = "provaProgetto";
		String username = "root";
		String password = "sara";
		newConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/provaProgetto", username,password);
		newConnection.setAutoCommit(false);
		return	newConnection;
	}

	public static synchronized Connection getConnection() throws	SQLException {
		Connection connection;
		if(! freeDbConnections.isEmpty()){
			connection = (Connection) freeDbConnections.get(0);
			DBConnectionPool.freeDbConnections.remove(0);
			try{
				if(connection.isClosed())
					connection = DBConnectionPool.getConnection();
			} catch(SQLException e) {
				System.out.println("ERRORE");
				//connection = DBConnectionPool.getConnection();
			}
			System.out.println("nell'if");
		}else  
			connection = DBConnectionPool.createDBConnection();
		return	connection;
	}

	public static synchronized void releaseConnection(Connection connection) {
		//DBConnectionPool.freeDbConnections.add(connection);
		try{
		connection.close();
		}catch(SQLException e){
			System.out.println("DBC ex : "+ e.getMessage());
		}
	}
	
	
}
