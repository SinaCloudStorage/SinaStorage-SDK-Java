package com.sina.cloudstorage.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sina.cloudstorage.SCSClientException;

public class Utils {

	/**
	 * 解析SCSClientException异常信息
	 * @param execption
	 * @return
	 */
	public static String parseSCSException(SCSClientException execption){
//		System.out.println(execption.getLocalizedMessage());
		String errorMsg = "";
		
		if (execption != null && execption.getLocalizedMessage() != null){
			Gson gson = new Gson();
			try {
				@SuppressWarnings("unchecked")
				Map<String,String> map = gson.fromJson(execption.getLocalizedMessage(), Map.class);
				
				errorMsg += map.get("Code")==null?"":map.get("Code") + "\n";
				errorMsg += "  Message: " + (map.get("Message")==null?"":map.get("Message")) + "\n";
				errorMsg += "  Resource: " + (map.get("Resource")==null?"":map.get("Resource")) + "\n";
				errorMsg += "  RequestId: " + (map.get("RequestId")==null?"":map.get("RequestId")) + "\n";
			} catch (JsonSyntaxException e) {
				errorMsg = execption.getLocalizedMessage();
			}
		}
		
		return errorMsg;
	}
	
	/**
	 * 校验ip格式
	 * @param IP
	 * @return
	 */
	public static boolean validIP(String IP) {
        if (IP == null) {
            return false;
        }
        String[] tokens = IP.split("\\.");
        if (tokens.length != 4) {
            return false;
        }
        for (String token : tokens) {
            int tokenInt;
            try {
                tokenInt = Integer.parseInt(token);
            } catch (NumberFormatException ase) {
                return false;
            }
            if (tokenInt < 0 || tokenInt > 255) {
                return false;
            }

        }
        return true;
    }
	
	/**
	 * 将optionValue数组转化成Map形式
	 * 数组里必须是K=V格式，否则直接忽略掉
	 * @param values
	 * @return
	 */
	public static Map<String,String> getValuesMap(String[] values){
		Map<String, String> valuesMap = new HashMap<String, String>();
		if (values != null && values.length > 0) {
			for (String v : values){
				int equalPos = v.indexOf("=");
				if (equalPos > 0 && equalPos < v.length() -1){
					valuesMap.put(v.split("=")[0], v.split("=")[1]);
				}
			}
		}
		return valuesMap;
	}
	
	/**
	 * 用于检查cannedAcl是否合法
	 * @param cannedAcl
	 * @return
	 */
	public static boolean validateCannedAcl(String cannedAcl){
		if (cannedAcl == null)
			return false;
		//private (default), public-read, public-read-write, authenticated-read
		List<String> cannedAclList = new ArrayList<String>(Arrays.asList(new String[]{"private","public-read","public-read-write","authenticated-read"}));
		return cannedAclList.contains(cannedAcl);
	}
}
