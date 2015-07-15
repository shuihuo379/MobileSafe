package com.itheima.mobilesafe.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
	/**
	 * MD5加密方法
	 * @param password
	 * @return
	 */
	public static String md5Password(String password){
		try{
			//得到一个消息摘要器
			MessageDigest md=MessageDigest.getInstance("md5");
			//只一次就将byte数组的内容(密码)实现md5加密,返回一个加密后的byte数组,若是一个很大的文件,则要有到其中的 md.update(byte[]b)
			byte[] result=md.digest(password.getBytes()); 
			StringBuffer sb=new StringBuffer();
			
			for(byte b:result){
				int number=b & 0xff; //加盐
				String str=Integer.toHexString(number);
				if(str.length()==1){
					sb.append("0");
				}
				sb.append(str);
			}
			return sb.toString();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
			return "";
		}
	}
}
