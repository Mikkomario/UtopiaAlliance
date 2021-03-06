package alliance_test;

import java.sql.SQLException;

import alliance_authorization.LoginManagerEntity;
import alliance_authorization.PasswordChecker;
import nexus_rest.ContentType;
import nexus_rest.RestEntity;
import nexus_rest.StaticRestServer;
import nexus_test.HttpServerAnalyzer;
import nexus_test.TestRestEntity;
import vault_database.DatabaseSettings;
import vault_database.DatabaseUnavailableException;

/**
 * This class hosts a test server for the alliance tests
 * @author Mikko Hilpinen
 * @since 26.1.2015
 */
public class AllianceTestServer
{
	// CONSTRUCTOR	-----------------------------
	
	private AllianceTestServer()
	{
		// Static interface
	}

	
	// MAIN METHOD	-----------------------------
	
	/**
	 * Starts the test server
	 * @param args The first parameter is the server ip. The second parameter is the port 
	 * number. The third parameter is the database password. The fourth one is database user 
	 * (default = root). The fifth is database address (default = jdbc:mysql://localhost:3306/)
	 */
	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.out.println("Please provide the correct parameters (ip, port, "
					+ "password, user (optional), database address (optional)");
			System.exit(0);
		}
		
		String connectionTarget = "jdbc:mysql://localhost:3306/";
		String user = "root";
		int port = Integer.parseInt(args[1]);
		
		if (args.length >= 5)
			connectionTarget = args[4];
		if (args.length >= 4)
			user = args[3];
		
		// Initializes database settings
		try
		{
			DatabaseSettings.initialize(connectionTarget, user, args[2], 100, "alliance_db", 
					"tableamounts");
		}
		catch (DatabaseUnavailableException | SQLException e)
		{
			System.err.println("Couldn't initialize the database settings");
			e.printStackTrace();
			System.exit(1);
		}
		
		// Creates the server entities
		RestEntity root = new TestRestEntity("root", null);
		new TestTableEntity("entities", root);
		new LoginManagerEntity("login", root, TestLoginKeyTable.DEFAULT, 
				new PasswordChecker(TestTable.SECURE, "passwordHash", "id"), false);
		
		// Starts the server
		StaticRestServer.startServer(args[0], port, true, ContentType.XML, root, 
				new HttpServerAnalyzer());
	}
}
