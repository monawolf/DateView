package log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恢复日志数据
 */
public class ReadLog {
	public ReadLog() {
	}

	public List<String> read(File dataFile) {
		List<String> readDate = new ArrayList<String>();
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dataFile));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {/*一次读入一行，直到读入null为文件结束*/
				readDate.add(tempString);
			}
			reader.close();
		} catch (IOException eio) {
			System.err.println("class ReadLog.java catch IOException, the IOException is \n" + eio);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException eio) {
					System.err.println("class ReadLog.java catch IOException, the IOException is \n" + eio);
				}
			}
		}

		return readDate;
	}
}
