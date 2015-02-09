package alliance_authorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexus_http.AuthorizationException;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
import vault_database.DatabaseAccessor;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;
import alliance_rest.DatabaseEntity;

/**
 * LoginKeys are used when some functionalities are to be limited for certain users only.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class LoginKey extends DatabaseEntity
{
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new login key by reading its data from the database
	 * @param rootPath The path preceding the entity
	 * @param table The table that holds the key data.
	 * @param userIDColumnName The name of the column that holds the userID data
	 * @param userID The identifier with which the correct key data is found
	 * @param keyColumnName The name of the column that holds the key
	 * @throws HttpException If the entity couldn't be read or created
	 */
	public LoginKey(String rootPath, DatabaseTable table,
			String userIDColumnName, String userID, String keyColumnName) throws HttpException
	{
		super(new SimpleRestData(), rootPath, table, userIDColumnName, userID);
	}
	
	/**
	 * Creates a new login key by reading its data from the database. LoginKeyTable.DEFAULT is 
	 * used for storing the keys. You must take this into account.
	 * @param rootPath The path preceding the entity
	 * @param userID The identifier with which the correct key data is found
	 * @throws HttpException If the entity couldn't be read or created
	 * @see LoginKeyTable
	 */
	public LoginKey(String rootPath, String userID) throws HttpException
	{
		super(new SimpleRestData(), rootPath, LoginKeyTable.DEFAULT, 
				LoginKeyTable.getUserIDColumnName(), userID);
	}

	/**
	 * Creates a new login key with the given parameters. The key parameter is generated 
	 * automatically.
	 * @param parent The parent entity of this key
	 * @param table The table that holds the key data
	 * @param userIDColumnName The name of the column that holds the user identifier
	 * @param userID The unique identifier of the user of this key
	 * @param parameters The parameters provided by the client
	 * @param keyColumnName The name of the column that holds the key data
	 * @throws HttpException If the key couldn't be created based on the given data
	 */
	public LoginKey(RestEntity parent, DatabaseTable table,
			String userIDColumnName, String userID, Map<String, String> parameters, 
			String keyColumnName) throws HttpException
	{
		super(new SimpleRestData(), parent, table, userIDColumnName, userID, 
				modifyConstructionParameters(parameters, keyColumnName), new HashMap<>());
	}
	
	/**
	 * Creates a new login key with the given parameters. The key parameter is generated 
	 * automatically. LoginKeyTable.Default is used for storing the keys. You must take this 
	 * into account.
	 * @param parent The parent entity of this key
	 * @param userID The unique identifier of the user of this key
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the key couldn't be created based on the given data
	 * @see LoginKeyTable
	 */
	public LoginKey(RestEntity parent, String userID, Map<String, String> parameters) 
			throws HttpException
	{
		super(new SimpleRestData(), parent, LoginKeyTable.DEFAULT, 
				LoginKeyTable.getUserIDColumnName(), userID, 
				modifyConstructionParameters(parameters, LoginKeyTable.getKeyColumnName()), 
				new HashMap<>());
	}
	
	
	// IMPLEMENTED METHODS	-------------------------------

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Key data cannot be changed
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(
			Map<String, String> parameters) throws HttpException
	{
		// Can't provide entities since the key has no connections
		return new HashMap<>();
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		throw new NotFoundException(getPath() + "/" + pathPart);
	}

	@Override
	protected RestEntityList wrapIntoList(String name, RestEntity parent,
			List<RestEntity> entities)
	{
		return new SimpleRestEntityList(name, parent, entities);
	}
	
	
	// OTHER METHODS	------------------------------
	
	/**
	 * Checks if the given login key is correct
	 * @param keyTable The table that holds login key data
	 * @param userIDColumnName The name of the column that holds user identifiers
	 * @param userID The identifier of the user in question
	 * @param keyColumnName The name of the column that holds the keys
	 * @param key The key provided by the client
	 * @throws HttpException Throws an authorization exception if the key was not acceptable
	 */
	public static void checkKey(DatabaseTable keyTable, String userIDColumnName, 
			String userID, String keyColumnName, String key) throws HttpException
	{
		if (userID == null || key == null)
			throw new AuthorizationException("Invalid login key");
		
		// Checks if there is a matching key in the database
		String[] keyColumns = {userIDColumnName, keyColumnName};
		String[] keyValues = {userID, key};
		
		try
		{
			List<String> matchingIDs = DatabaseAccessor.findMatchingData(keyTable, keyColumns, 
					keyValues, userIDColumnName);
			if (matchingIDs.isEmpty())
				throw new AuthorizationException("Invalid login key");
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Failed to check the key", e);
		}
	}
	
	/**
	 * Checks if the given key is correct. LoginKeyTable is used in this method. You should 
	 * take this into account.
	 * @param userID The provided user identifier
	 * @param key The provided key
	 * @throws HttpException If the key was not acceptable or there were other problems
	 * @see LoginKeyTable
	 */
	public static void checkKey(String userID, String key) throws HttpException
	{
		checkKey(LoginKeyTable.DEFAULT, LoginKeyTable.getUserIDColumnName(), userID, 
				LoginKeyTable.getKeyColumnName(), key);
	}
	
	/**
	 * Checks if the given login key is correct
	 * @param keyTable The table that holds login key data
	 * @param userIDColumnName The name of the column that holds user identifiers
	 * @param userID The unique identifier of the user the key is for
	 * @param keyColumnName The name of the column that holds the keys
	 * @param parameters The parameters provided by the client
	 * @throws HttpException Throws an authorization exception if the key was not acceptable
	 */
	public static void checkKey(DatabaseTable keyTable, String userIDColumnName, 
			String userID, String keyColumnName, Map<String, String> parameters) 
			throws HttpException
	{
		// Checks that the correct parameters exist
		String key = parameters.get(keyColumnName);
		
		if (userID == null || key == null)
		{
			throw new InvalidParametersException("Parameter " + 
					keyColumnName + " required");
		}
		
		// Then checks the key value
		checkKey(keyTable, userIDColumnName, userID, keyColumnName, key);
	}
	
	/**
	 * Checks if the given login key is correct. LoginKeyTable is used in this method. You 
	 * should take this into account.
	 * @param userID The unique identifier of the user the key is for
	 * @param parameters The parameters provided by the client
	 * @throws HttpException Throws an authorization exception if the key was not acceptable
	 * @see LoginKeyTable
	 */
	public static void checkKey(String userID, Map<String, String> parameters) throws HttpException
	{
		checkKey(LoginKeyTable.DEFAULT, LoginKeyTable.getUserIDColumnName(), userID, 
				LoginKeyTable.getKeyColumnName(), parameters);
	}
	
	private static Map<String, String> modifyConstructionParameters(
			Map<String, String> parameters, String keyColumnName)
	{
		// Adds a generated key to the parameters
		parameters.put(keyColumnName, generateAuthKey());
		
		return parameters;
	}
	
	private static String generateAuthKey()
	{
		SecureRandom random = new SecureRandom();
		String newKey = new BigInteger(130, random).toString(32);
		
		// Removes some of the unacceptable symbols (' ', '&', '=', '?')
		newKey.replace(' ', '+');
		newKey.replace('&', '-');
		newKey.replace('=', '#');
		newKey.replace('?', '.');
		
		return newKey;
	}
}
