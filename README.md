#SinaStorage SDK[![Build Status](https://travis-ci.org/poorevil/SinaStorage-SDK-Java-v2.png?branch=master)](https://travis-ci.org/SinaCloudStorage/SinaStorage-SDK-Java)

##概述
新浪云存储Java平台SDK为第三方应用提供了简单易用的API调用服务，使第三方客户端无需了解复杂的验证机制即可进行授权、上传、下载等文件操作。您可以通过**Maven**进行构建本项目或下载独立的[zip格式文件包](http://sdk.sinastorage.cn/scs-java-sdk-distribution/scs-java-sdk-0.0.1-SNAPSHOT-bin.zip?fn=scs-java-sdk-0.0.1-SNAPSHOT-bin.zip)
>本文档详细内容请查阅：[SinaStorage’s documentation](http://open.sinastorage.com/)

##SDK 环境要求

**Java 1.5+**

**依赖库**：

- commons-codec-1.3
- commons-logging-1.1.1
- httpclient-4.2

##目录说明
项目由**Maven**构建而成，每个子目录作为一个子项目存在。同步项目源码后，可以在本地通过‘mvn package’命令对项目进行编译、打包。

子目录						|说明
---------------------------|--------------------------
**scs-java-sdk-core**		|SCS JAVA SDK核心库
**scs-java-sdk**			|SDK主要业务实现
scs-android-sdk				|用于生成Android SDK jar包
scs-java-cli				|CLI命令工具
cs-java-sdk-distribution	|用于生成java sdk jar包


##快速上手

###1.创建bucket访问对象：

* 代码中设置你的accessKey和secretKey:
		
		import com.sina.cloudstorage.auth.AWSCrentials;
		import com.sina.cloudstorage.auth.BasicAWSCredentials;
		import com.sina.cloudstorage.services.scs.SCS;
		import com.sina.cloudstorage.services.scs.SCSClient;
		
		String accessKey = "你的accessKey";
		String secretKey = "你的secretKey";
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		SCS conn = new SCSClient(credentials);

* 配置文件设置你的accessKey和secretKey

	在根目录下，创建配置文件：SCSCredentials.properties，内容为：
	
		accessKey=你的accessKey
		secretKey=你的secretKey
		

	创建对象
	
		import com.sina.cloudstorage.services.scs.SCS;
		import com.sina.cloudstorage.services.scs.SCSClient;
		
		SCS conn = new SCSClient();


> 获取[access_key,secret_key](http://open.sinastorage.com/?c=doc&a=guide&section=quick_start#accesskey)



###2.bucket 操作:

* 列出所有bucket:
 
		/**
		 * 获取所有bucket
		 */
		public void getAllBuckets(){
			List<Bucket> list = conn.listBuckets();
			System.out.println("====getAllBuckets===="+list);
		}


* 创建bucket:

		/**
		 * 创建bucket
		 */
		public void createBucket(){
			Bucket bucket = conn.createBucket("bucket-name");
			System.out.println(bucket);
		}
	
	
* 删除bucket:
		
		/**
		 * 删除bucket
		 */
		public void deleteBucket(){
			conn.deleteBucket("create-a-bucket");
		}
		
* 获取bucket ACL:

		/**
		 * 获取bucket ACL
		 */
		public void getBucketAcl(){
			AccessControlList acl = conn.getBucketAcl("create-a-bucket");
			System.out.println(acl);
		}
		
* 设置bucket ACL:

		/**
		 * 设置bucket acl
		 */
		public void putBucketAcl(){
			AccessControlList acl = new AccessControlList();
			acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read, Permission.ReadAcp);
			acl.grantPermissions(UserIdGrantee.ANONYMOUSE, 
								Permission.ReadAcp,
								Permission.Write,
								Permission.WriteAcp);
			acl.grantPermissions(new UserIdGrantee("UserId"), 
								Permission.Read,
								Permission.ReadAcp,
								Permission.Write,
								Permission.WriteAcp);
			
			conn.setBucketAcl("create-a-bucket", acl);
		}

* 列bucket中所有文件:

		/**
		 * 列bucket中所有文件
		 */
		public void listObjects(){
			ObjectListing objectListing = conn.listObjects("test");
			System.out.println(objectListing);
		}

		
###3.object 操作:

* 获取文件信息:

		/**
		 * 获取object metadata
		 */
		public void getObjectMeta(){
			ObjectMetadata objectMetadata = conn.getObjectMetadata("bucket-name", "/test/file.txt");
			System.out.println(objectMetadata.getUserMetadata());
			System.out.println(objectMetadata.getContentLength());
			System.out.println(objectMetadata.getRawMetadata());
			System.out.println(objectMetadata.getETag());
		}

* 下载文件:

		/**
		 * 下载object 
	  	 *	//断点续传
		 *	GetObjectRequest rangeObjectRequest = new GetObjectRequest("test11", "/test/file.txt");
		 *	rangeObjectRequest.setRange(0, 10); // retrieve 1st 10 bytes.
		 *	S3Object objectPortion = conn.getObject(rangeObjectRequest);
		 *			
		 *	InputStream objectData = objectPortion.getObjectContent();
		 *	// "Process the objectData stream.
		 *	objectData.close();
		 */
		public void getObject(){
			//SDKGlobalConfiguration.setGlobalTimeOffset(-60*5);//自定义全局超时时间5分钟以后(可选项)
			S3Object s3Obj = conn.getObject("test11", "/test/file.txt");
			InputStream in = s3Obj.getObjectContent();
			byte[] buf = new byte[1024];
			OutputStream out = null;
			try {
				out = new FileOutputStream(new File("dage1.txt"));
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
				//SDKGlobalConfiguration.setGlobalTimeOffset(0);//还原超时时间
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
		

* 上传文件:
		
		/**
		 * 上传文件
		 */
		public void putObject(){
			PutObjectResult putObjectResult = conn.putObject("bucket名称",
												"文件上传路径", new File("本地文件"));
			System.out.println(putObjectResult);
		}
		
		
		
		/**
		 * 上传文件--进度回调方法
		 */
		public void putObject(){	
			PutObjectRequest por = new PutObjectRequest("bucket名称", "文件上传路径", 
					new File("本地文件")).withMetadata(new ObjectMetadata());
			por.setGeneralProgressListener(new ProgressListener() {
				@Override
				public void progressChanged(ProgressEvent progressEvent) {
					// TODO Auto-generated method stub
					System.out.println(progressEvent);
				}
			});
			
			PutObjectResult putObjectResult = conn.putObject(por);
			System.out.println(putObjectResult);
			
		}
		
* 上传文件(自定义请求头):
		
		/**
		 * 上传文件 自定义请求头
		 */
		public void putObjectWithCustomRequestHeader(){
			//自定义请求头k-v
			Map<String, String> requestHeader = new HashMap<String, String>();
			requestHeader.put("x-sina-additional-indexed-key", "stream/test111.txt");
			PutObjectResult putObjectResult = conn.putObject("bucket名称", "ssk/a/", 
												              new File("本地文件"), requestHeader);
			System.out.println(putObjectResult);//服务器响应结果
		}

		
* 秒传文件:
		
		/**
		 * 秒传
		 */
		public void putObjectRelax(){
			conn.putObjectRelax("bucket名称","文件上传路径","被秒传文件的sina_sha1值",被秒传文件的长度);
		}
		
		
* 复制文件:
		
		/**
		 * 拷贝object
		 */
		public void copyObject(){
			conn.copyObject("源bucket名称", "源文件路径", "目标bucket名称", "目标文件路径");
		}
		
		
* 修改文件meta信息:
	
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
			conn.setObjectMetadata("bucket名称", "文件路径", objectMetadata);
		}

* 删除文件:
		
		/**
		 * 删除Object
		 */
		public void deleteObject(){
			conn.deleteObject("bucket名称", "文件路径");
		}		

		
* 获取文件acl信息:

		/**
		 * 获取object acl
		 */
		public void getObjectAcl(){
			AccessControlList acl = conn.getObjectAcl("bucket名称", "文件路径");
			System.out.println(acl);
		}

* 修改文件acl信息:
		
		/**
		 * 设置object acl
		 */
		public void putObjectAcl(){
			AccessControlList acl = new AccessControlList();
			acl.grantPermissions(UserIdGrantee.CANONICAL, Permission.Read,Permission.ReadAcp);
			acl.grantPermissions(UserIdGrantee.ANONYMOUSE,Permission.ReadAcp,Permission.Write,Permission.WriteAcp);
			acl.grantPermissions(new UserIdGrantee("UserId"), Permission.Read,Permission.ReadAcp,Permission.Write,Permission.WriteAcp);
			
			conn.setObjectAcl("bucket名称", "文件路径", acl);
		}
		
* 分片上传文件:

		/**
		 * 分片上传文件
		 * @throws Exception
		 */
		public void multipartsUpload() throws Exception{
			//初始化上传任务
			InitiateMultipartUploadResult initiateMultipartUploadResult = conn.initiateMultipartUpload("bucket名称", "文件路径");
			
			if(initiateMultipartUploadResult!=null){
				//分片上传
				List<PartETag> partETags = null;
				PutObjectRequest putObjectRequest = new PutObjectRequest(initiateMultipartUploadResult.getBucketName(),
						initiateMultipartUploadResult.getKey(), new File("本地待上传文件"));
				 try {
					long optimalPartSize = 5 * 1024 * 1024; //分片大小5M
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
		
* 高级工具 TransferManager

		/* TransferManager */
		public void putObjectByTransferManager(){
			TransferManager tf = new TransferManager(conn);
			Upload myUpload = tf.upload("bucket名称", "文件路径", new File("待上传文件本地路径"));
			
			// You can poll your transfer's status to check its progress
			if (myUpload.isDone() == false) {
				System.out.println("Transfer: " + myUpload.getDescription());
				System.out.println("  - State: " + myUpload.getState());
				System.out.println("  - Progress: "
						+ myUpload.getProgress().getBytesTransferred());
			}
	
			// Transfers also allow you to set a <code>ProgressListener</code> to
			// receive
			// asynchronous notifications about your transfer's progress.
			myUpload.addProgressListener(new ProgressListener(){
				@Override
				public void progressChanged(ProgressEvent progressEvent) {
					System.out.println(progressEvent);
				}
			});
	
			// Or you can block the current thread and wait for your transfer to
			// to complete. If the transfer fails, this method will throw an
			// SCSClientException or SCSServiceException detailing the reason.
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


###3.URL签名工具:

* 含签名信息URL:
	
		/* 生成url*/
		public void generateUrl(){
			Date expiration = new Date();		//过期时间
	        long epochMillis = expiration.getTime();
	        epochMillis += 60*5*1000;
	        expiration = new Date(epochMillis);   
	        
			URL presignedUrl = conn.generatePresignedUrl("bucket名称", "文件路径", expiration, false);
			System.out.println(presignedUrl);
		}
	
	
For more detailed documentation, refer [here](http://open.sinastorage.com/)
