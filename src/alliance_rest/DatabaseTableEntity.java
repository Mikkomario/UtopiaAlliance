package alliance_rest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vault_database.DatabaseUnavailableException;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_rest.RestData;
import nexus_rest.RestEntity;

/**
 * These entities fetch entities from databases when necessary.
 * 
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public abstract class DatabaseTableEntity extends RestEntity
{
	// ATTRIBUTES	-------------------------------
	
	private DatabaseEntityTable table;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new entity
	 * @param name The name of the entity
	 * @param content The contents of the entity
	 * @param parent The parent of this entity
	 * @param table The table that holds the entities under this one
	 */
	public DatabaseTableEntity(String name, RestData content, RestEntity parent, 
			DatabaseEntityTable table)
	{
		super(name, content, parent);
		
		// Initializes attributes
		this.table = table;
	}
	
	
	// ABSTRACT METHODS	----------------------------
	
	/**
	 * This method should read and return an entity from the database
	 * @param id The identifier of the entity
	 * @return An entity from the database
	 * @throws HttpException if the entity couldn't be found or read
	 */
	protected abstract RestEntity loadEntityWithID(String id) throws HttpException;
	
	
	// IMPLEMENTED METHODS	------------------------

	@Override
	protected Map<String, RestEntity> getMissingEntities(Map<String, String> parameters) throws 
			HttpException
	{
		// The parameters may cast restrictions on which entities are fetched
		List<String> restrictionColumns = new ArrayList<>();
		List<String> restrictionValues = new ArrayList<>();
		List<String> columnNames = getTable().getColumnNames();
		
		for (String parameterName : parameters.keySet())
		{
			if (columnNames.contains(parameterName))
			{
				restrictionColumns.add(parameterName);
				restrictionValues.add(parameters.get(parameterName));
			}
		}
		
		// Finds all the (matching) entities from the database
		List<String> entityIDs = null;
		try
		{
			entityIDs = DatabaseEntityTable.findMatchingIDs(getTable(), 
					restrictionColumns.toArray(new String[0]), 
					restrictionValues.toArray(new String[0]));
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Failed to read entity data", e);
		}
		
		Map<String, RestEntity> entities = new HashMap<>();
		for (String id : entityIDs)
		{
			RestEntity entity = loadEntityWithID(id);
			entities.put(entity.getName(), entity);
		}
		
		return entities;
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		// Finds the entity from the database
		return loadEntityWithID(pathPart);
	}
	
	
	// GETTERS & SETTERS	------------------------
	
	/**
	 * @return The table this entity uses
	 */
	protected DatabaseEntityTable getTable()
	{
		return this.table;
	}
}
