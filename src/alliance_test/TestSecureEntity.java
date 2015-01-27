package alliance_test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import nexus_http.MethodNotSupportedException;
import nexus_http.MethodType;
import nexus_http.NotFoundException;
import nexus_rest.RestEntity;
import nexus_rest.RestEntityList;
import nexus_rest.SimpleRestData;
import nexus_rest.SimpleRestEntityList;
import alliance_authorization.LoginKey;
import alliance_authorization.PasswordHash;
import alliance_rest.DatabaseEntity;

/**
 * Secure holds the entity's hashed password.
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class TestSecureEntity extends DatabaseEntity
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
		super(new SimpleRestData(), rootPath, TestTable.SECURE, "id", id);
	}

	/**
	 * Creates a new secure
	 * @param parent The parent of this entity
	 * @param parameters The parameters provided by the client
	 * @throws HttpException If the entity couldn't be created
	 */
	public TestSecureEntity(RestEntity parent, Map<String, String> parameters) throws 
			HttpException
	{
		super(new SimpleRestData(), parent, TestTable.SECURE, "id", 
				modifyParameters(parameters), new HashMap<>());
	}

	
	// IMPLEMENTED METHODS	---------------------
	
	@Override
	public void Put(Map<String, String> parameters) throws HttpException
	{
		// Requires authorization
		LoginKey.checkKey(parameters);
		
		// Also, has to hash the new password
		if (parameters.containsKey("password"))
		{
			try
			{
				setAttribute("passwordHash", PasswordHash.createHash(
						parameters.get("password")));
				writeData();
			}
			catch (NoSuchAlgorithmException | InvalidKeySpecException e)
			{
				throw new InternalServerException("Failed to hash the password", e);
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
	protected RestEntityList wrapIntoList(String name, RestEntity parent,
			List<RestEntity> entities)
	{
		return new SimpleRestEntityList(name, parent, entities);
	}
	
	@Override
	public void writeContent(String serverLink, XMLStreamWriter writer) throws 
			MethodNotSupportedException
	{
		// Secure entities cannot be written
		throw new MethodNotSupportedException(MethodType.GET);
	}

	
	// OTHER METHODS	-------------------------
	
	private static Map<String, String> modifyParameters(Map<String, String> parameters) 
			throws HttpException
	{
		// Parameter 'password' must exist
		if (!parameters.containsKey("password"))
			throw new InvalidParametersException("Parameter 'password' must be provided");
		// Hashes the password
		try
		{
			parameters.put("passwordHash", PasswordHash.createHash(parameters.get("password")));
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new InternalServerException("Couldn't hash the password", e);
		}
		
		return parameters;
	}
}
