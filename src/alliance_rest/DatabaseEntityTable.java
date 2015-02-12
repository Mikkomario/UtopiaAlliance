package alliance_rest;

import vault_database.DatabaseTable;

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
}
