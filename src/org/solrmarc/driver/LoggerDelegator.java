package org.solrmarc.driver;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LoggerDelegator
{
  private static Logger logger = null;
  private static List<Object[]> preLogDebug = new ArrayList<Object[]>();
  private static boolean initialized = false;
  private String className;
  private static final int DEBUG_INT = 10000;
  private static final int INFO_INT = 20000;
  private static final int WARN_INT = 30000;
  private static final int ERROR_INT = 40000;
  private static final int FATAL_INT = 50000;
  
  public LoggerDelegator(String className)
  {
    this.className = className;
  }
  
  public LoggerDelegator(Class<?> clazz)
  {
    this(clazz.getName());
  }
  
  public static void reInit(String[] homeDirs)
  {
    String[] arrayOfString = homeDirs;int j = homeDirs.length;
    for (int i = 0; i < j; i++)
    {
      String dir = arrayOfString[i];
      
      File log4jProps = new File(dir, "log4j.properties");
      if (log4jProps.exists())
      {
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(log4jProps.getAbsolutePath());
        initialized = true;
        break;
      }
    }
    flushToLog();
  }
  
  public static void flushToLog()
  {
    try
    {
      Class.forName("org.apache.log4j.Logger");
      if (!initialized) {
        BasicConfigurator.configure();
      }
      for (Object[] msg : preLogDebug)
      {
        logger = Logger.getLogger(msg[0].toString());
        int level = ((Integer)msg[1]).intValue();
        String levelStr = levelToString(level);
        Object msgStr = msg[2];
        Object levelObj = intToLevel(level);
        StackTraceElement stack = (StackTraceElement)msg[5];
        String callerFQCN = "";
        String threadName = msg[4].toString();
        String loggerClass = stack.getFileName();
        
        String delegatedMsg = "(" + loggerClass + ":" + stack.getLineNumber() + ") - " + msgStr;
        
        Method logMethod = logger.getClass().getMethod("log", new Class[] { String.class, Class.forName("org.apache.log4j.Priority"), Object.class, Throwable.class });
        logMethod.invoke(logger, new Object[] { callerFQCN, levelObj, delegatedMsg, msg[3] });
      }
    }
    catch (NoClassDefFoundError|ClassNotFoundException|NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException ncd)
    {
      Logger logger;
      for (Object[] msg : preLogDebug)
      {
        String loggerClass = msg[0].toString();
        int level = ((Integer)msg[1]).intValue();
        String levelStr = levelToString(level);
        Object msgStr = msg[2];
        String threadName = msg[4].toString();
        StackTraceElement stack = (StackTraceElement)msg[5];
        System.err.println(levelStr + " [" + threadName + "] (" + stack.getFileName() + ":" + stack.getLineNumber() + ") - " + msgStr);
        if (msg[3] != null)
        {
          Throwable th = (Throwable)msg[3];
          th.printStackTrace(System.err);
        }
      }
    }
  }
  
  private static String levelToString(int level)
  {
    switch (level)
    {
    case DEBUG_INT: 
      return "DEBUG";
    case INFO_INT: 
      return "INFO";
    case WARN_INT: 
      return "WARN";
    case ERROR_INT: 
      return "ERROR";
    case FATAL_INT: 
      return "FATAL";
    }
    return "UNKNOWN";
  }
  
  private static Object intToLevel(int level)
  {
    Object levelObj = null;
    try
    {
      Class<?> clazz = Class.forName("org.apache.log4j.Level");
      Method method = clazz.getMethod("toLevel", new Class[] { Integer.TYPE });
      levelObj = method.invoke(null, new Object[] { Integer.valueOf(level) });
    }
    catch (ClassNotFoundException|NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
    {
      e.printStackTrace();
    }
    return levelObj;
  }
  
  private void push(int level, Object message, Throwable th)
  {
    Object[] msg = new Object[6];
    String threadName = Thread.currentThread().getName();
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    
    msg[0] = this.className;
    msg[1] = Integer.valueOf(level);
    msg[2] = message;
    msg[3] = th;
    msg[4] = threadName;
    msg[5] = (stack.length > 3 ? stack[3] : null);
    preLogDebug.add(msg);
  }
  
  private boolean log4jexists()
  {
    try
    {
      Class<?> clazz = Class.forName("org.apache.log4j.LogManager");
      return true;
    }
    catch (ClassNotFoundException e) {}
    return false;
  }
  
  private void delegateMessageToLog4j(int level, Object message, Throwable th)
  {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    String callingClassname = stack[3].getClassName();
    if (callingClassname.contains("$")) {
      callingClassname = callingClassname.replaceFirst("\\$.*$", "");
    }
    Logger logger = Logger.getLogger(callingClassname);
    try
    {
      Method logMethod = logger.getClass().getMethod("log", new Class[] { String.class, Class.forName("org.apache.log4j.Priority"), Object.class, Throwable.class });
      String callerFQCN = "org.solrmarc.driver.LoggerDelegator";
      Object levelObj = intToLevel(level);
      logMethod.invoke(logger, new Object[] { callerFQCN, levelObj, message, th });
    }
    catch (NoSuchMethodException|SecurityException|ClassNotFoundException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
    {
      e.printStackTrace();
    }
  }
  
  public void debug(Object message)
  {
    if ((!log4jexists()) || (!initialized)) {
      push(DEBUG_INT, message, null);
    } else {
      delegateMessageToLog4j(10000, message, null);
    }
  }
  
  public void info(Object message)
  {
    if ((!log4jexists()) || (!initialized)) {
      push(INFO_INT, message, null);
    } else {
      delegateMessageToLog4j(20000, message, null);
    }
  }
  
  public void warn(Object message)
  {
    if ((!log4jexists()) || (!initialized)) {
      push(WARN_INT, message, null);
    } else {
      delegateMessageToLog4j(30000, message, null);
    }
  }
  
  public void error(Object message)
  {
    if ((!log4jexists()) || (!initialized)) {
      push(ERROR_INT, message, null);
    } else {
      delegateMessageToLog4j(40000, message, null);
    }
  }
  
  public void fatal(Object message)
  {
    if ((!log4jexists()) || (!initialized)) {
      push(FATAL_INT, message, null);
    } else {
      delegateMessageToLog4j(50000, message, null);
    }
  }
  
  public void fatal(Object message, Throwable th)
  {
    if ((!log4jexists()) || (!initialized)) {
      push(FATAL_INT, message, th);
    } else {
      delegateMessageToLog4j(50000, message, th);
    }
  }
}
