package com.sina.cloudstorage.services.scs.model.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.sina.cloudstorage.services.scs.model.PartETag;

public class RequestJsonFactory {

	/**
     * Converts the specified list of PartETags to an Json fragment that can be
     * sent to the CompleteMultipartUpload operation of Amazon S3.
     *
     * @param partETags
     *            The list of part ETags containing the data to include in the
     *            new XML fragment.
     *
     * @return A byte array containing the data
     */
    public static byte[] convertToJsonByteArray(List<PartETag> partETags) {
    	
    	ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
    	for(PartETag pet : partETags){
    		HashMap<String,Object> map = new HashMap<String,Object>();
    		map.put("PartNumber", Integer.valueOf(pet.getPartNumber()));
    		map.put("ETag", pet.getETag());
    		list.add(map);
    	}
    	
    	Gson gson = new Gson();
    	String jsonStr = gson.toJson(list);
    	
    	return jsonStr.getBytes();
    }
}
