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
package com.sina.cloudstorage.services.scs.model.transform;

import java.io.InputStream;
import java.util.List;

import com.sina.cloudstorage.services.scs.model.AccessControlList;
import com.sina.cloudstorage.services.scs.model.Bucket;
import com.sina.cloudstorage.services.scs.model.BucketInfo;
import com.sina.cloudstorage.services.scs.model.InitiateMultipartUploadResult;
import com.sina.cloudstorage.services.scs.model.ObjectInfo;
import com.sina.cloudstorage.services.scs.model.ObjectListing;
import com.sina.cloudstorage.services.scs.model.PartListing;
import com.sina.cloudstorage.transform.Unmarshaller;


/**
 * Collection of unmarshallers for S3 XML responses.
 */
public class Unmarshallers {

    /**
     * Unmarshaller for the ListBuckets XML response.
     */
    public static final class ListBucketsUnmarshaller implements
            Unmarshaller<List<Bucket>, InputStream> {
		public List<Bucket> unmarshall(InputStream in) throws Exception {
            return new JsonResponsesParser().parseListMyBucketsResponse(in);
        }
    }

    /**
     * Unmarshaller for the ListObjects XML response.
     */
    public static final class ListObjectsUnmarshaller implements
            Unmarshaller<ObjectListing, InputStream> {
        public ObjectListing unmarshall(InputStream in) throws Exception {
            return new JsonResponsesParser().parseListBucketObjectsResponse(in);
        }
    }

    /**
     * Unmarshaller for the AccessControlList XML response.
     */
    public static final class AccessControlListUnmarshaller implements
            Unmarshaller<AccessControlList, InputStream> {
        public AccessControlList unmarshall(InputStream in) throws Exception {
            return new JsonResponsesParser().parseAccessControlListResponse(in);
        }
    }

    /**
     * Unmarshaller for the a direct InputStream response.
     */
//    public static final class InputStreamUnmarshaller implements
//           Unmarshaller<InputStream, InputStream> {
//        public InputStream unmarshall(InputStream in) throws Exception {
//            return in;
//        }
//    }

    public static final class InitiateMultipartUploadResultUnmarshaller implements
            Unmarshaller<InitiateMultipartUploadResult, InputStream> {
        public InitiateMultipartUploadResult unmarshall(InputStream in) throws Exception {
            return new JsonResponsesParser().parseInitiateMultipartUploadResponse(in);
        }
    }

    public static final class ListPartsResultUnmarshaller implements
        Unmarshaller<PartListing, InputStream> {
        public PartListing unmarshall(InputStream in) throws Exception {
            return new JsonResponsesParser().parseListPartsResponse(in);
        }
    }
    
	public static final class ObjectInfoUnmarshaller implements
			Unmarshaller<ObjectInfo, InputStream> {
		public ObjectInfo unmarshall(InputStream in) throws Exception {
			return new JsonResponsesParser().parseObjectInfoResponse(in);
		}
	}
	
	public static final class BucketInfoUnmarshaller implements
			Unmarshaller<BucketInfo, InputStream> {
		public BucketInfo unmarshall(InputStream in) throws Exception {
			return new JsonResponsesParser().parseBucketInfoResponse(in);
		}
	}
    
}
