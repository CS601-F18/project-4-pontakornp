package cs601.project4;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TicketPurchaseApplicationLogger {
	private static Logger logger = null;
	private static Handler fileout  = null;
	public static void initialize(String logName, String logFile)	{
		logger = Logger.getLogger(logName);
//		logger.setLevel(Level.INFO);
		FileHandler fileout;				
		try {
			fileout = new FileHandler(logFile);
			fileout.setFormatter(new SimpleFormatter());			
			logger.addHandler(fileout);
			logger.log(Level.INFO, "Logger Name: " + logName + "   |    LogFile: " + logFile, 0);
		} catch (SecurityException | IOException e) {
			System.out.println("File handler error.");
		}
	}
	public static void write(Level level, String msg, int thrown) {
		logger.log(level, msg, thrown);
	}
}