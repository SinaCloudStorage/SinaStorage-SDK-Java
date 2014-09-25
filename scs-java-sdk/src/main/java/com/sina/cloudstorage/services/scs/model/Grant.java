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

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Specifies a grant, consisting of one grantee and one permission.
 * 
 * @see Grant#Grant(Grantee, Permission)
 */
public class Grant {
    private Grantee grantee = null;
    private HashSet<Permission> permissions = new HashSet<Permission>();

    /**
     * Constructs a new {@link Grant} object using the specified grantee and permission
     * objects.
     *
     * @param grantee
     *            The grantee being granted a permission by this grant.
     * @param permission
     *            The permission being granted to the grantee by this grant.
     */
    public Grant(Grantee grantee, Permission permission) {
        this.grantee = grantee;
        this.permissions.add(permission);
    }
    
    public Grant(Grantee grantee, Permission... permissions) {
        this.grantee = grantee;
        
        for (Permission permission : permissions) {
        	this.permissions.add(permission);
        }
    }

    public Grant(Grantee grantee, HashSet<Permission> permissions) {
        this.grantee = grantee;
        
        for (Permission permission : permissions) {
        	this.permissions.add(permission);
        }
    }
    
    /**
     * Gets the grantee being granted a permission by this grant.
     *
     * @return The grantee being granted a permission by this grant.
     * 
     * @see Grant#getPermission()
     */
    public Grantee getGrantee() {
        return grantee;
    }

    /**
     * Gets the permission being granted to the grantee by this grant.
     *
     * @return The permission being granted to the grantee by this grant.
     * 
     * @see Grant#getGrantee()
     */
    public HashSet<Permission> getPermissions() {
        return this.permissions;
    }
    
    public String[] getPermissionsForJsonArray() {
        ArrayList<String> list = new ArrayList<String>();
    	for(Permission p : this.permissions){
        	list.add(p.toString());
        }
    	
    	return list.toArray(new String[list.size()]);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((grantee == null) ? 0 : grantee.hashCode());
        result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
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
        Grant other = (Grant) obj;
        if ( grantee == null ) {
            if ( other.grantee != null )
                return false;
        } else if ( !grantee.equals(other.grantee) )
            return false;
        
        if ( permissions.size() != other.permissions.size() )
            return false;
        //TODO:判断permissions内容是否相等
        
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Grant [grantee=" + grantee + ", permissions=" + permissions + "]";
    }
}
