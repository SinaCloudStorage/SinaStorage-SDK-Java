package com.sina.cloudstorage.http;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sina.cloudstorage.ClientConfiguration;
import com.sina.cloudstorage.Request;
import com.sina.cloudstorage.RequestClientOptions;
import com.sina.cloudstorage.Response;
import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.SCSServiceException.ErrorType;
import com.sina.cloudstorage.SCSWebServiceRequest;
//import com.reader.epubreader.cm.utils.cookiestore.PersistentCookieStore;
import com.sina.cloudstorage.SCSWebServiceResponse;
import com.sina.cloudstorage.SDKGlobalConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import com.sina.cloudstorage.retry.RetryUtils;
import com.sina.cloudstorage.retry.RetryPolicy;
import com.sina.cloudstorage.util.DateUtils;

/**
 * HTTP请求相应处理类
 * @author hanchao
 * created by May 5, 2010 5:28:34 PM
 */
public class HttpConnect {
	
	private static final String HEADER_USER_AGENT = "User-Agent";

	private HttpClient httpClient;
	
	ClientConfiguration config;
	
	/**
     * Logger for more detailed debugging information, that might not be as
     * useful for end users (ex: HTTP client configuration, etc).
     */
    static final Log log = LogFactory.getLog("com.sina.cloudstorage.request");
	
	HttpRequestBase httpRequest;
	
	private static final HttpRequestFactory httpRequestFactory = new HttpRequestFactory();
    private static final HttpClientFactory httpClientFactory = new HttpClientFactory();
	
	public HttpConnect(ClientConfiguration config) {
		this.config = config;
		this.httpClient = httpClientFactory.createHttpClient(config);
	}

	 /**
     * Executes the request and returns the result.
     *
     * @param request
     *            The AmazonWebServices request to send to the remote server
     * @param responseHandler
     *            A response handler to accept a successful response from the
     *            remote server
     * @param errorResponseHandler
     *            A response handler to accept an unsuccessful response from the
     *            remote server
     * @param executionContext
     *            Additional information about the context of this web service
     *            call
     */
    public <T> Response<T> execute(Request<?> request,
            HttpResponseHandler<SCSWebServiceResponse<T>> responseHandler,
            HttpResponseHandler<SCSServiceException> errorResponseHandler,
            ExecutionContext executionContext) throws SCSClientException, SCSServiceException {
        if (executionContext == null)
            throw new SCSClientException("Internal SDK Error: No execution context parameter specified.");
//        List<RequestHandler2> requestHandler2s = requestHandler2s(request, executionContext);
//        final AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();
        Response<T> response = null;
        try {
            response = executeHelper(request, responseHandler, errorResponseHandler, executionContext);
//            TimingInfo timingInfo = awsRequestMetrics.getTimingInfo().endTiming();
//            afterResponse(request, requestHandler2s, response, timingInfo);
            return response;
        } catch (SCSClientException e) {
//            afterError(request, response, requestHandler2s, e);
            throw e;
        }
    }
    
