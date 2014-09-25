/*
 * Copyright 2011-2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sina.cloudstorage.services.scs.internal;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.sina.cloudstorage.SCSWebServiceResponse;
import com.sina.cloudstorage.http.HttpResponse;
import com.sina.cloudstorage.transform.Unmarshaller;



/**
 * An XML response handler that can also process an arbitrary number of headers
 * in the response.
 */
public class ResponseHeaderHandlerChain <T> extends S3JsonResponseHandler<T> {

    private final List<HeaderHandler<T>> headerHandlers;
    
    public ResponseHeaderHandlerChain(Unmarshaller<T, InputStream> responseUnmarshaller, HeaderHandler<T>... headerHandlers) {
        super(responseUnmarshaller);
        this.headerHandlers = Arrays.asList(headerHandlers);
    }

    /* (non-Javadoc)
     * @see com.amazonaws.services.s3.internal.S3XmlResponseHandler#handle(com.amazonaws.http.HttpResponse)
     */
    @Override
    public SCSWebServiceResponse<T> handle(HttpResponse response) throws Exception {
        SCSWebServiceResponse<T> awsResponse = super.handle(response);
        
        T result = awsResponse.getResult();
        if (result != null) {
            for (HeaderHandler<T> handler : headerHandlers) {
                handler.handle(result, response);
            }
        }
        
        return awsResponse;
    }
}
