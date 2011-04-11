package fructose.Invbank;

import java.sql.*;
import java.util.ArrayList;
import com.mysql.jdbc.Driver;




public class DBToolKit {
	private static DBToolKit instance;
	private static Connection conn;
	public ArrayList theSelectStatementData;
	
	private DBToolKit(String url, String user, String pass) throws SQLException, ClassNotFoundException{
		conn = getConnection(url,user,pass);
	}
	public static void init(String url, String user, String pass) throws SQLException, ClassNotFoundException{
		if(instance==null)
			instance = new DBToolKit(url,user,pass);
	}
	public static DBToolKit getInstance(){
		return instance;
	}
	public int selectQuery(String query) throws SQLException{
		ArrayList theColumns;
		int nNumberOfColumns = 0;
		int nRowCounter = 0;
		Statement stmtStatement = conn.createStatement();
		//the drivers default to waiting 60 seconds and then timing out.  We want 120 seconds for selects.
		stmtStatement.setQueryTimeout(120);
		ResultSet rsResultSet = stmtStatement.executeQuery(query);
		ResultSetMetaData rsmd = rsResultSet.getMetaData();

		nNumberOfColumns = rsmd.getColumnCount();
		try
		{
			try {
				theSelectStatementData = new ArrayList();

				while (rsResultSet.next())
				{
					theColumns = new ArrayList();
					for (int nCount = 1; nCount <= nNumberOfColumns; nCount++)
						theColumns.add(rsResultSet.getString(nCount));

					theSelectStatementData.add(theColumns);

					nRowCounter++;
				}
			}
			catch (SQLException e) {
			System.out.println("Error encountered executing SQL statement \n     " + query + "\n     ");
			}
		}
		catch (Exception sError)
		{
				System.out.println("Error encountered executing SQL statement \n     " + query);
				return -1;
		}
		try
		{
			// Release the object's database and JDBC resources.
			stmtStatement.close();
			rsResultSet.close();
		} catch (SQLException sError)
		{
		}

		return nRowCounter;
	}
	public String get_value(int nRowIndex, int nColumnIndex)
	{
		String sReturn;

		try
		{
			sReturn = (String) ((ArrayList) theSelectStatementData.get(nRowIndex)).get(nColumnIndex);
		}
		catch (Exception sError)
		{
			sReturn = "";
		}

		return sReturn;
	}
	public int updateQuery(String query) throws SQLException{
		Statement st = conn.createStatement();
		int val = 0;
		try {
			val = st.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Query failed: "+query);
			e.printStackTrace();
		}
		st.close();
		return val;
	}
	private Connection getConnection(String dbURL, String user, String password)
		throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(dbURL,user,password);
	}
	public void close(){
		if(conn!=null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			instance = null;
			conn = null;
		}
	}
}
