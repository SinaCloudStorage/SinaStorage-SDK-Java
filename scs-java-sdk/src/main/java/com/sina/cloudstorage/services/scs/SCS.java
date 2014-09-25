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
package com.sina.cloudstorage.services.scs;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sina.cloudstorage.HttpMethod;
import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.BucketInfo;
import com.sina.cloudstorage.services.scs.model.CannedAccessControlList;
import com.sina.cloudstorage.services.scs.model.CompleteMultipartUploadRequest;
import com.sina.cloudstorage.services.scs.model.CopyObjectRequest;
import com.sina.cloudstorage.services.scs.model.CreateBucketRequest;
import com.sina.cloudstorage.services.scs.model.DeleteBucketRequest;
import com.sina.cloudstorage.services.scs.model.DeleteObjectRequest;
import com.sina.cloudstorage.services.scs.model.GeneratePresignedUrlRequest;
import com.sina.cloudstorage.services.scs.model.GetBucketAclRequest;
import com.sina.cloudstorage.services.scs.model.GetObjectMetadataRequest;
import com.sina.cloudstorage.services.scs.model.GetObjectRequest;
import com.sina.cloudstorage.services.scs.model.InitiateMultipartUploadRequest;
import com.sina.cloudstorage.services.scs.model.InitiateMultipartUploadResult;
import com.sina.cloudstorage.services.scs.model.ListBucketsRequest;
import com.sina.cloudstorage.services.scs.model.ListObjectsRequest;
import com.sina.cloudstorage.services.scs.model.ListPartsRequest;
import com.sina.cloudstorage.services.scs.model.ObjectInfo;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.ObjectMetadata;
import com.sina.cloudstorage.services.scs.model.PartListing;
import com.sina.cloudstorage.services.scs.model.Permission;
import com.sina.cloudstorage.services.scs.model.PutObjectRequest;
import com.sina.cloudstorage.services.scs.model.PutObjectResult;
import com.sina.cloudstorage.services.scs.model.S3Object;
import com.sina.cloudstorage.services.scs.model.SetBucketAclRequest;
import com.sina.cloudstorage.services.scs.model.UploadPartRequest;
import com.sina.cloudstorage.services.scs.model.UploadPartResult;

/**
 * <p>
 * Provides an interface for accessing the SCS web service.
 * </p>
 * <p>
 * SCS provides storage for the Internet,
 * and is designed to make web-scale computing easier for developers.
 * </p>
 * <p>
 * The SCS Java SDK provides a simple interface that can be
 * used to store and retrieve any amount of data, at any time,
 * from anywhere on the web. It gives any developer access to the same
 * highly scalable, reliable, secure, fast, inexpensive infrastructure
 * that Amazon uses to run its own global network of web sites.
 * The service aims to maximize benefits of scale and to pass those
 * benefits on to developers.
 * </p>
 * <p>
 * For more information about SCS, please see
 * <a href="http://aws.amazon.com/s3">
 * http://aws.amazon.com/s3</a>
 * </p>
 */
public interface SCS {

    /**
     * <p>
     * Override the default S3 client options for this client.
     * </p>
     * @param clientOptions
     *            The S3 client options to use.
     */
    public void setS3ClientOptions(S3ClientOptions clientOptions);

    /**
     * <p>
     * Returns a list of summary information about the objects in the specified
     * buckets.
     * List results are <i>always</i> returned in lexicographic (alphabetical) order.
     * </p>
     * <p>
     * Because buckets can contain a virtually unlimited number of keys, the
     * complete results of a list query can be extremely large. To manage large
     * result sets, SCS uses pagination to split them into multiple
     * responses. Always check the
     * {@link ObjectListing#isTruncated()} method to see if the returned
     * listing is complete or if additional calls are needed to get
     * more results. Alternatively, use the
     * {@link SCSClient#listNextBatchOfObjects(ObjectListing)} method as
     * an easy way to get the next page of object listings.
     * </p>
     * <p>
     * The total number of keys in a bucket doesn't substantially
     * affect list performance.
     * </p>
     *
     * @param bucketName
     *            The name of the SCS bucket to list.
     *
     * @return A listing of the objects in the specified bucket, along with any
     *         other associated information, such as common prefixes (if a
     *         delimiter was specified), the original request parameters, etc.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#listObjects(String, String)
     * @see SCSClient#listObjects(ListObjectsRequest)
     */
    public ObjectListing listObjects(String bucketName) throws SCSClientException,
            SCSServiceException;

