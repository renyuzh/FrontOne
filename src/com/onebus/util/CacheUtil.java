package com.onebus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

public class CacheUtil {

	public static void WriteToFile(String name, Bitmap bitmap, String path)
			throws IOException {
		String fString = name.replaceAll("/", ".");

		File dir = new File("/sdcard/OneBus/user/" + path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File("/sdcard/OneBus/user/" + path + "/" + fString
				+ ".jpg");
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap GetFromFile(String fileName, String path) {
		String fString = fileName.replaceAll("/", ".");
		File file = new File("/sdcard/OneBus/user/" + path + "/" + fString
				+ ".jpg");
		if (!file.exists()) {
			return null;
		}
		try {

			FileInputStream fis = new FileInputStream(file);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void DeleteFile(String fileName, String path) {

		String fString = fileName.replaceAll("/", ".");

		File file = new File("/sdcard/OneBus/user/" + path + "/" + fString
				+ ".jpg");

		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;

			} else if (file.isDirectory()) { // 否则如果它是一个目录
				Log.e("Message", "删除失败");
			}
			file.delete();
		} else {
			Log.e("Message", "文件不存在");
		}
	}
}
