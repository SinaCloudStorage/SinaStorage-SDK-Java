import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.SDKGlobalConfiguration;
import com.sina.cloudstorage.auth.AWSCredentials;
import com.sina.cloudstorage.auth.BasicAWSCredentials;
import com.sina.cloudstorage.event.ProgressEvent;
import com.sina.cloudstorage.event.ProgressListener;
import com.sina.cloudstorage.services.scs.SCS;
import com.sina.cloudstorage.services.scs.SCSClient;
import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.CompleteMultipartUploadRequest;
import com.sina.cloudstorage.services.scs.model.InitiateMultipartUploadResult;
import com.sina.cloudstorage.services.scs.model.ListPartsRequest;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.ObjectMetadata;
import com.sina.cloudstorage.services.scs.model.PartETag;
import com.sina.cloudstorage.services.scs.model.PartListing;
import com.sina.cloudstorage.services.scs.model.Permission;
import com.sina.cloudstorage.services.scs.model.PutObjectRequest;
import com.sina.cloudstorage.services.scs.model.PutObjectResult;
import com.sina.cloudstorage.services.scs.model.S3Object;
import com.sina.cloudstorage.services.scs.model.UploadPartRequest;
import com.sina.cloudstorage.services.scs.model.UserIdGrantee;
import com.sina.cloudstorage.services.scs.transfer.TransferManager;
import com.sina.cloudstorage.services.scs.transfer.Upload;
import com.sina.cloudstorage.services.scs.transfer.internal.UploadPartRequestFactory;


public class Sample {

	String accessKey = "AccessKey";
	String secretKey = "SecretKey";
	
	AWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
	SCS conn = new SCSClient(credentials);
	
	/* Service操作 */
	
	/**
	 * 获取所有bucket
	 */
	public void getAllBuckets(){
		List<Bucket> list = conn.listBuckets();
		System.out.println("====getAllBuckets===="+list);
	}
	
	/* Bucket操作 */
	
	/**
	 * 创建bucket
	 */
	public void createBucket(){
		Bucket bucket = conn.createBucket("buckte11test");
		
		System.out.println(bucket);
	}
	
	/**
	 * 删除bucket
	 */
	public void deleteBucket(){
		conn.deleteBucket("create-a-bucket");
	}
	
	/**
	 * 获取bucket ACL
	 */
	public void getBucketAcl(){
		AccessControlList acl = conn.getBucketAcl("buckte11test");
		System.out.println(acl);
	}
	
	/**
	 * 设置bucket acl
	 */
	public void putBucketAcl(){
		AccessControlList acl = new AccessControlList();
		acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read,Permission.ReadAcp);
		acl.grantPermissions(UserIdGrantee.ANONYMOUSE,Permission.ReadAcp,Permission.Write,Permission.WriteAcp);
		acl.grantPermissions(new UserIdGrantee("SINA0000001001Nxxxxx"), Permission.Read,Permission.ReadAcp,Permission.Write,Permission.WriteAcp);
		
