package net.umpay.mailbill.util.security;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomUtil {
	private static Random r = new java.util.Random();
	static{
		r.setSeed(System.currentTimeMillis());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void swap(List a,int p1,int p2){
		Object t = a.get(p1);
		a.set(p1, a.get(p2));
		a.set(p2, t);
	}
	/**
	 * @return random boolean
	 */
	public static boolean randomBoolean(){
		return r.nextBoolean();
	}
	/**
	 * 
	 * @param maxoffset
	 * 
	 */
	public static Long randomOffset(Long maxoffset){
		return Double.valueOf(r.nextFloat()*maxoffset).longValue();
	}

	/**
	 * 随机数集合
	 * @param max
	 * @param setsize
	 * 
	 */
	public static Set<Integer> getRandomLongSet(int max,int setsize){
		Set<Integer> rset = new HashSet<Integer>();
		while(true){
			int rLong = r.nextInt(max+1);
			if(rLong>=0){
				rset.add(rLong);
			}
			if(rset.size()>=setsize){
				return rset;
			}
		}
	} 
	/**
	 * 获取随机集合
	 * @param samples 样本空间
	 * @param size 保留数量
	 * @param keepsamples 是否保持样本空间,设置成true,则返回新的集合,否则直接修改样本空间集合
	 * @return 从样本空间中随机选出来的size个样本的集合
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getRandomSet(List samples, int size, boolean keepsamples) {
		if (samples.isEmpty()) {
			return samples;
		} else if (samples.size() <= size) {
			size = samples.size();
		}
		List ret = null;
		if (keepsamples) {
			ret = new ArrayList();
			ret.addAll(samples);
		} else {
			ret = samples;
		}
		// 随机样本空间
		for (int i = 0; i < samples.size(); i++) {
			swap(ret, i, r.nextInt(i + 1));
		}
		// 截取
		for (int i = samples.size() - size - 1; i > -1; i--) {
			ret.remove(i);
		}
		return ret;
	}
	
	/**
	 * 生成一个大随机数,比如用来做GUID.
	 * 基于时间和随机数组合的方式.
	 * @param length
	 * 
	 */
	public static BigDecimal getUniqueBigDecimal(int length) {
		
		long seed = (new Date()).getTime();
		String result = String.valueOf(seed)
				+ String.valueOf(Math.abs(r.nextLong()));
		return new BigDecimal(result.substring(0, length));
	}

	public static void main(String[] args) {
		Set<BigDecimal> set = new HashSet<BigDecimal>(10000);
		for (int i = 0 ; i < 10000; i++){
			BigDecimal uniqueBigDecimal = RandomUtil.getUniqueBigDecimal(20);
			
			set.add(uniqueBigDecimal);
			double doubleValue = uniqueBigDecimal.longValue();
			System.out.println(doubleValue);
		}
		System.out.println(set.size());
	}
}
