package com.itheima.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamTools {
	/**
	 * 采用内存流实现将流中的内容转化为字符串
	 * @param is 输入流
	 * @return String 返回的字符串
	 * @throws IOException 
	 */
	public static String readFromStream(InputStream is) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while((len = is.read(buffer))!=-1){
			baos.write(buffer, 0, len);
		}
		is.close();
		String result = baos.toString();
		baos.close();
		return result;
	}
}
