package com.sina.cloudstorage.cli;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sina.cloudstorage.ClientConfiguration;
import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.SDKGlobalConfiguration;
import com.sina.cloudstorage.auth.AWSCredentials;
import com.sina.cloudstorage.event.ProgressEvent;
import com.sina.cloudstorage.event.ProgressListener;
import com.sina.cloudstorage.services.scs.S3ClientOptions;
import com.sina.cloudstorage.services.scs.SCS;
import com.sina.cloudstorage.services.scs.SCSClient;
import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.CannedAccessControlList;
import com.sina.cloudstorage.services.scs.model.CompleteMultipartUploadRequest;
import com.sina.cloudstorage.services.scs.model.CopyObjectRequest;
import com.sina.cloudstorage.services.scs.model.CreateBucketRequest;
import com.sina.cloudstorage.services.scs.model.GeneratePresignedUrlRequest;
import com.sina.cloudstorage.services.scs.model.GetObjectRequest;
import com.sina.cloudstorage.services.scs.model.InitiateMultipartUploadResult;
import com.sina.cloudstorage.services.scs.model.ListObjectsRequest;
import com.sina.cloudstorage.services.scs.model.ListPartsRequest;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.ObjectMetadata;
import com.sina.cloudstorage.services.scs.model.PartETag;
import com.sina.cloudstorage.services.scs.model.PartListing;
import com.sina.cloudstorage.services.scs.model.PutObjectRequest;
import com.sina.cloudstorage.services.scs.model.PutObjectResult;
import com.sina.cloudstorage.services.scs.model.S3Object;
import com.sina.cloudstorage.services.scs.model.UploadPartRequest;
import com.sina.cloudstorage.services.scs.transfer.TransferManager;
import com.sina.cloudstorage.services.scs.transfer.Upload;
import com.sina.cloudstorage.services.scs.transfer.internal.UploadPartRequestFactory;


public class Client {
	public SCS conn;
	
	public Client(AWSCredentials credentials, ClientConfiguration clientConfiguration, S3ClientOptions clientOptions){
		conn = new SCSClient(credentials, clientConfiguration);
		if (clientOptions!=null)
			conn.setS3ClientOptions(clientOptions);
	}
	
	/* Service操作 */
	
	/**
	 * 获取所有bucket
	 */
	public List<Bucket> getAllBuckets(){
		List<Bucket> list = conn.listBuckets();
		return list;
	}
	
	/* Bucket操作 */
	
	/**
	 * 创建bucket
	 */
	public Bucket createBucket(String bucketName, String cannedAcl){
		CreateBucketRequest cbr = new CreateBucketRequest(bucketName);

		//private (default), public-read, public-read-write, authenticated-read
		if (cannedAcl!=null){
			if ("private".equals(cannedAcl)){
				cbr.setCannedAcl(CannedAccessControlList.Private);
			}else if ("public-read".equals(cannedAcl)){
				cbr.setCannedAcl(CannedAccessControlList.PublicRead);
			}else if ("public-read-write".equals(cannedAcl)){
				cbr.setCannedAcl(CannedAccessControlList.PublicReadWrite);
			}else if ("authenticated-read".equals(cannedAcl)){
				cbr.setCannedAcl(CannedAccessControlList.AuthenticatedRead);
			}else {
				cbr.setCannedAcl(CannedAccessControlList.valueOf(cannedAcl));
			} 
		}
		
		return conn.createBucket(cbr);
	}
	
	
	/**
	 * 删除bucket
	 */
	public void deleteBucket(String bucketName){
		conn.deleteBucket(bucketName);
	}
	
	/**
	 * 获取bucket ACL
	 */
	public AccessControlList getBucketAcl(String bucketName){
		AccessControlList acl = conn.getBucketAcl("buckte11test");
		return acl;
	}
	
	/**
	 * 设置bucket acl
	 */
	public void putBucketAcl(String bucketName, AccessControlList acl){
		conn.setBucketAcl(bucketName, acl);
	}
	
