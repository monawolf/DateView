package log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import date.DateFormat;
import event.CloseEvent;
import event.EventBus;

/** 
 * 记录数据日志
 */
public class WriteLog {
	private String filePath = "";/* 文件路径 */
	private RandomAccessFile randomFile;/* 随机访问文件流 */

	private boolean isClose = false;/* 是否停止记录，并关闭文件流 */

	public WriteLog() {
		/* 创建文件流 */
		try {
			Date date = new Date(System.currentTimeMillis());
			Format formatter = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
			String fileName = formatter.format(date);
			filePath = "..\\log\\" + fileName + ".txt";
			File file = new File("..\\log\\");
			if (!file.isDirectory()) {
				file.mkdirs();
			}
			randomFile = new RandomAccessFile(filePath, "rw");
		} catch (FileNotFoundException ef) {
			System.err.println("class WriteLog.java catch FileNotFoundException, the FileNotFoundException is \n" + ef);
		}

		/* 写入内容 */
		EventBus.getInstance().addEventListener(DateFormat.class, e -> {
			boolean isWriteLog = e.isNeedWriteLog();
			if (isWriteLog) {
				Number time = e.getDateX();
				Number amplitude = e.getDateY1();
				Number frequency = e.getDateY2();
				write(time, amplitude, frequency);
			}
		});

		/* 关闭事件 */
		EventBus.getInstance().addEventListener(CloseEvent.class, e -> {
			try {
				long fileLength = randomFile.length();
				/* 如果文件没有写入任何内容则删除空的日志文件 */
				if (fileLength == 0) {
					randomFile.close();
					File nullFile = new File(filePath);
					if (nullFile.isFile()) {
						nullFile.delete();
					}
				}
			} catch (Exception efileLength) {
				System.err.println("class WriteLog.java catch Exception, the Exception is \n" + efileLength);
			}
			isClose = true;
		});
	}

	private void write(Number time, Number amplitude, Number frequency) {
		try {
			if (isClose) {
				long fileLength = randomFile.length();/* 文件长度 */
				randomFile.seek(fileLength);/* 将写文件指针移到文件尾。 */
				randomFile.writeBytes(time + " " + amplitude + " " + frequency + "\r\n");
				randomFile.close();
			} else {
				long fileLength = randomFile.length();/* 文件长度 */
				randomFile.seek(fileLength);/* 将写文件指针移到文件尾。 */
				randomFile.writeBytes(time + " " + amplitude + " " + frequency + "\r\n");
			}
		} catch (IOException eio) {
			System.err.println("class WriteLog.java catch IOException, the IOException is \n" + eio);
		} catch (Exception ee) {
			System.err.println("class WriteLog.java catch CloseFileException, the CloseFileException is \n" + ee);
		}
	}
}
