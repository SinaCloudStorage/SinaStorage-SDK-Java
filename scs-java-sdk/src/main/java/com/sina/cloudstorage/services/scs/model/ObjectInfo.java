package com.sina.cloudstorage.services.scs.model;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import com.sina.cloudstorage.util.DateUtils;

/**
 * http://open.sinastorage.cn/?c=doc&a=api#get_object_meta ObjectInfo Object
 */
public class ObjectInfo {
	private String info;
	private String fileName;
	private int infoInt;
	private String contentMD5;
	private Date lastModified;
	private String contentSHA1;
	private Owner owner;
	private String type;
	private Map<String, String> fileMeta;
	private long size;
	
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		sb.append("info:"+info+"\n");
		sb.append("fileName:"+fileName+"\n");
		sb.append("infoInt:"+infoInt+"\n");
		sb.append("contentMD5:"+contentMD5+"\n");
		sb.append("lastModified:"+lastModified+"\n");
		sb.append("contentSHA1:"+contentSHA1+"\n");
		sb.append("owner:"+owner+"\n");
		sb.append("type:"+type+"\n");
		sb.append("fileMeta:"+fileMeta+"\n");
		sb.append("size:"+size+"\n");
		return sb.toString();
	}
	
	
	@SuppressWarnings("unchecked")
	public ObjectInfo(Map<String,Object> jsonMap){
//		System.out.println(jsonMap);
		if (jsonMap != null) {
			this.info = (String)jsonMap.get("Info");
			this.fileName = (String)jsonMap.get("File-Name");
			this.infoInt = jsonMap.get("Info-Int")==null?0:((Double)jsonMap.get("Info-Int")).intValue();
			this.contentMD5 = (String)jsonMap.get("Content-MD5");
			//时间
			String lastModifiedStr = (String) jsonMap.get("Last-Modified");
			if(lastModifiedStr!=null && !"".equals(lastModifiedStr)){
				DateUtils du = new DateUtils();
				try {
					this.lastModified = du.parseRfc822Date(lastModifiedStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			this.contentSHA1 = (String)jsonMap.get("Content-SHA1");
			this.owner = new Owner((String)jsonMap.get("Owner"), "");
			this.type = (String)jsonMap.get("Type");
			this.fileMeta = (Map<String, String>) jsonMap.get("File-Meta");
			this.size = ((Double)jsonMap.get("Size")).intValue();
		}
	}
	
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getInfoInt() {
		return infoInt;
	}

	public void setInfoInt(int infoInt) {
		this.infoInt = infoInt;
	}

	public String getContentMD5() {
		return contentMD5;
	}

	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getContentSHA1() {
		return contentSHA1;
	}

	public void setContentSHA1(String contentSHA1) {
		this.contentSHA1 = contentSHA1;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getFileMeta() {
		return fileMeta;
	}

	public void setFileMeta(Map<String, String> fileMeta) {
		this.fileMeta = fileMeta;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
