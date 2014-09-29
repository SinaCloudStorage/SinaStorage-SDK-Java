package com.sina.cloudstorage.services.scs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.sina.cloudstorage.HttpMethod;
import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.BucketInfo;
import com.sina.cloudstorage.services.scs.model.CannedAccessControlList;
import com.sina.cloudstorage.services.scs.model.CopyObjectRequest;
import com.sina.cloudstorage.services.scs.model.CreateBucketRequest;
import com.sina.cloudstorage.services.scs.model.DeleteBucketRequest;
import com.sina.cloudstorage.services.scs.model.DeleteObjectRequest;
import com.sina.cloudstorage.services.scs.model.GeneratePresignedUrlRequest;
import com.sina.cloudstorage.services.scs.model.GetBucketAclRequest;
import com.sina.cloudstorage.services.scs.model.GetObjectMetadataRequest;
import com.sina.cloudstorage.services.scs.model.GetObjectRequest;
import com.sina.cloudstorage.services.scs.model.Grant;
import com.sina.cloudstorage.services.scs.model.ListBucketsRequest;
import com.sina.cloudstorage.services.scs.model.ListObjectsRequest;
import com.sina.cloudstorage.services.scs.model.ObjectInfo;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.ObjectMetadata;
import com.sina.cloudstorage.services.scs.model.Permission;
import com.sina.cloudstorage.services.scs.model.PutObjectRequest;
import com.sina.cloudstorage.services.scs.model.PutObjectResult;
import com.sina.cloudstorage.services.scs.model.S3Object;
import com.sina.cloudstorage.services.scs.model.S3ObjectSummary;
import com.sina.cloudstorage.services.scs.model.SCSS3Exception;
import com.sina.cloudstorage.services.scs.model.SetBucketAclRequest;
import com.sina.cloudstorage.services.scs.model.UserIdGrantee;
import com.sina.cloudstorage.util.BinaryUtils;
import com.sina.cloudstorage.util.DateUtils;
import com.sina.cloudstorage.util.Md5Utils;

public class SCSClientTest extends TestCase {

	private SCSClient client;
	
