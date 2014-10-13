package com.sina.cloudstorage.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sina.cloudstorage.ClientConfiguration;
import com.sina.cloudstorage.Protocol;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.auth.AWSCredentials;
import com.sina.cloudstorage.auth.BasicAWSCredentials;
import com.sina.cloudstorage.retry.PredefinedRetryPolicies;
import com.sina.cloudstorage.services.scs.S3ClientOptions;
import com.sina.cloudstorage.services.scs.SCSClient;
import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.CannedAccessControlList;
import com.sina.cloudstorage.services.scs.model.CopyObjectRequest;
import com.sina.cloudstorage.services.scs.model.GeneratePresignedUrlRequest;
import com.sina.cloudstorage.services.scs.model.GetObjectRequest;
import com.sina.cloudstorage.services.scs.model.Grant;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.ObjectMetadata;
import com.sina.cloudstorage.services.scs.model.Permission;
import com.sina.cloudstorage.services.scs.model.PutObjectRequest;
import com.sina.cloudstorage.services.scs.model.PutObjectResult;
import com.sina.cloudstorage.services.scs.model.S3ObjectSummary;
import com.sina.cloudstorage.util.DateUtils;

/*

 Options:

   Command Line:

   -f/--force           : force operation despite warnings
   -h/--vhost-style     : use virtual-host-style URIs (default is path-style)
   -u/--unencrypted     : unencrypted (use HTTP instead of HTTPS)
   -s/--show-properties : show response properties on stdout
   -r/--retries         : retry retryable failures this number of times
                          (default is 5)

   Environment:

   S3_ACCESS_KEY_ID     : access key ID (required)
   S3_SECRET_ACCESS_KEY : secret access key (required)
   S3_HOSTNAME          : specify alternative host (optional)

 Commands (with <required parameters> and [optional parameters]) :

   (NOTE: all command parameters take a value and are specified using the
          pattern parameter=value)

   help                 : Prints this help text

   list                 : Lists owned buckets
     [allDetails]       : Show full details

   list                 : List bucket contents
     <bucket>           : Bucket to list
     [prefix]           : Prefix for results set
     [marker]           : Where in results set to start listing
     [delimiter]        : Delimiter for rolling up results set
     [maxkeys]          : Maximum number of keys to return in results set
     [allDetails]       : Show full details for each key

   create               : Create a new bucket
     <bucket>           : Bucket to create
     [cannedAcl]        : Canned ACL for the bucket (see Canned ACLs)

   delete               : Delete a bucket or key
     <bucket>[/<key>]   : Bucket or bucket/key to delete

   getacl               : Get the ACL of a bucket or key
     <bucket>[/<key>]   : Bucket or bucket/key to get the ACL of
     [filename]         : Output filename for ACL (default is stdout)

   setacl               : Set the ACL of a bucket or key
     <bucket>[/<key>]   : Bucket or bucket/key to set the ACL of
     <filename>         : Input filename for ACL

   put                  : Puts an object
     <bucket>/<key>     : Bucket/key to put object to
     [filename]         : Filename to read source data from (default is stdin)
     [contentLength]    : How many bytes of source data to put (required if
                          source file is stdin)
     [cacheControl]     : Cache-Control HTTP header string to associate with
                          object
     [contentType]      : Content-Type HTTP header string to associate with
                          object
     [md5]              : MD5 for validating source data
     [contentDispositionFilename] : Content-Disposition filename string to
                          associate with object
     [contentEncoding]  : Content-Encoding HTTP header string to associate
                          with object
     [expires]          : Expiration date to associate with object
     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
     [x-amz-meta-...]]  : Metadata headers to associate with the object

   copy                 : Copies an object; if any options are set, the entire
                          metadata of the object is replaced
     <sourcebucket>/<sourcekey> : Source bucket/key
     <destbucket>/<destkey> : Destination bucket/key
     [cacheControl]     : Cache-Control HTTP header string to associate with
                          object
     [contentType]      : Content-Type HTTP header string to associate with
                          object
     [contentDispositionFilename] : Content-Disposition filename string to
                          associate with object
     [contentEncoding]  : Content-Encoding HTTP header string to associate
                          with object
     [expires]          : Expiration date to associate with object
     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
     [x-amz-meta-...]]  : Metadata headers to associate with the object

   get                  : Gets an object
     <buckey>/<key>     : Bucket/key of object to get
     [filename]         : Filename to write object data to (required if -s
                          command line parameter was used)
     [ifModifiedSince]  : Only return the object if it has been modified since
                          this date
     [ifNotmodifiedSince] : Only return the object if it has not been modified
                          since this date
     [ifMatch]          : Only return the object if its ETag header matches
                          this string
     [ifNotMatch]       : Only return the object if its ETag header does not
                          match this string
     [startByte]        : First byte of byte range to return
     [byteCount]        : Number of bytes of byte range to return

   head                 : Gets only the headers of an object, implies -s
     <bucket>/<key>     : Bucket/key of object to get headers of

   gqs                  : Generates an authenticated query string
     <bucket>[/<key>]   : Bucket or bucket/key to generate query string for
     [expires]          : Expiration date for query string
     [resource]         : Sub-resource of key for query string, without a
                          leading '?', for example, "meta"
	 [ip]          	    : Sub-resource of key for query string. 
	 [hostBucket]       : Bucket name as Domain. boolean value, for example, hostBucket=true

 Canned ACLs:

  The following canned ACLs are supported:
    private (default), public-read, public-read-write, authenticated-read

 ACL Format:

  For the getacl and setacl commands, the format of the ACL list is JSON :
  1) The value of "Owner" must be Owner ID or one of :GRPS0000000CANONICAL, GRPS000000ANONYMOUSE.
  	 The GRPS0000000CANONICAL means CANONICA group, GRPS000000ANONYMOUSE means ANONYMOUSE group.
  2) The format of "ACL" muset be a map. Key is Owner ID, value is Permission set.
  3) The Permission is one of: READ, WRITE, READ_ACP, or WRITE_ACP.

  Examples:
     {
	      "ACL": {
		          "SINA000000xxxxxx": [
		              "read",
		              "write",
		              "read_acp",
		              "write_acp"
		          ],
		          "SINA000000xxxxxx": [
		              "read",
		              "read_acp"
		          ]
		  }
	  }

  Note that the easiest way to modify an ACL is to first get it, saving it
  into a file, then modifying the file, and then setting the modified file
  back as the new ACL for the bucket/object.

 Date Format:

  The format for dates used in parameters is as ISO 8601 dates, i.e.
  YYYY-MM-DDTHH:MM:SS[+/-dd:dd].  Examples:
      2008-07-29T20:36:14Z
 */

/**
 * 
 * @author hanchao
 *
 */
public class MainCLI {

	ResourceBundle resourceBundle = ResourceBundle.getBundle("message", Locale.getDefault());//resourceBundle.getString("LIST_ALLDETAILS_DESCRIPTION")
	HelpFormatter hf = new HelpFormatter();
	
	S3ClientOptions clientOptions;
	ClientConfiguration clientConfiguration;
	Client client;//调用sdk主要类
	
	/*
	 * Environment:
	 *
   	 * S3_ACCESS_KEY_ID     : access key ID (required)
   	 * S3_SECRET_ACCESS_KEY : secret access key (required)
   	 * S3_HOSTNAME          : specify alternative host (optional)
	 */
	public static final String S3_ACCESS_KEY_ID = "S3_ACCESS_KEY_ID";
	public static final String S3_SECRET_ACCESS_KEY = "S3_SECRET_ACCESS_KEY";
	public static final String S3_HOSTNAME = "S3_HOSTNAME";
	
