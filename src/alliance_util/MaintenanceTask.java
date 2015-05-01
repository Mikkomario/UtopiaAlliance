package alliance_util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;

/**
 * Management tasks are repeated constantly as long as the server is active
 * @author Mikko Hilpinen
 * @since 9.4.2015
 */
public abstract class MaintenanceTask extends TimerTask
{
	// ABSTRACT METHODS	------------------------
	
	/**
	 * @return How many minutes there are between each maintenance task
	 */
	public abstract int getMaintenanceIntervalMinutes();
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * @return The amount of minutes from this moment until midnight
	 */
	public static int getMinutesTillMidnight()
	{
		Calendar tomorrow = new GregorianCalendar();
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		tomorrow.set(Calendar.MINUTE, 0);
		tomorrow.set(Calendar.SECOND, 0);
		tomorrow.set(Calendar.MILLISECOND, 0);
		tomorrow.add(Calendar.DAY_OF_MONTH, 1);
		
		return new SimpleDate(tomorrow.getTime()).minus(new SimpleDate());
	}
}
