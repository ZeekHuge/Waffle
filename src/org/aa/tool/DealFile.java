package org.aa.tool;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import android.os.Environment;
import android.util.Log;

public class DealFile {

	private static File SD = Environment.getExternalStorageDirectory();

	private static String appName = "mybook";

	private static String downPath = SD.getPath() + "/" + appName + "/"
			+ "downFiles/";
	
	private static String fileType = ".txt";

	private static String stringType = "utf-8";

	public static String createFile(String content, int fileName) {
		File files = new File(downPath);
		files.mkdirs();
		File file = new File(downPath + fileName + fileType);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			DataOutputStream os;
			os = new DataOutputStream(new FileOutputStream(file));
			os.writeChars(content);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file.getPath();

	}

}
