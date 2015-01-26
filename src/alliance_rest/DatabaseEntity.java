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
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
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
	
	private DatabaseTable table;
	private String id, idColumnName;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new entity by reading its data from the database.
	 * @param content The content of this entity
	 * @param parent The parent of this entity
	 * @param table The table that contains the entity's data
	 * @param idColumnName The name of the column that contains the entity's identifier
	 * @param id The entity's identifier with which it can be found from the database
	 * @throws HttpException If the entity couldn't be read from the database
	 */
	public DatabaseEntity(RestData content, RestEntity parent, 
			DatabaseTable table, String idColumnName, String id) throws HttpException
	{
		super(id, content, parent);
		
		// Initializes attributes
		this.table = table;
		this.id = id;
		this.idColumnName = idColumnName;
		
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
	 * @param id The entity's unique identifier. If the table uses auto-increment indexing, 
	 * this should be left as null.
	 * @param idColumnName The name of the column that will hold the entity's identifier
	 * @param parameters The parameters used for creating this entity. These parameters should 
	 * be checked beforehand in case they can't be parsed or are otherwise invalid.
	 * @param defaultParameters The parameters that are used if some are not provided in the 
	 * other parameters. These should be checked beforehand as well.
	 * @throws HttpException If the entity couldn't be initialized or written
	 */
	public DatabaseEntity(RestData content, RestEntity parent, 
			DatabaseTable table, String id, String idColumnName, 
			Map<String, String> parameters, Map<String, String> defaultParameters) 
			throws HttpException
	{
		super(id, content, parent);
		
		// Initializes attributes
		this.table = table;
		this.id = id;
		this.idColumnName = idColumnName;
		
		initialize(parameters, defaultParameters);
		
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
			DatabaseAccessor.delete(getTable(), this.idColumnName, "'" + this.id + "'");
		}
		catch (SQLException | DatabaseUnavailableException e)
		{
			throw new InternalServerException("Couldn't delete " + getPath(), e);
		}
	}
	
	@Override
	public String getName()
	{	
		if (Character.isDigit(this.id.charAt(0)))
			return this.idColumnName + this.id;
		return this.id;
	}
	
	@Override
	public void writeLinkAsAttribute(String serverLink, XMLStreamWriter writer) throws 
			XMLStreamException
	{
		super.writeLinkAsAttribute(serverLink, writer);
		
		// Also writes the id as an id attribute
		if (this.id != null)
			writer.writeAttribute("id", this.id);
	}
	
	
	// GETTERS & SETTERS	------------------
	
	/**
	 * @return The table that holds the data for this entity
	 */
	public DatabaseTable getTable()
	{
		return this.table;
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
		
		try
		{
			// Inserts the data into the database
			if (getTable().usesAutoIncrementIndexing())
				this.id = "" + DatabaseAccessor.insert(getTable(), getColumnData(), 
						this.idColumnName);
			else
			{
				DatabaseAccessor.insert(getTable(), getColumnData());
				if (getTable().usesIndexing())
					DatabaseSettings.getTableHandler().informAboutNewRow(getTable(), 
							Integer.parseInt(this.id));
			}
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Can't write " + getPath() + 
					" into the database", e);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidParametersException("ID " + this.id + 
					" can't be parsed into an integer");
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
				intID = Integer.parseInt(this.id);
			
			statement = accessor.getPreparedStatement("SELECT * FROM " + 
					DatabaseSettings.getTableHandler().getTableNameForIndex(getTable(), 
					intID, false) + " WHERE " + this.idColumnName + " = '" + this.id + "'");
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
			throw new InvalidParametersException("ID " + this.id + 
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
			DatabaseAccessor.update(getTable(), this.idColumnName, "'" + this.id + "'", 
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
		if (this.id == null)
			return false;
		try
		{
			return !DatabaseAccessor.findMatchingData(getTable(), this.idColumnName, this.id, 
					this.idColumnName).isEmpty();
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
			else
				throw new InvalidParametersException("Parameter " + field + " not provided");
		}
	}
	
	private List<String> getColumnData()
	{
		List<String> columnData = new ArrayList<>();
		Map<String, String> attributes = getAttributes();
		for (String columnName : getTable().getColumnNames())
		{
			columnData.add(attributes.get(columnName));
		}
		
		return columnData;
	}
}