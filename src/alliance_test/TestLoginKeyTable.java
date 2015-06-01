package alliance_test;

import java.sql.SQLException;
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
	 * The default (and only) login key table. Should contain the following columns: 
	 * 'userID', 'userKey' and 'created'
	 */
	DEFAULT;
	
	
	// ATTRIBUTES	-------------------------------
	
	private static List<ColumnInfo> columnInfo = null;

	
	// IMPLEMENTED METHODS	-----------------------
	
	@Override
	public List<String> getColumnNames()
	{
		return DatabaseTable.getColumnNamesFromColumnInfo(getColumnInfo());
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

	@Override
	public boolean usesIntegerIndexing()
	{
		return true;
	}

	@Override
	public String getPrimaryColumnName()
	{
		return getUserIDColumnName();
	}
	
	
	// OTHER METHODS	------------------------	
	
	private List<ColumnInfo> getColumnInfo()
	{
		if (columnInfo == null)
		{
			try
			{
				columnInfo = DatabaseTable.readColumnInfoFromDatabase(this);
			}
			catch (DatabaseUnavailableException | SQLException e)
			{
				System.err.println("Failed to read the column info");
				e.printStackTrace();
			}
		}
		
		return columnInfo;
	}
}
