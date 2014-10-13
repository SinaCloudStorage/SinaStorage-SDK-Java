SCS-JAVA-CLI
===================
新浪云存储 JAVA [命令行工具]

### Requirements

* commons-cli
* scs-java-sdk

### 调用示例 & 命令行工具

```
# 设置环境变量:

$ export S3_ACCESS_KEY_ID="您的access key"
$ export S3_SECRET_ACCESS_KEY="您的secret key"

# 使用命令行:

$ java -Dfile.encoding=utf-8 -jar ./SCS-JAVA-CLI.jar -help

This is a program for performing single requests to Sina Cloud Storage.

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

```