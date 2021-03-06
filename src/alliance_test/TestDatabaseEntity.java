package alliance_test;

import java.util.HashMap;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.InvalidParametersException;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;
import alliance_authorization.LoginKeyTable;
import alliance_rest.DatabaseEntity;

/**
 * These entities are used for testing the basic functions in testDatabaseEntity class
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public class TestDatabaseEntity extends DatabaseEntity
{
	// CONSTRUCTOR	------------------------------
	
	/**
	 * Creates a new entity by loading its data from the database
	 * @param rootPath The path preceding the entity, including the final '/'
	 * @param id The identifier used for finding the entity
	 * @throws HttpException If the entity couldn't be read
	 */
	public TestDatabaseEntity(String rootPath, String id) throws HttpException
	{
		super(new SimpleRestData(), rootPath, TestTable.ENTITY, id);
	}

	/**
	 * Creates a new entity and saves it to the database
	 * @param parent The parent of this entity
	 * @param parameters The parameters used for initializing this entity
	 * @throws HttpException If the entity couldn't be created based on the parameters
	 */
	public TestDatabaseEntity(RestEntity parent, Map<String, String> parameters) 
			throws HttpException
	{
		super(new SimpleRestData(), parent, TestTable.ENTITY, 
				checkParameters(parameters), getDefaultParameters());
		
		// Also creates the secure
		new TestSecureEntity(this, getDatabaseID(), parameters);
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		LoginKeyTable.checkKey(TestLoginKeyTable.DEFAULT, getDatabaseID(), parameters);
		
		// Checks the parameters but allows update
		defaultPut(checkParameters(parameters));
		
		// Also saves the changes
		updateToDatabase();
	}
	
	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters)
			throws HttpException
	{
		// Has a link to the "friend"
		Map<String, RestEntity> links = new HashMap<>();
		
		RestEntity friend = getFriend();
		if (friend != null)
			links.put("friend", friend);
		
		links.put("secure", getSecure());
		
		return links;
	}

	@Override
	protected RestEntity getMissingEntity(String path, Map<String, String> parameters)
			throws HttpException
	{
		// Has a link to the "friend"
		if (path.equals(getAttributes().get("friendID")))
			return getFriend();
		
		if (path.equals("secure"))
			return getSecure();
		
		throw new NotFoundException(getPath() + "/" + path);
	}

	@Override
	protected void prepareDelete(Map<String, String> parameters) throws HttpException
	{
		// Also deletes the secure
		getSecure().delete(parameters);
		super.prepareDelete(parameters);
	}
	
	// OTHER METHODS	--------------------------------
	
	private static Map<String, String> getDefaultParameters()
	{
		Map<String, String> defaults = new HashMap<>();
		defaults.put("friendID", "-1");
		return defaults;
	}
	
	private static Map<String, String> checkParameters(Map<String, String> parameters) 
			throws HttpException
	{
		// Password must be provided
		if (!parameters.containsKey("password"))
			throw new InvalidParametersException("Parameter 'password' must be provided");
		
		// FriendID must point to an existing testEntity or be -1
		if (parameters.containsKey("friendID") && !parameters.get("friendID").equals("-1"))
		{
			try
			{
				new TestDatabaseEntity(null, parameters.get("friendID"));
			}
			catch (NotFoundException e)
			{
				throw new InvalidParametersException(
						"The provided friendID doesn't point to any entity");
			}
		}
		
		return parameters;
	}
	
	private TestDatabaseEntity getFriend() throws HttpException
	{
		if (!getAttributes().get("friendID").equals("-1"))
		{
			try
			{
				return new TestDatabaseEntity(getRootPath(), getAttributes().get("friendID"));
			}
			catch (NotFoundException e)
			{
				// If the entity has been deleted, forgets this previous friend
				// TODO: The change takes places after the data has been written, which is 
				// a bit problematic
				setAttribute("friendID", "-1");
				updateToDatabase();
			}
		}
		
		return null;
	}
	
	private TestSecureEntity getSecure() throws HttpException
	{
		return new TestSecureEntity(getPath() + "/", getDatabaseID());
	}
}
