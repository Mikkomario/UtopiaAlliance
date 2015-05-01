package alliance_test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import alliance_authorization.LoginKeyTable;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * LoginKeyTable holds the login keys.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public enum TestLoginKeyTable implements LoginKeyTable
{
	/**
	 * The default (and only) default login key table. Should contain the following columns: 
	 * 'userID', 'userKey' and 'created'
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
		return "loginKeys";
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
	
	@Override
	public String getIDColumnName()
	{
		return "userID";
	}
	
	
	// OTHER METHODS	------------------------

	@Override
	public String getUserIDColumnName()
	{
		return "userID";
	}

	@Override
	public String getKeyColumnName()
	{
		return "userKey";
	}

	@Override
	public String getCreationTimeColumnName()
	{
		return "created";
	}
}
