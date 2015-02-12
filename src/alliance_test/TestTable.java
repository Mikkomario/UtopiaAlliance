package alliance_test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alliance_rest.DatabaseEntityTable;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * This table is used for holding the test entities created during alliance tests
 * 
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public enum TestTable implements DatabaseEntityTable
{
	// id (auto-increment) | name | friendID
	
	/**
	 * This table holds the basic entity data
	 */
	ENTITY,
	/**
	 * This table holds the entity's password data
	 */
	SECURE;
	
	// ATTRIBUTES	---------------------------
	
	private static Map<DatabaseTable, List<String>> columnNames = null;
	
	
	// IMPLEMENTED METHODS	-------------------

	@Override
	public List<String> getColumnNames()
	{
		try
		{
			if (columnNames == null)
				columnNames = new HashMap<>();
			
			if (!columnNames.containsKey(this))
				columnNames.put(this, DatabaseTable.readColumnNamesFromDatabase(this));
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			System.err.println("Failed to read the column names");
			e.printStackTrace();
		}
		
		return columnNames.get(this);
	}

	@Override
	public String getDatabaseName()
	{
		return "alliance_db";
	}

	@Override
	public String getTableName()
	{
		switch (this)
		{
			case ENTITY: return "entities";
			case SECURE: return "secure";
		}
		
		return null;
	}

	@Override
	public boolean usesAutoIncrementIndexing()
	{
		switch (this)
		{
			case ENTITY: return true;
			case SECURE: return false;
		}
		
		return false;
	}

	@Override
	public boolean usesIndexing()
	{
		return true;
	}

	@Override
	public String getIDColumnName()
	{
		return "id";
	}
}
