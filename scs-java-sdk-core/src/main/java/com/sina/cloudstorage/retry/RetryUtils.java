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
package com.sina.cloudstorage.retry;

import com.sina.cloudstorage.SCSServiceException;


public class RetryUtils {
    
    /**
     * Returns true if the specified exception is a throttling error.
     *
     * @param ase
     *            The exception to test.
     *
     * @return True if the exception resulted from a throttling error message
     *         from a service, otherwise false.
     */
    public static boolean isThrottlingException(SCSServiceException ase) {
        if (ase == null) return false;
        
        String errorCode = ase.getErrorCode();
        return "Throttling".equals(errorCode)
            || "ThrottlingException".equals(errorCode)
            || "ProvisionedThroughputExceededException".equals(errorCode);
    }
    
    /**
     * Returns true if the specified exception is a request entity too large
     * error.
     *
     * @param ase
     *            The exception to test.
     *
     * @return True if the exception resulted from a request entity too large
     *         error message from a service, otherwise false.
     */
    public static boolean isRequestEntityTooLargeException(SCSServiceException ase) {
        if (ase == null) return false;
        return "Request entity too large".equals(ase.getErrorCode());
    }
    
    /**
     * Returns true if the specified exception is a clock skew error.
     *
     * @param ase
     *            The exception to test.
     *
     * @return True if the exception resulted from a clock skews error message
     *         from a service, otherwise false.
     */
    public static boolean isClockSkewError(SCSServiceException ase) {
        if (ase == null) return false;

        String errorCode = ase.getErrorCode();
        return "RequestTimeTooSkewed".equals(errorCode)
                || "RequestExpired".equals(errorCode)
                || "InvalidSignatureException".equals(errorCode)
                || "SignatureDoesNotMatch".equals(errorCode);
    }
}
