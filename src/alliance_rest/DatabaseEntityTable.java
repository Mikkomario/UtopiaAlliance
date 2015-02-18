package alliance_rest;

import java.sql.SQLException;
import java.util.List;

import vault_database.DatabaseAccessor;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * These tables contain data about database entities
 * 
 * @author Mikko Hilpinen
 * @since 12.2.2015
 */
public interface DatabaseEntityTable extends DatabaseTable
{
	/**
	 * @return The name of the column that contains the entity's identifier
	 */
	public String getIDColumnName();
	
	/**
	 * Finds the identifiers of the entities with the matching data
	 * @param table The entity table
	 * @param keyColumn The name of the column that holds the key data
	 * @param keyValue The key data that is searched for
	 * @return A list of all the identifiers with matching data
	 * @throws DatabaseUnavailableException If the database couldn't be reached
	 * @throws SQLException If the search couldn't be performed
	 */
	public static List<String> findMatchingIDs(DatabaseEntityTable table, 
			String keyColumn, String keyValue) throws DatabaseUnavailableException, 
			SQLException
	{
		return DatabaseAccessor.findMatchingData(table, keyColumn, keyValue, table.getIDColumnName());
	}
	
	/**
	 * Finds the identifiers of the entities with the matching data
	 * @param table The entity table
	 * @param keyColumns The name of the columns that holds the key data
	 * @param keyValues The key data that is searched for
	 * @return A list of all the identifiers with matching data
	 * @throws DatabaseUnavailableException If the database couldn't be reached
	 * @throws SQLException If the search couldn't be performed
	 */
	public static List<String> findMatchingIDs(DatabaseEntityTable table, String[] keyColumns, 
			String[] keyValues) throws DatabaseUnavailableException, SQLException
	{
		return DatabaseAccessor.findMatchingData(table, keyColumns, keyValues, 
				table.getIDColumnName());
	}
	
	/**
	 * Finds the identifiers of the entities with the matching data
	 * @param table The entity table
	 * @param keyColumn The name of the column that holds the key data
	 * @param keyValue The key data that is searched for
	 * @param limit How many identifiers will be returned at maximum
	 * @return A list of all the identifiers with matching data
	 * @throws DatabaseUnavailableException If the database couldn't be reached
	 * @throws SQLException If the search couldn't be performed
	 */
	public static List<String> findMatchingIDs(DatabaseEntityTable table, 
			String keyColumn, String keyValue, int limit) throws DatabaseUnavailableException, 
			SQLException
	{
		return DatabaseAccessor.findMatchingData(table, keyColumn, keyValue, 
				table.getIDColumnName(), limit);
	}
	
	/**
	 * Finds the identifiers of the entities with the matching data
	 * @param table The entity table
	 * @param keyColumns The name of the columns that holds the key data
	 * @param keyValues The key data that is searched for
	 * @param limit How many identifiers will be returned at maximum
	 * @return A list of all the identifiers with matching data
	 * @throws DatabaseUnavailableException If the database couldn't be reached
	 * @throws SQLException If the search couldn't be performed
	 */
	public static List<String> findMatchingIDs(DatabaseEntityTable table, String[] keyColumns, 
			String[] keyValues, int limit) throws DatabaseUnavailableException, SQLException
	{
		return DatabaseAccessor.findMatchingData(table, keyColumns, keyValues, 
				table.getIDColumnName(), limit);
	}
}