	/*
	 * Command Line:
	 */
	private Option vhostStyleOpt;			//-h/--vhost-style     : use virtual-host-style URIs (default is path-style)
	private Option unencryptedOpt;			//-u/--unencrypted     : unencrypted (use HTTP instead of HTTPS)
	private Option showPropertiesdOpt;		//-s/--show-properties : show response properties on stdout
	private Option retriesOpt;				//-r/--retries         : retry retryable failures this number of times
                           					//						(default is 5)
	
	/*
	 * Commands (with <required parameters> and [optional parameters]) :
	 */
	private Option helpOpt;					//help                 : Prints this help text
	
	/*
		create               : Create a new bucket
	     <bucket>           : Bucket to create
	     [cannedAcl]        : Canned ACL for the bucket (see Canned ACLs)
	 */
	private Option createOpt;				//create               : Create a new bucket
	
	/*
	   list                 : Lists owned buckets
     	 [allDetails]       : Show full details

	   list                 : List bucket contents
	     <bucket>           : Bucket to list
	     [prefix]           : Prefix for results set
	     [marker]           : Where in results set to start listing
	     [delimiter]        : Delimiter for rolling up results set
	     [maxkeys]          : Maximum number of keys to return in results set
	     [allDetails]       : Show full details for each key
	 */
	private Option listOpt;
	
	/*
 		delete               : Delete a bucket or key
 		  <bucket>[/<key>]   : Bucket or bucket/key to delete
	 */
	private Option deleteOpt;
	
	/*
		 getacl               : Get the ACL of a bucket or key
	       <bucket>[/<key>]   : Bucket or bucket/key to get the ACL of
	       [filename]         : Output filename for ACL (default is stdout)
	 */
	private Option getaclOpt;
	
	/*
	   setacl               : Set the ACL of a bucket or key
	     <bucket>[/<key>]   : Bucket or bucket/key to set the ACL of
	     [filename]         : Input filename for ACL (default is stdin)
	 */
	private Option setaclOpt;
	
	/*
	   put                  : Puts an object
	     <bucket>/<key>     : Bucket/key to put object to
	     [filename]         : Filename to read source data from (default is stdin)
	     [contentLength]    : How many bytes of source data to put (required if
	                          source file is stdin)
	     [cacheControl]     : Cache-Control HTTP header string to associate with
	                          object
	     [contentType]      : Content-Type HTTP header string to associate with
	                          object
	     [md5]              : MD5 for validating source data
	     [contentDispositionFilename] : Content-Disposition filename string to
	                          associate with object
	     [contentEncoding]  : Content-Encoding HTTP header string to associate
	                          with object
	     [expires]          : Expiration date to associate with object
	     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
	     [x-amz-meta-...]]  : Metadata headers to associate with the object
	 */
	private Option putOpt;
	
	/*
	   copy                 : Copies an object; if any options are set, the entire
	                          metadata of the object is replaced
	     <sourcebucket>/<sourcekey> : Source bucket/key
	     <destbucket>/<destkey> : Destination bucket/key
	     [cacheControl]     : Cache-Control HTTP header string to associate with
	                          object
	     [contentType]      : Content-Type HTTP header string to associate with
	                          object
	     [contentDispositionFilename] : Content-Disposition filename string to
	                          associate with object
	     [contentEncoding]  : Content-Encoding HTTP header string to associate
	                          with object
	     [expires]          : Expiration date to associate with object
	     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
	     [x-amz-meta-...]]  : Metadata headers to associate with the object
	 */
	private Option copyOpt;
	
	/*
	   get                  : Gets an object
	     <buckey>/<key>     : Bucket/key of object to get
	     [filename]         : Filename to write object data to (required if -s
	                          command line parameter was used)
	     [ifModifiedSince]  : Only return the object if it has been modified since
	                          this date
	     [ifNotmodifiedSince] : Only return the object if it has not been modified
	                          since this date
	     [ifMatch]          : Only return the object if its ETag header matches
	                          this string
	     [ifNotMatch]       : Only return the object if its ETag header does not
	                          match this string
	     [startByte]        : First byte of byte range to return
	     [byteCount]        : Number of bytes of byte range to return
	 */
	private Option getOpt;
	
	/*
	   head                 : Gets only the headers of an object, implies -s
	     <bucket>/<key>     : Bucket/key of object to get headers of
	 */
	private Option headOpt;
	
	/*
	   gqs                  : Generates an authenticated query string
	     <bucket>[/<key>]   : Bucket or bucket/key to generate query string for
	     [expires]          : Expiration date for query string
	     [resource]         : Sub-resource of key for query string, without a
	                          leading '?', for example, "meta"
	     [ip]          	    : Sub-resource of key for query string.                      
		 [hostBucket]       : Bucket name as Domain. boolean value, for example, hostBucket=true                
	 */
	private Option gqsOpt;
	
	public MainCLI(){
		initOptions();
		
		clientOptions = new S3ClientOptions();
		clientConfiguration = new ClientConfiguration();
	}
	
