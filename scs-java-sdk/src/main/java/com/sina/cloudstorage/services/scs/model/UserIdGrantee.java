/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Portions copyright 2006-2009 James Murty. Please see LICENSE.txt
 * for applicable license terms and NOTICE.txt for applicable notices.
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
package com.sina.cloudstorage.services.scs.model;

/**
 * userid 对象
 *
 */
public class UserIdGrantee implements Grantee {
	
	//匿名用户组 
	public static final UserIdGrantee ANONYMOUSE = new UserIdGrantee("GRPS000000ANONYMOUSE");
	//认证用户组
	public static final UserIdGrantee CANONICAL = new UserIdGrantee("GRPS0000000CANONICAL");
	
    private String accessKeyId = null;
    
    /* (non-Javadoc)
     * @see com.amazonaws.services.s3.model.Grantee#getTypeIdentifier()
     */
    public String getTypeIdentifier() {
        return "accessKeyId";
    }

    /**
     * Constructs a new {@link UserIdGrantee} object
     * with the given email address.
     *
     * @param accessKeyId
     *        The e-mail address used to identify the e-mail grantee.
     */
    public UserIdGrantee(String accessKeyId) {
        this.setIdentifier(accessKeyId);
    }

    /**
     * Set the e-mail address as the grantee's ID.
     * 
     * @param accessKeyId
     *        The e-mail address used to identify the e-mail grantee.
     *        
     * @see UserIdGrantee#getIdentifier()       
     */
    public void setIdentifier(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /**
     * Gets the grantee's e-mail address.
     * 
     * @see UserIdGrantee#setIdentifier(string)     
     */
    public String getIdentifier() {
        return accessKeyId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessKeyId == null) ? 0 : accessKeyId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        UserIdGrantee other = (UserIdGrantee) obj;
        if ( accessKeyId == null ) {
            if ( other.accessKeyId != null )
                return false;
        } else if ( !accessKeyId.equals(other.accessKeyId) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return accessKeyId;
    }
}
