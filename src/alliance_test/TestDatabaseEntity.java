package alliance_test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
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
	 * @param parent The parent of the entity
	 * @param id The identifier used for finding the entity
	 * @throws HttpException If the entity couldn't be read
	 */
	public TestDatabaseEntity(RestEntity parent, String id) throws HttpException
	{
		super(new SimpleRestData(), parent, TestTable.DEFAULT, "id", id);
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
		super(new SimpleRestData(), parent, TestTable.DEFAULT, null, "id", 
				checkParameters(parameters), getDefaultParameters());
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Checks the parameters but allows update
		defaultPut(checkParameters(parameters));
	}

	@Override
	protected RestEntityList wrapIntoList(String name, RestEntity parent,
			List<RestEntity> entities)
	{
		return new SimpleRestEntityList(name, parent, entities);
	}
	
	@Override
	protected List<RestEntity> getMissingEntities(Map<String, String> parameters)
			throws HttpException
	{
		// Has a link to the "friend"
		List<RestEntity> links = new ArrayList<>();
		
		RestEntity friend = getFriend();
		if (friend != null)
			links.add(friend);
		
		// TODO: These are not shown as links but like children
		
		return links;
	}

	@Override
	protected RestEntity getMissingEntity(String path, Map<String, String> parameters)
			throws HttpException
	{
		// Has a link to the "friend"
		if (path.equals(getAttributes().get("friendID")))
			return getFriend();
		
		throw new NotFoundException(getPath() + "/" + path);
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
	
	private RestEntity getFriend() throws InternalServerException
	{
		if (!getAttributes().get("friendID").equals("-1"))
		{
			try
			{
				return new TestDatabaseEntity((RestEntity) getParent(), 
						getAttributes().get("friendID"));
			}
			catch (HttpException e)
			{
				throw new InternalServerException("Couldn't find the linked entity", e);
			}
		}
		
		return null;
	}
}