	/**
	 * 初始化option
	 */
	@SuppressWarnings("static-access")
	private void initOptions(){
		vhostStyleOpt = new Option("h", "use virtual-host-style URIs (default is path-style)");
		unencryptedOpt = new Option("u", "unencrypted (use HTTP instead of HTTPS)");
		showPropertiesdOpt = new Option("s", "show response properties on stdout");
		retriesOpt = OptionBuilder.withArgName("times").hasArg()
				.withDescription("retry retryable failures this number of times\n(default is 5)")
				.create("r");
		
		helpOpt = new Option("help", "Prints this help text");
		
		/*
			create               : Create a new bucket
		     <bucket>           : Bucket to create
		     [cannedAcl]        : Canned ACL for the bucket (see Canned ACLs)
		 */
		createOpt = new Option("create", null, false, "Create a new bucket\n"
					+ "with <bucket>\t: Bucket to create.(Required)\n" 
					+ "[cannedAcl]\t: Canned ACL for the bucket (see Canned ACLs) ");
		createOpt.setArgs(2);
		createOpt.setOptionalArg(true);
		createOpt.setArgName("bucket,cannedAcl");
		
		/*
		   list                 : Lists owned buckets
	     	 [allDetails]       : Show full details
		*/
		/*
		   list                 : List bucket contents
		     <bucket>           : Bucket to list
		     [prefix]           : Prefix for results set
		     [marker]           : Where in results set to start listing
		     [delimiter]        : Delimiter for rolling up results set
		     [maxkeys]          : Maximum number of keys to return in results set
		     [allDetails]       : Show full details for each key
		 */
		
		listOpt = new Option("list", null, false, "Lists owned buckets or bucket contents.\n"
				+ "without [bucket] param , the result is owned buckets.\n"
				+ "avalable params :\n"
				+ "[allDetails]    : Show full details\n"	
				+ "with [bucket] param , the result is bucket contents.\n"
				+ "avalable params :\n"
				+ "[bucket]    : The bucket name to be list contents , if not set the result is owned bucket's list set\n" 
				+ "[prefix]    : Prefix for bucket contents \n"
				+ "[marker]    : Where in bucket contents to start listing \n"
				+ "[delimiter] : Delimiter for rolling up bucket contents  \n"
				+ "[maxkeys]   : Maximum number of keys to return in bucket contents \n"
				+ "[allDetails]: Show full details for each key ");
		listOpt.setArgs(6);
		listOpt.setOptionalArg(true);
		listOpt.setArgName("bucket,prefix,marker,delimiter,maxkeys,allDetails");
		
		/*
	 		delete               : Delete a bucket or key
	 		  <bucket>[/<key>]   : Bucket or bucket/key to delete
		 */
		deleteOpt = OptionBuilder.withArgName("bucket[/<key>]").hasArg()
				.withDescription("Delete a bucket or key")
				.create("delete");
		
		/*
			 getacl               : Get the ACL of a bucket or key
		       <bucket>[/<key>]   : Bucket or bucket/key to get the ACL of
		       [filename]         : Output filename for ACL (default is stdout)
		 */
		getaclOpt = new Option("getacl", null, false, "Get the ACL of a bucket or key.\n"
				+ "avalable params :\n"
				+ "<bucket>[/<key>]   : Bucket or bucket/key to get the ACL of\n" 
				+ "[filename]         : Output filename for ACL (default is stdout) \n");
		getaclOpt.setArgs(2);
		getaclOpt.setOptionalArg(true);
		getaclOpt.setArgName("<bucket>[/<key>],filename");
	
		/*
		   setacl               : Set the ACL of a bucket or key
		     <bucket>[/<key>]   : Bucket or bucket/key to set the ACL of
		     [filename]         : Input filename for ACL
		 */
		setaclOpt = new Option("setacl", null, false, "Set the ACL of a bucket or key.\n"
				+ "avalable params :\n"
				+ "<bucket>[/<key>]   : Bucket or bucket/key to set the ACL of\n" 
				+ "[filename]         : Input filename for ACL (default is stdin) \n");
		setaclOpt.setArgs(2);
		setaclOpt.setOptionalArg(true);
		setaclOpt.setArgName("<bucket>[/<key>],filename");
		
		/*
		   put                  : Puts an object
		     <bucket>/<key>     : Bucket/key to put object to
		     [filename]         : Filename to read source data from (default is stdin)
		     [contentLength]    : How many bytes of source data to put (required if
		                          source file is stdin)
		     [cacheControl]     : Cache-Control HTTP header string to associate with
		                          object
		     [contentType]      : Content-Type HTTP header string to associate with
		                          object
		     [md5]              : MD5 for validating source data
		     [contentDispositionFilename] : Content-Disposition filename string to
		                          associate with object
		     [contentEncoding]  : Content-Encoding HTTP header string to associate
		                          with object
		     [expires]          : Expiration date to associate with object
		     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
		     [x-amz-meta-...]]  : Metadata headers to associate with the object
		 */
		putOpt = new Option("put", null, false, "Puts an object.\n"
				+ "avalable params :\n"
				+ "<bucket>/<key>     : Bucket/key to put object to\n" 
				+ "[filename]         : Filename to read source data from \n"
//				+ "[contentLength]    : How many bytes of source data to put\n"
				+ "[cacheControl]     : Cache-Control HTTP header string to associate with object\n"
				+ "[contentType]      : Content-Type HTTP header string to associate with object\n"
//				+ "[md5]              : MD5 for validating source data\n"
				+ "[contentDispositionFilename] : Content-Disposition filename string to associate with object\n"
				+ "[contentEncoding]  : Content-Encoding HTTP header string to associate with object\n"
				+ "[expires]          : Expiration date to associate with object\n"
				+ "[cannedAcl]        : Canned ACL for the object (see Canned ACLs)\n"
				+ "[x-amz-meta-...]]  : Metadata headers to associate with the object\n");
		putOpt.setArgs(Option.UNLIMITED_VALUES);
		putOpt.setOptionalArg(true);
		putOpt.setArgName("<bucket>[/<key>],filename,...");
		
		/*
		   copy                 : Copies an object; if any options are set, the entire
		                          metadata of the object is replaced
		     <sourcebucket>/<sourcekey> : Source bucket/key
		     <destbucket>/<destkey> : Destination bucket/key
		     [cacheControl]     : Cache-Control HTTP header string to associate with
		                          object
		     [contentType]      : Content-Type HTTP header string to associate with
		                          object
		     [contentDispositionFilename] : Content-Disposition filename string to
		                          associate with object
		     [contentEncoding]  : Content-Encoding HTTP header string to associate
		                          with object
		     [expires]          : Expiration date to associate with object
		     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
		     [x-amz-meta-...]]  : Metadata headers to associate with the object
		 */
		copyOpt = new Option("copy", null, false, "Copies an object; if any options are set, the entire metadata of the object is replaced.\n"
				+ "avalable params :\n"
				+ "<sourcebucket>/<sourcekey> : Source bucket/key\n" 
				+ "<destbucket>/<destkey> : Destination bucket/key\n"
				+ "[cacheControl]     : Cache-Control HTTP header string to associate with object\n"
				+ "[contentType]      : Content-Type HTTP header string to associate with object\n"
				+ "[contentDispositionFilename] : Content-Disposition filename string to associate with object\n"
				+ "[contentEncoding]  : Content-Encoding HTTP header string to associate with object\n"
				+ "[expires]          : Expiration date to associate with object\n"
				+ "[cannedAcl]        : Canned ACL for the object (see Canned ACLs)\n"
				+ "[x-amz-meta-...]]  : Metadata headers to associate with the object\n");
		copyOpt.setArgs(Option.UNLIMITED_VALUES);
		copyOpt.setOptionalArg(true);
		copyOpt.setArgName("<sourcebucket>/<sourcekey>,<destbucket>/<destkey>,...");
		
		/*
		   get                  : Gets an object
		     <buckey>/<key>     : Bucket/key of object to get
		     [filename]         : Filename to write object data to (required if -s
		                          command line parameter was used)
		     [ifModifiedSince]  : Only return the object if it has been modified since
		                          this date
		     [ifNotmodifiedSince] : Only return the object if it has not been modified
		                          since this date
		     [ifMatch]          : Only return the object if its ETag header matches
		                          this string
		     [ifNotMatch]       : Only return the object if its ETag header does not
		                          match this string
		     [startByte]        : First byte of byte range to return
		     [byteCount]        : Number of bytes of byte range to return
		 */
		getOpt = new Option("get", null, false, "Gets an object.\n"
				+ "avalable params :\n"
				+ "<buckey>/<key>     : Bucket/key of object to get\n" 
				+ "[filename]         : Filename to write object data to (required if -s command line parameter was used)\n"
				+ "[ifModifiedSince]  : Only return the object if it has been modified since this date\n"
				+ "[ifNotmodifiedSince] : Only return the object if it has not been modified since this date\n"
				+ "[ifMatch]          : Only return the object if its ETag header matches this string\n"
				+ "[ifNotMatch]       : Only return the object if its ETag header does not match this string\n"
				+ "[startByte]        : First byte of byte range to return\n"
				+ "[byteCount]        : Number of bytes of byte range to return\n");
		getOpt.setArgs(8);
		getOpt.setOptionalArg(true);
		getOpt.setArgName("<buckey>/<key>,filename");
		
		/*
		   head                 : Gets only the headers of an object, implies -s
		     <bucket>/<key>     : Bucket/key of object to get headers of
		 */
		headOpt = new Option("head", null, false, "Gets only the headers of an object, implies -s.\n"
				+ "avalable params :\n"
				+ "<buckey>/<key>     : Bucket/key of object to get headers of\n");
		headOpt.setArgs(1);
		headOpt.setOptionalArg(false);
		headOpt.setArgName("<buckey>/<key>");
		
		/*
		   gqs                  : Generates an authenticated query string
		     <bucket>[/<key>]   : Bucket or bucket/key to generate query string for
		     [expires]          : Expiration date for query string
		     [resource]         : Sub-resource of key for query string, without a
		                          leading '?', for example, "meta"
			 [ip]          	    : Sub-resource of key for query string.                      
		     [hostBucket]       : Bucket name as Domain. boolean value, for example, hostBucket=true
		 */
		gqsOpt = new Option("gqs", null, false, "Generates an authenticated query string.\n"
				+ "avalable params :\n"
				+ "<bucket>[/<key>]   : Bucket or bucket/key to generate query string for\n"
				+ "[expires]          : Expiration date for query string\n"
				+ "[resource]         : Sub-resource of key for query string, without a leading '?', for example, \"meta\"\n"
				+ "[ip]          	  : Sub-resource of key for query string.\n"
				+ "[hostBucket]       : Bucket name as Domain. boolean value, for example, hostBucket=true\n");
		gqsOpt.setArgs(5);
		gqsOpt.setOptionalArg(true);
		gqsOpt.setArgName("<bucket>[/<key>]");
		
	}
	