	/**
	 * 列bucket中所有文件
	 * <bucket>           : Bucket to list
	     [prefix]           : Prefix for results set
	     [marker]           : Where in results set to start listing
	     [delimiter]        : Delimiter for rolling up results set
	     [maxkeys]          : Maximum number of keys to return in results set
	 */
	public ObjectListing listObjects(String bucketName, String prefix, String marker, String delimiter, Integer maxkeys){
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName, prefix, marker, delimiter, maxkeys);
		ObjectListing objectListing = conn.listObjects(listObjectsRequest);
		return objectListing;
	}
	
	/* Object操作 */
	/**
	 * 获取object metadata
	 */
	public ObjectMetadata getObjectMetadata(String bucketName, String key){
		ObjectMetadata metadata = conn.getObjectMetadata(bucketName, key);
//		System.out.println(metadata.getUserMetadata());
//		System.out.println(metadata.getETag());
//		System.out.println(metadata.getLastModified());
//		System.out.println(metadata.getRawMetadata());
		return metadata;
	}
	
	/**
	 * 下载object 
	 * more detail:http://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html
  	 *	//		断点续传
	 *	//		GetObjectRequest rangeObjectRequest = new GetObjectRequest("test11", "/aaa/bbb.txt");
	 *	//		rangeObjectRequest.setRange(0, 10); // retrieve 1st 10 bytes.
	 *	//		S3Object objectPortion = conn.getObject(rangeObjectRequest);
	 *	//		
	 *	//		InputStream objectData = objectPortion.getObjectContent();
	 *	//		//Process the objectData stream.
	 *	//		objectData.close();
	 */
	public void getObject(GetObjectRequest getObjectRequest, File opFile){
		SDKGlobalConfiguration.setGlobalTimeOffset(-60*5);//超时时间5分钟以后
		S3Object s3Obj = conn.getObject(getObjectRequest);
		InputStream in = s3Obj.getObjectContent();
		byte[] buf = new byte[1024];
		OutputStream out = null;
		try {
			out = new FileOutputStream(opFile);
			int count;
			while( (count = in.read(buf)) != -1)
			{
			   if( Thread.interrupted() )
			   {
			       throw new InterruptedException();
			   }
			   out.write(buf, 0, count);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			SDKGlobalConfiguration.setGlobalTimeOffset(0);
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 上传文件
	 */
	public PutObjectResult putObject(PutObjectRequest por){
		PutObjectResult putObjectResult = conn.putObject(por);
		return putObjectResult;
	}
	
	/**
	 * 拷贝object
	 */
	public void copyObject(CopyObjectRequest copyObjectRequest){
		conn.copyObject(copyObjectRequest);
	}
	
//	/**
//	 * 设置object metadata
//	 */
//	@SuppressWarnings("serial")
//	public void putObjectMeta(){
//		ObjectMetadata objectMetadata = new ObjectMetadata();
//		objectMetadata.setUserMetadata(new HashMap<String,String>(){{
//					put("aaa","1111");
//					put("bbb","222");
//					put("ccc","3333");
//					put("asdfdsaf","vvvvvv");
//		}});
//		conn.setObjectMetadata("bucket-name", "object-key", objectMetadata);
//	}
	
	/**
	 * 删除Object
	 */
	public void deleteObject(String bucketName, String path){
		conn.deleteObject(bucketName, path);
	}
	
	/**
	 * 获取object acl
	 */
	public AccessControlList getObjectAcl(String bucketName, String path){
		AccessControlList acl = conn.getObjectAcl(bucketName, path);
		return acl;
	}
	
	/**
	 * 设置object acl
	 */
	public void putObjectAcl(String bucketName, String key, AccessControlList acl){
		conn.setObjectAcl(bucketName, key, acl);
	}
	
	/* 分片上传文件 */
	
	/* TransferManager */
	public void putObjectByTransferManager(){
		TransferManager tf = new TransferManager(conn);
		Upload myUpload = tf.upload("bucket-name", "object-key", new File("local-file-path"));
		
		// You can poll your transfer's status to check its progress
		if (myUpload.isDone() == false) {
			System.out.println("Transfer: " + myUpload.getDescription());
			System.out.println("  - State: " + myUpload.getState());
			System.out.println("  - Progress: "
					+ myUpload.getProgress().getBytesTransferred());
		}

		myUpload.addProgressListener(new ProgressListener(){
			@Override
			public void progressChanged(ProgressEvent progressEvent) {
				System.out.println(progressEvent);
			}
		});

		try {
			myUpload.waitForCompletion();
		} catch (SCSServiceException e) {
			e.printStackTrace();
		} catch (SCSClientException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 分片上传文件
	 * @throws Exception
	 */
	public void multipartsUpload() throws Exception{
		//初始化上传任务
		InitiateMultipartUploadResult initiateMultipartUploadResult = conn.initiateMultipartUpload("bucket-name", "object-key");
		
		if(initiateMultipartUploadResult!=null){
			//分片上传
			List<PartETag> partETags = null;
			PutObjectRequest putObjectRequest = new PutObjectRequest(initiateMultipartUploadResult.getBucketName(),
					initiateMultipartUploadResult.getKey(), new File("local-file-path"));
			 try {
				long optimalPartSize = 5 * 1024 * 1024; //5M
	            UploadPartRequestFactory requestFactory = new UploadPartRequestFactory(putObjectRequest, initiateMultipartUploadResult.getUploadId()
	            		, optimalPartSize);
	
	            partETags = uploadPartsInSeries(requestFactory);
	        } catch (Exception e) {
	            throw e;
	        } finally {
	            if (putObjectRequest.getInputStream() != null) {
					try {
						putObjectRequest.getInputStream().close();
					} catch (Exception e) {
						throw e;
					}
	            }
	        }
		
			 //分片列表
			PartListing partList = conn.listParts(new ListPartsRequest(initiateMultipartUploadResult.getBucketName(),
											initiateMultipartUploadResult.getKey(),
											initiateMultipartUploadResult.getUploadId()));
			System.out.println("已上传的文件分片列表:\n"+partList);
			
			//分片合并，完成上传
			ObjectMetadata objectMetadata = conn.completeMultipartUpload(new CompleteMultipartUploadRequest(putObjectRequest.getBucketName(),
					putObjectRequest.getKey(), initiateMultipartUploadResult.getUploadId(), partETags));
			
			System.out.println("合并文件结果:\n");
			System.out.println(objectMetadata.getUserMetadata());
			System.out.println(objectMetadata.getContentLength());
			System.out.println(objectMetadata.getRawMetadata());
			System.out.println(objectMetadata.getETag());
		}
		
	}
	
	/**
     * Uploads all parts in the request in serial in this thread, then completes
     * the upload and returns the result.
     */
    private List<PartETag> uploadPartsInSeries(UploadPartRequestFactory requestFactory) {

        final List<PartETag> partETags = new ArrayList<PartETag>();

        while (requestFactory.hasMoreRequests()) {
            UploadPartRequest uploadPartRequest = requestFactory.getNextUploadPartRequest();
            // Mark the stream in case we need to reset it
            InputStream inputStream = uploadPartRequest.getInputStream();
            if (inputStream != null && inputStream.markSupported()) {
                if (uploadPartRequest.getPartSize() >= Integer.MAX_VALUE) {
                    inputStream.mark(Integer.MAX_VALUE);
                } else {
                    inputStream.mark((int)uploadPartRequest.getPartSize());
                }
            }
            partETags.add(conn.uploadPart(uploadPartRequest).getPartETag());
        }

        return partETags;
    }
	
	/* 生成url */
	public URL generateUrl(GeneratePresignedUrlRequest request){
		URL presignedUrl = conn.generatePresignedUrl(request);
		return presignedUrl;
	}
}
