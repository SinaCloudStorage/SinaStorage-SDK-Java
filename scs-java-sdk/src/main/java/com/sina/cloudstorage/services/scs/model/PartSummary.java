/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.sina.cloudstorage.services.scs.model;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import com.sina.cloudstorage.util.DateUtils;

/**
 * Container for summary information about a part in a multipart upload, such as
 * part number, size, etc.
 */
public class PartSummary {

	public PartSummary(){
		super();
	}
	
	@Override
	public String toString(){
		return "[PartNumber:"+partNumber+"lastModified:"+lastModified+"ETag:"+eTag+"size:"+size+"]";
	}
	
	public PartSummary(Map<String,Object> jsonMap){
		/*
		 *   {
	     *       "PartNumber": 1,
	     *       "Last-Modified": "Wed, 20 Jun 2012 14:57:10 UTC",
	     *       "ETag": "050fdc0e690bfae7b29392f152bcf301",
	     *       "Size": 1024
	     *   }
		 */
		if(jsonMap != null){
			partNumber = (Double)jsonMap.get("PartNumber");
			DateUtils du = new DateUtils();
			try {
				lastModified = du.parseRfc822Date((String)jsonMap.get("Last-Modified"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			eTag = (String)jsonMap.get("ETag");
			size = ((Double)jsonMap.get("Size")).longValue();
		}
	}
	
	
    /**
     * The part number describing this part's position relative to the other
     * parts in the multipart upload. Part number must be between 1 and 10,000
     * (inclusive).
     */
    private double partNumber;

    /** The date at which this part was last modified. */
    private Date lastModified;

    /** The entity tag generated from the part content. */
    private String eTag;

    /** The size, in bytes, of the part. */
    private long size;


    /**
     * Returns the part number describing this part's position relative to the
     * other parts in the multipart upload. Part number must be between 1 and
     * 10,000 (inclusive).
     *
     * @return the part number describing this part's position relative to the
     *         other parts in the multipart upload. Part number must be between
     *         1 and 10,000 (inclusive).
     */
    public double getPartNumber() {
        return partNumber;
    }

    /**
     * Sets the part number describing this part's position relative to the
     * other parts in the multipart upload. Part number must be between 1 and
     * 10,000 (inclusive).
     *
     * @param partNumber
     *            the part number describing this part's position relative to
     *            the other parts in the multipart upload. Part number must be
     *            between 1 and 10,000 (inclusive).
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Returns the date this part was last modified.
     *
     * @return the date this part was last modified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the date this part was last modified.
     *
     * @param lastModified
     *            the date this part was last modified.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns the entity tag generated from the part content.
     *
     * @return the entity tag generated from the part content.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the entity tag generated from the part content.
     *
     * @param eTag
     *            the entity tag generated from the part content.
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Returns the size of this part, in bytes.
     *
     * @return the size of this part, in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of this part, in bytes.
     *
     * @param size
     *            the size of this part, in bytes.
     */
    public void setSize(long size) {
        this.size = size;
    }

}
