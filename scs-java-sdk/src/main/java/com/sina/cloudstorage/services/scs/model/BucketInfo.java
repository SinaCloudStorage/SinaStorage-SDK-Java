package com.sina.cloudstorage.services.scs.model;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import com.sina.cloudstorage.util.DateUtils;

/**
 * http://open.sinastorage.cn/?c=doc&a=api#get_bucket_meta Bucket Meta Object
 */
public class BucketInfo {
	private int deleteQuantity;
	private int capacity;
	private AccessControlList acl;
	private int projectID;
	private int downloadQuantity;
	private int downloadCapacity;
	private int capacityC;
	private int quantityC;
	private String project;
	private int uploadCapacity;
	private int uploadQuantity;
	private Date lastModified;
	private int sizeC;
	private Owner owner;
	private int deleteCapacity;
	private int quantity;

	public String toString(){
		StringBuilder sb = new StringBuilder("");
		sb.append("deleteQuantity:"+deleteQuantity+"\n");
		sb.append("capacity:"+capacity+"\n");
		sb.append("acl:"+acl+"\n");
		sb.append("projectID:"+projectID+"\n");
		sb.append("downloadQuantity:"+downloadQuantity+"\n");
		sb.append("downloadCapacity:"+downloadCapacity+"\n");
		sb.append("capacityC:"+capacityC+"\n");
		sb.append("quantityC:"+quantityC+"\n");
		sb.append("project:"+project+"\n");
		sb.append("uploadCapacity:"+uploadCapacity+"\n");
		sb.append("uploadQuantity:"+uploadQuantity+"\n");
		sb.append("lastModified:"+lastModified+"\n");
		sb.append("sizeC:"+sizeC+"\n");
		sb.append("owner:"+owner+"\n");
		sb.append("deleteCapacity:"+deleteCapacity+"\n");
		sb.append("quantity:"+quantity+"\n");
		return sb.toString();
	}
	
	public BucketInfo(){
		super();
	}
	
	public BucketInfo(Map<String,Object> jsonMap){
		if (jsonMap != null) {
			this.deleteCapacity = ((Double)jsonMap.get("DeleteQuantity")).intValue();
			this.capacity = ((Double)jsonMap.get("Capacity")).intValue();
			this.acl = new AccessControlList(jsonMap);
			this.projectID = ((Double)jsonMap.get("ProjectID")).intValue();
			this.downloadQuantity = ((Double)jsonMap.get("DownloadQuantity")).intValue();
			this.downloadCapacity = ((Double)jsonMap.get("DownloadCapacity")).intValue();
			this.capacityC = ((Double)jsonMap.get("CapacityC")).intValue();
			this.quantityC = ((Double)jsonMap.get("QuantityC")).intValue();
			this.project = (String)jsonMap.get("Project");
			this.uploadCapacity = ((Double)jsonMap.get("UploadCapacity")).intValue();
			this.uploadQuantity = ((Double)jsonMap.get("UploadQuantity")).intValue();
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
			this.sizeC = ((Double)jsonMap.get("SizeC")).intValue();
			this.owner = new Owner((String)jsonMap.get("Owner"), "");
			this.deleteCapacity = ((Double)jsonMap.get("DeleteCapacity")).intValue();
			this.quantity = ((Double)jsonMap.get("Quantity")).intValue();
		}
	}
	
	public int getDeleteQuantity() {
		return deleteQuantity;
	}

	public void setDeleteQuantity(int deleteQuantity) {
		this.deleteQuantity = deleteQuantity;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(AccessControlList acl) {
		this.acl = acl;
	}

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public int getDownloadQuantity() {
		return downloadQuantity;
	}

	public void setDownloadQuantity(int downloadQuantity) {
		this.downloadQuantity = downloadQuantity;
	}

	public int getDownloadCapacity() {
		return downloadCapacity;
	}

	public void setDownloadCapacity(int downloadCapacity) {
		this.downloadCapacity = downloadCapacity;
	}

	public int getCapacityC() {
		return capacityC;
	}

	public void setCapacityC(int capacityC) {
		this.capacityC = capacityC;
	}

	public int getQuantityC() {
		return quantityC;
	}

	public void setQuantityC(int quantityC) {
		this.quantityC = quantityC;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public int getUploadCapacity() {
		return uploadCapacity;
	}

	public void setUploadCapacity(int uploadCapacity) {
		this.uploadCapacity = uploadCapacity;
	}

	public int getUploadQuantity() {
		return uploadQuantity;
	}

	public void setUploadQuantity(int uploadQuantity) {
		this.uploadQuantity = uploadQuantity;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public int getSizeC() {
		return sizeC;
	}

	public void setSizeC(int sizeC) {
		this.sizeC = sizeC;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public int getDeleteCapacity() {
		return deleteCapacity;
	}

	public void setDeleteCapacity(int deleteCapacity) {
		this.deleteCapacity = deleteCapacity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