	/**
	 * 创建包含所有Option的Options
	 * @return Options
	 */
	private Options createOptions(){
		Options opts = new Options();
		opts.addOption(vhostStyleOpt);
		opts.addOption(unencryptedOpt);
		opts.addOption(showPropertiesdOpt);
		opts.addOption(retriesOpt);
		
		opts.addOption(helpOpt);
		opts.addOption(createOpt);
		opts.addOption(listOpt);
		opts.addOption(deleteOpt);
		opts.addOption(getaclOpt);
		opts.addOption(setaclOpt);
		opts.addOption(putOpt);
		opts.addOption(copyOpt);
		opts.addOption(getOpt);
		opts.addOption(headOpt);
		opts.addOption(gqsOpt);
		
		return opts;
	}
	
	public void cliRunner(String[] argv){
		AWSCredentials credentials = null;
		
		//从环境变量中获取AccessKey、SecretKey并做格式判断
		if (System.getenv(S3_ACCESS_KEY_ID) != null
				&& System.getenv(S3_SECRET_ACCESS_KEY) != null) {
			String accessKey = System.getenv(S3_ACCESS_KEY_ID);
			String secretKey = System.getenv(S3_SECRET_ACCESS_KEY);
			//判断AccessKey、SecretKey长度是否正确
			if (accessKey.length() > 1 && accessKey.length() < 20 && secretKey.length() == 40){
				credentials = new BasicAWSCredentials(accessKey,secretKey);
			}else{
				printErrorMsg("S3_ACCESS_KEY_ID or S3_SECRET_ACCESS_KEY is wrong.");
			}
		}
		
		if (credentials == null){
			printErrorMsg("Did not found S3_ACCESS_KEY_ID S3_SECRET_ACCESS_KEY in environment");
		}
		
		Options opts = createOptions();
		
		BasicParser parser = new BasicParser();
		
		CommandLine cl;
		try {
			cl = parser.parse(opts, argv);
			if (cl.getOptions().length > 0){
				if (cl.hasOption(helpOpt.getOpt())){
					printHelp();
					return;
				}else{
					/*
					 * 判断Command Line部分
					 * Command Line:
					 *
					 *  (暂无)-f/--force           : force operation despite warnings
					 *  -h/--vhost-style     : use virtual-host-style URIs (default is path-style)
					 *  -u/--unencrypted     : unencrypted (use HTTP instead of HTTPS)
					 *  -s/--show-properties : show response properties on stdout
					 *  -r/--retries         : retry retryable failures this number of times
					 *                         (default is 5)
					 */
					if (cl.hasOption(vhostStyleOpt.getOpt()))
						clientOptions.setPathStyleAccess(false);
					if (cl.hasOption(unencryptedOpt.getOpt()))
						clientConfiguration.setProtocol(Protocol.HTTP);
					//TODO:-s/--show-properties : show response properties on stdout
					if (cl.hasOption('r')){
						PredefinedRetryPolicies.DEFAULT_MAX_ERROR_RETRY = Integer.valueOf(cl.getOptionValue('r'));
						clientConfiguration.setRetryPolicy(PredefinedRetryPolicies.DEFAULT);
					}
					
					//实例化client
					client = new Client(credentials, clientConfiguration, clientOptions);
					
					//设置接口域名
					if (System.getenv(S3_HOSTNAME) != null){
						try {
							((SCSClient)client.conn).setEndpoint(System.getenv(S3_HOSTNAME));
						} catch (IllegalArgumentException e) {
							printErrorMsg(e.getMessage());
						}
					}
					
					/*
					 * 参数合法性验证，检查是否同时存在多个主命令
					 * 过滤掉基本参数
					 */
					List<String> optList = new ArrayList<String>();
					for (Option opt : cl.getOptions())
						optList.add(opt.getOpt());
					optList.remove(vhostStyleOpt.getOpt());
					optList.remove(unencryptedOpt.getOpt());
					optList.remove(showPropertiesdOpt.getOpt());
					optList.remove(retriesOpt.getOpt());
					if (optList.size() > 1){
						printErrorMsg("Unknown param: " + optList.get(1));
					}
					
					/*
					 * ================
					 * 处理业务部分
					 * ================
					 */
					
					/*
					 * create               : Create a new bucket
					 *     <bucket>           : Bucket to create
					 *     [cannedAcl]        : Canned ACL for the bucket (see Canned ACLs)
					 */
					if (cl.hasOption(createOpt.getOpt())){
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(createOpt.getOpt()));

						String bucketName = cl.getOptionValue(createOpt.getOpt());
						if (bucketName==null || "".equals(bucketName) || bucketName.contains("="))
							printErrorMsg("Missing arguments for option: "+createOpt.getOpt()+".The bucket parameter must be set.");
						
						String cannedAcl = values.get("cannedAcl");
						if (cannedAcl!=null && !Utils.validateCannedAcl(cannedAcl))
							printErrorMsg("Wrong arguments for option: "+createOpt.getOpt()+
									".The cannedAcl parameter must be one of private,public-read,public-read-write,authenticated-read.");

						try {
							client.createBucket(bucketName, cannedAcl);
						} catch (SCSServiceException e) {
							printErrorMsg(Utils.parseSCSException(e));
						} catch (Exception e){
							printErrorMsg(e.getLocalizedMessage());
						}
					}else if (cl.hasOption(listOpt.getOpt())){
						/*
						   list                 : Lists owned buckets
					     	 [allDetails]       : Show full details

						   list                 : List bucket contents
						     <bucket>           : Bucket to list
						     [prefix]           : Prefix for results set
						     [marker]           : Where in results set to start listing
						     [delimiter]        : Delimiter for rolling up results set
						     [maxkeys]          : Maximum number of keys to return in results set
						     [allDetails]       : Show full details for each key
						 */
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(listOpt.getOpt()));
						
						boolean showAllDetails = false;
						if ("true".equalsIgnoreCase(values.get("allDetails")))
							showAllDetails = true;
						
						String bucketName = cl.getOptionValue(listOpt.getOpt());
						/*
						 * bucket存在，列bucket内容
						 * 如果第一个参数不包含"="，则认为是bucket name
						 */
						if (bucketName != null && !bucketName.contains("=")){
							Integer maxkeys = null;
							if (values.containsKey("maxkeys")){
								try {
									maxkeys = Integer.valueOf(values.get("maxkeys"));
								} catch (Exception e) {
									printErrorMsg("maxkeys must be a Integer value");
								}
							}

							try{
								ObjectListing objectList = client.listObjects(bucketName,
										values.get("prefix"),
										values.get("marker"),
										values.get("delimiter"),
										maxkeys);
								
								printObjectList(objectList, showAllDetails);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}else{//bucket不存在，列所有bucket列表
							try{
								List<Bucket> bucketList = client.getAllBuckets();
								printBuckets(bucketList, showAllDetails);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}
						
					}else if (cl.hasOption(deleteOpt.getOpt())){
						/*
					 		delete               : Delete a bucket or key
					 		  <bucket>[/<key>]   : Bucket or bucket/key to delete
						 */
						String bucketAndKey = cl.getOptionValue(deleteOpt.getOpt());
						
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+deleteOpt.getOpt()+".The Bucket/key parameter must be set.");
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+deleteOpt.getOpt()+". Invalid bucket name, too short.");
						
						if (bucketAndKey.contains("/")){
							String[] pathArray = bucketAndKey.split("/");
							String bucketName = pathArray[0];
							String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
							
							try{
								client.deleteObject(bucketName, path);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}else{
							try{
								client.deleteBucket(bucketAndKey);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}
					}else if (cl.hasOption(getaclOpt.getOpt())){
						/*
							 getacl               : Get the ACL of a bucket or key
						       <bucket>[/<key>]   : Bucket or bucket/key to get the ACL of
						       [filename]         : Output filename for ACL (default is stdout)
						 */
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(getaclOpt.getOpt()));
						
						String bucketAndKey = cl.getOptionValue(getaclOpt.getOpt());
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+getaclOpt.getOpt()+".The bucket parameter must be set.");
						
						File opFile = null;
						String filename = values.get("filename");
						if (filename!=null){
							opFile = new File(filename);
							if (!opFile.exists()){
								try {
									opFile.createNewFile();
								} catch (IOException e) {
									printErrorMsg("Wrong arguments for option: "+getaclOpt.getOpt()+"."+e.getLocalizedMessage());
								}
							}
						}
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+getaclOpt.getOpt()+". Invalid bucket name, too short.");
						
						AccessControlList acl = null;
						//文件
						if (bucketAndKey.contains("/")){
							String[] pathArray = bucketAndKey.split("/");
							String bucketName = pathArray[0];
							String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
							try{
								acl = client.getObjectAcl(bucketName, path);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}else{//bucket
							try{
								acl = client.getBucketAcl(bucketAndKey);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}
						
						if (opFile != null){
							FileWriter fw = null;
							try {
								fw = new FileWriter(opFile);
								fw.write(generateAclStringBuilder(acl).toString());
								fw.flush();
							} catch (IOException e) {
								printErrorMsg(e.getLocalizedMessage());
							} finally{
								try {
									if (fw != null)
										fw.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}else{
							printAcl(acl);
						}
					}else if(cl.hasOption(setaclOpt.getOpt())){
						/*
						   setacl               : Set the ACL of a bucket or key
						     <bucket>[/<key>]   : Bucket or bucket/key to set the ACL of
						     <filename>         : Input filename for ACL 
						 */
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(setaclOpt.getOpt()));
						
						String bucketAndKey = cl.getOptionValue(setaclOpt.getOpt());
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+setaclOpt.getOpt()+".The bucket parameter must be set.");
						
						File opFile = null;
						String filename = values.get("filename");
						if (filename!=null){
							opFile = new File(filename);
							if (!opFile.exists())
								printErrorMsg("Wrong arguments for option: "+setaclOpt.getOpt()+".The filename is not exist.");
							
							//暂时判断acl文件不能大于5k
							if (opFile.length() > 1024*5){
								printErrorMsg("Failed to parse ACLs");
							}
							
						}else{
							printErrorMsg("Wrong arguments for option: "+setaclOpt.getOpt()+".The filename must be set.");
						}
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+setaclOpt.getOpt()+". Invalid bucket name, too short.");
						
						StringBuilder aclStringBuilder = new StringBuilder();
						if (opFile != null){
							FileReader fr = null;
							try {
								fr = new FileReader(opFile);
							    char data[]=new char[1024*8];
							    int len = 0;
							    while((len = fr.read(data)) > 0){
							    	aclStringBuilder.append(new String(data, 0, len));
							    }
							} catch (IOException e) {
								printErrorMsg("Wrong arguments for option: "+setaclOpt.getOpt()+"."+e.getMessage());
							} finally{
								if (fr != null){
									try {
										fr.close();
									} catch (IOException e) {
										printErrorMsg("Wrong arguments for option: "+setaclOpt.getOpt()+"."+e.getMessage());
									}
								}
							}
						}
						
						if (aclStringBuilder == null || aclStringBuilder.length() == 0)
							printErrorMsg("Wrong arguments for option: "+setaclOpt.getOpt()+".Acl can't be empty!");
						
						AccessControlList acl = null;
						Gson gson = new Gson();
						try {
							@SuppressWarnings("unchecked")
							Map<String,Object> aclMap = gson.fromJson(aclStringBuilder.toString(), Map.class);
							acl = new AccessControlList(aclMap);
							if (acl.getGrants().size() == 0)
								printErrorMsg("Failed to parse ACLs");
						} catch (Exception e) {
							printErrorMsg("Failed to parse ACLs");
						}
						
						//文件
						if (bucketAndKey.contains("/")){
							String[] pathArray = bucketAndKey.split("/");
							String bucketName = pathArray[0];
							String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
							try{
								System.out.println(acl);
								client.putObjectAcl(bucketName, path, acl);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}else{//bucket
							try{
								client.putBucketAcl(bucketAndKey, acl);
							} catch (SCSServiceException e) {
								printErrorMsg(Utils.parseSCSException(e));
							} catch (Exception e){
								printErrorMsg(e.getLocalizedMessage());
							}
						}
					}else if(cl.hasOption(putOpt.getOpt())){
						/*
						   put                  : Puts an object
						     <bucket>/<key>     : Bucket/key to put object to
						     [filename]         : Filename to read source data from (default is stdin)
						     [cacheControl]     : Cache-Control HTTP header string to associate with
						                          object
						     [contentType]      : Content-Type HTTP header string to associate with
						                          object
						     [contentDispositionFilename] : Content-Disposition filename string to
						                          associate with object
						     [contentEncoding]  : Content-Encoding HTTP header string to associate
						                          with object
						     [expires]          : Expiration date to associate with object
						     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
						     [x-amz-meta-...]]  : Metadata headers to associate with the object
						 */
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(putOpt.getOpt()));
						
						String bucketAndKey = cl.getOptionValue(putOpt.getOpt());
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+putOpt.getOpt()+".The Bucket/key parameter must be set.");
						
						File opFile = null;
						String filename = values.get("filename");
						if (filename!=null){
							opFile = new File(filename);
							if (!opFile.exists())
								printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+".The filename is not exist.");
						}else{
							printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+".The filename must be set.");
						}
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+". Invalid bucket name, too short.");
						
						if (!bucketAndKey.contains("/")){
							printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+". Invalid Bucket/key parameter.");
						}
						
						String[] pathArray = bucketAndKey.split("/");
						String bucketName = pathArray[0];
						String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
						
						/*
						 * 处理其他参数
						 [cacheControl]     : Cache-Control HTTP header string to associate with
					                          object
					     [contentType]      : Content-Type HTTP header string to associate with
					                          object
					     [contentDispositionFilename] : Content-Disposition filename string to
					                          associate with object
					     [contentEncoding]  : Content-Encoding HTTP header string to associate
					                          with object
					     [expires]          : Expiration date to associate with object
					     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
					     [x-amz-meta-...]]  : Metadata headers to associate with the object
						 */
						ObjectMetadata meta = new ObjectMetadata();
						
						if (values.get("cacheControl") != null)
							meta.setCacheControl(values.get("cacheControl"));
						
						if (values.get("contentType") != null)
							meta.setContentType(values.get("contentType"));
						
						if (values.get("contentDispositionFilename") != null)
							meta.setContentDisposition(values.get("contentDispositionFilename"));
						
						if (values.get("contentEncoding") != null)
							meta.setContentEncoding(values.get("contentEncoding"));
						
						if (values.get("expires") != null){
							Date expirationTime = null;
							DateUtils du = new DateUtils();
							try {
								expirationTime = du.parseIso8601Date(values.get("expires"));
							} catch (java.text.ParseException e) {
								printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+". "+e.getLocalizedMessage());
							}
							
							meta.setExpirationTime(expirationTime);
						}
						
						CannedAccessControlList cannedAcl = null;
						if (values.get("cannedAcl") != null){
							String cannedAclStr = values.get("cannedAcl");
							if (!Utils.validateCannedAcl(cannedAclStr))
								printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+
										".The cannedAcl parameter must be one of private,public-read,public-read-write,authenticated-read.");
							
							//private (default), public-read, public-read-write, authenticated-read
							if ("private".equalsIgnoreCase(cannedAclStr)){
								cannedAcl = CannedAccessControlList.Private;
							}else if ("public-read".equals(cannedAclStr)){
								cannedAcl = CannedAccessControlList.PublicRead;
							}else if ("public-read-write".equals(cannedAclStr)){
								cannedAcl = CannedAccessControlList.PublicReadWrite;
							}else {//if ("authenticated-read".equals(cannedAclStr)){
								cannedAcl = CannedAccessControlList.AuthenticatedRead;
							} 
						}
						
						//[x-amz-meta-...]]
						for (Entry<String, String> entry : values.entrySet()){
							if (entry.getKey().startsWith("x-amz-meta-")){
								meta.addUserMetadata(entry.getKey().substring(11, entry.getKey().length()), entry.getValue());
							}
						}
						
						PutObjectRequest por = new PutObjectRequest(bucketName, path, opFile).withMetadata(meta).withCannedAcl(cannedAcl);
