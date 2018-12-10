package cs601.project4;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * 
 * @author pontakornp
 *
 *
 * Logger class for this application
 */
public class TicketManagementApplicationLogger {
	private static Logger logger;
	private static FileHandler fileout;
	public static void initialize(String logName, String logFile)	{
		logger = Logger.getLogger(logName);
//		logger.setLevel(Level.INFO);
//		FileHandler fileout;				
		try {
			fileout = new FileHandler(logFile);
			fileout.setFormatter(new SimpleFormatter());			
			
		} catch (SecurityException | IOException e) {
			System.out.println("File handler error.");
		}
		logger.addHandler(fileout);
		logger.log(Level.INFO, "Logger Name: " + logName + "   |    LogFile: " + logFile, 0);
	}
	public static void write(Level level, String msg, int thrown) {
		logger.log(level, msg, thrown);
	}
}