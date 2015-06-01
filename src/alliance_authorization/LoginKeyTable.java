package alliance_authorization;

import java.sql.SQLException;
import java.util.Map;

import nexus_http.AuthorizationException;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import vault_database.DatabaseAccessor;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;
import vault_database.InvalidTableTypeException;

/**
 * LoginKeyTables are used for storing login key entities. This interface should be 
 * implemented by an enumeration
 * @author Mikko Hilpinen
 * @since 1.5.2015
 */
public interface LoginKeyTable extends DatabaseTable
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
	
	
	// METHODS	-----------------------
	
	/**
	 * Checks if the given login key is correct
	 * @param keyTable The table that holds login key data
	 * @param userID The identifier of the user in question
	 * @param key The key provided by the client
	 * @throws HttpException Throws an authorization exception if the key was not acceptable
	 */
	public static void checkKey(LoginKeyTable keyTable, String userID, String key) throws 
			HttpException
	{
		if (userID == null || key == null)
			throw new AuthorizationException("Invalid login key");
		
		// Checks if there is a matching key in the database
		String[] keyColumns = {keyTable.getUserIDColumnName(), keyTable.getKeyColumnName()};
		String[] keyValues = {userID, key};
		
		try
		{
			if (DatabaseAccessor.findMatchingIDs(keyTable, 
					keyColumns, keyValues, 1).isEmpty())
				throw new AuthorizationException("Invalid login key");
		}
		catch (DatabaseUnavailableException | SQLException | InvalidTableTypeException e)
		{
			throw new InternalServerException("Failed to check the key", e);
		}
	}
	
	/**
	 * Checks if the given login key is correct
	 * @param keyTable The table that holds login key data
	 * @param userID The unique identifier of the user the key is for
	 * @param parameters The parameters provided by the client
	 * @throws HttpException Throws an authorization exception if the key was not acceptable
	 */
	public static void checkKey(LoginKeyTable keyTable, String userID, 
			Map<String, String> parameters) throws HttpException
	{
		// Checks that the correct parameters exist
		String key = parameters.get(keyTable.getKeyColumnName());
		
		if (userID == null)
			throw new InternalServerException("Can't check loginkey for nonexisting user");
		if (key == null)
		{
			throw new InvalidParametersException("Parameter " + 
					keyTable.getKeyColumnName() + " required");
		}
		
		// Then checks the key value
		checkKey(keyTable, userID, key);
	}
}
