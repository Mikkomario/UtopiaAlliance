package alliance_rest;

import java.sql.SQLException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import vault_database.DatabaseAccessor;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;
import vault_database.InvalidTableTypeException;
import vault_recording.DatabaseReadable;
import vault_recording.DatabaseWritable;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestData;
import nexus_rest.RestEntity;
import nexus_rest.TemporaryRestEntity;

/**
 * These restEntities hold their data in a database. They are considered temporary since the 
 * data is not saved to the server itself.
 * 
 * @author Mikko Hilpinen
 * @since 25.1.2015
 */
public abstract class DatabaseEntity extends TemporaryRestEntity implements DatabaseReadable, 
		DatabaseWritable
{
	// ATTRIBUTES	-------------------------
	
	private DatabaseTable table;
	private String id;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new entity by reading its data from the database.
	 * @param content The content of this entity
	 * @param rootPath The path preceding the entity, including the last '/'
	 * @param table The table that contains the entity's data
	 * @param id The entity's identifier with which it can be found from the database
	 * @throws HttpException If the entity couldn't be read from the database
	 */
	public DatabaseEntity(RestData content, String rootPath, 
			DatabaseTable table, String id) throws HttpException
	{
		super(id, content, rootPath);
		
		// Initializes attributes
		this.table = table;
		setDatabaseID(id);
		
		// Loads some data from the database
		try
		{
			if (!DatabaseAccessor.readObjectData(this, getDatabaseID()))
				throw new NotFoundException(rootPath + "/" + id);
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Couldn't read " + rootPath + "/" + id + 
					" from the database", e);
		}
	}
	
	/**
	 * Creates a new entity based on the given data. This data will be registered into 
	 * the database. This constructor should be used by entities that use auto-increment 
	 * indexed tables
	 * @param content The content of this entity
	 * @param parent The parent of this entity
	 * @param table The table that contains the entity's data
	 * @param parameters The parameters used for creating this entity. These parameters should 
	 * be checked beforehand in case they can't be parsed or are otherwise invalid.
	 * @param defaultParameters The parameters that are used if some are not provided in the 
	 * other parameters. These should be checked beforehand as well.
	 * @throws HttpException If the entity couldn't be initialized or written
	 */
	public DatabaseEntity(RestData content, RestEntity parent, 
			DatabaseTable table, Map<String, String> parameters, 
			Map<String, String> defaultParameters) throws HttpException
	{
		super("unknown", content, parent);
		
		// Initializes attributes
		this.table = table;
		
		initialize(parameters, defaultParameters);
		setDatabaseID(getAttributes().get(getTable().getPrimaryColumnName()));
		
		// Saves the entity into database
		try
		{
			DatabaseAccessor.insert(this);
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't write " + getPath() + 
					" into the database", e);
		}
	}
	
	/**
	 * Creates a new entity based on the given data. This data will be registered into 
	 * the database. This constructor should be used for entities that use indexed tables that 
	 * don't use auto-increment indexing.
	 * @param content The content of this entity
	 * @param parent The parent of this entity
	 * @param table The table that contains the entity's data
	 * @param id The identifier given to the entity
	 * @param parameters The parameters used for creating this entity. These parameters should 
	 * be checked beforehand in case they can't be parsed or are otherwise invalid.
	 * @param defaultParameters The parameters that are used if some are not provided in the 
	 * other parameters. These should be checked beforehand as well.
	 * @throws HttpException If the entity couldn't be initialized or written
	 */
	public DatabaseEntity(RestData content, RestEntity parent, 
			DatabaseTable table, String id, 
			Map<String, String> parameters, Map<String, String> defaultParameters) 
			throws HttpException
	{
		super("unknown", content, parent);
		
		// Initializes attributes
		this.table = table;
		
		initialize(parameters, defaultParameters);
		setDatabaseID(id);
		
		// Saves the entity into database
		try
		{
			DatabaseAccessor.insert(this);
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't write " + getPath() + 
					" into the database", e);
		}
	}
	
	
	// IMPLEMENTED METHODS	-----------------

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		try
		{
			DatabaseAccessor.delete(getTable(), getTable().getPrimaryColumnName(), getDatabaseID());
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't delete " + getPath(), e);
		}
	}
	
	@Override
	public String getName()
	{
		return getDatabaseID();
	}
	
	@Override
	public RestEntity Post(Map<String, String> parameters) throws HttpException
	{
		// By default, databaseEntities don't have any entities below them
		throw new MethodNotSupportedException(MethodType.POST);
	}
	
	@Override
	public void writeLinkAsAttribute(String serverLink, XMLStreamWriter writer, 
			Map<String, String> parameters) throws XMLStreamException
	{
		super.writeLinkAsAttribute(serverLink, writer, parameters);
		
		// Also writes the id as an id attribute
		if (this.id != null)
			writer.writeAttribute("id", getDatabaseID());
	}
	
	@Override
	public String getColumnValue(String columnName)
	{
		return getAttributes().get(columnName);
	}

	@Override
	public void newIndexGenerated(int newIndex)
	{
		setDatabaseID(newIndex + "");
	}

	@Override
	public void setValue(String columnName, String readValue)
	{
		setAttribute(columnName, readValue);
	}
	
	@Override
	public DatabaseTable getTable()
	{
		return this.table;
	}
	
	
	// GETTERS & SETTERS	------------------
	
	/**
	 * @return The id of this entity in the database
	 */
	public String getDatabaseID()
	{
		return this.id;
	}
	
	
	// OTHER METHODS	----------------------
	
	/**
	 * Updates the object's data in the database. No new data will be inserted but previous 
	 * data may be modified.
	 * @throws HttpException If the operation failed
	 */
	protected void updateToDatabase() throws HttpException
	{
		try
		{
			DatabaseAccessor.update(this);
		}
		catch (InvalidTableTypeException | SQLException
				| DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't update " + getPath() + 
					" to the database", e);
		}
	}
	
	private void setDatabaseID(String newID)
	{
		this.id = newID;
		setAttribute(getTable().getPrimaryColumnName(), newID);
	}
	
	private void initialize(Map<String, String> parameters, 
			Map<String, String> defaultParameters) throws HttpException
	{
		// Goes through all the required parameters (columns), if a data can't be found 
		// from the given parameters, defaults are used. If it can't be found on either, 
		// initialization fails
		for (String field : getTable().getColumnNames())
		{
			if (parameters.containsKey(field))
				setAttribute(field, parameters.get(field));
			else if (defaultParameters.containsKey(field))
				setAttribute(field, defaultParameters.get(field));
			
			// The id parameter needn't be in the parameters
			// TODO: Also add support for optional parameters
			else if (!field.equals(getTable().getPrimaryColumnName()))
				throw new InvalidParametersException("Parameter " + field + " not provided");
		}
	}
}