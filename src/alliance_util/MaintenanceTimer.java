package alliance_util;

import java.util.Random;
import java.util.Timer;

/**
 * MaintenanceTimer is a timer specifically made for handling maintenance tasks
 * @author Mikko Hilpinen
 * @since 9.4.2015
 */
public class MaintenanceTimer extends Timer
{
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new timer. The tasks must be added separately
	 */
	public MaintenanceTimer()
	{
		super(true);
	}

	
	// OTHER METHODS	------------------------
	
	/**
	 * Adds a new task to the list of performed tasks
	 * @param task The task that will be performed
	 */
	public void addTask(MaintenanceTask task)
	{
		int delayMinutes = new Random().nextInt(task.getMaintenanceIntervalMinutes());
		addTask(task, delayMinutes);
	}
	
	/**
	 * Adds a new task to the list of performed tasks
	 * @param task The task that will be performed
	 * @param delayMinutes How many minutes there will be until the task is performed the 
	 * first time
	 */
	public void addTask(MaintenanceTask task, int delayMinutes)
	{
		long interval = SimpleDate.minutesToMillis(task.getMaintenanceIntervalMinutes());
		long delay = SimpleDate.minutesToMillis(delayMinutes);
		schedule(task, delay, interval);
	}
}
