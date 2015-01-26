package alliance_test;

import java.sql.SQLException;
import java.util.List;

import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * This table is used for holding the test entities created during alliance tests
 * 
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public enum TestTable implements DatabaseTable
{
	// id (auto-increment) | name | friendID
	
	/**
	 * The only test table at this point
	 */
	DEFAULT;
	
	// ATTRIBUTES	---------------------------
	
	private static List<String> columnNames = null;
	
	
	// IMPLEMENTED METHODS	-------------------

	@Override
	public List<String> getColumnNames()
	{
		try
		{
			if (columnNames == null)
				columnNames = DatabaseTable.readColumnNamesFromDatabase(this);
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			System.err.println("Failed to read the column names");
			e.printStackTrace();
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
		return "test";
	}

	@Override
	public boolean usesAutoIncrementIndexing()
	{
		return true;
	}

	@Override
	public boolean usesIndexing()
	{
		return true;
	}
}
