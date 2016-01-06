package net.umpay.mailbill.util.string;

import java.util.List;
import java.util.Map;

public class GetUserInfoUtil {
	/**
	 * 获取用户姓名、性别
	 * @param map
	 * @param string
	 * @param firstindex
	 * @param lastindex
	 */
	public static void getUserInfo(Map<String, String> map, String string, int firstindex, int lastindex) {
		if (string.contains("尊敬的")){
			String substring = string.substring(firstindex, string.length()-lastindex);
			String substring2 = string.substring(string.length()-lastindex, string.length());
			map.put("姓名", substring);
			if(substring2.contains("女士")){
				map.put("性别", "女");
			}else if(substring2.contains("先生")){
				map.put("性别", "男");
			}else{
				map.put("性别", "");
			}
		}
	}
	/**
	 * 获取用户姓名、性别
	 * @param list
	 * @param map
	 * @param firstindex
	 * @param lastindex
	 */
	public static void getUserInfoFor(List<String> list, Map<String, String> map, int firstindex, int lastindex) {
		String string ;
		for(int i = 0 ; i < list.size(); i ++){
			string = list.get(i);
			if (string.contains("尊敬的")){
				String substring = string.substring(firstindex, string.length()-lastindex);
				String substring2 ;
				int indexOf = string.indexOf("您好!");
				int indexOf1 = string.indexOf("：");
				if (-1 != indexOf || -1 != indexOf1){
					substring2 = string.substring((indexOf == -1 ? indexOf1 : indexOf)-lastindex, string.length());
					map.put("姓名", substring);
					if(substring2.contains("女士")){
						map.put("性别", "女");
						break;
					}else if (substring2.contains("先生")){
						map.put("性别", "男");
						break;
					}else{
						map.put("性别", "");
						break;
					}
				}
			}
		}
	}
}