	private String bucketName;//用来创建、删除等操作的bucket名称
	private String objectKey;//用来创建、删除等操作的object名
	private String localFileName;//用来创建、删除等操作的本地文件名

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bucketName = "junit-test"+System.currentTimeMillis();
		objectKey = "junit-test-object-key";
		localFileName = "/junit_test_file";
		client = new SCSClient();
	}
	
	@Override
	protected void tearDown(){
		if (client.doesBucketExist(bucketName)){
			try {
				ObjectListing objectListing= client.listObjects(bucketName);
				for(S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()){
					client.deleteObject(bucketName, s3ObjectSummary.getKey());
				}
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("empty bucket failed.Error is :"+e.getMessage());
			}
			//删除bucket
			client.deleteBucket(bucketName);
		}
	}

	public void testSCSClient() {
		Assert.assertNotNull("client is null!!", client);
	}

	public void testListObjectsString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
	
		ObjectListing objectListing = client.listObjects(bucketName);
		Assert.assertNotNull("list objects result is null", objectListing);
		Assert.assertTrue("list objectes result is empty", objectListing.getObjectSummaries().size() > 0);
	}

	public void testListObjectsStringString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传并拷贝测试文件
		 */
		try {
			client.putObject(bucketName, "a/object1.txt", new File(getClass().getResource(localFileName).toURI()));
			client.copyObject(bucketName, "a/object1.txt", bucketName, "b/object1.txt");
			client.copyObject(bucketName, "a/object1.txt", bucketName, "path/object1.txt");
			client.copyObject(bucketName, "a/object1.txt", bucketName, "a/object2.txt");
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectListing objectListing = client.listObjects(bucketName, "a");
		Assert.assertNotNull("list objects result is null", objectListing);
		Assert.assertTrue("list objectes result is empty", objectListing.getObjectSummaries().size() > 0);
	}

	public void testListObjectsListObjectsRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
	
		/*
		 * 上传并拷贝测试文件
		 */
		try {
			client.putObject(bucketName, "a/object1.txt", new File(getClass().getResource(localFileName).toURI()));
			client.copyObject(bucketName, "a/object1.txt", bucketName, "b/object1.txt");
			client.copyObject(bucketName, "a/object1.txt", bucketName, "path/object1.txt");
			client.copyObject(bucketName, "a/object1.txt", bucketName, "a/object2.txt");
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(2).withPrefix("a/object2.txt");
		ObjectListing objectListing = client.listObjects(listObjectsRequest);
		Assert.assertNotNull("list objects result is null", objectListing);
		Assert.assertTrue("list objectes result is empty", objectListing.getObjectSummaries().size() > 0);
		Assert.assertEquals("list result contentsQuantity is not 1", 1, objectListing.getContentsQuantity());
		Assert.assertEquals("list result objectSummary key name is not a/object2.txt", "a/object2.txt", 
				objectListing.getObjectSummaries().get(0).getKey());
	}

	public void testListNextBatchOfObjects() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		/*
		 * 上传并拷贝测试文件
		 */
		try {
			client.putObject(bucketName, "a/object1.txt", new File(getClass().getResource(localFileName).toURI()));
			client.copyObject(bucketName, "a/object1.txt", bucketName, "b/object1.txt");
			client.copyObject(bucketName, "a/object1.txt", bucketName, "a/object3.txt");
			client.copyObject(bucketName, "a/object1.txt", bucketName, "a/object2.txt");
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(2).withPrefix("a");
		
		ObjectListing objectListing = client.listObjects(listObjectsRequest);
		Assert.assertNotNull("list objects result is null", objectListing);
		
		objectListing = client.listNextBatchOfObjects(objectListing);
		Assert.assertTrue("list objectes result is empty", objectListing.getObjectSummaries().size() > 0);
		Assert.assertEquals("list result contentsQuantity is not 1", 1, objectListing.getContentsQuantity());
		Assert.assertTrue("list result objectSummary key name is not a/object2.txt", 
				objectListing.getObjectSummaries().get(0).getKey().startsWith("a/"));
	}

	public void testListBucketsListBucketsRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		List<Bucket> list = client.listBuckets(new ListBucketsRequest());
		Assert.assertNotNull("the result is null", list);
		for (Bucket bucket : list){
			Assert.assertNotNull("bucket:"+bucket+" name is null", bucket.getName());
			Assert.assertNotNull("bucket:"+bucket+" owner is null", bucket.getOwner());
			Assert.assertNotNull("bucket:"+bucket+" creationDate is null", bucket.getCreationDate());
		}
	}
	
	public void testListBuckets() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		List<Bucket> list = client.listBuckets();
		Assert.assertNotNull("the result is null", list);
		for (Bucket bucket : list){
			Assert.assertNotNull("bucket:"+bucket+" name is null", bucket.getName());
			Assert.assertNotNull("bucket:"+bucket+" owner is null", bucket.getOwner());
			Assert.assertNotNull("bucket:"+bucket+" creationDate is null", bucket.getCreationDate());
		}
	}

	public void testCreateBucketString() {
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
	}

	public void testCreateBucketCreateBucketRequest() {
		try{
			CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName).
					withCannedAcl(CannedAccessControlList.PublicReadWrite);
			client.createBucket(createBucketRequest);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
			Assert.assertNotNull("bucket:"+bucketName+" acl sets is null", bucketInfo.getAcl().getGrants());
			
			Grant expectedGrant = new Grant(new UserIdGrantee("GRPS000000ANONYMOUSE"), 
					Permission.Read, Permission.Write);
			Assert.assertTrue("bucket:"+bucketName+" acl dose not contains expected grant:"+expectedGrant, 
					bucketInfo.getAcl().getGrants().contains(expectedGrant));
			
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
	}

	public void testGetObjectAcl() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try{
			AccessControlList acl = client.getObjectAcl(bucketName, objectKey);
//			System.out.println(acl);
			Assert.assertNotNull("object:"+objectKey+" acl is null", acl);
			Assert.assertTrue("object:"+objectKey+" acl's grants is empty", acl.getGrants().size() > 0);
		} catch (SCSServiceException e){
			Assert.fail("failed to get object's acl.Error is :"+e.getMessage());
		}
	}

	public void testSetObjectAclStringStringAccessControlList() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try{
			AccessControlList acl = new AccessControlList();
			acl.grantPermissions(UserIdGrantee.ANONYMOUSE, Permission.Read, Permission.Write);
			acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read, Permission.WriteAcp, Permission.ReadAcp);
			client.setObjectAcl(bucketName, objectKey, acl);
			
			AccessControlList objectACL = client.getObjectAcl(bucketName, objectKey);
			Assert.assertNotNull("object:"+objectKey+" acl is null", objectACL);
			
			Grant expectedAnonymouseGrant = new Grant(UserIdGrantee.ANONYMOUSE, Permission.Read, Permission.Write);
			Grant expectedCanonicalGrant = new Grant(UserIdGrantee.CANONICAL, Permission.Read, Permission.WriteAcp, Permission.ReadAcp);
			Assert.assertTrue("object:"+objectKey+" acl dose not contains expected grant:"+expectedAnonymouseGrant, 
					objectACL.getGrants().contains(expectedAnonymouseGrant));
			Assert.assertTrue("object:"+objectKey+" acl dose not contains expected grant:"+expectedCanonicalGrant, 
					objectACL.getGrants().contains(expectedCanonicalGrant));
		} catch (SCSServiceException e){
			Assert.fail("failed to get object's acl.Error is :"+e.getMessage());
		}
	}

	public void testGetBucketInfo() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		BucketInfo bucketInfo = client.getBucketInfo(bucketName);
		Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
		Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
		Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		Assert.assertNotNull("bucket:"+bucketName+" acl sets is null", bucketInfo.getAcl().getGrants());
	}

	public void testGetObjectInfo() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try{
			ObjectInfo objectInfo = client.getObjectInfo(bucketName, objectKey);
			Assert.assertNotNull("object:"+objectKey+" is null", objectInfo);
			Assert.assertEquals("object:"+objectKey+" fileName is not equal "+objectInfo.getFileName(), objectKey, objectInfo.getFileName());
		} catch (SCSServiceException e){
			Assert.fail("failed to get object's info. Error is :"+e.getMessage());
		}
	}

	public void testGetBucketAclString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		AccessControlList acl = client.getBucketAcl(bucketName);
		
		Assert.assertNotNull("bucket:"+bucketName+" acl is null", acl);
		Assert.assertTrue("bucket:"+bucketName+" acl's grants is empty", acl.getGrants().size() > 0);
	}

	public void testGetBucketAclGetBucketAclRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		AccessControlList acl = client.getBucketAcl(new GetBucketAclRequest(bucketName));
		
		Assert.assertNotNull("bucket:"+bucketName+" acl is null", acl);
		Assert.assertTrue("bucket:"+bucketName+" acl's grants is empty", acl.getGrants().size() > 0);
	}

	public void testSetBucketAclStringAccessControlList() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		AccessControlList acl = new AccessControlList();
		acl.grantPermissions(UserIdGrantee.ANONYMOUSE, Permission.Read, Permission.Write);
		acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read, Permission.WriteAcp, Permission.ReadAcp);
		client.setBucketAcl(bucketName, acl);
		
		BucketInfo bucketInfo = client.getBucketInfo(bucketName);
		Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
		Assert.assertNotNull("bucket:"+bucketName+" acl sets is null", bucketInfo.getAcl().getGrants());
		
		Grant expectedAnonymouseGrant = new Grant(UserIdGrantee.ANONYMOUSE, Permission.Read, Permission.Write);
		Grant expectedCanonicalGrant = new Grant(UserIdGrantee.CANONICAL, Permission.Read, Permission.WriteAcp, Permission.ReadAcp);
		Assert.assertTrue("bucket:"+bucketName+" acl dose not contains expected grant:"+expectedAnonymouseGrant, 
				bucketInfo.getAcl().getGrants().contains(expectedAnonymouseGrant));
		Assert.assertTrue("bucket:"+bucketName+" acl dose not contains expected grant:"+expectedCanonicalGrant, 
				bucketInfo.getAcl().getGrants().contains(expectedCanonicalGrant));
	}

	public void testSetBucketAclSetBucketAclRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		AccessControlList acl = new AccessControlList();
		acl.grantPermissions(UserIdGrantee.ANONYMOUSE, Permission.Read);
		acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read, Permission.ReadAcp);
		SetBucketAclRequest setBucketAclRequest = new SetBucketAclRequest(bucketName, acl);
		client.setBucketAcl(setBucketAclRequest);
		
		BucketInfo bucketInfo = client.getBucketInfo(bucketName);
		Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
		Assert.assertNotNull("bucket:"+bucketName+" acl sets is null", bucketInfo.getAcl().getGrants());
		
		Grant expectedAnonymouseGrant = new Grant(UserIdGrantee.ANONYMOUSE, Permission.Read);
		Grant expectedCanonicalGrant = new Grant(UserIdGrantee.CANONICAL, Permission.Read, Permission.ReadAcp);
		Assert.assertTrue("bucket:"+bucketName+" acl dose not contains expected grant:"+expectedAnonymouseGrant, 
				bucketInfo.getAcl().getGrants().contains(expectedAnonymouseGrant));
		Assert.assertTrue("bucket:"+bucketName+" acl dose not contains expected grant:"+expectedCanonicalGrant, 
				bucketInfo.getAcl().getGrants().contains(expectedCanonicalGrant));
	}

	public void testGetObjectMetadataStringString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try {
			ObjectMetadata metadata = client.getObjectMetadata(bucketName, objectKey);
			Assert.assertTrue("object:"+objectKey+" have no user metadata.", metadata.getUserMetadata().size()>0);
		} catch (SCSServiceException e) {
			Assert.fail("get object:"+objectKey+" metadata failed. Error message is :"+e.getMessage());
		}
	}

	public void testSetObjectMetadata() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
	
		ObjectMetadata objectMetadata = new ObjectMetadata();
		Map<String,String> userMetadata = new HashMap<String,String>();
		userMetadata.put("LocalLocation", "My Home");
		userMetadata.put("WrittenBy", "Mr White");
		userMetadata.put("FileChecksum", "0x02661779");
		userMetadata.put("description", "blablablablablablablablablablablablablablablabla");
		objectMetadata.setUserMetadata(userMetadata);
		
		try {
			client.setObjectMetadata(bucketName, objectKey, objectMetadata);
		} catch (SCSServiceException e) {
			Assert.fail("set object:"+objectKey+" metadata failed. Error message is :"+e.getMessage());
		}
	
		/*
		 * 获取object info，判断请求头部分内容是否正确
		 */
		ObjectInfo objectInfo = client.getObjectInfo(bucketName, objectKey);
		Assert.assertNotNull("object:"+objectKey+" is null", objectInfo);
		for (Entry<String,String> entry : userMetadata.entrySet()){
			Assert.assertTrue("object:"+objectKey+"'s fileMeta does not contains key "+Headers.S3_USER_METADATA_PREFIX+entry.getKey(),
					objectInfo.getFileMeta().containsKey(Headers.S3_USER_METADATA_PREFIX+entry.getKey().toLowerCase()));
			Assert.assertTrue("object:"+objectKey+"'s fileMeta entry key "+entry.getKey()+" does not equals "+entry.getValue(),
					objectInfo.getFileMeta().get(Headers.S3_USER_METADATA_PREFIX+entry.getKey().toLowerCase()).equals(entry.getValue()));
		}
	}

	public void testGetObjectMetadataGetObjectMetadataRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try {
			GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(bucketName, objectKey);
			ObjectMetadata metadata = client.getObjectMetadata(getObjectMetadataRequest);
			Assert.assertTrue("object:"+objectKey+" have no user metadata.", metadata.getUserMetadata().size()>0);
		} catch (SCSServiceException e) {
			Assert.fail("get object:"+objectKey+" metadata failed. Error message is :"+e.getMessage());
		}
	}

	public void testGetObjectStringString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try {
			S3Object s3Object = client.getObject(bucketName, objectKey);
			Assert.assertNotNull("get object:"+objectKey+" result is null", s3Object);
			
			InputStream in = s3Object.getObjectContent();
			byte[] buf = new byte[1024];
			OutputStream out = null;
			try {
				out = new FileOutputStream(new File(s3Object.getKey()));
				int count;
				while( (count = in.read(buf)) != -1)
				{
				   if( Thread.interrupted() )
				   {
				       throw new InterruptedException();
				   }
				   out.write(buf, 0, count);
				}
			} catch (Exception e) {
				Assert.fail("get object:"+objectKey+" failed.Error is:"+e.getMessage());
			}finally{
				try {
					out.close();
				} catch (IOException e) {
					Assert.fail("get object:"+objectKey+" failed.Error is:"+e.getMessage());
				}
				try {
					in.close();
				} catch (IOException e) {
					Assert.fail("get object:"+objectKey+" failed.Error is:"+e.getMessage());
				}
			}
			
			File file = new File(s3Object.getKey());
			Assert.assertTrue("downloaded file does not exists", file.exists());
		} catch (SCSServiceException e) {
			Assert.fail("get object:"+objectKey+" failed. Error message is :"+e.getMessage());
		}
	}

	public void testDoesBucketExist() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
	}

	public void testGetObjectGetObjectRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try {
			GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectKey);
			S3Object s3Object = client.getObject(getObjectRequest);
			Assert.assertNotNull("get object:"+objectKey+" result is null", s3Object);
			
			InputStream in = s3Object.getObjectContent();
			byte[] buf = new byte[1024];
			OutputStream out = null;
			try {
				out = new FileOutputStream(new File(s3Object.getKey()));
				int count;
				while( (count = in.read(buf)) != -1)
				{
				   if( Thread.interrupted() )
				   {
				       throw new InterruptedException();
				   }
				   out.write(buf, 0, count);
				}
			} catch (Exception e) {
				Assert.fail("get object:"+objectKey+" failed.Error is:"+e.getMessage());
			}finally{
				try {
					out.close();
				} catch (IOException e) {
					Assert.fail("get object:"+objectKey+" failed.Error is:"+e.getMessage());
				}
				try {
					in.close();
				} catch (IOException e) {
					Assert.fail("get object:"+objectKey+" failed.Error is:"+e.getMessage());
				}
			}
			
			File file = new File(s3Object.getKey());
			Assert.assertTrue("downloaded file does not exists", file.exists());
		} catch (SCSServiceException e) {
			Assert.fail("get object:"+objectKey+" failed. Error message is :"+e.getMessage());
		}
		
	}

	public void testGetObjectGetObjectRequestFile() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		try {
			GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectKey);
			ObjectMetadata objectMetadata = client.getObject(getObjectRequest, new File("downloadedFile.txt"));
			Assert.assertNotNull("get object:"+objectKey+" objectMetadata is null", objectMetadata);
			
			File file = new File("downloadedFile.txt");
			Assert.assertTrue("downloaded file does not exists", file.exists());
			
		} catch (SCSServiceException e) {
			Assert.fail("get object:"+objectKey+" failed. Error message is :"+e.getMessage());
		}
	}

	public void testDeleteBucketString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));

		try {
			client.deleteBucket(bucketName);
		} catch (SCSServiceException e) {
			Assert.fail("delete bucket failed.Error is :"+e.getMessage());
		}
		
		Assert.assertFalse("bucket:"+bucketName+" is still exist", client.doesBucketExist(bucketName));
	}

	public void testDeleteBucketDeleteBucketRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		try {
			client.deleteBucket(new DeleteBucketRequest(bucketName));
		} catch (SCSServiceException e) {
			Assert.fail("delete bucket failed.Error is :"+e.getMessage());
		}
		
		Assert.assertFalse("bucket:"+bucketName+" is still exist", client.doesBucketExist(bucketName));
	
	}

	public void testPutObjectStringStringFile() {

		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		URI localFileURI = null;
		
		try {
			localFileURI = getClass().getResource(localFileName).toURI();
		} catch (URISyntaxException e) {
			Assert.fail("failed to load file from local path");
		}
		
		File localFile = new File(localFileURI);
		Assert.assertTrue("local file does not exist", localFile.exists());
		
		PutObjectResult result = client.putObject(bucketName, objectKey, localFile);
		Assert.assertNotNull("PutObjectResult is null", result);
		Assert.assertNotNull("PutObjectResult's contentMd5 is null", result.getContentMd5());
		
		/*
		 * 对比上传结果与本地文件的MD5
		 */
		try {
			FileInputStream fileInputStream = new FileInputStream(localFile);
			byte[] md5Hash = Md5Utils.computeMD5Hash(fileInputStream);
			String localFileMD5 = BinaryUtils.toBase64(md5Hash);
			Assert.assertEquals("PutObjectResult's contentMd5 is not equals to localFileMD5", localFileMD5, result.getContentMd5());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("calculate local file MD5 is failed");
		}
	}

	public void testPutObjectStringStringFileMapOfStringString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		URI localFileURI = null;
		try {
			localFileURI = getClass().getResource(localFileName).toURI();
		} catch (URISyntaxException e) {
			Assert.fail("failed to load file from local path");
		}
		
		File localFile = new File(localFileURI);
		Assert.assertTrue("local file does not exist", localFile.exists());
		
		//请求头
		Map<String,String> requestHeader = new HashMap<String,String>();
		requestHeader.put("x-amz-meta-UploadLocation", "My Home");
		requestHeader.put("X-amz-meta-ReviewedBy", "test@test.net");
		requestHeader.put("X-amz-meta-FileChecksum", "0x02661779");
		requestHeader.put("X-amz-meta-CheckSumAlgorithm", "crc32");
		PutObjectResult result = client.putObject(bucketName, objectKey, localFile, requestHeader);
		
		Assert.assertNotNull("PutObjectResult is null", result);
		Assert.assertNotNull("PutObjectResult's contentMd5 is null", result.getContentMd5());
		
		/*
		 * 对比上传结果与本地文件的MD5
		 */
		try {
			FileInputStream fileInputStream = new FileInputStream(localFile);
			byte[] md5Hash = Md5Utils.computeMD5Hash(fileInputStream);
			String localFileMD5 = BinaryUtils.toBase64(md5Hash);
			Assert.assertEquals("PutObjectResult's contentMd5 is not equals to localFileMD5", localFileMD5, result.getContentMd5());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("calculate local file MD5 is failed");
		}
		
		/*
		 * 获取object info，判断请求头部分内容是否正确
		 */
		ObjectInfo objectInfo = client.getObjectInfo(bucketName, objectKey);
		Assert.assertNotNull("object:"+objectKey+" is null", objectInfo);
		
		for (Entry<String,String> entry : requestHeader.entrySet()){
			Assert.assertTrue("object:"+objectKey+"'s fileMeta does not contains key "+entry.getKey(),
					objectInfo.getFileMeta().containsKey(entry.getKey().toLowerCase()));
			Assert.assertTrue("object:"+objectKey+"'s fileMeta entry key "+entry.getKey()+" does not equals "+entry.getValue(),
					objectInfo.getFileMeta().get(entry.getKey().toLowerCase()).equals(entry.getValue()));
		}
		
	}

	public void testPutObjectRelax() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		/*
		 * 获取文件sha1
		 */
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("get object:"+objectKey+" info failed");
		}
		Assert.assertNotNull("object:"+objectKey+" info is null", objectInfo);
		
		client.putObjectRelax(bucketName, objectKey+"-relax", objectInfo.getContentSHA1(), objectInfo.getSize());
		
		ObjectInfo objectRelaxInfo = null;
		try {
			objectRelaxInfo = client.getObjectInfo(bucketName, objectKey+"-relax");
		} catch (SCSClientException e) {
			Assert.fail("put object:"+objectKey+"-relax"+" failed");
		}
		Assert.assertNotNull("object:"+objectKey+"-relax"+" info is null", objectRelaxInfo);
		Assert.assertEquals("object:"+objectKey+"-relax"+" contentMd5 is wrong", objectInfo.getContentMD5(), objectRelaxInfo.getContentMD5());
	}

	public void testPutObjectStringStringInputStreamObjectMetadata() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		URI localFileURI = null;
		try {
			localFileURI = getClass().getResource(localFileName).toURI();
		} catch (URISyntaxException e) {
			Assert.fail("failed to load file from local path");
		}
		File localFile = new File(localFileURI);
		Assert.assertTrue("local file does not exist", localFile.exists());
		
		InputStream inputStream = null;
		try {
			inputStream = getClass().getResourceAsStream(localFileName);
			Assert.assertNotNull("inputStream is null"+inputStream);
			
			ObjectMetadata metadata = new ObjectMetadata();
			Date expirationTime = new Date(System.currentTimeMillis() + 1000 * 60 * 15);//过期时间，15分钟后过期
			metadata.setHttpExpiresDate(expirationTime);
			Map<String,String> userMetadata = new HashMap<String,String>();
			userMetadata.put("LocalLocation", "My Home");
			userMetadata.put("WrittenBy", "Mr White");
			userMetadata.put("FileChecksum", "0x02661779");
			userMetadata.put("description", "blablablablablablablablablablablablablablablabla");
			metadata.setUserMetadata(userMetadata);
			metadata.setContentLength(localFile.length());
			
			PutObjectResult result = client.putObject(bucketName, objectKey, inputStream, metadata);
			Assert.assertNotNull("PutObjectResult is null", result);
			Assert.assertNotNull("PutObjectResult's contentMd5 is null", result.getContentMd5());
			
			/*
			 * 对比上传结果与本地文件的MD5
			 */
			try {
				FileInputStream fileInputStream = new FileInputStream(localFile);
				byte[] md5Hash = Md5Utils.computeMD5Hash(fileInputStream);
				String localFileMD5 = BinaryUtils.toBase64(md5Hash);
				Assert.assertEquals("PutObjectResult's contentMd5 is not equals to localFileMD5", localFileMD5, result.getContentMd5());
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("calculate local file MD5 is failed");
			}
			
			/*
			 * 获取object info，判断请求头部分内容是否正确
			 */
			ObjectInfo objectInfo = client.getObjectInfo(bucketName, objectKey);
			Assert.assertNotNull("object:"+objectKey+" is null", objectInfo);
			for (Entry<String,String> entry : userMetadata.entrySet()){
				Assert.assertTrue("object:"+objectKey+"'s fileMeta does not contains key "+Headers.S3_USER_METADATA_PREFIX+entry.getKey(),
						objectInfo.getFileMeta().containsKey(Headers.S3_USER_METADATA_PREFIX+entry.getKey().toLowerCase()));
				Assert.assertTrue("object:"+objectKey+"'s fileMeta entry key "+entry.getKey()+" does not equals "+entry.getValue(),
						objectInfo.getFileMeta().get(Headers.S3_USER_METADATA_PREFIX+entry.getKey().toLowerCase()).equals(entry.getValue()));
			}
			//判断文件超时时间是否正确
			Date objectExpiresDate = null;
			String objectExpiresDateStr = objectInfo.getFileMeta().get("Expires");
			Assert.assertNotNull("object expires date is null", objectExpiresDateStr);
			Assert.assertNotSame("object expires date is empty", "", objectExpiresDateStr);
			DateUtils du = new DateUtils();
			try {
				objectExpiresDate = du.parseRfc822Date(objectExpiresDateStr);
			} catch (ParseException e) {
				Assert.fail("failed to parse object expires date");
			}
			
			Assert.assertNotNull("object expires date is null", objectExpiresDate);
			
			/*
			 * 判断过期时间和之前设置的是否一致
			 * 忽略毫秒
			 */
			Calendar expirationTimeCal = Calendar.getInstance();
			expirationTimeCal.setTime(expirationTime);
			expirationTimeCal.set(Calendar.MILLISECOND, 0);
			Calendar objectExpiresDateCal = Calendar.getInstance();
			objectExpiresDateCal.setTime(objectExpiresDate);
			objectExpiresDateCal.set(Calendar.MILLISECOND, 0);
			Assert.assertEquals("object expires date is not equals the date what setting before", expirationTimeCal, objectExpiresDateCal);
			
		} catch (Exception e) {
			Assert.fail("failed to load inputStream from local path");
		}finally{
			if (inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void testPutObjectPutObjectRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		URI localFileURI = null;
		try {
			localFileURI = getClass().getResource(localFileName).toURI();
		} catch (URISyntaxException e) {
			Assert.fail("failed to load file from local path");
		}
		File localFile = new File(localFileURI);
		Assert.assertTrue("local file does not exist", localFile.exists());
		
		InputStream inputStream = null;
		try {
			inputStream = getClass().getResourceAsStream(localFileName);
			Assert.assertNotNull("inputStream is null"+inputStream);
			
			ObjectMetadata metadata = new ObjectMetadata();
			Date expirationTime = new Date(System.currentTimeMillis() + 1000 * 60 * 15);//过期时间，15分钟后过期
			metadata.setHttpExpiresDate(expirationTime);
			Map<String,String> userMetadata = new HashMap<String,String>();
			userMetadata.put("LocalLocation", "My Home");
			userMetadata.put("WrittenBy", "Mr White");
			userMetadata.put("FileChecksum", "0x02661779");
			userMetadata.put("description", "blablablablablablablablablablablablablablablabla");
			metadata.setUserMetadata(userMetadata);
			metadata.setContentLength(localFile.length());
			
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
			PutObjectResult result = client.putObject(putObjectRequest);
			Assert.assertNotNull("PutObjectResult is null", result);
			Assert.assertNotNull("PutObjectResult's contentMd5 is null", result.getContentMd5());
			
			/*
			 * 对比上传结果与本地文件的MD5
			 */
			try {
				FileInputStream fileInputStream = new FileInputStream(localFile);
				byte[] md5Hash = Md5Utils.computeMD5Hash(fileInputStream);
				String localFileMD5 = BinaryUtils.toBase64(md5Hash);
				Assert.assertEquals("PutObjectResult's contentMd5 is not equals to localFileMD5", localFileMD5, result.getContentMd5());
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("calculate local file MD5 is failed");
			}
			
			/*
			 * 获取object info，判断请求头部分内容是否正确
			 */
			ObjectInfo objectInfo = client.getObjectInfo(bucketName, objectKey);
			Assert.assertNotNull("object:"+objectKey+" is null", objectInfo);
			for (Entry<String,String> entry : userMetadata.entrySet()){
				Assert.assertTrue("object:"+objectKey+"'s fileMeta does not contains key "+Headers.S3_USER_METADATA_PREFIX+entry.getKey(),
						objectInfo.getFileMeta().containsKey(Headers.S3_USER_METADATA_PREFIX+entry.getKey().toLowerCase()));
				Assert.assertTrue("object:"+objectKey+"'s fileMeta entry key "+entry.getKey()+" does not equals "+entry.getValue(),
						objectInfo.getFileMeta().get(Headers.S3_USER_METADATA_PREFIX+entry.getKey().toLowerCase()).equals(entry.getValue()));
			}
			//判断文件超时时间是否正确
			Date objectExpiresDate = null;
			String objectExpiresDateStr = objectInfo.getFileMeta().get("Expires");
			Assert.assertNotNull("object expires date is null", objectExpiresDateStr);
			Assert.assertNotSame("object expires date is empty", "", objectExpiresDateStr);
			DateUtils du = new DateUtils();
			try {
				objectExpiresDate = du.parseRfc822Date(objectExpiresDateStr);
			} catch (ParseException e) {
				Assert.fail("failed to parse object expires date");
			}
			
			Assert.assertNotNull("object expires date is null", objectExpiresDate);
			
			/*
			 * 判断过期时间和之前设置的是否一致
			 * 忽略毫秒
			 */
			Calendar expirationTimeCal = Calendar.getInstance();
			expirationTimeCal.setTime(expirationTime);
			expirationTimeCal.set(Calendar.MILLISECOND, 0);
			Calendar objectExpiresDateCal = Calendar.getInstance();
			objectExpiresDateCal.setTime(objectExpiresDate);
			objectExpiresDateCal.set(Calendar.MILLISECOND, 0);
			Assert.assertEquals("object expires date is not equals the date what setting before", expirationTimeCal, objectExpiresDateCal);
			
		} catch (Exception e) {
			Assert.fail("failed to load inputStream from local path");
		}finally{
			if (inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void testCopyObjectStringStringStringString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("get object:"+objectKey+" info failed");
		}
		Assert.assertNotNull("object:"+objectKey+" info is null", objectInfo);
		
		client.copyObject(bucketName, objectKey, bucketName, objectKey+"-copy");
		
		ObjectInfo objectCopyInfo = null;
		try {
			objectCopyInfo = client.getObjectInfo(bucketName, objectKey+"-copy");
		} catch (SCSClientException e) {
			Assert.fail("put object:"+objectKey+"-copy"+" failed");
		}
		Assert.assertNotNull("object:"+objectKey+"-copy"+" info is null", objectCopyInfo);
		Assert.assertEquals("object:"+objectKey+"-copy"+" contentMd5 is wrong", objectInfo.getContentMD5(), objectCopyInfo.getContentMD5());
		
	}

	public void testCopyObjectCopyObjectRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("get object:"+objectKey+" info failed");
		}
		Assert.assertNotNull("object:"+objectKey+" info is null", objectInfo);
		CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, objectKey, bucketName, objectKey+"-copy");
		client.copyObject(copyObjectRequest);
		
		ObjectInfo objectCopyInfo = null;
		try {
			objectCopyInfo = client.getObjectInfo(bucketName, objectKey+"-copy");
		} catch (SCSClientException e) {
			Assert.fail("put object:"+objectKey+"-copy"+" failed");
		}
		Assert.assertNotNull("object:"+objectKey+"-copy"+" info is null", objectCopyInfo);
		Assert.assertEquals("object:"+objectKey+"-copy"+" contentMd5 is wrong", objectInfo.getContentMD5(), objectCopyInfo.getContentMD5());
	
	}

	public void testDeleteObjectStringString() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		
		try {
			client.deleteObject(bucketName, objectKey);
		} catch (SCSServiceException e){
			Assert.assertEquals("status code is not 404", 404, e.getStatusCode());
			Assert.assertTrue(e.getMessage().contains("\"NoSuchBucket\""));
			if (e.getStatusCode() == 404 && e.getMessage().contains("\"NoSuchBucket\"")){
				Assert.fail("no such bucket");
			}
		}
		
		objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSServiceException e) {
			Assert.assertEquals("status code is not 404 after delete object", 404, e.getStatusCode());
		}
		Assert.assertNull("object:"+objectKey+" still exist",objectInfo);
	}

	public void testDeleteObjectDeleteObjectRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		
		try {
			DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, objectKey);
			client.deleteObject(deleteObjectRequest);
		} catch (SCSServiceException e){
			Assert.assertEquals("status code is not 404", 404, e.getStatusCode());
			Assert.assertTrue(e.getMessage().contains("\"NoSuchBucket\""));
			if (e.getStatusCode() == 404 && e.getMessage().contains("\"NoSuchBucket\"")){
				Assert.fail("no such bucket");
			}
		}
		
		objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSServiceException e) {
			Assert.assertEquals("status code is not 404 after delete object", 404, e.getStatusCode());
		}
		Assert.assertNull("object:"+objectKey+" still exist",objectInfo);
	}
	
	/**
	 * 清空bucket里的所有内容
	 */
	public void testEmptyBucket(){
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		try {
			ObjectListing objectListing= client.listObjects(bucketName);
			for(S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()){
				client.deleteObject(bucketName, s3ObjectSummary.getKey());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("empty bucket failed.Error is :"+e.getMessage());
		}
	}

	public void testGeneratePresignedUrlStringStringDateBoolean() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		
		Date expiration = new Date();
        long epochMillis = expiration.getTime();
        epochMillis += 60*5*1000;
        expiration = new Date(epochMillis);
        URL url = client.generatePresignedUrl(bucketName, objectKey, expiration, false);
		URLConnection urlConnection = null;
		try {
			urlConnection = url.openConnection();
			int contentLength = urlConnection.getContentLength();
			if (contentLength != 587){
				System.out.println(urlConnection.getContent());
				
				Assert.assertEquals("the generated url is invalid", 587, contentLength);
			}
		} catch (IOException e) {
			Assert.fail("the generated url is invalid. Error is :"+e.getClass()+" Error message is :"+e.getMessage());
		}
	}

	public void testGeneratePresignedUrlStringStringDateHttpMethodBoolean() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		
		Date expiration = new Date();
        long epochMillis = expiration.getTime();
        epochMillis += 60*5*1000;
        expiration = new Date(epochMillis);
        URL url = client.generatePresignedUrl(bucketName, objectKey, expiration, HttpMethod.GET, false);
		URLConnection urlConnection = null;
		try {
			urlConnection = url.openConnection();
			int contentLength = urlConnection.getContentLength();
			if (contentLength != 587){
				System.out.println(urlConnection.getContent());
				
				Assert.assertEquals("the generated url is invalid", 587, contentLength);
			}
		} catch (IOException e) {
			Assert.fail("the generated url is invalid. Error is :"+e.getClass()+" Error message is :"+e.getMessage());
		}
	}

	public void testGeneratePresignedUrlGeneratePresignedUrlRequest() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		
		Date expiration = new Date();
        long epochMillis = expiration.getTime();
        epochMillis += 60*5*1000;
        expiration = new Date(epochMillis);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
		URLConnection urlConnection = null;
		try {
			urlConnection = url.openConnection();
			int contentLength = urlConnection.getContentLength();
			if (contentLength != 587){
				System.out.println(urlConnection.getContent());
				
				Assert.assertEquals("the generated url is invalid", 587, contentLength);
			}
		} catch (IOException e) {
			Assert.fail("the generated url is invalid. Error is :"+e.getClass()+" Error message is :"+e.getMessage());
		}
	}

