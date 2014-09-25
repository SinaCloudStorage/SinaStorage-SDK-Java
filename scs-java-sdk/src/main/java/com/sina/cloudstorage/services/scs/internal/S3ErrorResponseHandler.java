/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.sina.cloudstorage.services.scs.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sina.cloudstorage.services.scs.Headers;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.SCSServiceException.ErrorType;
import com.sina.cloudstorage.http.HttpMethodName;
import com.sina.cloudstorage.http.HttpResponse;
import com.sina.cloudstorage.http.HttpResponseHandler;
import org.apache.http.Header;
import com.sina.cloudstorage.services.scs.model.SCSS3Exception;


/**
 * Response handler for S3 error responses. S3 error responses are different
 * from other AWS error responses in a few ways. Most error responses will
 * contain an XML body, but not all (ex: error responses to HEAD requests will
 * not), so this error handler has to account for that. The actual XML error
 * response body is slightly different than other services like SimpleDB or EC2
 * and some information isn't explicitly represented in the XML error response
 * body (ex: error type/fault information) so it has to be inferred from other
 * parts of the error response.
 */
public class S3ErrorResponseHandler
        implements HttpResponseHandler<SCSServiceException> {
	 private static final Log log = LogFactory.getLog(S3ErrorResponseHandler.class);
    /**
     * @see com.amazonaws.http.HttpResponseHandler#handle(com.amazonaws.http.HttpResponse)
     */
    public SCSServiceException handle(HttpResponse errorResponse)
            throws Exception {
        /*
         * We don't always get an error response body back from S3. When we send
         * a HEAD request, we don't receive a body, so we'll have to just return
         * what we can.
         */
        if (errorResponse.getContent() == null
                || errorResponse.getRequest().getHttpMethod() == HttpMethodName.HEAD) {
            String requestId = errorResponse.getHeaders().get(Headers.REQUEST_ID);
            String extendedRequestId = errorResponse.getHeaders().get(Headers.EXTENDED_REQUEST_ID);
            SCSS3Exception ase = new SCSS3Exception(errorResponse.getStatusText());
            ase.setStatusCode(errorResponse.getStatusCode());
            ase.setRequestId(requestId);
            ase.setExtendedRequestId(extendedRequestId);
            fillInErrorType(ase, errorResponse);
            return ase;
        }

        if (log.isDebugEnabled()) {
            log.debug(errorResponse.getHttpRequest().getURI());
            for(Header h : errorResponse.getHttpRequest().getAllHeaders())
            	log.debug(h);
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(errorResponse.getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
          buffer.append(line);
        }
        
        SCSS3Exception ase = new SCSS3Exception(buffer.toString());
        ase.setStatusCode(errorResponse.getStatusCode());
//        ase.setErrorCode(errorCode);
//        ase.setRequestId(requestId);
//        ase.setExtendedRequestId(extendedRequestId);
        fillInErrorType(ase, errorResponse);

        return ase;
    }

    /**
     * Fills in the AWS error type information in the specified
     * AmazonServiceException by looking at the HTTP status code in the error
     * response. S3 error responses don't explicitly declare a sender or client
     * fault like other AWS services, so we have to use the HTTP status code to
     * infer this information.
     *
     * @param ase
     *            The AmazonServiceException to populate with error type
     *            information.
     * @param errorResponse
     *            The HTTP error response to use to determine the right error
     *            type to set.
     */
    private void fillInErrorType(SCSServiceException ase, HttpResponse errorResponse) {
        if (errorResponse.getStatusCode() >= 500) {
            ase.setErrorType(ErrorType.Service);
        } else {
            ase.setErrorType(ErrorType.Client);
        }
    }

    /**
     * Since this response handler completely consumes all the data from the
     * underlying HTTP connection during the handle method, we don't need to
     * keep the HTTP connection open.
     *
     * @see com.amazonaws.http.HttpResponseHandler#needsConnectionLeftOpen()
     */
    public boolean needsConnectionLeftOpen() {
        return false;
    }

}
