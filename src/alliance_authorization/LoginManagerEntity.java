package alliance_authorization;

import java.util.HashMap;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;
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
	
	private LoginKeyTable keyTable;
	private PasswordChecker passwordChecker;
	private boolean multiUserAccounts;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new entity
	 * @param name The name of the entity
	 * @param parent The parent of the entity
	 * @param keyTable The table which contains the key data
	 * @param passwordChecker The password checker which is used for validating the 
	 * requests (null if no validation is required)
	 * @param useMultiUserAccounts Should the service support multiple users using the same 
	 * account simultaneously? Allowing this can decrease the information security of the 
	 * program but will increase usability in multi user situations.
	 */
	public LoginManagerEntity(String name, RestEntity parent, LoginKeyTable keyTable, 
			PasswordChecker passwordChecker, boolean useMultiUserAccounts)
	{
		super(name, new SimpleRestData(), parent, keyTable);
		
		this.keyTable = keyTable;
		this.passwordChecker = passwordChecker;
		this.multiUserAccounts = useMultiUserAccounts;
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new LoginKey(getPath() + "/", this.keyTable, id);
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
	protected RestEntity getMissingEntity(String pathPart, Map<String, String> parameters) 
			throws HttpException
	{
		// Checks that the provided password (or a key) is correct
		try
		{
			LoginKeyTable.checkKey(this.keyTable, pathPart, parameters);
		}
		catch (HttpException e)
		{
			if (this.passwordChecker != null)
				this.passwordChecker.checkPassword(pathPart, parameters);
		}
		
		// For multi-user accounts, a new key is generated at each login
		if (this.multiUserAccounts)
			return new LoginKey(this, this.keyTable, pathPart, parameters);
		
		// Tries to find an existing key
		try
		{
			return super.getMissingEntity(pathPart, parameters);
		}
		catch (NotFoundException e)
		{
			// If there wasn't a key already, creates a new key
			return new LoginKey(this, this.keyTable, pathPart, parameters);
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
