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

import java.io.Serializable;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * Represents an Amazon S3 Access Control List (ACL), including the ACL's set of
 * grantees and the permissions assigned to each grantee.
 * </p>
 * <p>
 * Each bucket and object in Amazon S3 has an ACL that defines its access
 * control policy. When a request is made, Amazon S3 authenticates the request
 * using its standard authentication procedure and then checks the ACL to verify
 * the sender was granted access to the bucket or object. If the sender is
 * approved, the request proceeds. Otherwise, Amazon S3 returns an error.
 * </p>
 * <p>
 * An ACL contains a list of grants. Each grant consists of one grantee and one
 * permission. ACLs only grant permissions; they do not deny them.
 * </p>
 * <p>
 * For convenience, some commonly used ACLs are defined in
 * {@link CannedAccessControlList}.
 * </p>
 * <p>
 * Note: Bucket and object ACLs are completely independent; an object does not
 * inherit an ACL from its bucket. For example, if you create a bucket and
 * grant write access to another user, you will not be able to access the user's
 * objects unless the user explicitly grants access. This also applies if you
 * grant anonymous write access to a bucket. Only the user "anonymous" will be
 * able to access objects the user created unless permission is explicitly
 * granted to the bucket owner.
 * </p>
 * <p>
 * Important: Do not grant the anonymous group
 * write access to buckets, as you will have no control over the objects
 * others can store and their associated charges. For more information, see
 * {@link Grantee} and {@link Permissions}.
 * </p>
 *
 * @see CannedAccessControlList
 */
public class AccessControlList implements Serializable {
    private static final long serialVersionUID = 8095040648034788376L;

    public AccessControlList(){
    	super();
    }
    
    /**
     * Create AccessControlList from json map
     * @param jsonMap
     */
    public AccessControlList(Map<String,Object> jsonMap){
//     {
//	      "Owner": "SINA000000xxxxx",
//	      "ACL": {
//		          "SINA000000xxxxxx": [
//		              "read",
//		              "write",
//		              "read_acp",
//		              "write_acp"
//		          ]
//		  }
//	  }
    	@SuppressWarnings("unchecked")
		Map<String,List<String>> acl = (Map<String, List<String>>) jsonMap.get("ACL");
    	for(Entry<String,List<String>> entry : acl.entrySet()){
    		List<String> list = entry.getValue();
    		if(list!=null){
    			this.grantPermission(new UserIdGrantee(entry.getKey()), 
    					Permission.parsePermissions(entry.getValue()));
    			
    		}
    	}
    }
    
    private HashSet<Grant> grants = new HashSet<Grant>();

    /**
     * Adds a grantee to the access control list (ACL) with the given permission. 
     * If this access control list already
     * contains the grantee (i.e. the same grantee object) the permission for the
     * grantee will be updated.
     *
     * @param grantee
     *            The grantee to whom the permission will apply.
     * @param permission
     *            The permission to apply to the grantee.
     */
    public void grantPermission(Grantee grantee, Permission permission) {
        grants.add(new Grant(grantee, permission));
    }
    
    public void grantPermissions(Grantee grantee, Permission... permissions) {
        grants.add(new Grant(grantee, permissions));
    }
    
    public void grantPermission(Grantee grantee, HashSet<Permission> permissions) {
        grants.add(new Grant(grantee, permissions));
    }

    /**
     * Adds a set of grantee/permission pairs to the access control list (ACL), where each item in the
     * set is a {@link Grant} object.
     *
     * @param grants
     *            A collection of {@link Grant} objects
     */
    public void grantAllPermissions(Grant... grantsVarArg) {
        for (Grant gap : grantsVarArg) {
            grantPermission(gap.getGrantee(), gap.getPermissions());
        }
    }

    /**
     * Revokes the permissions of a grantee by removing the grantee from the access control list (ACL).
     *
     * @param grantee
     *        The grantee to remove from this ACL.
     */
    public void revokeAllPermissions(Grantee grantee) {
        ArrayList<Grant> grantsToRemove = new ArrayList<Grant>();
        for (Grant gap : grants) {
            if (gap.getGrantee().equals(grantee)) {
                grantsToRemove.add(gap);
            }
        }
        grants.removeAll(grantsToRemove);
    }

    /**
     * Gets the set of {@link Grant} objects in this access control list (ACL).
     * 
     * @return The set of {@link Grant} objects in this ACL.
     */
    public Set<Grant> getGrants() {
        return grants;
    }

    public String toString() {
        return "AccessControlList [grants=" + getGrants() + "]";
    }

}
