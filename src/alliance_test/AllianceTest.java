package alliance_test;

import nexus_rest.ContentType;
import nexus_test.FileReaderClientTest;

/**
 * This is a test for the alliance test server
 * @author Mikko Hilpinen
 * @since 1.6.2015
 */
public class AllianceTest
{
	private AllianceTest()
	{
		// The interface is static
	}
	
	/**
	 * Runs the test
	 * @param args ip and port
	 */
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Requires parameters ip and port");
			System.exit(0);
		}
		
		FileReaderClientTest.run("testInstructions.txt", args[0], Integer.parseInt(args[1]), 
				ContentType.XML, true);
	}
}
