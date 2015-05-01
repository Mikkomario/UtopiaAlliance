package alliance_util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is a simplification used for date and time handling.
 * 
 * @author Mikko Hilpinen
 * @since 11.2.2015
 */
public class SimpleDate implements Comparable<SimpleDate>
{
	// ATTRIBUTES	-------------------------------
	
	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
	
	private Date date;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new date based on the given date instance
	 * @param date The date that is simplified
	 */
	public SimpleDate(Date date)
	{
		this.date = date;
	}

	/**
	 * Parses a new date from the given string
	 * @param dateString The string that contains the date information should be in format: 
	 * MM-dd-HH-mm (from months to days to hours to minutes, separated with a '-'). For 
	 * example: 11-21-18-30
	 * @throws ParseException If a date couldn't be parsed from the given string
	 */
	public SimpleDate(String dateString) throws ParseException
	{
		this.date = format.parse(dateString);
	}
	
	/**
	 * Creates a new date to represent the current moment
	 */
	public SimpleDate()
	{
		this.date = new Date(System.currentTimeMillis());
	}
	
	
	// IMPLEMENTED METHODS	-------------------------
	
	@Override
	public String toString()
	{
		return format.format(this.date);
	}

	@Override
	public int compareTo(SimpleDate o)
	{
		return this.date.compareTo(o.date);
	}
	
	
	// OTHER METHODS	------------------------------
	
	/**
	 * Adds a set number of minutes to this date
	 * @param minutes How many minutes will be added to the date
	 * @return A date that is past this one by the specified amount of minutes
	 */
	public SimpleDate plus(int minutes)
	{
		return new SimpleDate(new Date(this.date.getTime() + minutesToMillis(minutes)));
	}
	
	/**
	 * Calculates the amount of minutes between the two dates
	 * @param other The other date
	 * @return The amount of minutes since other date till this date
	 */
	public int minus(SimpleDate other)
	{
		return (int) millisToMinutes(this.date.getTime() - other.date.getTime());
	}
	
	/**
	 * Checks if this date is past the other date instance
	 * @param other The date instance that may be before this
	 * @return Is this date past the given date
	 */
	public boolean isPast(SimpleDate other)
	{
		return compareTo(other) > 0;
	}
	
	/**
	 * @return The date in a typical date format
	 */
	public Date toDate()
	{
		return this.date;
	}
	
	/**
	 * Converts minutes to milliseconds
	 * @param minutes The amount of minutes to be converted
	 * @return How many milliseconds there are in the given amount of minutes
	 */
	public static long minutesToMillis(int minutes)
	{
		return minutes * 60000;
	}
	
	/**
	 * Converts milliseconds to minutes
	 * @param millis How many milliseconds are to be converted
	 * @return How many minutes there are within the given amount of milliseconds
	 */
	public static long millisToMinutes(long millis)
	{
		return millis / 60000;
	}
}
