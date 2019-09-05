package com.redis.mq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UtilSerialize {
	/**
	 * 
	 * <p>Title: ObjTOSerialize</p>
	 * <p>Description: 序列化一个对象</p>
	 * @param obj
	 * @return
	 * @author guangshuai.wang
	 */
	public static byte[] serialize(Object obj){
		ObjectOutputStream oos = null;
		ByteArrayOutputStream byteOut = null;
		try{
			byteOut = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(byteOut);
			oos.writeObject(obj);
			byte[] bytes = byteOut.toByteArray();
			return bytes;
		}catch (Exception e) {
			return null;
		}
	}
	/**
	 * 
	 * <p>Title: unSerialize</p>
	 * <p>Description: 反序列化</p>
	 * @param bytes
	 * @return
	 * @author guangshuai.wang
	 */
	public static Object unSerialize(byte[] bytes){
		ByteArrayInputStream in = null;
		try{
			in = new ByteArrayInputStream(bytes);
			ObjectInputStream objIn = new ObjectInputStream(in);
			return objIn.readObject();
		}catch (Exception e) {
			return null;
		}
	}
}
