package alliance_authorization;

import java.sql.SQLException;
import java.util.List;

import nexus_http.HttpException;
import vault_database.DatabaseAccessor;
import vault_database.DatabaseUnavailableException;
import alliance_util.MaintenanceTask;
import alliance_util.SimpleDate;

/**
 * This task removes the login keys that are too old
 * @author Mikko Hilpinen
 * @since 1.5.2015
 */
public class LoginKeyRemovalTask extends MaintenanceTask
{
	// ATTRIBUTES	--------------------------
	
	private int loginKeyDurationMinutes;
	private LoginKeyTable keyTable;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new task.
	 * @param loginKeyDurationHours How many hours a single login key is valid for use
	 * @param loginKeyTable The table that contains the login key data
	 */
	public LoginKeyRemovalTask(LoginKeyTable loginKeyTable, int loginKeyDurationHours)
	{
		this.loginKeyDurationMinutes = loginKeyDurationHours * 60;
		this.keyTable = loginKeyTable;
	}
	
	
	// IMPLEMENTED METHODS	------------------

	@Override
	public int getMaintenanceIntervalMinutes()
	{
		return 60;
	}

	@Override
	public void run()
	{
		try
		{
			// Finds all the login keys
			List<String> loginKeyIDs = DatabaseAccessor.findMatchingData(this.keyTable, 
					new String[0], new String[0], this.keyTable.getPrimaryColumnName());
			
			// Removes any old ones
			for (String id : loginKeyIDs)
			{
				LoginKey key = new LoginKey("/", this.keyTable, id);
				if (new SimpleDate().isPast(key.getCreationTime().plus(
						this.loginKeyDurationMinutes)))
					key.deleteWithoutAuthorization();
			}
		}
		catch (DatabaseUnavailableException | SQLException | HttpException e)
		{
			System.err.println("Failed to remove the old login keys");
			e.printStackTrace();
		}
	}
}
