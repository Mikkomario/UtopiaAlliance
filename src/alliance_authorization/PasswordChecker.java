package alliance_authorization;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import nexus_http.AuthorizationException;
import nexus_http.HttpException;
import nexus_http.InternalServerException;
import nexus_http.InvalidParametersException;
import vault_database.DatabaseAccessor;
import vault_database.DatabaseTable;
import vault_database.DatabaseUnavailableException;

/**
 * This class is able to check if a password matches that of a user
 * 
 * @author Mikko Hilpinen
 * @since 27.1.2015
 */
public class PasswordChecker
{
	// ATTRIBUTES	-------------------------
	
	private String passwordColumnName, userIDColumnName;
	private DatabaseTable passwordTable;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new passwordChecker that uses the given database table as reference
	 * @param passwordTable The table that contains the hashed passwords
	 * @param hashColumnName The column that holds the password hash
	 * @param userIDColumnName The column that holds the user identifiers
	 */
	public PasswordChecker(DatabaseTable passwordTable, String hashColumnName, 
			String userIDColumnName)
	{
		this.passwordColumnName = hashColumnName;
		this.userIDColumnName = userIDColumnName;
		this.passwordTable = passwordTable;
	}
	
	
	// OTHER METHODS	--------------------
	
	/**
	 * Checks if the given password matches that of the given user
	 * @param userID The identifier of the user
	 * @param password The password (unhashed)
	 * @throws HttpException If the password or username were incorrect or the password 
	 * couldn't be checked at all
	 */
	public void checkPassword(String userID, String password) throws HttpException
	{
		try
		{
			// Finds the correct hash
			List<String> correctHashes = DatabaseAccessor.findMatchingData(this.passwordTable, 
					this.userIDColumnName, userID, this.passwordColumnName);
			
			boolean correctFound = false;
			// Compares the hashes
			for (String correctHash : correctHashes)
			{
				if (PasswordHash.validatePassword(password, correctHash))
					correctFound = true;
			}
			
			if (!correctFound)
				throw new AuthorizationException("Invalid username or password");
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			throw new InternalServerException("Couldn't find the password data", e);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new InternalServerException("Couldn't compare password data", e);
		}
	}
	
	/**
	 * Checks if the given password matches that of the given user
	 * @param userID The identifier of the user
	 * @param parameters The parameters provided by the client. The password should be in 
	 * 'password' parameter
	 * @throws HttpException If the password or username were incorrect or the password 
	 * couldn't be checked at all
	 */
	public void checkPassword(String userID, Map<String, String> parameters) throws HttpException
	{
		// Checks that the parameters exist
		if (!parameters.containsKey("password"))
			throw new InvalidParametersException("Parameter 'password' required");
		
		// Checks the password
		checkPassword(userID, parameters.get("password"));
	}
}