    /**
     * Internal method to execute the HTTP method given.
     *
     * @see AmazonHttpClient#execute(Request, HttpResponseHandler, HttpResponseHandler)
     * @see AmazonHttpClient#execute(Request, HttpResponseHandler, HttpResponseHandler, ExecutionContext)
     */
    private <T> Response<T> executeHelper(Request<?> request,
            HttpResponseHandler<SCSWebServiceResponse<T>> responseHandler,
            HttpResponseHandler<SCSServiceException> errorResponseHandler,
            ExecutionContext executionContext)
            throws SCSClientException, SCSServiceException
    {
        /*
         * Depending on which response handler we end up choosing to handle the
         * HTTP response, it might require us to leave the underlying HTTP
         * connection open, depending on whether or not it reads the complete
         * HTTP response stream from the HTTP connection, or if delays reading
         * any of the content until after a response is returned to the caller.
         */
        boolean leaveHttpConnectionOpen = false;
//        AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();
        /* add the service endpoint to the logs. You can infer service name from service endpoint */
//        awsRequestMetrics.addProperty(Field.ServiceName, request.getServiceName());
//        awsRequestMetrics.addProperty(Field.ServiceEndpoint, request.getEndpoint());
        // Apply whatever request options we know how to handle, such as user-agent.
        setUserAgent(request);
        int requestCount = 0;
        URI redirectedURI = null;
        HttpEntity entity = null;
        SCSClientException retriedException = null;

        // Make a copy of the original request params and headers so that we can
        // permute it in this loop and start over with the original every time.
        Map<String, String> originalParameters = new HashMap<String, String>();
        originalParameters.putAll(request.getParameters());
        Map<String, String> originalHeaders = new HashMap<String, String>();
        originalHeaders.putAll(request.getHeaders());

        while (true) {
            ++requestCount;
//            awsRequestMetrics.setCounter(Field.RequestCount, requestCount);
            if (requestCount > 1) { // retry
                request.setParameters(originalParameters);
                request.setHeaders(originalHeaders);
            }

            HttpRequestBase httpRequest = null;
            org.apache.http.HttpResponse apacheResponse = null;

            try {
                // Sign the request if a signer was provided
                if (executionContext.getSigner() != null && executionContext.getCredentials() != null) {
//                    awsRequestMetrics.startEvent(Field.RequestSigningTime);
                    try {
						executionContext.getSigner().sign(request,
								executionContext.getCredentials());
                    } finally {
//                        awsRequestMetrics.endEvent(Field.RequestSigningTime);
                    }
                }

                 if (log.isDebugEnabled()) {
                    log.debug("Sending Request: " + request.toString());
                 }

                httpRequest = httpRequestFactory.createHttpRequest(request, config, entity, executionContext);

                if (httpRequest instanceof HttpEntityEnclosingRequest) {
                    entity = ((HttpEntityEnclosingRequest)httpRequest).getEntity();
                }

                if (redirectedURI != null) {
                    httpRequest.setURI(redirectedURI);
                }

                if (requestCount > 1) {   // retry
//                    awsRequestMetrics.startEvent(Field.RetryPauseTime);
                    try {
                        pauseBeforeNextRetry(request.getOriginalRequest(),
                                             retriedException,
                                             requestCount,
                                             config.getRetryPolicy());
                    } finally {
//                        awsRequestMetrics.endEvent(Field.RetryPauseTime);
                    }
                }

                if ( entity != null ) {
                    InputStream content = entity.getContent();
                    if ( requestCount > 1 ) {   // retry
                        if ( content.markSupported() ) {
                            content.reset();
                            content.mark(-1);
                        }
                    } else {
                        if ( content.markSupported() ) {
                            content.mark(-1);
                        }
                    }
                }
                
                HttpContext httpContext = new BasicHttpContext();
//                httpContext.setAttribute(
//                        AWSRequestMetrics.class.getSimpleName(),
//                        awsRequestMetrics);
                retriedException = null;
//                awsRequestMetrics.startEvent(Field.HttpRequestTime);
                try {
                    apacheResponse = httpClient.execute(httpRequest, httpContext);
                } finally {
//                    awsRequestMetrics.endEvent(Field.HttpRequestTime);
                }

                if (isRequestSuccessful(apacheResponse)) {
//                    awsRequestMetrics.addProperty(Field.StatusCode, apacheResponse.getStatusLine().getStatusCode());
                    /*
                     * If we get back any 2xx status code, then we know we should
                     * treat the service call as successful.
                     */
                    leaveHttpConnectionOpen = responseHandler
                            .needsConnectionLeftOpen();
                    HttpResponse httpResponse = createResponse(httpRequest,
                            request, apacheResponse);
                    T response = handleResponse(request, responseHandler,
                            httpRequest, httpResponse, apacheResponse,
                            executionContext);
                    return new Response<T>(response, httpResponse);
                } else if (isTemporaryRedirect(apacheResponse)) {
                    /*
                     * S3 sends 307 Temporary Redirects if you try to delete an
                     * EU bucket from the US endpoint. If we get a 307, we'll
                     * point the HTTP method to the redirected location, and let
                     * the next retry deliver the request to the right location.
                     */
                    Header[] locationHeaders = apacheResponse.getHeaders("location");
                    String redirectedLocation = locationHeaders[0].getValue();
                    log.debug("Redirecting to: " + redirectedLocation);
                    redirectedURI = URI.create(redirectedLocation);
                    httpRequest.setURI(redirectedURI);
//                    awsRequestMetrics.addProperty(Field.StatusCode, apacheResponse.getStatusLine().getStatusCode());
//                    awsRequestMetrics.addProperty(Field.RedirectLocation, redirectedLocation);
//                    awsRequestMetrics.addProperty(Field.AWSRequestID, null);

                } else {
                    leaveHttpConnectionOpen = errorResponseHandler.needsConnectionLeftOpen();
                    SCSServiceException ase = handleErrorResponse(request, errorResponseHandler, httpRequest, apacheResponse);
//                    awsRequestMetrics.addProperty(Field.AWSRequestID, ase.getRequestId());
//                    awsRequestMetrics.addProperty(Field.AWSErrorCode, ase.getErrorCode());
//                    awsRequestMetrics.addProperty(Field.StatusCode, ase.getStatusCode());
                    
                	if (!shouldRetry(request.getOriginalRequest(),
				            httpRequest,
				            ase,
				            requestCount,
				            config.getRetryPolicy())) {
                        throw ase;
                    }

                    // Cache the retryable exception
                    retriedException = ase;
                    /*
                     * Checking for clock skew error again because we don't want to set the
                     * global time offset for every service exception.
                     */
                    if(RetryUtils.isClockSkewError(ase)) {
                        int timeOffset = parseClockSkewOffset(apacheResponse, ase);
                        SDKGlobalConfiguration.setGlobalTimeOffset(timeOffset);
                    }
                    resetRequestAfterError(request, ase);
                }
            } catch (IOException ioe) {
                if (log.isInfoEnabled()) {
                    log.info("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
                }
//                awsRequestMetrics.incrementCounter(Field.Exception);
//                awsRequestMetrics.addProperty(Field.Exception, ioe);
//                awsRequestMetrics.addProperty(Field.AWSRequestID, null);

                SCSClientException ace = new SCSClientException("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
                if (!shouldRetry(request.getOriginalRequest(),
				            httpRequest,
				            ace,
				            requestCount,
				            config.getRetryPolicy())) {
                    throw ace;
                }
                
                // Cache the retryable exception
                retriedException = ace;
                resetRequestAfterError(request, ioe);
            } catch(RuntimeException e) {
//                throw handleUnexpectedFailure(e, awsRequestMetrics);
            	throw e;
            } catch(Error e) {
//                throw handleUnexpectedFailure(e, awsRequestMetrics);
            	throw e;
            } finally {
                /*
                 * Some response handlers need to manually manage the HTTP
                 * connection and will take care of releasing the connection on
                 * their own, but if this response handler doesn't need the
                 * connection left open, we go ahead and release the it to free
                 * up resources.
                 */
                if (!leaveHttpConnectionOpen) {
                    try {
                        if (apacheResponse != null && apacheResponse.getEntity() != null
                                && apacheResponse.getEntity().getContent() != null) {
                            apacheResponse.getEntity().getContent().close();
                        }
                    } catch (IOException e) {
                        log.warn("Cannot close the response content.", e);
                    }
                }
            }
        } /* end while (true) */
    }
    
    /**
     * Sets a User-Agent for the specified request, taking into account
     * any custom data.
     */
    private void setUserAgent(Request<?> request) {
        String userAgent = config.getUserAgent();
        if ( !userAgent.equals(ClientConfiguration.DEFAULT_USER_AGENT) ) {
            userAgent += ", " + ClientConfiguration.DEFAULT_USER_AGENT;
        }
        if ( userAgent != null ) {
            request.addHeader(HEADER_USER_AGENT, userAgent);
        }
        SCSWebServiceRequest awsreq = request.getOriginalRequest();
        if (awsreq != null) {
            RequestClientOptions opts = awsreq.getRequestClientOptions();
            if (opts != null) {
                String userAgentMarker = opts.getClientMarker(com.sina.cloudstorage.RequestClientOptions.Marker.USER_AGENT);
                if (userAgentMarker != null) {
                    request.addHeader(HEADER_USER_AGENT,
                        createUserAgentString(userAgent, userAgentMarker));
                }
            }
        }
    }
    
    /**
     * Appends the given user-agent string to the existing one and returns it.
     */
    private static String createUserAgentString(String existingUserAgentString, String userAgent) {
        if (existingUserAgentString.contains(userAgent)) {
            return existingUserAgentString;
        } else {
            return existingUserAgentString.trim() + " " + userAgent.trim();
        }
    }
    
    /**
     * Sleep for a period of time on failed request to avoid flooding a service
     * with retries.
     * 
     * @param originalRequest
     *            The original service request that is being executed.
     * @param previousException
     *            Exception information for the previous attempt, if any.
     * @param requestCount
     *            current request count (including the next attempt after the delay)
     * @param retryPolicy
     *            The retry policy configured in this http client.
     */
    private void pauseBeforeNextRetry(SCSWebServiceRequest originalRequest,
                                    SCSClientException previousException,
                                    int requestCount,
                                    RetryPolicy retryPolicy) {

        final int retries = requestCount // including next attempt
                            - 1          // number of attempted requests
                            - 1;         // number of attempted retries
        
        long delay = retryPolicy.getBackoffStrategy().delayBeforeNextRetry(
                originalRequest, previousException, retries);
        
        if (log.isDebugEnabled()) {
            log.debug("Retriable error detected, " +
                    "will retry in " + delay + "ms, attempt number: " + retries);
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SCSClientException(e.getMessage(), e);
        }
    }

    private boolean isRequestSuccessful(org.apache.http.HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status / 100 == HttpStatus.SC_OK / 100;
    }
    
    /**
     * Creates and initializes an HttpResponse object suitable to be passed to
     * an HTTP response handler object.
     *
     * @param method
     *            The HTTP method that was invoked to get the response.
     * @param request
     *            The HTTP request associated with the response.
     *
     * @return The new, initialized HttpResponse object ready to be passed to an
     *         HTTP response handler object.
     *
     * @throws IOException
     *             If there were any problems getting any response information
     *             from the HttpClient method object.
     */
    private HttpResponse createResponse(HttpRequestBase method, Request<?> request, org.apache.http.HttpResponse apacheHttpResponse) throws IOException {
        HttpResponse httpResponse = new HttpResponse(request, method);

        if (apacheHttpResponse.getEntity() != null) {
            httpResponse.setContent(apacheHttpResponse.getEntity().getContent());
        }

        httpResponse.setStatusCode(apacheHttpResponse.getStatusLine().getStatusCode());
        httpResponse.setStatusText(apacheHttpResponse.getStatusLine().getReasonPhrase());
        for (Header header : apacheHttpResponse.getAllHeaders()) {
            httpResponse.addHeader(header.getName(), header.getValue());
        }

        return httpResponse;
    }
    
    /**
     * Handles a successful response from a service call by unmarshalling the
     * results using the specified response handler.
     *
     * @param <T>
     *            The type of object expected in the response.
     *
     * @param request
     *            The original request that generated the response being
     *            handled.
     * @param responseHandler
     *            The response unmarshaller used to interpret the contents of
     *            the response.
     * @param method
     *            The HTTP method that was invoked, and contains the contents of
     *            the response.
     * @param executionContext
     *            Extra state information about the request currently being
     *            executed.
     * @return The contents of the response, unmarshalled using the specified
     *         response handler.
     *
     * @throws IOException
     *             If any problems were encountered reading the response
     *             contents from the HTTP method object.
     */
    private <T> T handleResponse(Request<?> request,
            HttpResponseHandler<SCSWebServiceResponse<T>> responseHandler,
            HttpRequestBase method, HttpResponse httpResponse,
            org.apache.http.HttpResponse apacheHttpResponse,
            ExecutionContext executionContext) throws IOException
    {
        if (responseHandler.needsConnectionLeftOpen() && method instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest)method;
            httpResponse.setContent(new HttpMethodReleaseInputStream(httpEntityEnclosingRequest));
        }

        try {
//            com.sina.cloudstorage.util.CountingInputStream countingInputStream = null;
//            if (System.getProperty(PROFILING_SYSTEM_PROPERTY) != null) {
//                countingInputStream = new CountingInputStream(httpResponse.getContent());
//                httpResponse.setContent(countingInputStream);
//            }
//
//            AWSRequestMetrics awsRequestMetrics = executionContext.getAwsRequestMetrics();
            SCSWebServiceResponse<? extends T> awsResponse;
//            awsRequestMetrics.startEvent(Field.ResponseProcessingTime);
            try {
                awsResponse = responseHandler.handle(httpResponse);
            } finally {
//                awsRequestMetrics.endEvent(Field.ResponseProcessingTime);
            }
//            if (countingInputStream != null) {
//                awsRequestMetrics.setCounter(Field.BytesProcessed, countingInputStream.getByteCount());
//            }

            if (awsResponse == null)
                throw new RuntimeException("Unable to unmarshall response metadata");

//            responseMetadataCache.add(request.getOriginalRequest(), awsResponse.getResponseMetadata());

//            if (requestLog.isDebugEnabled()) {
//                requestLog.debug("Received successful response: " + apacheHttpResponse.getStatusLine().getStatusCode()
//                        + ", AWS Request ID: " + awsResponse.getRequestId());
//            }
//            awsRequestMetrics.addProperty(Field.AWSRequestID, awsResponse.getRequestId());

            return awsResponse.getResult();
//        } catch (CRC32MismatchException e) {
//            throw e;
        } catch (Exception e) {
        	e.printStackTrace();
            String errorMessage = "Unable to unmarshall response (" + e.getMessage() + ")";
            throw new SCSClientException(errorMessage, e);
        }
    }
    
    private static boolean isTemporaryRedirect(org.apache.http.HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status == HttpStatus.SC_TEMPORARY_REDIRECT &&
                         response.getHeaders("Location") != null &&
                         response.getHeaders("Location").length > 0;
    }

    /**
     * Responsible for handling an error response, including unmarshalling the
     * error response into the most specific exception type possible, and
     * throwing the exception.
     *
     * @param request
     *            The request that generated the error response being handled.
     * @param errorResponseHandler
     *            The response handler responsible for unmarshalling the error
     *            response.
     * @param method
     *            The HTTP method containing the actual response content.
     *
     * @throws IOException
     *             If any problems are encountering reading the error response.
     */
    private SCSServiceException handleErrorResponse(Request<?> request,
            HttpResponseHandler<SCSServiceException> errorResponseHandler,
            HttpRequestBase method, org.apache.http.HttpResponse apacheHttpResponse) throws IOException {

        int status = apacheHttpResponse.getStatusLine().getStatusCode();
        HttpResponse response = createResponse(method, request, apacheHttpResponse);
        if (errorResponseHandler.needsConnectionLeftOpen() && method instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityEnclosingRequest = (HttpEntityEnclosingRequestBase)method;
            response.setContent(new HttpMethodReleaseInputStream(entityEnclosingRequest));
        }

        SCSServiceException exception = null;
        try {
            exception = errorResponseHandler.handle(response);
//            requestLog.debug("Received error response: " + exception.toString());
        } catch (Exception e) {
            // If the errorResponseHandler doesn't work, then check for error
            // responses that don't have any content
            if (status == 413) {
                exception = new SCSServiceException("Request entity too large");
                exception.setServiceName(request.getServiceName());
                exception.setStatusCode(413);
                exception.setErrorType(ErrorType.Client);
                exception.setErrorCode("Request entity too large");
            } else if (status == 503 && "Service Unavailable".equalsIgnoreCase(apacheHttpResponse.getStatusLine().getReasonPhrase())) {
                exception = new SCSServiceException("Service unavailable");
                exception.setServiceName(request.getServiceName());
                exception.setStatusCode(503);
                exception.setErrorType(ErrorType.Service);
                exception.setErrorCode("Service unavailable");
            } else {
                String errorMessage = "Unable to unmarshall error response (" + e.getMessage() + ")";
                throw new SCSClientException(errorMessage, e);
            }
        }

        exception.setStatusCode(status);
        exception.setServiceName(request.getServiceName());
        exception.fillInStackTrace();
        return exception;
    }
    
    /**
     * Returns true if a failed request should be retried.
     * 
     * @param originalRequest
     *            The original service request that is being executed.
     * @param method
     *            The current HTTP method being executed.
     * @param exception
     *            The client/service exception from the failed request.
     * @param requestCount
     *            The number of times the current request has been attempted.
     * 
     * @return True if the failed request should be retried.
     */
    private boolean shouldRetry(SCSWebServiceRequest originalRequest,
                                HttpRequestBase method, 
                                SCSClientException exception, 
                                int requestCount,
                                RetryPolicy retryPolicy) {
        final int retries = requestCount - 1;
        
        int maxErrorRetry = config.getMaxErrorRetry();
        // We should use the maxErrorRetry in
        // the RetryPolicy if either the user has not explicitly set it in
        // ClientConfiguration, or the RetryPolicy is configured to take
        // higher precedence.
        if ( maxErrorRetry < 0
                || !retryPolicy.isMaxErrorRetryInClientConfigHonored() ) {
            maxErrorRetry = retryPolicy.getMaxErrorRetry();
        }
        
        // Immediately fails when it has exceeds the max retry count.
        if (retries >= maxErrorRetry) return false;
        
        // Never retry on requests containing non-repeatable entity
        if (method instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest)method).getEntity();
            if (entity != null && !entity.isRepeatable()) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity not repeatable");
                }
                return false;
            }
        }
        
        // Pass all the context information to the RetryCondition and let it
        // decide whether it should be retried.
        return retryPolicy.getRetryCondition().shouldRetry(originalRequest,
                                                           exception,
                                                           retries);
    }
    
    /**
     * Returns date string from the exception message body in form of yyyyMMdd'T'HHmmss'Z'
     * We needed to extract date from the message body because SQS is the only service
     * that does not provide date header in the response. Example, when device time is
     * behind than the server time than we get a string that looks something like this:
     * "Signature expired: 20130401T030113Z is now earlier than 20130401T034613Z (20130401T040113Z - 15 min.)"
     * 
     * 
     * @param body
     *              The message from where the server time is being extracted
     * 
     * @return Return datetime in string format (yyyyMMdd'T'HHmmss'Z')
     */
    private String getServerDateFromException(String body) {
        int startPos = body.indexOf("(");
        int endPos = 0;
        if(body.contains(" + 15")) {
            endPos = body.indexOf(" + 15");
        } else {
            endPos = body.indexOf(" - 15");
        }
        String msg = body.substring(startPos+1, endPos);
        return msg;
    }
    
    private int parseClockSkewOffset(org.apache.http.HttpResponse response, SCSServiceException exception) {
        DateUtils dateUtils = new DateUtils(); 
        Date deviceDate = new Date();
        Date serverDate = null;
        String serverDateStr = null;
        Header[] responseDateHeader = response.getHeaders("Date");
        
        try {

            if(responseDateHeader.length == 0) {
                // SQS doesn't return Date header
                serverDateStr = getServerDateFromException(exception.getMessage());
                serverDate = dateUtils.parseCompressedIso8601Date(serverDateStr);
            } else {
                serverDateStr = responseDateHeader[0].getValue();
                serverDate = dateUtils.parseRfc822Date(serverDateStr);
            }

        } catch (ParseException e) {
            log.warn("Unable to parse clock skew offset from response: "
                     + serverDateStr,
                     e);
            return 0;
        } catch (RuntimeException e) {
            log.warn("Unable to parse clock skew offset from response: "
                     + serverDateStr,
                     e);
            return 0;
        }
        
        long diff = deviceDate.getTime() - serverDate.getTime();
        return (int)(diff / 1000);
    }
    
    @Override
    protected void finalize() throws Throwable {
        this.shutdown();
        super.finalize();
    }
    
    /**
     * Shuts down this HTTP client object, releasing any resources that might be
     * held open. This is an optional method, and callers are not expected to
     * call it, but can if they want to explicitly release any open resources.
     * Once a client has been shutdown, it cannot be used to make more requests.
     */
    public void shutdown() {
        IdleConnectionReaper.removeConnectionManager(httpClient.getConnectionManager());
        httpClient.getConnectionManager().shutdown();
    }
    
    /**
     * Resets the specified request, so that it can be sent again, after
     * receiving the specified error. If a problem is encountered with resetting
     * the request, then an AmazonClientException is thrown with the original
     * error as the cause (not an error about being unable to reset the stream).
     *
     * @param request
     *            The request being executed that failed and needs to be reset.
     * @param cause
     *            The original error that caused the request to fail.
     *
     * @throws AmazonClientException
     *             If the request can't be reset.
     */
    private void resetRequestAfterError(Request<?> request, Exception cause) throws SCSClientException {
        if ( request.getContent() == null ) {
            return; // no reset needed
        }
        if ( ! request.getContent().markSupported() ) {
            throw new SCSClientException("Encountered an exception and stream is not resettable", cause);
        }
        try {
            request.getContent().reset();
        } catch ( IOException e ) {
            // This exception comes from being unable to reset the input stream,
            // so throw the original, more meaningful exception
            throw new SCSClientException(
                    "Encountered an exception and couldn't reset the stream to retry", cause);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
//	/**
//	 * 发送请求获取服务器响应
//	 * @throws IllegalStateException
//	 * @throws Exception
//	 */
//	private void makeRequest() throws IllegalStateException, Exception {
//	
//	    /*发送请求并等待响应*/
//		HttpResponse httpResponse = httpClient.execute(httpRequest,httpContext);
//		int statusCode = httpResponse.getStatusLine().getStatusCode();
//		//200
//		if (statusCode == 200) {// 206为断点续传状态码，暂时不考虑
//			onDataReceived(httpResponse);
//		} else {// 200以外的响应状态码
//			throw new ErrorResponseException(httpResponse.getStatusLine()
//					.getStatusCode());
//		}
//		
//		httpClient.getConnectionManager().shutdown();
//    }
//	
//	/**
//	 * 服务器响应事件
//	 * @throws IOException 
//	 * @throws IllegalStateException 
//	 * @throws IOException 
//	 */
//	protected void onDataReceived(HttpResponse httpResponse) throws IllegalStateException, IOException,Exception
//	{
//		/*
//		 * 检查Content-Type类型
//		 * 只支持纯文本形式的返回数据，HttpConnect只用来处理纯文本形式的返回数据，
//		 * 二进制文件下载功能可以用后台下载服务ResumeDownloadService！！！！！
//		 * 
//		 * 目前支持的Content-Type类型为：text/* 、 application/*   
//		 * 其他类型均认为二进制文件下载
//		 * 
//		 * 详见：http://www.w3.org/Protocols/rfc1341/4_Content-Type.html
//		 */
//		Header[] contentTypeHeaders = httpResponse.getHeaders("Content-Type");
//		if(contentTypeHeaders!=null&&contentTypeHeaders.length>0){
//			for(Header header : contentTypeHeaders){
//				if(header!=null){
//					String contentType = header.getValue().trim();
//					if(!(contentType.startsWith("text/") || contentType.startsWith("application/"))){
//						throw new Exception("The response Content-Type is not PLAIN TEXT !!!!");
//					}
//				}
//			}
//		}
//		
//		onReceiverHeaders(httpResponse);
//		
//		getResponseEntity(httpResponse);
//	}
//	
//	/**
//	 * 获取响应头参数事件
//	 * 虚函数，当调用HttpConnect时，需要实现onReceiverHeaders(HttpResponse httpResponse)
//	 * 注意：
//	 * 	若不需要获取响应头信息，则只实现空函数体即可.
//	 * 	若需要获取响应头信息，则可以直接调用super.getResponseHeaders(HttpResponse httpResponse)方法,
//	 * 		该方法返回HashMap<String,String>类型的响应头参数
//	 * @param httpResponse
//	 */
//	protected abstract void onReceiverHeaders(HttpResponse httpResponse) throws UnsupportedEncodingException;
//	
////	/**
////	 * 接收相应体触发事件.利用该事件可以实现大文件下载时的进度监控。
////	 * 虚函数，当调用HttpConnect时，需要实现onReceiverBodyStream(InputStream is)
////	 * 注意：
////	 * 	1.该函数是在服务器响应请求时，相应体内有数据的情况下(content-length>0)才会触发。
////	 * 
////	 * 两种读取方式：
////	 * 	1.文本文件类型：
////	 * 		//InputStreamReader isr = new InputStreamReader(is);
////	 *		//BufferedReader br = new BufferedReader(isr);
////	 *		//br.readLine();
////	 *
////	 *	2.文件类型：
////	 *		//读取到的数据长度  
////	 *		//int len;  
////	 *		//1K的数据缓冲  
////	 *		//byte[] bs = new byte[1024];  
////	 *		//while ((len = is.read(bs)) != -1)
////	 *		//	os.write(bs, 0, len);
////	 * @param bytes
////	 * @param len
////	 */
////	protected abstract void onReceiveBodyStream(InputStream is,long contentLength) throws IOException,Exception;
//	
//	/**
//	 * response 响应内容
//	 * @param bodyString
//	 */
//	protected abstract void onReceiveBodyString(String bodyString);
//	
//	/**
//	 * 获取响应头参数
//	 * @param httpResponse
//	 * @return
//	 * @throws UnsupportedEncodingException
//	 */
//	protected Map<String,String> getResponseHeaders(HttpResponse httpResponse) throws UnsupportedEncodingException
//	{
//		/*读取相应头参数*/
//		Map<String,String> resHeaders = new HashMap<String,String>();
//		
//		HeaderIterator hIte = httpResponse.headerIterator();
//		while(hIte.hasNext())
//		{
//			Header header = hIte.nextHeader();
//			String key = header.getName();
//			String value = header.getValue();
//			value = new String(value.getBytes("ISO-8859-1"),"UTF-8");
//			resHeaders.put(key, value);
//		}
//		
//		return resHeaders;
//	}
//	
//	/**
//	 * 获取相应体
//	 * @param httpResponse
//	 * @throws IllegalStateException
//	 * @throws IOException
//	 */
//	protected void getResponseEntity(HttpResponse httpResponse) throws IllegalStateException, IOException,Exception
//	{
//		/*读取响应体*/
//		HttpEntity entity = httpResponse.getEntity();
//		if (entity != null ) {//&& entity.getContentLength() > 0) {
//			
//			entity = new BufferedHttpEntity(entity); 
//
//			onReceiveBodyString(EntityUtils.toString(entity));
//			
////			InputStream is = null;
////			try {
////				is = entity.getContent();
////				
////				onReceiveBodyStream(is,entity.getContentLength());
////			}
////			finally
////			{
////				if(is!=null)
////					is.close();
////			}
//		}
//	}
//	
////	/**
////	 * 设置请求参数
////	 * 
////	 * @throws UnsupportedEncodingException
////	 */
////	private HttpPost getHttpPostRequest(String requestUri)
////			throws UnsupportedEncodingException {
////		
////		HttpPost httpRequest = new HttpPost(requestUri);
////		/*设置请求参数*/
////		HashMap<String, String> reqParams = requestParam.getParams();
////		if(reqParams!=null&&reqParams.size()>0)
////		{
////			ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
////			for(Entry<String,String> entry : reqParams.entrySet()){
////				params.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
////			}
////			
////			httpRequest.setEntity(new UrlEncodedFormEntity(params, requestParam.getCharset()));
////		}
////		
////		/*设置请求头参数*/
////		HashMap<String, String> reqHeaders = requestParam.getHeaders();
////		if (reqHeaders != null && reqHeaders.size() > 0) {
////			for(Entry<String,String> entry : reqHeaders.entrySet()){
////				httpRequest.setHeader(entry.getKey(), entry.getValue());
////			}
////		}
////		
////		/*设置请求实体*/
////		byte[] postStream = requestParam.getPostStream();
////		if(postStream!=null&&postStream.length>0)
////		{
////			InputStream is = new ByteArrayInputStream(postStream);
////	    	InputStreamEntity reqEntity = new InputStreamEntity(is, postStream.length);  
////	    	//reqEntity.setContentType("binary/octet-stream"); 
//////	    	reqEntity.setChunked(true);  //设置chunked 传输模式
////	    	httpRequest.setEntity(reqEntity);
////		}
////		
////		return httpRequest;
////	}
////	
////	private HttpGet getHttpGetRequest(String requestUri){
////		
////		StringBuilder urlSb = new StringBuilder(requestUri);
////		
////		if(!requestUri.contains("?"))
////			urlSb.append("?");
////
////		/*设置请求参数*/
////		HashMap<String, String> reqParams = requestParam.getParams();
////		if (reqParams != null && reqParams.size() > 0) {
////			
////			List<NameValuePair> params = new LinkedList<NameValuePair>();
////			
////			for(Entry<String, String> entry : reqParams.entrySet()){
////				params.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
////			}
////			
////			urlSb.append(URLEncodedUtils.format(params, "utf-8"));
////		}
////		
////		HttpGet httpRequest = new HttpGet(urlSb.toString());
////		
////		/*设置请求头参数*/
////		HashMap<String, String> reqHeaders = requestParam.getHeaders();
////		if (reqHeaders != null && reqHeaders.size() > 0) {
////			for(Entry<String,String> entry : reqHeaders.entrySet()){
////				httpRequest.setHeader(entry.getKey(), entry.getValue());
////			}
////		}
////		
////		return httpRequest;
////	}
//	
//	//压缩文件处理的实体包装类  
//    class GzipDecompressingEntity extends HttpEntityWrapper {//static   
//        public GzipDecompressingEntity(final HttpEntity entity) {  
//            super(entity);  
//        }  
//        @Override  
//        public InputStream getContent()  
//            throws IOException, IllegalStateException {  
//            // the wrapped entity's getContent() decides about repeatability  
//            InputStream wrappedin = wrappedEntity.getContent();  
//            return new GZIPInputStream(wrappedin);  
//        }  
//        @Override  
//        public long getContentLength() {  
//            // length of ungzipped content is not known  
//            return -1;  
//        }  
//  
//    }
	
}


