package alliance_rest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import vault_database.DatabaseAccessor;
import vault_database.DatabaseSettings;
import vault_database.DatabaseUnavailableException;
import vault_database.InvalidTableTypeException;
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
public abstract class DatabaseEntity extends TemporaryRestEntity
{
	// ATTRIBUTES	-------------------------
	
	private DatabaseEntityTable table;
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
			DatabaseEntityTable table, String id) throws HttpException
	{
		super(id, content, rootPath);
		
		// Initializes attributes
		this.table = table;
		this.id = id;
		
		// Loads some data from the database
		Map<String, String> data = readData();
		initialize(data, new HashMap<String, String>());
	}
	
	/**
	 * Creates a new entity based on the given data. This data will be registered into 
	 * the database.
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
			DatabaseEntityTable table, Map<String, String> parameters, 
			Map<String, String> defaultParameters) throws HttpException
	{
		super("unknown", content, parent);
		
		// Initializes attributes
		this.table = table;
		
		initialize(parameters, defaultParameters);
		this.id = getAttributes().get(getTable().getIDColumnName());
		
		// Saves the entity into database
		writeData();
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
			DatabaseEntityTable table, String id, 
			Map<String, String> parameters, Map<String, String> defaultParameters) 
			throws HttpException
	{
		super("unknown", content, parent);
		
		// Initializes attributes
		this.table = table;
		
		initialize(parameters, defaultParameters);
		this.id = id;
		
		// Saves the entity into database
		writeData();
	}
	
	
	// IMPLEMENTED METHODS	-----------------

	@Override
	protected void prepareDelete(Map<String, String> parameters)
			throws HttpException
	{
		try
		{
			DatabaseAccessor.delete(getTable(), getTable().getIDColumnName(), getDatabaseID());
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
			writer.writeAttribute("id", this.id);
	}
	
	
	// GETTERS & SETTERS	------------------
	
	/**
	 * @return The table that holds the data for this entity
	 */
	public DatabaseEntityTable getTable()
	{
		return this.table;
	}
	
	/**
	 * @return The id of this entity in the database
	 */
	public String getDatabaseID()
	{
		return this.id;
	}
	
	
	// OTHER METHODS	----------------------
	
	/**
	 * Saves the entity's data into the database
	 * @throws HttpException If the operation fails
	 */
	protected void writeData() throws HttpException
	{
		// Checks if the data should be updated instead of written as new
		if (isInDatabase())
			updateData();
		else
		{
			try
			{
				// Inserts the data into the database
				if (getTable().usesAutoIncrementIndexing())
					this.id = "" + DatabaseAccessor.insert(getTable(), getColumnData(), 
							getTable().getIDColumnName());
				else
				{
					if (!getTable().usesIndexing())
						DatabaseAccessor.insert(getTable(), getColumnData());
					else if (getTable().usesAutoIncrementIndexing())
						DatabaseAccessor.insert(getTable(), getColumnData(), 
								getTable().getIDColumnName());
					else
					{
						DatabaseAccessor.insert(getTable(), getColumnData(), 
								Integer.parseInt(getDatabaseID()));
					}
				}
			}
			catch (DatabaseUnavailableException | SQLException | InvalidTableTypeException e)
			{
				// TODO: How about localization?
				throw new InternalServerException("Can't write " + getPath() + 
						" into the database", e);
			}
			catch (NumberFormatException e)
			{
				throw new InvalidParametersException("ID " + getDatabaseID() + 
						" can't be parsed into an integer");
			}
		}
	}
	
	private Map<String, String> readData() throws HttpException
	{
		Map<String, String> data;
		DatabaseAccessor accessor = new DatabaseAccessor(getTable().getDatabaseName());
		PreparedStatement statement = null;
		ResultSet results = null;
		try
		{
			// Parses the id, if necessary
			int intID = 0;
			if (getTable().usesIndexing())
				intID = Integer.parseInt(getDatabaseID());
			
			statement = accessor.getPreparedStatement("SELECT * FROM " + 
					DatabaseSettings.getTableHandler().getTableNameForIndex(getTable(), 
					intID, false) + " WHERE " + getTable().getIDColumnName() + " = '" + 
					getDatabaseID() + "'");
			results = statement.executeQuery();
			
			// Data was found
			if (results.next())
			{
				data = new HashMap<>();
				// Goes through the columns and collects the data
				for (String field : getTable().getColumnNames())
				{
					data.put(field, results.getString(field));
				}
			}
			// Data wasn't found
			else
				throw new NotFoundException(getPath());
		}
		catch (NumberFormatException e)
		{
			throw new InvalidParametersException("ID " + getDatabaseID() + 
					" can't be parsed into an integer");
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Failed to load " + getPath(), e);
		}
		finally
		{
			DatabaseAccessor.closeResults(results);
			DatabaseAccessor.closeStatement(statement);
			accessor.closeConnection();
		}
		
		return data;
	}
	
	private void updateData() throws InternalServerException
	{
		try
		{
			String[] columnNames = 
					getTable().getColumnNames().toArray(new String[0]);
			String[] columnData = getColumnData().toArray(new String[0]);
			
			DatabaseAccessor.update(getTable(), getTable().getIDColumnName(), getDatabaseID(), 
					columnNames, columnData);
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Couldn't overwrite " + getPath() + 
					" into the database", e);
		}
	}
	
	private boolean isInDatabase() throws InternalServerException
	{
		if (getDatabaseID() == null)
			return false;
		try
		{
			return !DatabaseEntityTable.findMatchingIDs(getTable(), 
					getTable().getIDColumnName(), getDatabaseID(), 1).isEmpty();
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Can't check if " + getPath() + 
					" exists in the database", e);
		}
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
			else if (!field.equals(getTable().getIDColumnName()))
				throw new InvalidParametersException("Parameter " + field + " not provided");
		}
	}
	
	private List<String> getColumnData()
	{
		List<String> columnData = new ArrayList<>();
		Map<String, String> attributes = getAttributes();
		for (String columnName : getTable().getColumnNames())
		{
			if (columnName.equals(getTable().getIDColumnName()))
				columnData.add(getDatabaseID());
			else
				columnData.add(attributes.get(columnName));
		}
		
		return columnData;
	}
}