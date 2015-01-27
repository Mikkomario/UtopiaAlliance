package alliance_authorization;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * LoginKeyTable holds the login keys.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public enum LoginKeyTable implements DatabaseTable
{
	/**
	 * The default (and only) login key table. Should contain the following columns: 'userID' 
	 * and 'key'
	 */
	DEFAULT;
	
	
	// ATTRIBUTES	-------------------------------
	
	private static List<String> columnNames = null;

	
	// IMPLEMENTED METHODS	-----------------------
	
	@Override
	public List<String> getColumnNames()
	{
		if (columnNames == null)
		{
			try
			{
				columnNames = DatabaseTable.readColumnNamesFromDatabase(this);
			}
			catch (DatabaseUnavailableException | SQLException e)
			{
				System.err.println("Failed to read the column names");
				e.printStackTrace();
				columnNames = new ArrayList<>();
			}
		}
		
		return columnNames;
	}

	@Override
	public String getDatabaseName()
	{
		return "alliance_db";
	}

	@Override
	public String getTableName()
	{
		return "keys";
	}

	@Override
	public boolean usesAutoIncrementIndexing()
	{
		return false;
	}

	@Override
	public boolean usesIndexing()
	{
		return false;
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * @return The name of the column used for storing the user's identifier
	 */
	public static String getUserIDColumnName()
	{
		return "userID";
	}
	
	/**
	 * @return The name of the column used for storing the key
	 */
	public static String getKeyColumnName()
	{
		return "key";
	}
}
