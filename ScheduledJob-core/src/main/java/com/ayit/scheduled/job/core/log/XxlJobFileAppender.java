package com.ayit.scheduled.job.core.log;

import com.ayit.scheduled.job.core.biz.model.LogResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XxlJobFileAppender {

	private static Logger logger = LoggerFactory.getLogger(XxlJobFileAppender.class);

	private static String logBasePath = "/data/applogs/xxl-job/jobhandler";

	private static String glueSrcPath = logBasePath.concat("/gluesource");

	public static void initLogPath(String logPath){
		if (logPath!=null && logPath.trim().length()>0) {
			logBasePath = logPath;
		}
		File logPathDir = new File(logBasePath);
		if (!logPathDir.exists()) {
			logPathDir.mkdirs();
		}
		logBasePath = logPathDir.getPath();
		File glueBaseDir = new File(logPathDir, "gluesource");
		if (!glueBaseDir.exists()) {
			glueBaseDir.mkdirs();
		}
		glueSrcPath = glueBaseDir.getPath();
	}


	public static String getLogPath() {
		return logBasePath;
	}

	public static String getGlueSrcPath() {
		return glueSrcPath;
	}


	public static String makeLogFileName(Date triggerDate, long logId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//这里的getLogPath()会得到存储日志的基础路径，就是用户在配置文件设置的那个路径
		File logFilePath = new File(getLogPath(), sdf.format(triggerDate));
		if (!logFilePath.exists()) {
			logFilePath.mkdir();
		}
		String logFileName = logFilePath.getPath()
				.concat(File.separator)
				.concat(String.valueOf(logId))
				.concat(".log");
		return logFileName;
	}


	public static void appendLog(String logFileName, String appendLog) {
		if (logFileName==null || logFileName.trim().length()==0) {
			return;
		}
		File logFile = new File(logFileName);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				return;
			}
		}
		if (appendLog == null) {
			appendLog = "";
		}
		appendLog += "\r\n";
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(logFile, true);
			fos.write(appendLog.getBytes("utf-8"));
			fos.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
	}

	public static LogResult readLog(String logFileName, int fromLineNum){
		if (logFileName==null || logFileName.trim().length()==0) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not found", true);
		}
		File logFile = new File(logFileName);
		if (!logFile.exists()) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not exists", true);
		}
		StringBuffer logContentBuffer = new StringBuffer();
		int toLineNum = 0;
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new InputStreamReader(new FileInputStream(logFile), "utf-8"));
			String line = null;
			while ((line = reader.readLine())!=null) {
				toLineNum = reader.getLineNumber();
				if (toLineNum >= fromLineNum) {
					logContentBuffer.append(line).append("\n");
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		LogResult logResult = new LogResult(fromLineNum, toLineNum, logContentBuffer.toString(), false);
		return logResult;
	}

	public static String readLines(File logFile){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), "utf-8"));
			if (reader != null) {
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
				return sb.toString();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

}
