package alliance_test;

import java.util.Map;

import nexus_http.HttpException;
import alliance_authorization.LoginKey;
import alliance_authorization.SecureEntity;
import alliance_rest.DatabaseEntity;

/**
 * Secure holds the entity's hashed password.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class TestSecureEntity extends SecureEntity
{
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new secure by reading its data from the database
	 * @param rootPath The path preceding the entity
	 * @param id The identifier of this entity
	 * @throws HttpException If the entity couldn't be read
	 */
	public TestSecureEntity(String rootPath, String id) throws HttpException
	{
		super(TestTable.SECURE, rootPath, "secure", id, "passwordHash", "password");
	}

	/**
	 * Creates a new secure
	 * @param parent The parent of this entity
	 * @param userID The identifier of the user this secure entity is for
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the entity couldn't be created
	 */
	public TestSecureEntity(DatabaseEntity parent, String userID, 
			Map<String, String> parameters) throws HttpException
	{
		super(TestTable.SECURE, parent, "secure", userID, "passwordHash", "password", 
				parameters);
	}

	
	// IMPLEMENTED METHODS	---------------------
	
	@Override
	protected void authorizeModification(Map<String, String> parameters)
			throws HttpException
	{
		LoginKey.checkKey(TestLoginKeyTable.DEFAULT, getDatabaseID(), parameters);
	}
}
