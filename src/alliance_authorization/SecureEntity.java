package alliance_authorization;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonGenerator;

import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.ContentType;
import nexus_rest.RestEntity;
import nexus_rest.SimpleRestData;
import alliance_authorization.PasswordHash;
import alliance_rest.DatabaseEntity;
import alliance_rest.DatabaseEntityTable;

/**
 * This entity holds a single piece of information that will be hashed and won't be shown 
 * when requested. The attribute can still be modified, however.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public abstract class SecureEntity extends DatabaseEntity
{
	// ATTRIBUTES	---------------------------
	
	private String name, hashColumnName, secureParameterName;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new secure by reading its data from the database
	 * @param table The table that holds this entity
	 * @param rootPath The path preceding the entity
	 * @param name The name of this entity
	 * @param userID The identifier of the user of this entity
	 * @param hashColumnName The name of the column that contains the hashed information
	 * @param secureParameterName The name of the parameter that has been hashed
	 * @throws HttpException If the entity couldn't be read
	 */
	public SecureEntity(DatabaseEntityTable table, String rootPath, String name, 
			String userID, String hashColumnName, String secureParameterName) 
			throws HttpException
	{
		super(new SimpleRestData(), rootPath, table, userID);
		
		this.name = name;
		this.hashColumnName = hashColumnName;
		this.secureParameterName = secureParameterName;
	}

	/**
	 * Creates a new secure
	 * @param table The table that holds the entity
	 * @param parent The parent of this entity
	 * @param name The name of the entity
	 * @param userID The identifier of the user this secure entity is for
	 * @param hashColumnName The name of the column that contains the hashed information
	 * @param secureParameterName The name of the parameter that will be hashed
	 * @param parameters The parameters provided by the client. The only necessary parameter 
	 * is 'password', which will be hashed and stored as 'passwordHash'
	 * @throws HttpException If the entity couldn't be created
	 */
	public SecureEntity(DatabaseEntityTable table, DatabaseEntity parent, String name, 
			String userID, String hashColumnName, String secureParameterName, 
			Map<String, String> parameters) throws HttpException
	{
		super(new SimpleRestData(), parent, table, userID, 
				modifyParameters(parameters, hashColumnName, secureParameterName), 
				new HashMap<>());
		
		this.name = name;
		this.hashColumnName = hashColumnName;
		this.secureParameterName = secureParameterName;
	}
	
	
	// ABSTRACT METHODS	-------------------------
	
	/**
	 * This method should throw an exception if the entity shouldn't be modified
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the change cannot be authorized
	 */
	protected abstract void authorizeModification(Map<String, String> parameters) 
			throws HttpException;

	
	// IMPLEMENTED METHODS	---------------------
	
	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Requires authorization
		authorizeModification(parameters);
		
		// Also, has to hash the new password
		if (parameters.containsKey(this.secureParameterName))
		{
			try
			{
				setAttribute(this.hashColumnName, PasswordHash.createHash(
						parameters.get(this.secureParameterName)));
				writeData();
			}
			catch (NoSuchAlgorithmException | InvalidKeySpecException e)
			{
				throw new InternalServerException("Failed to hash the " + 
						this.secureParameterName, e);
			}
		}
	}

	@Override
	protected Map<String, RestEntity> getMissingEntities(
			Map<String, String> parameters) throws HttpException
	{
		// Has no entities under it
		return new HashMap<>();
	}

	@Override
	protected RestEntity getMissingEntity(String pathPart,
			Map<String, String> parameters) throws HttpException
	{
		throw new NotFoundException(getPath() + "/" + pathPart);
	}
	
	@Override
	public void writeContent(String serverLink, XMLStreamWriter xmlWriter, 
			JsonGenerator jsonWriter, ContentType contentType, 
			Map<String, String> parameters) throws MethodNotSupportedException
	{
		// Secure entities cannot be written
		throw new MethodNotSupportedException(MethodType.GET);
	}
	
	@Override
	public String getName()
	{
		if (this.name != null)
			return this.name;
		
		return super.getName();
	}
	
	@Override
	public String getPath()
	{
		return getRootPath() + getName();
	}

	
	// OTHER METHODS	-------------------------
	
	private static Map<String, String> modifyParameters(Map<String, String> parameters, 
			String hashColumnName, String secureParameterName) throws HttpException
	{
		// The correct parameter must exist
		if (!parameters.containsKey(secureParameterName))
			throw new InvalidParametersException("Parameter '" + secureParameterName + 
					"' must be provided");
		// Hashes the information
		try
		{
			parameters.put(hashColumnName, 
					PasswordHash.createHash(parameters.get(secureParameterName)));
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new InternalServerException("Couldn't hash the " + secureParameterName, e);
		}
		
		return parameters;
	}
}
