package alliance_test;

import java.util.List;
import java.util.Map;

import nexus_http.HttpException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
import alliance_rest.DatabaseTableEntity;

/**
 * This test entity handles the contents of the testTable
 * 
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public class TestTableEntity extends DatabaseTableEntity
{
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new entity to the given location
	 * @param name The name of the entity
	 * @param parent The parent of the entity
	 */
	public TestTableEntity(String name, RestEntity parent)
	{
		super(name, new SimpleRestData(), parent, TestTable.ENTITY, "id");
	}
	
	
	// IMPLEMENTED METHODS	-----------------------

	@Override
	protected RestEntity loadEntityWithID(String id) throws HttpException
	{
		return new TestDatabaseEntity(getPath() + "/", id);
	}

	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		return new TestDatabaseEntity(this, parameters);
	}

	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Doesn't support PUT
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
}
