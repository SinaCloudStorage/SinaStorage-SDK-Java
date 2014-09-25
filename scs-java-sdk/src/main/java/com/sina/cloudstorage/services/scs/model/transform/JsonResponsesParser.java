package com.sina.cloudstorage.services.scs.model.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.services.scs.internal.Constants;
import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.BucketInfo;
import com.sina.cloudstorage.services.scs.model.InitiateMultipartUploadResult;
import com.sina.cloudstorage.services.scs.model.ObjectInfo;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.Owner;
import com.sina.cloudstorage.services.scs.model.PartListing;

public class JsonResponsesParser {
    private static final Log log = LogFactory.getLog(JsonResponsesParser.class);
   
    /**
     * Parses a ListBucket response json document from an input stream.
     *
     *	{
	 *	    "Delimiter": null,
	 *	    "Prefix": null,
	 *	    "CommonPrefixes": [],
	 *	    "Marker": null,
	 *	    "ContentsQuantity": 10,
	 *	    "CommonPrefixesQuantity": 0,
	 *	    "NextMarker": null,
	 *	    "IsTruncated": false,
	 *	    "Contents": [
	 *	        {
	 *	            "SHA1": "4a09518d3c402d0a444e2f6c964a1b5xxxxxx",
	 *	            "Name": "/aaa/file.txt",
	 *	            "Expiration-Time": null,
	 *	            "Last-Modified": "Mon, 31 Mar 2014 08:53:41 UTC",
	 *	            "Owner": "SINA000000100xxxxxx",
	 *	            "MD5": "49c60d1ef444d46939xxxxxxxxxx",
	 *	            "Content-Type": "text/plain",
	 *	            "Size": 48
	 *	        },
	 *	        ...
	 *	    ]
	 *	}
     *
     * @param inputStream
     *            json data input stream.
     * @return the json handler object populated with data parsed from the json
     *         stream.
     * @throws SCSClientException
     */
    public ObjectListing parseListBucketObjectsResponse(InputStream inputStream)
            throws SCSClientException {
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            Gson gson = new Gson();
            @SuppressWarnings("unchecked")
			Map<String,Object> modelObject = gson.fromJson(breader, Map.class);//mapper.readValue(breader,Map.class);
            
            return new ObjectListing(modelObject);
            
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            t.printStackTrace();
            throw new SCSClientException("Failed to parse json document with handler --"+ t.getLocalizedMessage(), t);
        }
    	
    }

    /**
     * Parses a ListAllMyBuckets response json document from an input stream.
     *
     *	{
     *       "Owner": {
     *           "DisplayName": "",
     *           "ID": "SINA000000xxxxx"
     *       },
     *       "Buckets": [
     *           {
     *               "ConsumedBytes": 22536776,
     *               "CreationDate": "Fri, 28 Mar 2014 09:07:45 UTC",
     *               "Name": "test11"
     *           },
     *           {
     *               "ConsumedBytes": 0,
     *               "CreationDate": "Tue, 01 Apr 2014 03:28:32 UTC",
     *               "Name": "asdasdasdasd"
     *           }
     *       ]
     *   }
     *
     *
     * @param inputStream
     *            json data input stream.
     * @return the json handler object populated with data parsed from the json
     *         stream.
     * @throws SCSClientException
     */
    @SuppressWarnings("unchecked")
    public List<Bucket> parseListMyBucketsResponse(InputStream inputStream)
            throws SCSClientException {
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            List<Bucket> resultList = new ArrayList<Bucket>();
            Gson gson = new Gson();
			Map<String,Object> modelObject = gson.fromJson(breader, Map.class);
			if (modelObject != null && modelObject.get("Buckets") != null){
				Map<String,String> ownerMap = (Map<String,String>) modelObject.get("Owner");
				Owner owner = new Owner((String)ownerMap.get("ID"), (String)ownerMap.get("DisplayName"));
				
				List<Map<String,Object>> bucketList = (List<Map<String,Object>>) modelObject.get("Buckets");
	        	for(Map<String,Object> bucketMap : bucketList)
	        		resultList.add(new Bucket(bucketMap,owner));
	        }
            
            return resultList;
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            t.printStackTrace();
            throw new SCSClientException("Failed to parse json document with handler ", t);
        }
    	
    }

    /**
     * Parses an AccessControlListHandler response json document from an input
     * stream.
     *
     *   {
	 *       "Owner": "SINA000000xxxxx",
	 *       "ACL": {
	 *           "SINA000000xxxxx": [
	 *               "read",
	 *               "write",
	 *               "read_acp",
	 *               "write_acp"
	 *           ]
	 *       }
	 *   }
     *
     * @param inputStream
     *            json data input stream.
     * @return the json handler object populated with data parsed from the json
     *         stream.
     *
     * @throws SCSClientException
     */
    public AccessControlList parseAccessControlListResponse(InputStream inputStream)
        throws SCSClientException{
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			Map<String,Object> jsonObject = gson.fromJson(breader, Map.class);
			if(jsonObject!=null)
				return new AccessControlList(jsonObject);
            
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            throw new SCSClientException("Failed to parse json document with handler ", t);
        }
    	
    	return null;
    	
    }
    
    /**
     * Parse ObjectInfo Response
     * @param inputStream
     * @return
     * @throws SCSClientException
     */
    public ObjectInfo parseObjectInfoResponse(InputStream inputStream)
        throws SCSClientException{
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			Map<String,Object> jsonObject = gson.fromJson(breader, Map.class);
			if(jsonObject!=null)
				return new ObjectInfo(jsonObject);
            
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            t.printStackTrace();
            throw new SCSClientException("Failed to parse json document with handler ", t);
        }
    	
    	return null;
    }
    
    /**
     * Parse BucketInfo Response
     * @param inputStream
     * @return
     * @throws SCSClientException
     */
    public BucketInfo parseBucketInfoResponse(InputStream inputStream)
        throws SCSClientException{
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			Map<String,Object> jsonObject = gson.fromJson(breader, Map.class);
			if(jsonObject!=null)
				return new BucketInfo(jsonObject);
            
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            throw new SCSClientException("Failed to parse json document with handler ", t);
        }
    	
    	return null;
    }

    /**
     * 
     *  {
	 *	    "Bucket": "<Your-Bucket-Name>",
	 *	    "Key": "<ObjectName>",
	 *	    "UploadId": "7517c1c49a3b4b86a5f08858290c5cf6"
	 *	}
     * @param inputStream
     * @return
     * @throws SCSClientException
     */
    public InitiateMultipartUploadResult parseInitiateMultipartUploadResponse(InputStream inputStream)
        throws SCSClientException
    {
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			Map<String,String> jsonObject = gson.fromJson(breader,Map.class);
			if(jsonObject!=null)
				return new InitiateMultipartUploadResult(jsonObject);
            
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            throw new SCSClientException("Failed to parse json document with handler ", t);
        }
    	
    	return null;
    }

    /**
     *   * {
	 *	    "Bucket": "<Your-Bucket-Name>",
	 *	
	 *	    "Key": "<ObjectName>",
	 *	
	 *	    "Initiator": {
	 *		
	 *	        "ID": "<ID>",
	 *	        "DisplayName": "<DisplayName>"
	 *	    },
	 *	
	 *	    "PartNumberMarker": null,
	 *	
	 *	    "NextPartNumberMarker": null,
	 *	
	 *	    "MaxParts": null,
	 *	
	 *	    "IsTruncated": false,
	 *	
	 *	    "Part": [
	 *	
	 *	        {
	 *	            "PartNumber": 1,
	 *	            "Last-Modified": "Wed, 20 Jun 2012 14:57:10 UTC",
	 *	            "ETag": "050fdc0e690bfae7b29392f152bcf301",
	 *	            "Size": 1024
	 *	        },
	 *		
	 *	        {
	 *	            "PartNumber": 2,
	 *	            "Last-Modified": "Wed, 20 Jun 2012 14:57:10 UTC",
	 *	            "ETag": "050fdc0e690bfae7b29392f152bcf302",
	 *	            "Size": 1024
	 *	        },
	 *		
	 *	        {
	 *	            "PartNumber": 3,
	 *	            "Last-Modified": "Wed, 20 Jun 2012 14:57:10 UTC",
	 *	            "ETag": "050fdc0e690bfae7b29392f152bcf303",
	 *	            "Size": 1024
	 *	        },
	 *		
	 *	        ...
	 *	    ]
	 *		
	 *	}
	 *
     * @param inputStream
     * @return
     * @throws SCSClientException
     */
    public PartListing parseListPartsResponse(InputStream inputStream)
        throws SCSClientException
    {
    	try {
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            
            Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			Map<String,Object> jsonObject = gson.fromJson(breader,Map.class);
			if(jsonObject!=null)
				return new PartListing(jsonObject);
            
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after json parse failure", e);
                }
            }
            throw new SCSClientException("Failed to parse json document with handler ", t);
        }
    	
    	return null;
    }
    
}