		conn.setBucketAcl("buckte11test", acl);
	}
	
	/**
	 * 列bucket中所有文件
	 */
	public void listObjects(){
		ObjectListing objectListing = conn.listObjects("buckte11test");
		System.out.println(objectListing);
	}
	
	/* Object操作 */
	/**
	 * 获取object metadata
	 */
	public void getObjectMetadata(){
		ObjectMetadata metadata = conn.getObjectMetadata("bucket-name", "object-key");
		System.out.println(metadata.getUserMetadata());
		System.out.println(metadata.getETag());
		System.out.println(metadata.getLastModified());
		System.out.println(metadata.getRawMetadata());
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
	public void getObject(){
		SDKGlobalConfiguration.setGlobalTimeOffset(-60*5);//超时时间5分钟以后
		S3Object s3Obj = conn.getObject("bucket-name", "object-key");
		InputStream in = s3Obj.getObjectContent();
		byte[] buf = new byte[1024];
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File("local-file-path.txt"));
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
	public void putObject(){
//		PutObjectResult putObjectResult = conn.putObject("create-a-bucket11", "Evernote1111_402491.dmg", 
//				new File("/Users/evil/Desktop/Evernote_402491.dmg"));
//		System.out.println(putObjectResult);
		
		
		PutObjectRequest por = new PutObjectRequest("buckte11test", "qt-opensource-mac-x64-clang-5.3.0.dmg", 
				new File("/Users/hanchao/Desktop/qt-opensource-mac-x64-clang-5.3.0.dmg")).withMetadata(new ObjectMetadata());
		por.setGeneralProgressListener(new ProgressListener() {
			@Override
			public void progressChanged(ProgressEvent progressEvent) {
				System.out.println(progressEvent);
			}
		});
		
		PutObjectResult putObjectResult = conn.putObject(por);
		System.out.println(putObjectResult);
		
	}
	
	/**
	 * 上传文件 自定义请求头
	 */
	public void putObjectWithCustomRequestHeader(){
		Map<String, String> requestHeader = new HashMap<String, String>();
		requestHeader.put("x-sina-additional-indexed-key", "stream/test111.txt");
		PutObjectResult putObjectResult = conn.putObject("sandbox2", "ssk/a/", new File("local-file-path"),requestHeader);
		System.out.println(putObjectResult);
	}
	
	/**
	 * 拷贝object
	 */
	public void copyObject(){
		conn.copyObject("source-bucket-name", "source-object-key", "dest-bucket-name", "dest-object-key");
	}
	
	/**
	 * 秒传
	 */
	public void putObjectRelax(){
		conn.putObjectRelax("bucket-name","object-key","4322fec3dd44787585f818a2d7bfa85ae0b664ab",12526362624l);
	}
	
	/**
	 * 获取object metadata
	 */
	public void getObjectMeta(){
		ObjectMetadata objectMetadata = conn.getObjectMetadata("bucket-name", "object-key");
		System.out.println(objectMetadata.getUserMetadata());
		System.out.println(objectMetadata.getContentLength());
		System.out.println(objectMetadata.getRawMetadata());
		System.out.println(objectMetadata.getETag());
	}
	
	/**
	 * 设置object metadata
	 */
	@SuppressWarnings("serial")
	public void putObjectMeta(){
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setUserMetadata(new HashMap<String,String>(){{
					put("aaa","1111");
					put("bbb","222");
					put("ccc","3333");
					put("asdfdsaf","vvvvvv");
		}});
		conn.setObjectMetadata("bucket-name", "object-key", objectMetadata);
	}
	
	/**
	 * 删除Object
	 */
	public void deleteObject(){
		conn.deleteObject("bucket-name", "object-key");
	}
	
	/**
	 * 获取object acl
	 */
	public void getObjectAcl(){
		AccessControlList acl = conn.getObjectAcl("bucket-name", "object-key");
		System.out.println(acl);
	}
	
	/**
	 * 设置object acl
	 */
	public void putObjectAcl(){
		AccessControlList acl = new AccessControlList();
		acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read,Permission.ReadAcp);
		acl.grantPermissions(UserIdGrantee.ANONYMOUSE,Permission.ReadAcp,Permission.Write,Permission.WriteAcp);
		acl.grantPermissions(new UserIdGrantee("SINA000000"+accessKey), Permission.Read,Permission.ReadAcp,Permission.Write,Permission.WriteAcp);
		
		conn.setObjectAcl("bucket-name", "object-key", acl);
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
	public void generateUrl(){
		Date expiration = new Date();
        long epochMillis = expiration.getTime();
        epochMillis += 60*5*1000;
        expiration = new Date(epochMillis);   
        
		URL presignedUrl = conn.generatePresignedUrl("bucket-name", "object-key", expiration, false);
		System.out.println(presignedUrl);
	}
	
	public static void main(String[] args){
		Sample sample = new Sample();
		/* Service操作 */
//		sample.getAllBuckets();
		/* Bucket操作 */
//		sample.createBucket();
//		sample.deleteBucket();
//		sample.getBucketAcl();
//		sample.putBucketAcl();
//		sample.listObjects();
		/* Object操作 */
//		sample.getObjectMetadata();
//		sample.getObject();
//		sample.putObject();
//		sample.putObjectWithCustomRequestHeader();
//		sample.copyObject();
//		sample.putObjectRelax();
//		sample.getObjectMeta();
//		sample.putObjectMeta();
//		sample.deleteObject();
//		sample.getObjectAcl();
//		sample.putObjectAcl();
		/* 生成url */
		sample.generateUrl();
		
		
//		try {
//			sample.multipartsUpload();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		/* TransferManager */
//		sample.putObjectByTransferManager();
	}
	
}
