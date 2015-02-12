package alliance_authorization;

import java.util.HashMap;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;
import alliance_rest.DatabaseEntityTable;
import alliance_rest.DatabaseTableEntity;

/**
 * This entity manages the login and logout activities. Login is done with GET .../userID 
 * while logout is done with DELETE .../userID
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class LoginManagerEntity extends DatabaseTableEntity
{
	// ATTIRBUTES	--------------------------------
	
	private String keyColumnName;
	private PasswordChecker passwordChecker;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new entity
	 * @param name The name of the entity
	 * @param parent The parent of the entity
	 * @param keyTable The table which contains the key data
	 * @param keyColumnName The name of the key column in the key table
	 * @param passwordChecker The password checker which is used for validating the 
	 * requests (null if no validation is required)
	 */
	public LoginManagerEntity(String name, RestEntity parent, DatabaseEntityTable keyTable, 
			String keyColumnName, PasswordChecker passwordChecker)
	{
		super(name, new SimpleRestData(), parent, keyTable);
		
		this.keyColumnName = keyColumnName;
		this.passwordChecker = passwordChecker;
	}
	
	/**
	 * Creates a new entity. The entity uses LoginKeyTable, so you should take it into account.
	 * @param name The name of the entity
	 * @param parent The parent of the entity
	 * @param passwordChecker The password checker which is used for validating the 
	 * requests (null if no validation is required)
	 * @see LoginKeyTable
	 */
	public LoginManagerEntity(String name, RestEntity parent, PasswordChecker passwordChecker)
	{
		super(name, new SimpleRestData(), parent, LoginKeyTable.DEFAULT);
		
		this.keyColumnName = LoginKeyTable.getKeyColumnName();
		this.passwordChecker = passwordChecker;
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new LoginKey(getPath() + "/", getTable(), id, this.keyColumnName);
	}

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		// Doesn't support post
		throw new MethodNotSupportedException(MethodType.POST);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Doesn't support put
		throw new MethodNotSupportedException(MethodType.PUT);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		// Can't be deleted
		throw new MethodNotSupportedException(MethodType.DELETE);
	}
	
	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		// Checks that the provided password (or a key) is correct
		try
		{
			LoginKey.checkKey(getTable(), pathPart, this.keyColumnName, parameters);
		}
		catch (HttpException e)
		{
			if (this.passwordChecker != null)
				this.passwordChecker.checkPassword(pathPart, parameters);
		}
		
		// Tries to find an existing key
		try
		{
			return super.getMissingEntity(pathPart, parameters);
		}
		catch (NotFoundException e)
		{
			// If there wasn't a key already, creates a new key
			return new LoginKey(this, getTable(), pathPart, parameters, this.keyColumnName);
		}
	}
	
	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters)
	{
		// The login manager can't offer or show multiple login keys at once since there's 
		// No way to authorize that
		return new HashMap<>();
	}
}
