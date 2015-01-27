package alliance_authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexus_http.AuthorizationException;
import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
import vault_database.DatabaseTable;
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
	 * @param userIDColumName The name of the userID column in the key table
	 * @param keyColumnName The name of the key column in the key table
	 * @param passwordChecker The password checker which is used for validating the 
	 * requests (null if no validation is required)
	 */
	public LoginManagerEntity(String name, RestEntity parent, DatabaseTable keyTable, 
			String userIDColumName, String keyColumnName, PasswordChecker passwordChecker)
	{
		super(name, new SimpleRestData(), parent, keyTable, userIDColumName);
		
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
		super(name, new SimpleRestData(), parent, LoginKeyTable.DEFAULT, 
				LoginKeyTable.getUserIDColumnName());
		
		this.keyColumnName = LoginKeyTable.getKeyColumnName();
		this.passwordChecker = passwordChecker;
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new LoginKey(getPath() + "/", getTable(), getIDColumnName(), id, 
				this.keyColumnName);
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
	protected RestEntityList wrapIntoList(String name, RestEntity parent,
			List<RestEntity> entities)
	{
		return new SimpleRestEntityList(name, parent, entities);
	}
	
	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		// Checks that the provided password (or a key) is correct
		try
		{
			if (this.passwordChecker != null)
				this.passwordChecker.checkPassword(pathPart, parameters);
		}
		catch (AuthorizationException e)
		{
			LoginKey.checkKey(getTable(), getIDColumnName(), this.keyColumnName, parameters);
		}
		
		return super.getMissingEntity(pathPart, parameters);
	}
	
	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters)
	{
		// The login manager can't offer or show multiple login keys at once since there's 
		// No way to authorize that
		return new HashMap<>();
	}
}
