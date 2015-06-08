package com.rusdelphi.scan;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Tools {
	private static final String TAG = "Tools";

	public static void hideSoftKeyboard(Activity a) {
		if (a.getCurrentFocus() != null)
			((InputMethodManager) a
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(a.getCurrentFocus()
							.getWindowToken(), 0);
	}

	public static String upperFirstLetter(String s) {
		if (s == null)
			return null;
		else if (s.length() == 0)
			return s;
		else
			return s.substring(0, 1).toUpperCase()
					+ s.substring(1, s.length()).toLowerCase();
	}

	public static byte[] intArrayToBytes(int[] ints) {
		byte[] bytes = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			bytes[i] = (byte) ints[i];
		}
		return bytes;
	}

	public static void unpackZip(InputStream is, OutputStream os)
			throws IOException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;

		while ((ze = zis.getNextEntry()) != null) {
			byte[] buffer = new byte[1024];
			int count;

			while ((count = zis.read(buffer)) > -1) {
				os.write(buffer, 0, count);
			}

			os.close();
			zis.closeEntry();
		}

		zis.close();
		is.close();
	}

	public static void unzip(String zipFile, String location)
			throws IOException {
		int size;
		byte[] buffer = new byte[2048];
		try {
			File f = new File(location);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			ZipInputStream zin = new ZipInputStream(
					new FileInputStream(zipFile));
			try {
				ZipEntry ze = null;
				while ((ze = zin.getNextEntry()) != null) {
					String path = location + ze.getName();

					if (ze.isDirectory()) {
						File unzipFile = new File(path);
						if (!unzipFile.isDirectory()) {
							unzipFile.mkdirs();
						}
					} else {
						FileOutputStream fout = new FileOutputStream(path,
								false);
						BufferedOutputStream bufferOut = new BufferedOutputStream(
								fout, buffer.length);
						while ((size = zin.read(buffer, 0, buffer.length)) != -1) {
							bufferOut.write(buffer, 0, size);
						}
						bufferOut.flush();
						bufferOut.close();
						fout.close();
						zin.closeEntry();
					}
				}
			} finally {
				zin.close();
			}
		} catch (Exception e) {
			Log.e(TAG, "Unzip exception", e);
		}
	}

//	public static void loadImage(Context context, ImageView img, String file) {
//		try {
//			if (Main.mDBisUnzipped) {
//				String path = context.getApplicationInfo().dataDir.toString()
//						+ "/" + file;
//				InputStream is = new BufferedInputStream(new FileInputStream(
//						path));
//				img.setImageBitmap(BitmapFactory.decodeStream(is));
//				//? ���� �� ? is.close();
//			} else
//				img.setImageBitmap(BitmapFactory.decodeStream(context
//						.getAssets().open(file)));
//			img.setTag(file);
//			img.setVisibility(View.VISIBLE);
//		} catch (IOException e) {
//			Log.e(TAG, "can't find file: " + file);
//			img.setVisibility(View.GONE);
//		}
//	}

	public static void hideSoftKeyboard(Context c, View view) {
		InputMethodManager imm = (InputMethodManager) c
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
