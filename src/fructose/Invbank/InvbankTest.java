package fructose.Invbank;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvbankTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Testing Invbank.");
		InvbankDBConn dbConn = new InvbankDBConn();
		if(dbConn.isConnected())
			System.out.println("Connected to database.");
		else
			System.out.println("Unable to connect to database.");
		
		try {
			int rows = DBToolKit.getInstance().selectQuery("SELECT * FROM user");
			System.out.println("Rows affected: "+rows);
			for(int i = 0; i < rows; i++)
			{
				if(DBToolKit.getInstance().get_value(i, 2) == null)
					System.out.println("ID: "+DBToolKit.getInstance().get_value(i, 0)+": "+DBToolKit.getInstance().get_value(i, 1)+" : "+DBToolKit.getInstance().get_value(i, 2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if(DBToolKit.getInstance().selectQuery("SELECT password FROM user WHERE name = 'testuser3'")==0);
				DBToolKit.getInstance().updateQuery("INSERT IGNORE INTO user SET name = 'testuser3'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}

}