    /**
     * <p>
     * Returns a list of summary information about the objects in the specified
     * bucket. Depending on request parameters, additional information is returned,
     * such as common prefixes if a delimiter was specified.  List
     * results are <i>always</i> returned in lexicographic (alphabetical) order.
     * </p>
     * <p>
     * Because buckets can contain a virtually unlimited number of keys, the
     * complete results of a list query can be extremely large. To manage large
     * result sets, SCS uses pagination to split them into multiple
     * responses. Always check the
     * {@link ObjectListing#isTruncated()} method to see if the returned
     * listing is complete or if additional calls are needed to get
     * more results. Alternatively, use the
     * {@link SCSClient#listNextBatchOfObjects(ObjectListing)} method as
     * an easy way to get the next page of object listings.
     * </p>
     * <p>
     * For example, consider a bucket that contains the following keys:
     * <ul>
     * 	<li>"foo/bar/baz"</li>
     * 	<li>"foo/bar/bash"</li>
     * 	<li>"foo/bar/bang"</li>
     * 	<li>"foo/boo"</li>
     * </ul>
     * If calling <code>listObjects</code> with
     * a <code>prefix</code> value of "foo/" and a <code>delimiter</code> value of "/"
     * on this bucket, an <code>ObjectListing</code> is returned that contains one key
     * ("foo/boo") and one entry in the common prefixes list ("foo/bar/").
     * To see deeper into the virtual hierarchy, make another
     * call to <code>listObjects</code> setting the prefix parameter to any interesting
     * common prefix to list the individual keys under that prefix.
     * </p>
     * <p>
     * The total number of keys in a bucket doesn't substantially
     * affect list performance.
     * </p>
     *
     * @param bucketName
     *            The name of the SCS bucket to list.
     * @param prefix
     *            An optional parameter restricting the response to keys
     *            beginning with the specified prefix. Use prefixes to
     *            separate a bucket into different sets of keys,
     *            similar to how a file system organizes files
     * 		      into directories.
     *
     * @return A listing of the objects in the specified bucket, along with any
     *         other associated information, such as common prefixes (if a
     *         delimiter was specified), the original request parameters, etc.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#listObjects(String)
     * @see SCSClient#listObjects(ListObjectsRequest)
     */
    public ObjectListing listObjects(String bucketName, String prefix)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Returns a list of summary information about the objects in the specified
     * bucket. Depending on the request parameters, additional information is returned,
     * such as common prefixes if a delimiter was specified. List
     * results are <i>always</i> returned in lexicographic (alphabetical) order.
     * </p>
     * <p>
     * Because buckets can contain a virtually unlimited number of keys, the
     * complete results of a list query can be extremely large. To manage large
     * result sets, SCS uses pagination to split them into multiple
     * responses. Always check the
     * {@link ObjectListing#isTruncated()} method to see if the returned
     * listing is complete or if additional calls are needed to get
     * more results. Alternatively, use the
     * {@link SCSClient#listNextBatchOfObjects(ObjectListing)} method as
     * an easy way to get the next page of object listings.
     * </p>
     * <p>
     * Calling {@link ListObjectsRequest#setDelimiter(String)}
     * sets the delimiter, allowing groups of keys that share the
     * delimiter-terminated prefix to be included
     * in the returned listing. This allows applications to organize and browse
     * their keys hierarchically, similar to how a file system organizes files
     * into directories. These common prefixes can be retrieved
     * through the {@link ObjectListing#getCommonPrefixes()} method.
     * </p>
     * <p>
     * For example, consider a bucket that contains the following keys:
     * <ul>
     * 	<li>"foo/bar/baz"</li>
     * 	<li>"foo/bar/bash"</li>
     * 	<li>"foo/bar/bang"</li>
     * 	<li>"foo/boo"</li>
     * </ul>
     * If calling <code>listObjects</code> with
     * a prefix value of "foo/" and a delimiter value of "/"
     * on this bucket, an <code>ObjectListing</code> is returned that contains one key
     * ("foo/boo") and one entry in the common prefixes list ("foo/bar/").
     * To see deeper into the virtual hierarchy, make another
     * call to <code>listObjects</code> setting the prefix parameter to any interesting
     * common prefix to list the individual keys under that prefix.
     * </p>
     * <p>
     * The total number of keys in a bucket doesn't substantially
     * affect list performance.
     * </p>
     *
     * @param listObjectsRequest
     *            The request object containing all options for listing the
     *            objects in a specified bucket.
     *
     * @return A listing of the objects in the specified bucket, along with any
     *         other associated information, such as common prefixes (if a
     *         delimiter was specified), the original request parameters, etc.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#listObjects(String)
     * @see SCSClient#listObjects(String, String)
     */
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Provides an easy way to continue a truncated object listing and retrieve
     * the next page of results.
     * </p>
     * <p>
     * To continue the object listing and retrieve the next page of results,
     * call the initial {@link ObjectListing} from one of the
     * <code>listObjects</code> methods.
     * If truncated
     * (indicated when {@link ObjectListing#isTruncated()} returns <code>true</code>),
     * pass the <code>ObjectListing</code> back into this method
     * in order to retrieve the
     * next page of results. Continue using this method to
     * retrieve more results until the returned <code>ObjectListing</code> indicates that
     * it is not truncated.
     * </p>
     * @param previousObjectListing
     *            The previous truncated <code>ObjectListing</code>.
     *            If a
     *            non-truncated <code>ObjectListing</code> is passed in, an empty
     *            <code>ObjectListing</code> is returned without ever contacting
     *            SCS.
     *
     * @return The next set of <code>ObjectListing</code> results, beginning immediately
     *         after the last result in the specified previous <code>ObjectListing</code>.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#listObjects(String)
     * @see SCSClient#listObjects(String, String)
     * @see SCSClient#listObjects(ListObjectsRequest)
     */
    public ObjectListing listNextBatchOfObjects(ObjectListing previousObjectListing)
            throws SCSClientException, SCSServiceException;

    /**
     * Checks if the specified bucket exists. SCS buckets are named in a
     * global namespace; use this method to determine if a specified
     * bucket name already exists, and therefore can't be used to create a new
     * bucket.
     *
     * @param bucketName
     *            The name of the bucket to check.
     *
     * @return The value <code>true</code> if the specified bucket exists in
     *         SCS; the value
     *         <code>false</code> if there is no bucket in SCS with that name.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#createBucket(CreateBucketRequest)
     */
    public boolean doesBucketExist(String bucketName)
        throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Returns a list of all SCS buckets that the
     * authenticated sender of the request owns.
     * </p>
     * <p>
     * Users must authenticate with a valid AWS Access Key ID that is registered
     * with SCS. Anonymous requests cannot list buckets, and users cannot
     * list buckets that they did not create.
     * </p>
     *
     * @return A list of all of the SCS buckets owned by the authenticated
     *         sender of the request.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#listBuckets(ListBucketsRequest)
     */
    public List<Bucket> listBuckets() throws SCSClientException,
            SCSServiceException;

    /**
     * <p>
     * Returns a list of all SCS buckets that the
     * authenticated sender of the request owns.
     * </p>
     * <p>
     * Users must authenticate with a valid AWS Access Key ID that is registered
     * with SCS. Anonymous requests cannot list buckets, and users cannot
     * list buckets that they did not create.
     * </p>
     *
     * @param request
     *          The request containing all of the options related to the listing
     *          of buckets.
     *
     * @return A list of all of the SCS buckets owned by the authenticated
     *         sender of the request.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#listBuckets()
     */
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Creates a new SCS bucket in the default
     * </p>
     * <p>
     * Every object stored in SCS is contained within a bucket. Buckets
     * partition the namespace of objects stored in SCS at the top level.
     * Within a bucket, any name can be used for objects. However, bucket names
     * must be unique across all of SCS.
     * </p>
     * <p>
     * Bucket ownership is similar to the ownership of Internet domain names.
     * Within SCS, only a single user owns each bucket.
     * Once a uniquely named bucket is created in SCS,
     * organize and name the objects within the bucket in any way.
     * Ownership of the bucket is retained as long as the owner has an SCS account.
     * </p>
     * <p>
     * To conform with DNS requirements, the following constraints apply:
     *  <ul>
     *      <li>Bucket names should not contain underscores</li>
     *      <li>Bucket names should be between 3 and 63 characters long</li>
     *      <li>Bucket names should not end with a dash</li>
     *      <li>Bucket names cannot contain adjacent periods</li>
     *      <li>Bucket names cannot contain dashes next to periods (e.g.,
     *      "my-.bucket.com" and "my.-bucket" are invalid)</li>
     *      <li>Bucket names cannot contain uppercase characters</li>
     *  </ul>
     * </p>
     * <p>
     * There are no limits to the number of objects that can be stored in a bucket.
     * Performance does not vary based on the number of buckets used. Store
     * all objects within a single bucket or organize them across several buckets.
     * </p>
     * <p>
     * Buckets cannot be nested; buckets cannot be created within
     * other buckets.
     * </p>
     * <p>
     * Do not make bucket
     * create or delete calls in the high availability code path of an
     * application. Create or delete buckets in a separate
     * initialization or setup routine that runs less often.
     * </p>
     * <p>
     * To create a bucket, authenticate with an account that has a
     * valid AWS Access Key ID and is registered with SCS. Anonymous
     * requests are never allowed to create buckets.
     * </p>
     *
     * @param createBucketRequest
     *            The request object containing all options for creating an SCS
     *            bucket.
     * @return The newly created bucket.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public Bucket createBucket(CreateBucketRequest createBucketRequest)
            throws SCSClientException, SCSServiceException;


    /**
     * <p>
     * Creates a new SCS bucket with the specified name in the default
     * </p>
     * <p>
     * Every object stored in SCS is contained within a bucket. Buckets
     * partition the namespace of objects stored in SCS at the top level.
     * Within a bucket, any name can be used for objects. However, bucket names
     * must be unique across all of SCS.
     * </p>
     * <p>
     * Bucket ownership is similar to the ownership of Internet domain names.
     * Within SCS, only a single user owns each bucket.
     * Once a uniquely named bucket is created in SCS,
     * organize and name the objects within the bucket in any way.
     * Ownership of the bucket is retained as long as the owner has an SCS account.
     * </p>
     * <p>
     * To conform with DNS requirements, the following constraints apply:
     *  <ul>
     *      <li>Bucket names should not contain underscores</li>
     *      <li>Bucket names should be between 3 and 63 characters long</li>
     *      <li>Bucket names should not end with a dash</li>
     *      <li>Bucket names cannot contain adjacent periods</li>
     *      <li>Bucket names cannot contain dashes next to periods (e.g.,
     *      "my-.bucket.com" and "my.-bucket" are invalid)</li>
     *      <li>Bucket names cannot contain uppercase characters</li>
     *  </ul>
     * </p>
     * <p>
     * There are no limits to the number of objects that can be stored in a bucket.
     * Performance does not vary based on the number of buckets used. Store
     * all objects within a single bucket or organize them across several buckets.
     * </p>
     * <p>
     * Buckets cannot be nested; buckets cannot be created within
     * other buckets.
     * </p>
     * <p>
     * Do not make bucket
     * create or delete calls in the high availability code path of an
     * application. Create or delete buckets in a separate
     * initialization or setup routine that runs less often.
     * </p>
     * <p>
     * To create a bucket, authenticate with an account that has a
     * valid AWS Access Key ID and is registered with SCS. Anonymous
     * requests are never allowed to create buckets.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket to create.
     *            All buckets in SCS share a single namespace;
     *            ensure the bucket is given a unique name.
     *
     * @return The newly created bucket.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public Bucket createBucket(String bucketName)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Gets the {@link AccessControlList} (ACL) for the specified object in SCS.
     * </p>
     * <p>
     * Each bucket and object in SCS has an ACL that defines its access
     * control policy. When a request is made, SCS authenticates the
     * request using its standard authentication procedure and then checks the
     * ACL to verify the sender was granted access to the bucket or object. If
     * the sender is approved, the request proceeds. Otherwise, SCS
     * returns an error.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket containing the object whose ACL is
     *            being retrieved.
     * @param key
     *            The key of the object within the specified bucket whose ACL is
     *            being retrieved.
     *
     * @return The <code>AccessControlList</code> for the specified SCS object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#getObjectAcl(String, String, String)
     */
    public AccessControlList getObjectAcl(String bucketName, String key)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Sets the {@link AccessControlList} for the specified object in SCS.
     * </p>
     * <p>
     * Each bucket and object in SCS has an ACL that defines its access
     * control policy. When a request is made, SCS authenticates the
     * request using its standard authentication procedure and then checks the
     * ACL to verify the sender was granted access to the bucket or object. If
     * the sender is approved, the request proceeds. Otherwise, SCS
     * returns an error.
     * </p>
     * <p>
     * When constructing a custom <code>AccessControlList</code>,
     * callers typically retrieve
     * the existing <code>AccessControlList</code> for an object (
     * {@link SCSClient#getObjectAcl(String, String)}), modify it as
     * necessary, and then use this method to upload the new ACL.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket containing the object whose ACL is
     *            being set.
     * @param key
     *            The key of the object within the specified bucket whose ACL is
     *            being set.
     * @param acl
     *            The new <code>AccessControlList</code> for the specified object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#setObjectAcl(String, String, CannedAccessControlList)
     * @see SCS#setObjectAcl(String, String, String, AccessControlList)
     * @see SCS#setObjectAcl(String, String, String, CannedAccessControlList)
     */
    public void setObjectAcl(String bucketName, String key, AccessControlList acl)
            throws SCSClientException, SCSServiceException;

//    /**
//     * <p>
//     * Sets the {@link CannedAccessControlList} for the specified object in
//     * SCS using one
//     * of the pre-configured <code>CannedAccessControlLists</code>.
//     * A <code>CannedAccessControlList</code>
//     * provides a quick way to configure an object or bucket with commonly used
//     * access control policies.
//     * </p>
//     * <p>
//     * Each bucket and object in SCS has an ACL that defines its access
//     * control policy. When a request is made, SCS authenticates the
//     * request using its standard authentication procedure and then checks the
//     * ACL to verify the sender was granted access to the bucket or object. If
//     * the sender is approved, the request proceeds. Otherwise, SCS
//     * returns an error.
//     * </p>
//     *
//     * @param bucketName
//     *            The name of the bucket containing the object whose ACL is
//     *            being set.
//     * @param key
//     *            The key of the object within the specified bucket whose ACL is
//     *            being set.
//     * @param acl
//     *            The new pre-configured <code>CannedAccessControlList</code> for the
//     *            specified object.
//     *
//     * @throws SCSClientException
//     *             If any errors are encountered in the client while making the
//     *             request or handling the response.
//     * @throws SCSServiceException
//     *             If any errors occurred in SCS while processing the
//     *             request.
//     *
//     * @see SCS#setObjectAcl(String, String, AccessControlList)
//     * @see SCS#setObjectAcl(String, String, String, AccessControlList)
//     * @see SCS#setObjectAcl(String, String, String, CannedAccessControlList)
//     */
//    public void setObjectAcl(String bucketName, String key, CannedAccessControlList acl)
//            throws SCSClientException, SCSServiceException;


    /**
     * http://open.sinastorage.cn/?c=doc&a=api#get_bucket_meta Bucket Meta Object
     * @param bucketName
     * @return
     * @throws SCSClientException
     * @throws SCSServiceException
     */
	public BucketInfo getBucketInfo(String bucketName)
			throws SCSClientException, SCSServiceException;
	
	/**
	 * http://open.sinastorage.cn/?c=doc&a=api#get_object_meta ObjectInfo Object
	 * @param bucketName
	 * @param key
	 * @return
	 * @throws SCSClientException
	 * @throws SCSServiceException
	 */
	public ObjectInfo getObjectInfo(String bucketName, String key)
			throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Gets the {@link AccessControlList} (ACL) for the specified SCS bucket.
     * </p>
     * <p>
     * Each bucket and object in SCS has an ACL that defines its access
     * control policy. When a request is made, SCS authenticates the
     * request using its standard authentication procedure and then checks the
     * ACL to verify the sender was granted access to the bucket or object. If
     * the sender is approved, the request proceeds. Otherwise, SCS
     * returns an error.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket whose ACL is being retrieved.
     *
     * @return The <code>AccessControlList</code> for the specified S3 bucket.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public AccessControlList getBucketAcl(String bucketName) throws SCSClientException,
            SCSServiceException;

    /**
     * Sets the {@link AccessControlList} for the specified SCS bucket.
     * <p>
     * Each bucket and object in SCS has an ACL that defines its access
     * control policy. When a request is made, SCS authenticates the
     * request using its standard authentication procedure and then checks the
     * ACL to verify the sender was granted access to the bucket or object. If
     * the sender is approved, the request proceeds. Otherwise, SCS
     * returns an error.
     * <p>
     * When constructing a custom <code>AccessControlList</code>, callers
     * typically retrieve the existing <code>AccessControlList</code> for a
     * bucket ( {@link SCSClient#getBucketAcl(String)}), modify it as
     * necessary, and then use this method to upload the new ACL.
     *
     * @param setBucketAclRequest
     *            The request object containing the bucket to modify and the ACL
     *            to set.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public void setBucketAcl(SetBucketAclRequest setBucketAclRequest)
            throws SCSClientException, SCSServiceException;

    /**
     * Gets the {@link AccessControlList} (ACL) for the specified SCS
     * bucket.
     * <p>
     * Each bucket and object in SCS has an ACL that defines its access
     * control policy. When a request is made, SCS authenticates the
     * request using its standard authentication procedure and then checks the
     * ACL to verify the sender was granted access to the bucket or object. If
     * the sender is approved, the request proceeds. Otherwise, SCS
     * returns an error.
     *
     * @param getBucketAclRequest
     *            The request containing the name of the bucket whose ACL is
     *            being retrieved.
     *
     * @return The <code>AccessControlList</code> for the specified S3 bucket.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public AccessControlList getBucketAcl(GetBucketAclRequest getBucketAclRequest)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Sets the {@link AccessControlList} for the specified SCS bucket.
     * </p>
     * <p>
     * Each bucket and object in SCS has an ACL that defines its access
     * control policy. When a request is made, SCS authenticates the
     * request using its standard authentication procedure and then checks the
     * ACL to verify the sender was granted access to the bucket or object. If
     * the sender is approved, the request proceeds. Otherwise, SCS
     * returns an error.
     * </p>
     * <p>
     * When constructing a custom <code>AccessControlList</code>, callers typically retrieve
     * the existing <code>AccessControlList</code> for a bucket (
     * {@link SCSClient#getBucketAcl(String)}), modify it as necessary, and
     * then use this method to upload the new ACL.
     *
     * @param bucketName
     *            The name of the bucket whose ACL is being set.
     * @param acl
     *            The new AccessControlList for the specified bucket.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#setBucketAcl(String, CannedAccessControlList)
     */
    public void setBucketAcl(String bucketName, AccessControlList acl)
            throws SCSClientException, SCSServiceException;

//    /**
//     * <p>
//     * Sets the {@link CannedAccessControlList} for the specified SCS bucket using one of
//     * the pre-configured <code>CannedAccessControlLists</code>.
//     * A <code>CannedAccessControlList</code>
//     * provides a quick way to configure an object or bucket with commonly used
//     * access control policies.
//     * </p>
//     * <p>
//     * Each bucket and object in SCS has an ACL that defines its access
//     * control policy. When a request is made, SCS authenticates the
//     * request using its standard authentication procedure and then checks the
//     * ACL to verify the sender was granted access to the bucket or object. If
//     * the sender is approved, the request proceeds. Otherwise, SCS
//     * returns an error.
//     * </p>
//     *
//     * @param bucketName
//     *            The name of the bucket whose ACL is being set.
//     * @param acl
//     *            The pre-configured <code>CannedAccessControlLists</code> to set for the
//     *            specified bucket.
//     *
//     * @throws SCSClientException
//     *             If any errors are encountered in the client while making the
//     *             request or handling the response.
//     * @throws SCSServiceException
//     *             If any errors occurred in SCS while processing the
//     *             request.
//     *
//     * @see SCS#setBucketAcl(String, AccessControlList)
//     */
//    public void setBucketAcl(String bucketName, CannedAccessControlList acl)
//            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Gets the metadata for the specified SCS object without
     * actually fetching the object itself.
     * This is useful in obtaining only the object metadata,
     * and avoids wasting bandwidth on fetching
     * the object data.
     * </p>
     * <p>
     * The object metadata contains information such as content type, content
     * disposition, etc., as well as custom user metadata that can be associated
     * with an object in SCS.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket containing the object's whose metadata
     *            is being retrieved.
     * @param key
     *            The key of the object whose metadata is being retrieved.
     *
     * @return All SCS object metadata for the specified object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#getObjectMetadata(GetObjectMetadataRequest)
     */
    public ObjectMetadata getObjectMetadata(String bucketName, String key)
            throws SCSClientException, SCSServiceException;

    /**
     * 设置object metadata
     * @param objectMetadata
     * @throws SCSClientException
     * @throws SCSServiceException
     */
    public void setObjectMetadata(String bucketName, String key, ObjectMetadata objectMetadata)
    		throws SCSClientException, SCSServiceException;
    
    /**
     * <p>
     * Gets the metadata for the specified SCS object without
     * actually fetching the object itself.
     * This is useful in obtaining only the object metadata,
     * and avoids wasting bandwidth on fetching
     * the object data.
     * </p>
     * <p>
     * The object metadata contains information such as content type, content
     * disposition, etc., as well as custom user metadata that can be associated
     * with an object in SCS.
     * </p>
     * <p>
     * For more information about enabling versioning for a bucket, see
     * {@link #setBucketVersioningConfiguration(SetBucketVersioningConfigurationRequest)}.
     * </p>
     *
     * @param getObjectMetadataRequest
     *            The request object specifying the bucket, key and optional
     *            version ID of the object whose metadata is being retrieved.
     *
     * @return All S3 object metadata for the specified object.
     *
     * @throws SCSClientException
     *             If any errors are encountered on the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#getObjectMetadata(String, String)
     */
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest getObjectMetadataRequest)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Gets the object stored in SCS under the specified bucket and
     * key.
     * </p>
     * <p>
     * Be extremely careful when using this method; the returned
     * SCS object contains a direct stream of data from the HTTP connection.
     * The underlying HTTP connection cannot be closed until the user
     * finishes reading the data and closes the stream.
     * Therefore:
     * </p>
     * <ul>
     *  <li>Use the data from the input stream in SCS object as soon as possible</li>
     *  <li>Close the input stream in SCS object as soon as possible</li>
     * </ul>
     * If these rules are not followed, the client can run out of
     * resources by allocating too many open, but unused, HTTP connections.
     * </p>
     * <p>
     * To get an object from SCS, the caller must have {@link Permission#Read}
     * access to the object.
     * </p>
     * <p>
     * If the object fetched is publicly readable, it can also read it
     * by pasting its URL into a browser.
     * </p>
     * <p>
     * For more advanced options (such as downloading only a range of an
     * object's content, or placing constraints on when the object should be downloaded)
     * callers can use {@link #getObject(GetObjectRequest)}.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket containing the desired object.
     * @param key
     *            The key under which the desired object is stored.
     *
     * @return The object stored in SCS in the specified bucket and key.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#getObject(GetObjectRequest)
     * @see SCS#getObject(GetObjectRequest, File)
     */
    public S3Object getObject(String bucketName, String key) throws SCSClientException,
            SCSServiceException;

    /**
     * <p>
     * Gets the object stored in SCS under the specified bucket and
     * key.
     * Returns <code>null</code> if the specified constraints weren't met.
     * </p>
     * <p>
     * Callers should be very careful when using this method; the returned
     * SCS object contains a direct stream of data from the HTTP connection.
     * The underlying HTTP connection cannot be closed until the user
     * finishes reading the data and closes the stream. Callers should
     * therefore:
     * </p>
     * <ul>
     *  <li>Use the data from the input stream in SCS object as soon as possible,</li>
     *  <li>Close the input stream in SCS object as soon as possible.</li>
     * </ul>
     * <p>
     * If callers do not follow those rules, then the client can run out of
     * resources if allocating too many open, but unused, HTTP connections.
     * </p>
     * <p>
     * To get an object from SCS, the caller must have {@link Permission#Read}
     * access to the object.
     * </p>
     * <p>
     * If the object fetched is publicly readable, it can also read it
     * by pasting its URL into a browser.
     * </p>
     * <p>
     * When specifying constraints in the request object, the client needs to be
     * prepared to handle this method returning <code>null</code>
     * if the provided constraints aren't met when SCS receives the request.
     * </p>
     * <p>
     * If the advanced options provided in {@link GetObjectRequest} aren't needed,
     * use the simpler {@link SCS#getObject(String bucketName, String key)} method.
     * </p>
     *
     * @param getObjectRequest
     *            The request object containing all the options on how to
     *            download the object.
     *
     * @return The object stored in SCS in the specified bucket and key.
     *         Returns <code>null</code> if constraints were specified but not met.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     * @see SCS#getObject(String, String)
     * @see SCS#getObject(GetObjectRequest, File)
     */
    public S3Object getObject(GetObjectRequest getObjectRequest)
            throws SCSClientException, SCSServiceException;


    /**
     * <p>
     * Gets the object metadata for the object stored
     * in SCS under the specified bucket and key,
     * and saves the object contents to the
     * specified file.
     * Returns <code>null</code> if the specified constraints weren't met.
     * </p>
     * <p>
     * Instead of
     * using {@link SCS#getObject(GetObjectRequest)},
     * use this method to ensure that the underlying
     * HTTP stream resources are automatically closed as soon as possible.
     * The SCS clients handles immediate storage of the object
     * contents to the specified file.
     * </p>
     * <p>
     * To get an object from SCS, the caller must have {@link Permission#Read}
     * access to the object.
     * </p>
     * <p>
     * If the object fetched is publicly readable, it can also read it
     * by pasting its URL into a browser.
     * </p>
     * <p>
     * When specifying constraints in the request object, the client needs to be
     * prepared to handle this method returning <code>null</code>
     * if the provided constraints aren't met when SCS receives the request.
     * </p>
     *
     * @param getObjectRequest
     *            The request object containing all the options on how to
     *            download the SCS object content.
     * @param destinationFile
     *            Indicates the file (which might already exist) where
     *            to save the object content being downloading from SCS.
     *
     * @return All S3 object metadata for the specified object.
     *         Returns <code>null</code> if constraints were specified but not met.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request, handling the response, or writing the incoming data
     *             from S3 to the specified destination file.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#getObject(String, String)
     * @see SCS#getObject(GetObjectRequest)
     */
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest, File destinationFile)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Deletes the specified bucket. All objects (and all object versions, if versioning
     * was ever enabled) in the bucket must be deleted before the bucket itself
     * can be deleted.
     * </p>
     * <p>
     * Only the owner of a bucket can delete it, regardless of the bucket's
     * access control policy (ACL).
     * </p>
     *
     * @param deleteBucketRequest
     *            The request object containing all options for deleting an SCS
     *            bucket.
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#deleteBucket(String)
     */
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest)
            throws SCSClientException, SCSServiceException;


    /**
     * <p>
     * Deletes the specified bucket. All objects (and all object versions, if versioning
     * was ever enabled) in the bucket must be deleted before the bucket itself
     * can be deleted.
     * </p>
     * <p>
     * Only the owner of a bucket can delete it, regardless of the bucket's
     * access control policy.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket to delete.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#deleteBucket(String)
     */
    public void deleteBucket(String bucketName)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Uploads a new object to the specified SCS bucket.
     * The <code>PutObjectRequest</code> contains all the
     * details of the request, including the bucket to upload to, the key the
     * object will be uploaded under, and the file or input stream containing the data
     * to upload.
     * </p>
     * <p>
     * SCS never stores partial objects; if during this call
     * an exception wasn't thrown, the entire object was stored.
     * </p>
     * <p>
     * Depending on whether a file or input stream is being uploaded, this
     * method has slightly different behavior.
     * </p>
     * <p>
     * When uploading a file:
     * </p>
     * <ul>
     *  <li>
     *  The client automatically computes
     *  a checksum of the file.
     *  SCS uses checksums to validate the data in each file.
     *  </li>
     *  <li>
     *  Using the file extension, SCS attempts to determine
     *  the correct content type and content disposition to use
     *  for the object.
     *  </li>
     * </ul>
     * <p>
     * When uploading directly from an input stream:
     * </p>
     * <ul>
     *  <li>Be careful to set the
     *  correct content type in the metadata object before directly sending a
     *  stream. Unlike file uploads, content types from input streams
     *  cannot be automatically determined.  If the caller doesn't explicitly set
     *  the content type, it will not be set in SCS.
     *  </li>
     *  <li>Content length <b>must</b> be specified before data can be uploaded
     *  to SCS. SCS explicitly requires that the
     *  content length be sent in the request headers before it
     *  will accept any of the data. If the caller doesn't provide
     *  the length, the library must buffer the contents of the
     *  input stream in order to calculate it.
     * </ul>
     * <p>
     * If versioning is enabled for the specified bucket,
     * this operation will never overwrite an existing object
     * with the same key, but will keep the existing object as
     * an older version
     * until that version is
     * explicitly deleted (see
     * {@link SCS#deleteVersion(String, String, String)}.
     * </p>

     * <p>
     * If versioning is not enabled, this operation will overwrite an existing object
     * with the same key; SCS will store the last write request.
     * SCS does not provide object locking.
     * If SCS receives multiple write requests for the same object nearly
     * simultaneously, all of the objects might be stored.  However, a single
     * object will be stored with the final write request.
     * </p>

     * <p>
     * When specifying a location constraint when creating a bucket, all objects
     * added to the bucket are stored in the bucket's region. For example, if
     * specifying a Europe (EU) region constraint for a bucket, all of that
     * bucket's objects are stored in the EU region.
     * </p>
     * <p>
     * The specified bucket must already exist and the caller must have
     * {@link Permission#Write} permission to the bucket to upload an object.
     * </p>
     *
     * @param putObjectRequest
     *            The request object containing all the parameters to upload a
     *            new object to SCS.
     *
     * @return A {@link PutObjectResult} object containing the information
     *         returned by SCS for the newly created object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#putObject(String, String, File)
     * @see SCS#putObject(String, String, InputStream, ObjectMetadata)
     */
    public PutObjectResult putObject(PutObjectRequest putObjectRequest)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Uploads the specified file to SCS under the specified bucket and
     * key name.
     * </p>
     * <p>
     * SCS never stores partial objects;
     * if during this call an exception wasn't thrown,
     * the entire object was stored.
     * </p>
     * <p>
     * The client automatically computes
     * a checksum of the file.
     * SCS uses checksums to validate the data in each file.
     * </p>
     * <p>
     *  Using the file extension, SCS attempts to determine
     *  the correct content type and content disposition to use
     *  for the object.
     * </p>
     * <p>
     * If versioning is enabled for the specified bucket,
     * this operation will
     * this operation will never overwrite an existing object
     * with the same key, but will keep the existing object as an
     * older version
     * until that version is
     * explicitly deleted (see
     * {@link SCS#deleteVersion(String, String, String)}.
     * </p>
     * <p>
     * If versioning is not enabled, this operation will overwrite an existing object
     * with the same key; SCS will store the last write request.
     * SCS does not provide object locking.
     * If SCS receives multiple write requests for the same object nearly
     * simultaneously, all of the objects might be stored.  However, a single
     * object will be stored with the final write request.
     * </p>

     * <p>
     * When specifying a location constraint when creating a bucket, all objects
     * added to the bucket are stored in the bucket's region. For example, if
     * specifying a Europe (EU) region constraint for a bucket, all of that
     * bucket's objects are stored in EU region.
     * </p>
     * <p>
     * The specified bucket must already exist and the caller must have
     * {@link Permission#Write} permission to the bucket to upload an object.
     * </p>
     *
     * @param bucketName
     *            The name of an existing bucket, to which you have
     *            {@link Permission#Write} permission.
     * @param key
     *            The key under which to store the specified file.
     * @param file
     *            The file containing the data to be uploaded to SCS.
     *
     * @return A {@link PutObjectResult} object containing the information
     *         returned by SCS for the newly created object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#putObject(PutObjectRequest)
     * @see SCS#putObject(String, String, InputStream, ObjectMetadata)
     */
    public PutObjectResult putObject(String bucketName, String key, File file)
            throws SCSClientException, SCSServiceException;
    
    /**
     * Uploads the specified file to SCS under the specified bucket and
     * key name.
     * @param bucketName
     * @param key
     * @param file
     * @param requestHeader
     * 				custom request header
     * @return
     * @throws SCSClientException
     * @throws SCSServiceException
     */
    public PutObjectResult putObject(String bucketName, String key, File file,Map<String, String> requestHeader)
            throws SCSClientException, SCSServiceException;

    /**
     * 秒传接口
     * @param bucketName
     * @param key
     * @param fileSha1		文件sha1值
     * @param fileLength	文件大小
     * @return
     */
    public PutObjectResult putObjectRelax(String bucketName, String key, String fileSha1, long fileLength)
    		throws SCSClientException, SCSServiceException;
    
    /**
     * <p>
     * Uploads the specified input stream and object metadata to SCS under
     * the specified bucket and key name.
     * </p>
     * <p>
     * SCS never stores partial objects;
     * if during this call an exception wasn't thrown,
     * the entire object was stored.
     * </p>
     * <p>
     * The client automatically computes
     * a checksum of the file. This checksum is verified against another checksum
     * that is calculated once the data reaches SCS, ensuring the data
     * has not corrupted in transit over the network.
     * </p>
     * <p>
     * Using the file extension, SCS attempts to determine
     * the correct content type and content disposition to use
     * for the object.
     * </p>
     * <p>
     * Content length <b>must</b> be specified before data can be uploaded to
     * SCS. If the caller doesn't provide it, the library will <b>have
     * to</b> buffer the contents of the input stream in order to calculate it
     * because SCS explicitly requires that the content length be sent in
     * the request headers before any of the data is sent.
     * </p>
     * <p>
     * If versioning is enabled for the specified bucket, this operation will
     * never overwrite an existing object at the same key, but instead will keep
     * the existing object around as an older version until that version is
     * explicitly deleted (see
     * {@link SCS#deleteVersion(String, String, String)}.
     * </p>

     * <p>
     * If versioning is not enabled,
     * this operation will overwrite an existing object
     * with the same key; SCS will store the last write request.
     * SCS does not provide object locking.
     * If SCS receives multiple write requests for the same object nearly
     * simultaneously, all of the objects might be stored.  However, a single
     * object will be stored with the final write request.
     * </p>

     * <p>
     * When specifying a location constraint when creating a bucket, all objects
     * added to the bucket are stored in the bucket's region. For example, if
     * specifying a Europe (EU) region constraint for a bucket, all of that
     * bucket's objects are stored in EU region.
     * </p>
     * <p>
     * The specified bucket must already exist and the caller must have
     * {@link Permission#Write} permission to the bucket to upload an object.
     * </p>
     *
     * @param bucketName
     *            The name of an existing bucket, to which you have
     *            {@link Permission#Write} permission.
     * @param key
     *            The key under which to store the specified file.
     * @param input
     *            The input stream containing the data to be uploaded to Amazon
     *            S3.
     * @param metadata
     *            Additional metadata instructing SCS how to handle the
     *            uploaded data (e.g. custom user metadata, hooks for specifying
     *            content type, etc.).
     *
     * @return A {@link PutObjectResult} object containing the information
     *         returned by SCS for the newly created object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCS#putObject(String, String, File)
     * @see SCS#putObject(PutObjectRequest)
     */
    public PutObjectResult putObject(
            String bucketName, String key, InputStream input, ObjectMetadata metadata)
            throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Copies a source object to a new destination in SCS.
     * </p>
     * <p>
     * By default, all object metadata for the source object are copied to
     * the new destination object. The SCS <code>AcccessControlList</code> (ACL)
     * is <b>not</b> copied to the new
     * object; the new object will have the default SCS ACL,
     * {@link CannedAccessControlList#Private}.
     * </p>
     * <p>
     * To copy an object, the caller's account must have read access to the source object and
     * write access to the destination bucket
     * </p>
     * <p>
     * This method only exposes the basic options for copying an SCS
     * object. Additional options are available by calling the
     * {@link SCSClient#copyObject(CopyObjectRequest)} method, including
     * conditional constraints for copying objects, setting ACLs, overwriting
     * object metadata, etc.
     * </p>
     *
     * @param sourceBucketName
     *            The name of the bucket containing the source object to copy.
     * @param sourceKey
     *            The key in the source bucket under which the source object is stored.
     * @param destinationBucketName
     *            The name of the bucket in which the new object will be
     *            created. This can be the same name as the source bucket's.
     * @param destinationKey
     *            The key in the destination bucket under which the new object
     *            will be created.
     *
     * @return A {@link CopyObjectResult} object containing the information
     *         returned by SCS for the newly created object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#copyObject(CopyObjectRequest)
     */
    public void copyObject(String sourceBucketName, String sourceKey,
            String destinationBucketName, String destinationKey) throws SCSClientException,
            SCSServiceException;

    /**
     * <p>
     * Copies a source object to a new destination in SCS.
     * </p>
     * <p>
     * By default, all object metadata for the source object are copied to
     * the new destination object, unless new object metadata in the
     * specified {@link CopyObjectRequest} is provided.
     * </p>
     * <p>
     * The SCS Acccess Control List (ACL)
     * is <b>not</b> copied to the new object. The new object will have
     * the default SCS ACL, {@link CannedAccessControlList#Private},
     * unless one is explicitly provided in the specified
     * {@link CopyObjectRequest}.
     * </p>
     * <p>
     * To copy an object, the caller's account must have read access to the source object and
     * write access to the destination bucket.
     * </p>
     * <p>
     * If constraints are specified in the <code>CopyObjectRequest</code>
     * (e.g.
     * {@link CopyObjectRequest#setMatchingETagConstraints(List)})
     * and are not satisfied when SCS receives the
     * request, this method returns <code>null</code>.
     * This method returns a non-null result under all other
     * circumstances.
     * </p>
     * <p>
     * This method exposes all the advanced options for copying an SCS
     * object. For simple needs, use the
     * {@link SCSClient#copyObject(String, String, String, String)}
     * method.
     * </p>
     *
     * @param copyObjectRequest
     *            The request object containing all the options for copying an
     *            SCS object.
     *
     * @return A {@link CopyObjectResult} object containing the information
     *         returned by SCS about the newly created object, or <code>null</code> if
     *         constraints were specified that weren't met when SCS attempted
     *         to copy the object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#copyObject(String, String, String, String)
     */
    public void copyObject(CopyObjectRequest copyObjectRequest)
            throws SCSClientException, SCSServiceException;

//    /**
//     * Copies a source object to a part of a multipart upload.
//     *
//     * To copy an object, the caller's account must have read access to the source object and
//     * write access to the destination bucket.
//     * </p>
//     * <p>
//     * If constraints are specified in the <code>CopyPartRequest</code>
//     * (e.g.
//     * {@link CopyPartRequest#setMatchingETagConstraints(List)})
//     * and are not satisfied when SCS receives the
//     * request, this method returns <code>null</code>.
//     * This method returns a non-null result under all other
//     * circumstances.
//     * </p>
//     *
//     * @param copyPartRequest
//     *            The request object containing all the options for copying an
//     *            SCS object.
//     *
//     * @return A {@link CopyPartResult} object containing the information
//     *         returned by SCS about the newly created object, or <code>null</code> if
//     *         constraints were specified that weren't met when SCS attempted
//     *         to copy the object.
//     *
//     * @throws SCSClientException
//     *             If any errors are encountered in the client while making the
//     *             request or handling the response.
//     * @throws SCSServiceException
//     *             If any errors occurred in SCS while processing the
//     *             request.
//     *
//     * @see AmazonS3Client#copyObject(CopyObjectRequest)
//     * @see AmazonS3Client#initiateMultipartUpload(InitiateMultipartUploadRequest)
//     */
//    public CopyPartResult copyPart(CopyPartRequest copyPartRequest) throws SCSClientException,
//            SCSServiceException;

    /**
     * <p>
     * Deletes the specified object in the specified bucket. Once deleted, the object
     * can only be restored if versioning was enabled when the object was deleted.
     * </p>
     * <p>
     * If attempting to delete an object that does not exist,
     * SCS returns
     * a success message instead of an error message.
     * </p>
     *
     * @param bucketName
     *            The name of the SCS bucket containing the object to
     *            delete.
     * @param key
     *            The key of the object to delete.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#deleteObject(DeleteObjectRequest)
     */
    public void deleteObject(String bucketName, String key)
        throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Deletes the specified object in the specified bucket. Once deleted, the
     * object can only be restored if versioning was enabled when the object was
     * deleted.
     * </p>
     * <p>
     * If attempting to delete an object that does not exist,
     * SCS will return
     * a success message instead of an error message.
     * </p>
     *
     * @param deleteObjectRequest
     *            The request object containing all options for deleting an SCS
     *            object.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     *
     * @see SCSClient#deleteObject(String, String)
     */
    public void deleteObject(DeleteObjectRequest deleteObjectRequest)
        throws SCSClientException, SCSServiceException;

    /**
     * <p>
     * Returns a pre-signed URL for accessing an SCS resource.
     * </p>
     * <p>
     * Pre-signed URLs allow clients to form a URL for an SCS resource,
     * and then sign it with the current AWS security credentials.
     * The pre-signed URL
     * can be shared to other users, allowing access to the resource without
     * providing an account's AWS security credentials.
     * </p>
     * <p>
     * Pre-signed URLs are useful in many situations where AWS security
     * credentials aren't available from the client that needs to make the
     * actual request to SCS.
     * </p>
     * <p>
     * For example, an application may need remote users to upload files to the
     * application owner's SCS bucket, but doesn't need to ship the
     * AWS security credentials with the application. A pre-signed URL
     * to PUT an object into the owner's bucket can be generated from a remote
     * location with the owner's AWS security credentials, then the pre-signed
     * URL can be passed to the end user's application to use.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket containing the desired object.
     * @param key
     *            The key in the specified bucket under which the desired object
     *            is stored.
     * @param expiration
     *            The time at which the returned pre-signed URL will expire.
     *
     * @return A pre-signed URL which expires at the specified time, and can be
     *         used to allow anyone to download the specified object from S3,
     *         without exposing the owner's AWS secret access key.
     *
     * @throws SCSClientException
     *             If there were any problems pre-signing the request for the
     *             specified S3 object.
     *
     * @see SCS#generatePresignedUrl(String, String, Date, HttpMethod)
     * @see SCS#generatePresignedUrl(GeneratePresignedUrlRequest)
     */
    public URL generatePresignedUrl(String bucketName, String key, Date expiration, boolean bucketNameAsDomain)
            throws SCSClientException;

    /**
     * <p>
     * Returns a pre-signed URL for accessing an SCS resource.
     * </p>
     * <p>
     * Pre-signed URLs allow clients to form a URL for an SCS resource,
     * and then sign it with the current AWS security credentials.
     * The pre-signed URL
     * can be shared to other users, allowing access to the resource without
     * providing an account's AWS security credentials.
     * </p>
     * <p>
     * Pre-signed URLs are useful in many situations where AWS security
     * credentials aren't available from the client that needs to make the
     * actual request to SCS.
     * </p>
     * <p>
     * For example, an application may need remote users to upload files to the
     * application owner's SCS bucket, but doesn't need to ship the
     * AWS security credentials with the application. A pre-signed URL
     * to PUT an object into the owner's bucket can be generated from a remote
     * location with the owner's AWS security credentials, then the pre-signed
     * URL can be passed to the end user's application to use.
     * </p>
     *
     * @param bucketName
     *            The name of the bucket containing the desired object.
     * @param key
     *            The key in the specified bucket under which the desired object
     *            is stored.
     * @param expiration
     *            The time at which the returned pre-signed URL will expire.
     * @param method
     *            The HTTP method verb to use for this URL
     *
     * @return A pre-signed URL which expires at the specified time, and can be
     *         used to allow anyone to download the specified object from S3,
     *         without exposing the owner's AWS secret access key.
     *
     * @throws SCSClientException
     *             If there were any problems pre-signing the request for the
     *             specified S3 object.
     *
     * @see SCS#generatePresignedUrl(String, String, Date)
     * @see SCS#generatePresignedUrl(GeneratePresignedUrlRequest)
     */
    public URL generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method, boolean bucketNameAsDomain)
            throws SCSClientException;


    /**
     * <p>
     * Returns a pre-signed URL for accessing an SCS resource.
     * </p>
     * <p>
     * Pre-signed URLs allow clients to form a URL for an SCS resource,
     * and then sign it with the current AWS security credentials. The
     * pre-signed URL can be shared to other users, allowing access to the
     * resource without providing an account's AWS security credentials.
     * </p>
     * <p>
     * Pre-signed URLs are useful in many situations where AWS security
     * credentials aren't available from the client that needs to make the
     * actual request to SCS.
     * </p>
     * <p>
     * For example, an application may need remote users to upload files to the
     * application owner's SCS bucket, but doesn't need to ship the AWS
     * security credentials with the application. A pre-signed URL to PUT an
     * object into the owner's bucket can be generated from a remote location
     * with the owner's AWS security credentials, then the pre-signed URL can be
     * passed to the end user's application to use.
     * </p>
     * <p>
     * Note that presigned URLs cannot be used to upload an object with an
     * attached policy, as described in <a href=
     * "https://aws.amazon.com/articles/1434?_encoding=UTF8&queryArg=searchQuery&x=0&fromSearch=1&y=0&searchPath=all"
     * >this blog post</a>. That method is only suitable for POSTs from HTML
     * forms by browsers.
     * </p>
     *
     * @param generatePresignedUrlRequest
     *            The request object containing all the options for generating a
     *            pre-signed URL (bucket name, key, expiration date, etc).
     * @return A pre-signed URL that can be used to access an SCS resource
     *         without requiring the user of the URL to know the account's AWS
     *         security credentials.
     * @throws SCSClientException
     *             If there were any problems pre-signing the request for the
     *             SCS resource.
     * @see SCS#generatePresignedUrl(String, String, Date)
     * @see SCS#generatePresignedUrl(String, String, Date, HttpMethod)
     */
    public URL generatePresignedUrl(GeneratePresignedUrlRequest generatePresignedUrlRequest)
            throws SCSClientException;

    /**
     * Initiates a multipart upload and returns an InitiateMultipartUploadResult
     * 
     * @param bucketName
     * @param key
     * @return
     * @throws SCSClientException
     * @throws SCSServiceException
     * @see SCS#initiateMultipartUpload(InitiateMultipartUploadRequest)
     */
    public InitiateMultipartUploadResult initiateMultipartUpload(String bucketName, String key)
    		throws SCSClientException, SCSServiceException;
    
    /**
     * Initiates a multipart upload and returns an InitiateMultipartUploadResult
     * which contains an upload ID. This upload ID associates all the parts in
     * the specific upload and is used in each of your subsequent
     * {@link #uploadPart(UploadPartRequest)} requests. You also include this
     * upload ID in the final request to either complete, or abort the multipart
     * upload request.
     *
     * @param request
     *            The InitiateMultipartUploadRequest object that specifies all
     *            the parameters of this operation.
     *
     * @return An InitiateMultipartUploadResult from SCS.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest request)
            throws SCSClientException, SCSServiceException;

    /**
     * Uploads a part in a multipart upload. You must initiate a multipart
     * upload before you can upload any part.
     * <p>
     * Your UploadPart request must include an upload ID and a part number. The
     * upload ID is the ID returned by SCS in response to your Initiate
     * Multipart Upload request. Part number can be any number between 1 and
     * 10,000, inclusive. A part number uniquely identifies a part and also
     * defines its position within the object being uploaded. If you upload a
     * new part using the same part number that was specified in uploading a
     * previous part, the previously uploaded part is overwritten.
     * <p>
     * To ensure data is not corrupted traversing the network, specify the
     * Content-MD5 header in the Upload Part request. SCS checks the part
     * data against the provided MD5 value. If they do not match, SCS
     * returns an error.
     * <p>
     * When you upload a part, the returned UploadPartResult contains an ETag
     * property. You should record this ETag property value and the part number.
     * After uploading all parts, you must send a CompleteMultipartUpload
     * request. At that time SCS constructs a complete object by
     * concatenating all the parts you uploaded, in ascending order based on the
     * part numbers. The CompleteMultipartUpload request requires you to send
     * all the part numbers and the corresponding ETag values.
     *
     * @param request
     *            The UploadPartRequest object that specifies all the parameters
     *            of this operation.
     *
     * @return An UploadPartResult from SCS containing the part number and
     *         ETag of the new part.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public UploadPartResult uploadPart(UploadPartRequest request)
            throws SCSClientException, SCSServiceException;

    /**
     * Lists the parts that have been uploaded for a specific multipart upload.
     * <p>
     * This method must include the upload ID, returned by the
     * {@link #initiateMultipartUpload(InitiateMultipartUploadRequest)}
     * operation. This request returns a maximum of 1000 uploaded parts by
     * default. You can restrict the number of parts returned by specifying the
     * MaxParts property on the ListPartsRequest. If your multipart upload
     * consists of more parts than allowed in the ListParts response, the
     * response returns a IsTruncated field with value true, and a
     * NextPartNumberMarker property. In subsequent ListParts request you can
     * include the PartNumberMarker property and set its value to the
     * NextPartNumberMarker property value from the previous response.
     *
     * @param request
     *            The ListPartsRequest object that specifies all the parameters
     *            of this operation.
     *
     * @return Returns a PartListing from SCS.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public PartListing listParts(ListPartsRequest request)
            throws SCSClientException, SCSServiceException;
//
//    /**
//     * Aborts a multipart upload. After a multipart upload is aborted, no
//     * additional parts can be uploaded using that upload ID. The storage
//     * consumed by any previously uploaded parts will be freed. However, if any
//     * part uploads are currently in progress, those part uploads may or may not
//     * succeed. As a result, it may be necessary to abort a given multipart
//     * upload multiple times in order to completely free all storage consumed by
//     * all parts.
//     *
//     * @param request
//     *            The AbortMultipartUploadRequest object that specifies all the
//     *            parameters of this operation.
//     *
//     * @throws SCSClientException
//     *             If any errors are encountered in the client while making the
//     *             request or handling the response.
//     * @throws SCSServiceException
//     *             If any errors occurred in SCS while processing the
//     *             request.
//     */
//    public void abortMultipartUpload(AbortMultipartUploadRequest request)
//            throws SCSClientException, SCSServiceException;
//
    /**
     * Completes a multipart upload by assembling previously uploaded parts.
     * <p>
     * You first upload all parts using the
     * {@link #uploadPart(UploadPartRequest)} method. After successfully
     * uploading all individual parts of an upload, you call this operation to
     * complete the upload. Upon receiving this request, SCS concatenates
     * all the parts in ascending order by part number to create a new object.
     * In the CompleteMultipartUpload request, you must provide the parts list.
     * For each part in the list, you provide the part number and the ETag
     * header value, returned after that part was uploaded.
     * <p>
     * Processing of a CompleteMultipartUpload request may take several minutes
     * to complete.
     *
     * @param request
     *            The CompleteMultipartUploadRequest object that specifies all
     *            the parameters of this operation.
     *
     * @return A CompleteMultipartUploadResult from S3 containing the ETag for
     *         the new object composed of the individual parts.
     *
     * @throws SCSClientException
     *             If any errors are encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in SCS while processing the
     *             request.
     */
    public ObjectMetadata completeMultipartUpload(CompleteMultipartUploadRequest request)
    		throws SCSClientException, SCSServiceException;
//    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request)
//            throws SCSClientException, SCSServiceException;
//
//    /**
//     * Lists in-progress multipart uploads. An in-progress multipart upload is a
//     * multipart upload that has been initiated, using the
//     * InitiateMultipartUpload request, but has not yet been completed or
//     * aborted.
//     * <p>
//     * This operation returns at most 1,000 multipart uploads in the response by
//     * default. The number of multipart uploads can be further limited using the
//     * MaxUploads property on the request parameter. If there are additional
//     * multipart uploads that satisfy the list criteria, the response will
//     * contain an IsTruncated property with the value set to true. To list the
//     * additional multipart uploads use the KeyMarker and UploadIdMarker
//     * properties on the request parameters.
//     *
//     * @param request
//     *            The ListMultipartUploadsRequest object that specifies all the
//     *            parameters of this operation.
//     *
//     * @return A MultipartUploadListing from SCS.
//     *
//     * @throws SCSClientException
//     *             If any errors are encountered in the client while making the
//     *             request or handling the response.
//     * @throws SCSServiceException
//     *             If any errors occurred in SCS while processing the
//     *             request.
//     */
//    public MultipartUploadListing listMultipartUploads(ListMultipartUploadsRequest request)
//            throws SCSClientException, SCSServiceException;

}