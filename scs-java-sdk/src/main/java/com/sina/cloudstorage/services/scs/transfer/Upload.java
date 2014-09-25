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
package com.sina.cloudstorage.services.scs.transfer;

import com.sina.cloudstorage.SCSClientException;
import com.sina.cloudstorage.SCSServiceException;
import com.sina.cloudstorage.services.scs.transfer.model.UploadResult;

/**
 * Represents an asynchronous upload to Amazon S3.   
 * <p>
 * See {@link TransferManager} for more information about creating transfers.
 * </p>
 * 
 * @see TransferManager#upload(String, String, java.io.File)
 * @see TransferManager#upload(com.sina.scs.model.PutObjectRequest)
 */
public interface Upload extends Transfer {
    
    /**
     * Waits for this upload to complete and returns the result of this
     * upload. Be prepared to handle errors when calling this method. Any
     * errors that occurred during the asynchronous transfer will be re-thrown
     * through this method.
     * 
     * @return The result of this transfer.
     * 
     * @throws SCSClientException
     *             If any errors were encountered in the client while making the
     *             request or handling the response.
     * @throws SCSServiceException
     *             If any errors occurred in Amazon S3 while processing the
     *             request.
     * @throws InterruptedException
     *             If this thread is interrupted while waiting for the upload to
     *             complete.
     */
    public UploadResult waitForUploadResult() 
            throws SCSClientException, SCSServiceException, InterruptedException;
}
