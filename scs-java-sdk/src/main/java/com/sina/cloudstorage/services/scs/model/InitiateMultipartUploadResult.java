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

import java.util.Map;

import com.sina.cloudstorage.services.scs.SCS;
import com.sina.cloudstorage.services.scs.internal.ServerSideEncryptionResult;

/**
 * Contains the results of initiating a multipart upload, particularly the
 * unique ID of the new multipart upload.
 *
 * @see SCS#initiateMultipartUpload(InitiateMultipartUploadRequest)
 */
public class InitiateMultipartUploadResult implements ServerSideEncryptionResult {

	public InitiateMultipartUploadResult(){
		super();
	}
	
	
	public InitiateMultipartUploadResult(Map<String,String> jsonMap){
		/*
		 * {
		 *	    "Bucket": "<Your-Bucket-Name>",
		 *	    "Key": "<ObjectName>",
		 *	    "UploadId": "7517c1c49a3b4b86a5f08858290c5cf6"
		 *	}
		 */
		if(jsonMap!=null){
			bucketName = jsonMap.get("Bucket");
			key = jsonMap.get("Key");
			uploadId = jsonMap.get("UploadId");
		}
	}
	
    /** The name of the bucket in which the new multipart upload was initiated */
    private String bucketName;

    /** The object key for which the multipart upload was initiated */
    private String key;

    /** The unique ID of the new multipart upload */
    private String uploadId;

    /** The server side encryption algorithm of the new object */
    private String serverSideEncryption;
    
    /**
     * Returns the name of the bucket in which the new multipart upload was
     * initiated.
     *
     * @return The name of the bucket in which the new multipart upload was
     *         initiated.
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the name of the bucket in which the new multipart upload was
     * initiated.
     *
     * @param bucketName
     *            The name of the bucket in which the new multipart upload was
     *            initiated.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Returns the object key for which the multipart upload was initiated.
     *
     * @return The object key for which the multipart upload was initiated.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the object key for which the multipart upload was initiated.
     *
     * @param key
     *            The object key for which the multipart upload was initiated.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the initiated multipart upload ID.
     *
     * @return the initiated multipart upload ID.
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * Sets the initiated multipart upload ID.
     *
     * @param uploadId
     *            The initiated multipart upload ID.
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
    
    /**
     * Returns the server-side encryption algorithm for the newly created
     * object, or null if none was used.
     */
    public String getServerSideEncryption() {
        return serverSideEncryption;
    }

    /**
     * Sets the server-side encryption algorithm for the newly created object.
     * 
     * @param serverSideEncryption
     *            The server-side encryption algorithm for the new object.
     */
    public void setServerSideEncryption(String serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }
}
