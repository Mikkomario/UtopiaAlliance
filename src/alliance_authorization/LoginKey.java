package alliance_authorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;
import vault_database.DatabaseAccessor;
import vault_database.DatabaseUnavailableException;
import alliance_rest.DatabaseEntity;
import alliance_util.SimpleDate;

/**
 * LoginKeys are used when some functionalities are to be limited for certain users only.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class LoginKey extends DatabaseEntity
{
	// ATTRIBUTES	-------------------------
	
	private LoginKeyTable table;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new login key by reading its data from the database
	 * @param rootPath The path preceding the entity
	 * @param table The table that holds the key data.
	 * @param id The identifier with which the correct key data is found
	 * @throws HttpException If the entity couldn't be read or created
	 */
	public LoginKey(String rootPath, LoginKeyTable table, String id) throws HttpException
	{
		super(new SimpleRestData(), rootPath, table, id);
		
		this.table = table;
	}

	/**
	 * Creates a new login key with the given parameters. The key parameter and the creation 
	 * time parameter are generated automatically.
	 * @param parent The parent entity of this key
	 * @param table The table that holds the key data
	 * @param userID The unique identifier of the user of this key
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the key couldn't be created based on the given data
	 */
	public LoginKey(RestEntity parent, LoginKeyTable table, String userID, 
			Map<String, String> parameters) throws HttpException
	{
		super(new SimpleRestData(), parent, table, userID, 
				modifyConstructionParameters(parameters, userID, table), new HashMap<>());
		
		this.table = table;
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
	
	/**
	 * Login keys require authorization before they can be deleted by the client. They are not 
	 * deleted by id but by key since multiple keys can exist for a single userID.
	 */
	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		// Normal delete requires authorization
		LoginKeyTable.checkKey(this.table, getUserID(), parameters);
		
		try
		{
			DatabaseAccessor.delete(getTable(), this.table.getKeyColumnName(), 
					parameters.get(this.table.getKeyColumnName()));
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't delete the login key", e);
		}
	}
	
	
	// OTHER METHODS	------------------------------
	
	/**
	 * Deletes the key from the database. Doesn't require authorization. Also, doesn't delete 
	 * the entity by id but by key.
	 * @throws HttpException If the operation failed
	 */
	public void deleteWithoutAuthorization() throws HttpException
	{
		try
		{
			DatabaseAccessor.delete(getTable(), this.table.getKeyColumnName(), getKey());
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't delete " + getPath(), e);
		}
	}
	
	/**
	 * @return The identifier of the user of this key
	 */
	public String getUserID()
	{
		return getAttributes().get(this.table.getUserIDColumnName());
	}
	
	/**
	 * @return The unique attribute key of this key
	 */
	public String getKey()
	{
		return getAttributes().get(this.table.getKeyColumnName());
	}
	
	/**
	 * @return The date when the key was created
	 * @throws HttpException If the creation time couldn't be read
	 */
	public SimpleDate getCreationTime() throws HttpException
	{
		try
		{
			return new SimpleDate(getAttributes().get(this.table.getCreationTimeColumnName()));
		}
		catch (ParseException e)
		{
			throw new InternalServerException("Failed to read login key creation time", e);
		}
	}
	
	private static Map<String, String> modifyConstructionParameters(
			Map<String, String> parameters,  String userID, LoginKeyTable table)
	{
		// Adds a generated key to the parameters
		parameters.put(table.getKeyColumnName(), generateAuthKey());
		// Also adds the userID to the parameters
		parameters.put(table.getUserIDColumnName(), userID);
		parameters.put(table.getCreationTimeColumnName(), new SimpleDate().toString());
		
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
