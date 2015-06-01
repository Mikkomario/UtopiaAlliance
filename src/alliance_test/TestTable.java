package alliance_test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * This table holds the basic entity data
	 */
	ENTITY,
	/**
	 * This table holds the entity's password data
	 */
	SECURE;
	
	// ATTRIBUTES	---------------------------
	
	private static Map<DatabaseTable, List<ColumnInfo>> columnInfo = null;
	
	
	// IMPLEMENTED METHODS	-------------------

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
		return DatabaseTable.findPrimaryColumnInfo(getColumnInfo()).usesAutoIncrementIndexing();
	}

	@Override
	public boolean usesIntegerIndexing()
	{
		return true;
	}

	@Override
	public String getPrimaryColumnName()
	{
		return DatabaseTable.findPrimaryColumnInfo(getColumnInfo()).getColumnName();
	}
	
	
	// OTHER METHODS	---------------------------
	
	private List<ColumnInfo> getColumnInfo()
	{
		if (columnInfo == null)
			columnInfo = new HashMap<>();
		
		if (!columnInfo.containsKey(this))
		{
			try
			{
				columnInfo.put(this, DatabaseTable.readColumnInfoFromDatabase(this));
			}
			catch (DatabaseUnavailableException | SQLException e)
			{
				System.err.println("Failed to read the column info");
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
		
		return columnInfo.get(this);
	}
}