//	public void testCompleteMultipartUpload() {
//		fail("Not yet implemented");
//	}
//
//	public void testInitiateMultipartUploadStringString() {
//		fail("Not yet implemented");
//	}
//
//	public void testInitiateMultipartUploadInitiateMultipartUploadRequest() {
//		fail("Not yet implemented");
//	}
//
//	public void testListParts() {
//		fail("Not yet implemented");
//	}
//
//	public void testUploadPart() {
//		fail("Not yet implemented");
//	}

	public void testGetResourceUrl() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		/*
		 * 先设置bucket的acl为 匿名用户组-读 权限
		 */
		AccessControlList acl = new AccessControlList();
		acl.grantPermissions(UserIdGrantee.ANONYMOUSE, Permission.Read);
		client.setBucketAcl(bucketName, acl);
		/*
		 * 判断待生成url的object是否存在
		 */
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		/*
		 * 生成url
		 */
		String urlStr = client.getResourceUrl(bucketName, objectKey);
		Assert.assertNotNull("url is null", urlStr);
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e1) {
			Assert.fail("failed to parse urlStr to URL");
		}
		
		/*
		 * 获取url内容
		 */
		URLConnection urlConnection = null;
		try {
			urlConnection = url.openConnection();
			int contentLength = urlConnection.getContentLength();
			if (contentLength != 587){
				System.out.println(urlConnection.getContent());
				Assert.assertEquals("the generated url is invalid", 587, contentLength);
			}
		} catch (IOException e) {
			Assert.fail("the generated url is invalid. Error is :"+e.getClass()+" Error message is :"+e.getMessage());
		}
	}

	public void testGetUrl() {
		/*
		 * 创建bucket
		 */
		try{
			client.createBucket(bucketName);
			
			BucketInfo bucketInfo = client.getBucketInfo(bucketName);
			Assert.assertNotNull("bucket:"+bucketName+" is null", bucketInfo);
			Assert.assertNotNull("bucket:"+bucketName+" owner is null", bucketInfo.getOwner());
			Assert.assertNotNull("bucket:"+bucketName+" creationDate is null", bucketInfo.getLastModified());
		}catch(SCSS3Exception err){
			//bucket 已经存在
			Assert.assertEquals("status code is not 409.Error is :"+err.getMessage(), 409, err.getStatusCode());
			Assert.assertTrue(err.getMessage().contains("\"BucketAlreadyExists\""));
		}
		
		Assert.assertTrue("bucket:"+bucketName+" does not exist", client.doesBucketExist(bucketName));
		
		/*
		 * 上传测试文件
		 */
		try {
			client.putObject(bucketName, objectKey, new File(getClass().getResource(localFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("put object failed.Error is :"+e.getMessage());
		}
		
		/*
		 * 先设置bucket的acl为 匿名用户组-读 权限
		 */
		AccessControlList acl = new AccessControlList();
		acl.grantPermissions(UserIdGrantee.ANONYMOUSE, Permission.Read);
		client.setBucketAcl(bucketName, acl);
		/*
		 * 判断待生成url的object是否存在
		 */
		ObjectInfo objectInfo = null;
		try {
			objectInfo = client.getObjectInfo(bucketName, objectKey);
		} catch (SCSClientException e) {
			Assert.fail("object:"+objectKey+" does not exist");
		}
		Assert.assertNotNull("object:"+objectKey+" does not exist", objectInfo);
		/*
		 * 生成url
		 */
		URL url = client.getUrl(bucketName, objectKey);
		Assert.assertNotNull("url is null", url);
		/*
		 * 获取url内容
		 */
		URLConnection urlConnection = null;
		try {
			urlConnection = url.openConnection();
			int contentLength = urlConnection.getContentLength();
			if (contentLength != 587){
				System.out.println(urlConnection.getContent());
				
				Assert.assertEquals("the generated url is invalid", 587, contentLength);
			}
		} catch (IOException e) {
			Assert.fail("the generated url is invalid. Error is :"+e.getClass()+" Error message is :"+e.getMessage());
		}
	}

}
