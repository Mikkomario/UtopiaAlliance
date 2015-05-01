package alliance_authorization;

import alliance_rest.DatabaseEntityTable;

/**
 * LoginKeyTables are used for storing login key entities. This interface should be 
 * implemented by an enumeration
 * @author Mikko Hilpinen
 * @since 1.5.2015
 */
public interface LoginKeyTable extends DatabaseEntityTable
{
	/**
	 * @return The name of the column that holds the user ID's
	 */
	public String getUserIDColumnName();
	
	/**
	 * @return The name of the column used for storing the key
	 */
	public String getKeyColumnName();
	
	/**
	 * @return The name of the column that holds the key's creation time
	 */
	public String getCreationTimeColumnName();
}
