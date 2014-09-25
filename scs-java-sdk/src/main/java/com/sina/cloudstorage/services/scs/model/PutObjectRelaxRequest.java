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

import com.sina.cloudstorage.SCSWebServiceRequest;
import com.sina.cloudstorage.event.ProgressListener;
/**
 * 秒传请求对象
 *
 */
public class PutObjectRelaxRequest extends SCSWebServiceRequest {

    /**
     * The name of an existing bucket, to which this request will upload a new
     * object. You must have {@link Permission#Write} permission granted to you
     * in order to upload new objects to a bucket.
     */
    private String bucketName;

    /**
     * The key under which to store the new object.
     */
    private String key;

    /**
     * 文件sha1值
     */
    private String fileSha1;

    /**
     * 文件长度
     */
    private long fileLength;

    public String getFileSha1() {
		return fileSha1;
	}

	public void setFileSha1(String fileSha1) {
		this.fileSha1 = fileSha1;
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	/**
     * Optional metadata instructing Amazon S3 how to handle the uploaded data
     * (e.g. custom user metadata, hooks for specifying content type, etc.). If
     * you are uploading from an InputStream, you <bold>should always</bold>
     * specify metadata with the content size set, otherwise the contents of the
     * InputStream will have to be buffered in memory before they can be sent to
     * Amazon S3, which can have very negative performance impacts.
     */
    private ObjectMetadata metadata;

    /**
     * An optional pre-configured access control policy to use for the new
     * object.  Ignored in favor of accessControlList, if present.
     */
    private CannedAccessControlList cannedAcl;

    /**
     * The optional progress listener for receiving updates about object download
     * status.
     */
    private ProgressListener generalProgressListener;

    /**
     * 创建秒传请求对象
     * @param bucketName
     * @param key
     * @param fileSha1		文件sha1值
     * @param fileLength	文件大小
     */
    public PutObjectRelaxRequest(String bucketName, String key, String fileSha1, long fileLength) {
        this.bucketName = bucketName;
        this.key = key;
        this.fileSha1 = fileSha1;
        this.fileLength = fileLength;
    }

    public PutObjectRelaxRequest(String bucketName, String key, String fileSha1, long fileLength, CannedAccessControlList cannedAcl) {
        this.bucketName = bucketName;
        this.key = key;
        this.fileSha1 = fileSha1;
        this.fileLength = fileLength;
        this.cannedAcl = cannedAcl;
    }

    /**
     * Gets the name of the existing bucket where this request will
     * upload a new object to.
     * In order to upload the object,
     * users must have {@link Permission#Write} permission granted.
     *
     * @return The name of an existing bucket where this request will
     * upload a new object to.
     *
     * @see PutObjectRelaxRequest#setBucketName(String)
     * @see PutObjectRelaxRequest#withBucketName(String)
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the name of an existing bucket where this request will
     * upload a new object to. In order to upload the object,
     * users must have {@link Permission#Write} permission granted.
     *
     * @param bucketName
     *            The name of an existing bucket where this request will
     *            upload a new object to.
     *            In order to upload the object,
     *            users must have {@link Permission#Write} permission granted.
     *
     * @see PutObjectRelaxRequest#getBucketName()
     * @see PutObjectRelaxRequest#withBucketName(String)
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Sets the name of the bucket where this request will upload a new
     * object to. Returns this object, enabling additional method calls to be
     * chained together.
     * <p>
     * In order to upload the object,
     * users must have {@link Permission#Write} permission granted.
     *
     * @param bucketName
     *            The name of an existing bucket where this request will
     *            upload a new object to.
     *            In order to upload the object,
     *            users must have {@link Permission#Write} permission granted.
     *
     * @return This {@link PutObjectRelaxRequest}, enabling additional method calls to be
     *         chained together.
     *
     * @see PutObjectRelaxRequest#getBucketName()
     * @see PutObjectRelaxRequest#setBucketName(String)
     */
    public PutObjectRelaxRequest withBucketName(String bucketName) {
        setBucketName(bucketName);
        return this;
    }

    /**
     * Gets the key under which to store the new object.
     *
     * @return The key under which to store the new object.
     *
     * @see PutObjectRelaxRequest#setKey(String)
     * @see PutObjectRelaxRequest#withKey(String)
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key under which to store the new object.
     *
     * @param key
     *            The key under which to store the new object.
     *
     * @see PutObjectRelaxRequest#getKey()
     * @see PutObjectRelaxRequest#withKey(String)
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Sets the key under which to store the new object. Returns this object,
     * enabling additional method calls to be chained together.
     *
     * @param key
     *            The key under which to store the new object.
     *
     * @return This {@link PutObjectRelaxRequest}, enabling additional method calls to be
     *         chained together.
     *
     * @see PutObjectRelaxRequest#getKey()
     * @see PutObjectRelaxRequest#setKey(String)
     */
    public PutObjectRelaxRequest withKey(String key) {
        setKey(key);
        return this;
    }


    /**
     * Gets the optional metadata instructing Amazon S3 how to handle the
     * uploaded data (e.g. custom user metadata, hooks for specifying content
     * type, etc.).
     * <p>
     * If uploading from an input stream,
     * <b>always</b> specify metadata with the content size set. Otherwise the
     * contents of the input stream have to be buffered in memory before
     * being sent to Amazon S3. This can cause very negative performance
     * impacts.
     * </p>
     *
     * @return The optional metadata instructing Amazon S3 how to handle the
     *         uploaded data (e.g. custom user metadata, hooks for specifying
     *         content type, etc.).
     *
     * @see PutObjectRelaxRequest#setMetadata(ObjectMetadata)
     * @see PutObjectRelaxRequest#withMetadata(ObjectMetadata)
     */
    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the optional metadata instructing Amazon S3 how to handle the
     * uploaded data (e.g. custom user metadata, hooks for specifying content
     * type, etc.).
     * <p>
     * If uploading from an input stream,
     * <b>always</b> specify metadata with the content size set. Otherwise the
     * contents of the input stream have to be buffered in memory before
     * being sent to Amazon S3. This can cause very negative performance
     * impacts.
     * </p>
     *
     * @param metadata
     *            The optional metadata instructing Amazon S3 how to handle the
     *            uploaded data (e.g. custom user metadata, hooks for specifying
     *            content type, etc.).
     *
     * @see PutObjectRelaxRequest#getMetadata()
     * @see PutObjectRelaxRequest#withMetadata(ObjectMetadata)
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the optional metadata instructing Amazon S3 how to handle the
     * uploaded data (e.g. custom user metadata, hooks for specifying content
     * type, etc.). Returns this {@link PutObjectRelaxRequest}, enabling additional method
     * calls to be chained together.
     * <p>
     * If uploading from an input stream,
     * <b>always</b> specify metadata with the content size set. Otherwise the
     * contents of the input stream have to be buffered in memory before
     * being sent to Amazon S3. This can cause very negative performance
     * impacts.
     * </p>
     *
     * @param metadata
     *            The optional metadata instructing Amazon S3 how to handle the
     *            uploaded data (e.g. custom user metadata, hooks for specifying
     *            content type, etc.).
     *
     * @return This {@link PutObjectRelaxRequest}, enabling additional method
     *         calls to be chained together.
     *
     * @see PutObjectRelaxRequest#getMetadata()
     * @see PutObjectRelaxRequest#setMetadata(ObjectMetadata)
     */
    public PutObjectRelaxRequest withMetadata(ObjectMetadata metadata) {
        setMetadata(metadata);
        return this;
    }

    /**
     * Gets the optional pre-configured access control policy to use for the
     * new object.
     *
     * @return The optional pre-configured access control policy to use for the
     *         new object.
     *
     * @see PutObjectRelaxRequest#setCannedAcl(CannedAccessControlList)
     * @see PutObjectRelaxRequest#withCannedAcl(CannedAccessControlList)
     */
    public CannedAccessControlList getCannedAcl() {
        return cannedAcl;
    }

    /**
     * Sets the optional pre-configured access control policy to use for the new
     * object.
     *
     * @param cannedAcl
     *            The optional pre-configured access control policy to use for
     *            the new object.
     *
     * @see PutObjectRelaxRequest#getCannedAcl()
     * @see PutObjectRelaxRequest#withCannedAcl(CannedAccessControlList)
     */
    public void setCannedAcl(CannedAccessControlList cannedAcl) {
        this.cannedAcl = cannedAcl;
    }

    /**
     * Sets the optional pre-configured access control policy to use for the new
     * object. Returns this {@link PutObjectRelaxRequest}, enabling additional method
     * calls to be chained together.
     *
     * @param cannedAcl
     *            The optional pre-configured access control policy to use for
     *            the new object.
     *
     * @return This {@link PutObjectRelaxRequest}, enabling additional method
     *         calls to be chained together.
     *
     * @see PutObjectRelaxRequest#getCannedAcl()
     * @see PutObjectRelaxRequest#setCannedAcl(CannedAccessControlList)
     */
    public PutObjectRelaxRequest withCannedAcl(CannedAccessControlList cannedAcl) {
        setCannedAcl(cannedAcl);
        return this;
    }

    /**
     * Sets the optional progress listener for receiving updates for object
     * upload status.
     *
     * @param progressListener
     *            The legacy progress listener that is used exclusively for Amazon S3 client.
     * 
     * @deprecated use {@link #setGeneralProgressListener(ProgressListener)} instead.
     */
    @Deprecated
    public void setProgressListener(com.sina.cloudstorage.services.scs.model.ProgressListener progressListener) {
        this.generalProgressListener = new LegacyS3ProgressListener(progressListener);
    }

    /**
     * Returns the optional progress listener for receiving updates about object
     * upload status.
     *
     * @return the optional progress listener for receiving updates about object
     *         upload status.
     * 
     * @deprecated use {@link #getGeneralProgressListener()} instead.
     */
    @Deprecated
    public com.sina.cloudstorage.services.scs.model.ProgressListener getProgressListener() {
         if (generalProgressListener instanceof LegacyS3ProgressListener) {
             return ((LegacyS3ProgressListener)generalProgressListener).unwrap();
         } else {
              return null;
         }
    }

    /**
     * Sets the optional progress listener for receiving updates about object
     * upload status, and returns this updated object so that additional method
     * calls can be chained together.
     *
     * @param progressListener
     *            The legacy progress listener that is used exclusively for Amazon S3 client.
     *
     * @return This updated PutObjectRequest object.
     * 
     * @deprecated use {@link #withGeneralProgressListener(ProgressListener)} instead.
     */
    @Deprecated
    public PutObjectRelaxRequest withProgressListener(com.sina.cloudstorage.services.scs.model.ProgressListener progressListener) {
        setProgressListener(progressListener);
        return this;
    }

    /**
     * Sets the optional progress listener for receiving updates about object
     * download status.
     *
     * @param generalProgressListener
     *            The new progress listener.
     */
    public void setGeneralProgressListener(ProgressListener generalProgressListener) {
        this.generalProgressListener = generalProgressListener;
    }

    /**
     * Returns the optional progress listener for receiving updates about object
     * download status.
     *
     * @return the optional progress listener for receiving updates about object
     *          download status.
     */
    public ProgressListener getGeneralProgressListener() {
        return generalProgressListener;
    }

    /**
     * Sets the optional progress listener for receiving updates about object
     * upload status, and returns this updated object so that additional method
     * calls can be chained together.
     *
     * @param generalProgressListener
     *            The new progress listener.
     *
     * @return This updated PutObjectRequest object.
     */
    public PutObjectRelaxRequest withGeneralProgressListener(ProgressListener generalProgressListener) {
        setGeneralProgressListener(generalProgressListener);
        return this;
    }

}