//						por.setGeneralProgressListener(new ProgressListener() {
//							@Override
//							public void progressChanged(ProgressEvent progressEvent) {
//								System.out.println(progressEvent);
//							}
//						});
						try{
							PutObjectResult putObjectResult = client.putObject(por);
							System.out.println(putObjectResult);
						} catch (SCSServiceException e) {
							printErrorMsg(Utils.parseSCSException(e));
						} catch (Exception e){
							printErrorMsg(e.getLocalizedMessage());
						}
					}else if(cl.hasOption(copyOpt.getOpt())){
						/*
						   copy                 : Copies an object; if any options are set, the entire
						                          metadata of the object is replaced
						     <sourcebucket>/<sourcekey> : Source bucket/key
						     <destbucket>/<destkey> : Destination bucket/key
						     [cacheControl]     : Cache-Control HTTP header string to associate with
						                          object
						     [contentType]      : Content-Type HTTP header string to associate with
						                          object
						     [contentDispositionFilename] : Content-Disposition filename string to
						                          associate with object
						     [contentEncoding]  : Content-Encoding HTTP header string to associate
						                          with object
						     [expires]          : Expiration date to associate with object
						     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
						     [x-amz-meta-...]]  : Metadata headers to associate with the object
						 */
						if (cl.getOptionValues(copyOpt.getOpt()).length < 2)
							printErrorMsg("Wrong arguments for option: "+copyOpt.getOpt()+". Arguments at least contains <sourcebucket>/<sourcekey> and <destbucket>/<destkey>.");
						
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(copyOpt.getOpt()));
						
						String[] args = cl.getOptionValues(copyOpt.getOpt());
						/*
						 * 源bucket/key
						 */
						String sourceBucketAndKey = args[0];
						if (sourceBucketAndKey==null || "".equals(sourceBucketAndKey))
							printErrorMsg("Missing arguments for option: "+copyOpt.getOpt()+".The <sourcebucket>/<sourcekey> parameter must be set.");
						
						if (sourceBucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+copyOpt.getOpt()+". Invalid bucket name in <sourcebucket>/<sourcekey>, too short.");
						
						if (!sourceBucketAndKey.contains("/")){
							printErrorMsg("Wrong arguments for option: "+copyOpt.getOpt()+". Invalid Bucket/key parameter in <sourcebucket>/<sourcekey>.");
						}
						
						String[] sourcePathArray = sourceBucketAndKey.split("/");
						String sourceBucketName = sourcePathArray[0];
						String sourcePath = sourceBucketAndKey.substring(sourceBucketName.length()+1,sourceBucketAndKey.length());
						
						/*
						 * 目标bucket/key
						 */
						String destBucketAndKey = args[1];
						if (destBucketAndKey==null || "".equals(destBucketAndKey))
							printErrorMsg("Missing arguments for option: "+copyOpt.getOpt()+".The <destbucket>/<destkey> parameter must be set.");
						
						if (destBucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+copyOpt.getOpt()+". Invalid bucket name in <destbucket>/<destkey>, too short.");
						
						if (!destBucketAndKey.contains("/")){
							printErrorMsg("Wrong arguments for option: "+copyOpt.getOpt()+". Invalid Bucket/key parameter in <destbucket>/<destkey>.");
						}
						
						String[] destPathArray = destBucketAndKey.split("/");
						String destBucketName = destPathArray[0];
						String destPath = destBucketAndKey.substring(destBucketName.length()+1,destBucketAndKey.length());
						
						/*
						 * 其他参数
						     [cacheControl]     : Cache-Control HTTP header string to associate with
						                          object
						     [contentType]      : Content-Type HTTP header string to associate with
						                          object
						     [contentDispositionFilename] : Content-Disposition filename string to
						                          associate with object
						     [contentEncoding]  : Content-Encoding HTTP header string to associate
						                          with object
						     [expires]          : Expiration date to associate with object
						     [cannedAcl]        : Canned ACL for the object (see Canned ACLs)
						     [x-amz-meta-...]]  : Metadata headers to associate with the object
						 */
						ObjectMetadata meta = new ObjectMetadata();
						
						if (values.get("cacheControl") != null)
							meta.setCacheControl(values.get("cacheControl"));
						
						if (values.get("contentType") != null)
							meta.setContentType(values.get("contentType"));
						
						if (values.get("contentDispositionFilename") != null)
							meta.setContentDisposition(values.get("contentDispositionFilename"));
						
						if (values.get("contentEncoding") != null)
							meta.setContentEncoding(values.get("contentEncoding"));
						
						if (values.get("expires") != null){
							Date expirationTime = null;
							DateUtils du = new DateUtils();
							try {
								expirationTime = du.parseIso8601Date(values.get("expires"));
							} catch (java.text.ParseException e) {
								printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+". "+e.getLocalizedMessage());
							}
							
							meta.setExpirationTime(expirationTime);
						}
						
						CannedAccessControlList cannedAcl = null;
						if (values.get("cannedAcl") != null){
							String cannedAclStr = values.get("cannedAcl");
							if (!Utils.validateCannedAcl(cannedAclStr))
								printErrorMsg("Wrong arguments for option: "+putOpt.getOpt()+
										".The cannedAcl parameter must be one of private,public-read,public-read-write,authenticated-read.");
							
							//private (default), public-read, public-read-write, authenticated-read
							if ("private".equalsIgnoreCase(cannedAclStr)){
								cannedAcl = CannedAccessControlList.Private;
							}else if ("public-read".equals(cannedAclStr)){
								cannedAcl = CannedAccessControlList.PublicRead;
							}else if ("public-read-write".equals(cannedAclStr)){
								cannedAcl = CannedAccessControlList.PublicReadWrite;
							}else {//if ("authenticated-read".equals(cannedAclStr)){
								cannedAcl = CannedAccessControlList.AuthenticatedRead;
							} 
						}
						
						//[x-amz-meta-...]]
						for (Entry<String, String> entry : values.entrySet()){
							if (entry.getKey().startsWith("x-amz-meta-")){
								meta.addUserMetadata(entry.getKey().substring(11, entry.getKey().length()), entry.getValue());
							}
						}
						
						try{
							CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucketName, sourcePath, destBucketName, destPath)
									.withNewObjectMetadata(meta).withCannedAccessControlList(cannedAcl);
							client.copyObject(copyObjectRequest);
						} catch (SCSServiceException e) {
							printErrorMsg(Utils.parseSCSException(e));
						} catch (Exception e){
							printErrorMsg(e.getLocalizedMessage());
						}
						
					}else if(cl.hasOption(getOpt.getOpt())){
						/*
						   get                  : Gets an object
						     <buckey>/<key>     : Bucket/key of object to get
						     [filename]         : Filename to write object data to (required if -s
						                          command line parameter was used)
						     [ifModifiedSince]  : Only return the object if it has been modified since
						                          this date
						     [ifNotmodifiedSince] : Only return the object if it has not been modified
						                          since this date
						     [ifMatch]          : Only return the object if its ETag header matches
						                          this string
						     [ifNotMatch]       : Only return the object if its ETag header does not
						                          match this string
						     [startByte]        : First byte of byte range to return
						     [byteCount]        : Number of bytes of byte range to return
						 */
						if (cl.getOptionValues(getOpt.getOpt()).length < 2)
							printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". Arguments at least contains <buckey>/<key> and filename.");
						
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(getOpt.getOpt()));
						
						String bucketAndKey = cl.getOptionValue(getOpt.getOpt());
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+getOpt.getOpt()+".The Bucket/key parameter must be set.");
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". Invalid bucket name, too short.");
						
						if (!bucketAndKey.contains("/")){
							printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". Invalid Bucket/key parameter.");
						}
						
						String[] pathArray = bucketAndKey.split("/");
						String bucketName = pathArray[0];
						String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
						
						/*
						 * 待上传文件
						 */
						File opFile = null;
						String filename = values.get("filename");
						if (filename!=null){
							opFile = new File(filename);
							if (!opFile.exists()){
								try {
									opFile.createNewFile();
								} catch (IOException e) {
									printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+"."+e.getLocalizedMessage());
								}
							}
						}else{
							printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+".The filename must be set.");
						}
						
						/*
						 * 其他参数
							 [ifModifiedSince]  : Only return the object if it has been modified since
						                          this date
						     [ifNotmodifiedSince] : Only return the object if it has not been modified
						                          since this date
						     [ifMatch]          : Only return the object if its ETag header matches
						                          this string
						     [ifNotMatch]       : Only return the object if its ETag header does not
						                          match this string
						     [startByte]        : First byte of byte range to return
						     [byteCount]        : Number of bytes of byte range to return
						 */
						GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, path);
						
						DateUtils du = new DateUtils();
						if (values.get("ifModifiedSince") != null){
							Date ifModifiedSince = null;
							try {
								ifModifiedSince = du.parseIso8601Date(values.get("ifModifiedSince"));
							} catch (java.text.ParseException e) {
								printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". "+e.getLocalizedMessage());
							}
							
							getObjectRequest.setModifiedSinceConstraint(ifModifiedSince);
						}
						
						if (values.get("ifNotmodifiedSince") != null){
							Date ifNotmodifiedSince = null;
							try {
								ifNotmodifiedSince = du.parseIso8601Date(values.get("ifNotmodifiedSince"));
							} catch (java.text.ParseException e) {
								printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". "+e.getLocalizedMessage());
							}
							
							getObjectRequest.setUnmodifiedSinceConstraint(ifNotmodifiedSince);
						}
						
						if (values.get("ifMatch") != null)
							getObjectRequest = getObjectRequest.withMatchingETagConstraint(values.get("ifMatch"));
						
						if (values.get("ifNotMatch") != null)
							getObjectRequest = getObjectRequest.withNonmatchingETagConstraint(values.get("ifNotMatch"));
						
						if (values.get("startByte") != null && values.get("byteCount") != null){
							try {
								long startByte = Long.parseLong(values.get("startByte"));
								long byteCount = Long.parseLong(values.get("byteCount"));
								
								if (startByte > 0 && byteCount > 0){
									getObjectRequest.setRange(startByte, startByte+byteCount-1);
								}else{
									printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". The startByte and byteCount must above 0 !");
								}
							} catch (NumberFormatException e) {
								printErrorMsg("Wrong arguments for option: "+getOpt.getOpt()+". "+e.getLocalizedMessage());
							}
						}
						
						try {
							client.getObject(getObjectRequest, opFile);
						} catch (SCSServiceException e) {
							printErrorMsg(Utils.parseSCSException(e));
						} catch (Exception e){
							printErrorMsg(e.getLocalizedMessage());
						}
					}else if(cl.hasOption(headOpt.getOpt())){
						/*
						   head                 : Gets only the headers of an object, implies -s
						     <bucket>/<key>     : Bucket/key of object to get headers of
						 */
						String bucketAndKey = cl.getOptionValue(headOpt.getOpt());
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+headOpt.getOpt()+".The Bucket/key parameter must be set.");
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+headOpt.getOpt()+". Invalid bucket name, too short.");
						
						if (!bucketAndKey.contains("/")){
							printErrorMsg("Wrong arguments for option: "+headOpt.getOpt()+". Invalid Bucket/key parameter.");
						}
						
						String[] pathArray = bucketAndKey.split("/");
						String bucketName = pathArray[0];
						String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
						
						try{
							ObjectMetadata metadata = client.getObjectMetadata(bucketName, path);
							//输出
							for (Entry<String, Object> entry : metadata.getRawMetadata().entrySet())
								System.out.println(entry.getKey()+":"+entry.getValue());
							
							for (Entry<String, String> entry : metadata.getUserMetadata().entrySet())
								System.out.println("x-amz-meta-"+entry.getKey()+":"+entry.getValue());
						} catch (SCSServiceException e) {
							printErrorMsg(Utils.parseSCSException(e));
						} catch (Exception e){
							printErrorMsg(e.getLocalizedMessage());
						}
						
					}else if(cl.hasOption(gqsOpt.getOpt())){
						/*
						   gqs                  : Generates an authenticated query string
						     <bucket>[/<key>]   : Bucket or bucket/key to generate query string for
						     [expires]          : Expiration date for query string
						     [resource]         : Sub-resource of key for query string, without a
						                          leading '?', for example, "meta"
							 [ip]          	    : Sub-resource of key for query string.                      
						     [hostBucket]       : Bucket name as Domain. boolean value, for example, hostBucket=true
						 */
						String bucketAndKey = cl.getOptionValue(gqsOpt.getOpt());
						
						if (bucketAndKey==null || "".equals(bucketAndKey))
							printErrorMsg("Missing arguments for option: "+gqsOpt.getOpt()+".The <bucket>[/<key>] parameter must be set.");
						
						if (bucketAndKey.startsWith("/"))
							printErrorMsg("Wrong arguments for option: "+gqsOpt.getOpt()+". Invalid bucket name, too short.");
						
						Map<String, String> values = Utils.getValuesMap(cl.getOptionValues(gqsOpt.getOpt()));
						
						GeneratePresignedUrlRequest request = null;
						
						//<bucket>[/<key>]
						if (bucketAndKey.contains("/")){
							String[] pathArray = bucketAndKey.split("/");
							String bucketName = pathArray[0];
							String path = bucketAndKey.substring(bucketName.length()+1,bucketAndKey.length());
							
							request = new GeneratePresignedUrlRequest(bucketName, path);
						}else{//<bucket>
							request = new GeneratePresignedUrlRequest(bucketAndKey, null);
						}
						
						/*
						 * 其他参数
						     [expires]          : Expiration date for query string
						     [resource]         : Sub-resource of key for query string, without a
						                          leading '?', for example, "meta"
							 [ip]          	    : Sub-resource of key for query string.                      
						     [hostBucket]       : Bucket name as Domain. boolean value, for example, hostBucket=true
						 */
						
						Date expirationTime = null;
						if (values.get("expires") != null){
							DateUtils du = new DateUtils();
							try {
								expirationTime = du.parseIso8601Date(values.get("expires"));
							} catch (java.text.ParseException e) {
								printErrorMsg("Wrong arguments for option: "+gqsOpt.getOpt()+". "+e.getLocalizedMessage());
							}
						}else{
							expirationTime = new Date();
					        long epochMillis = expirationTime.getTime();
					        epochMillis += 60*10*1000;
					        expirationTime = new Date(epochMillis);
						}
						
				        request.setExpiration(expirationTime);
						
				        if (values.get("resource") != null)
				        	request.addRequestParameter(values.get("resource"), null);
				        
						if (values.get("ip") != null && Utils.validIP(values.get("ip")))
							request.addRequestParameter("ip", values.get("ip"));
						
						if (values.get("hostBucket") != null){
							if ("true".equalsIgnoreCase(values.get("hostBucket")))
								request.setBucketNameAsDomain(true);
							else
								request.setBucketNameAsDomain(false);
						}
						
						try{
							URL url = client.generateUrl(request);
							System.out.println(url);
						} catch (SCSServiceException e) {
							printErrorMsg(Utils.parseSCSException(e));
						} catch (Exception e){
							printErrorMsg(e.getLocalizedMessage());
						}
					}else{
						printErrorMsg("Missing argument: command");
					}
				}
			}else{
				printErrorMsg("Missing argument: command");
			}
		} catch (ParseException e) {
			printErrorMsg(e.getLocalizedMessage());
		}
	}
	
	/**
	 * 打印help信息
	 */
	public void printHelp(){
		System.out.println(resourceBundle.getString("HELP_DETAIL"));
	}
	
	/**
	 * 打印错误信息
	 * @param errorMsg
	 */
	private void printErrorMsg(String errorMsg){
		System.err.println("ERROR: "+errorMsg+" \n");
//		printHelp();
		System.exit(1); 
	}
	
	/**
	 * 打印bucket结果
	 * @param buckets
	 */
	private void printBuckets(List<Bucket> buckets, boolean showAllDetails){
		StringBuffer sb = new StringBuffer();
		sb.append(" Created  \t   Size   ");
		if (showAllDetails)
			sb.append("\t       Owner        ");
		sb.append("\t       Bucket\n");
		sb.append("----------\t----------");
		if (showAllDetails)
			sb.append("\t--------------------");
		sb.append("\t----------------------\n");
		for (Bucket bucket : buckets){
			sb.append(String.format("%tF", bucket.getCreationDate())+"\t");
			sb.append(String.format("%10d", bucket.getConsumedBytes())+"\t");
			if (showAllDetails){
				sb.append(String.format("%20s", bucket.getOwner().getId())+"\t");
			}
			sb.append(bucket.getName()+"\n");
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * 打印object结果
	 * @param objectList
	 * @param showAllDetails
	 */
	private void printObjectList(ObjectListing objectList, boolean showAllDetails){
		/*
		 * 表头
		 */
		StringBuffer sb = new StringBuffer();
		sb.append(" Modified \t   Size   ");
		if (showAllDetails)
			sb.append("\t              ETag                \t      Owner ID      ");
		sb.append("\t       Key\n");
		
		sb.append("----------\t----------");
		if (showAllDetails)
			sb.append("\t--------------------------------\t--------------------");
		sb.append("\t----------------------\n");
		/*
		 * 内容
		 */
		for (S3ObjectSummary objSummary : objectList.getObjectSummaries()){
			sb.append(String.format("%tF", objSummary.getLastModified())+"\t");
			sb.append(String.format("%10d", objSummary.getSize())+"\t");
			if (showAllDetails){
				sb.append(String.format("%32s", objSummary.getETag())+"\t");
				sb.append(String.format("%20s", objSummary.getOwner().getId())+"\t");
			}
			sb.append(String.format("%s", objSummary.getKey()+"\n"));
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * 打印acl结果
	 * @param acl
	 * @return
	 */
	private StringBuilder generateAclStringBuilder(AccessControlList acl){
//	     {
//	      "ACL": {
//		          "SINA000000xxxxxx": [
//		              "read",
//		              "write",
//		              "read_acp",
//		              "write_acp"
//		          ]
//		  }
//	  }
		Map<String,Object> aclMap = new HashMap<String,Object>();
		Map<String,Object[]> grantMap = new HashMap<String,Object[]>();
		for (Grant grant : acl.getGrants()){
			HashSet<Permission> permissions = grant.getPermissions();
			String[] permissionStrings = new String[permissions.size()];
			
			int i = 0;
			for (Permission p : permissions){
				permissionStrings[i] = p.toString();
				i++;
			}
			
			grantMap.put(grant.getGrantee().getIdentifier(), permissionStrings);
		}
		aclMap.put("ACL", grantMap);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return new StringBuilder(gson.toJson(aclMap));
	}
	
	private void printAcl(AccessControlList acl){
		System.out.println(generateAclStringBuilder(acl));
	}
	
	public static void main(String[] argv){
		MainCLI client = new MainCLI();
		client.cliRunner(argv);
	}
}
