package net.umpay.mailbill.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 抽取网页的正文。 为保持通用性没有针对特定网站编写规则。
 * 
 * @author zhaozj
 * @version 1.0, 2014-04-09
 */
public class TextExtract {
	private List<String> lines;
	private List<String> stringLines;
	private int blocksWidth;
	private int threshold;
	private String html;
	private int start;
	private int end;
	private boolean boolstart;
	private boolean boolend;
	private StringBuilder text;
	private StringBuilder tmp; // 存储临时文本内容
	private ArrayList<Integer> indexDistribution;

	public void setthreshold(int value) {
		threshold = value;
	}

	/**
	 * 抽取网页正文
	 * 
	 * @param _html	网页HTML字符串
	 * 
	 * @return 网页正文string
	 */
	public List<String> parse(String _html) {
		// 初始化数据
		lines = new ArrayList<String>();
		stringLines = new ArrayList<String>();
		indexDistribution = new ArrayList<Integer>();
		text = new StringBuilder();
		tmp = new StringBuilder();
		start = end = -1;
		boolstart = boolend = false;
		blocksWidth = 3; // 块的宽度，默认是3，该值目前不可调
		threshold = 1; // 要获取的文本内容的宽度
		
		html = _html;
		preProcess();
		return getText();
	}

	/**
	 * 去掉html文档所有的标签及特殊字符
	 */
	private void preProcess() {
		html = html.replaceAll("(?is)<!DOCTYPE.*?>", ""); // remove html top infomation
		html = html.replaceAll("(?is)<!--.*?-->", ""); // remove html comment
		html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
		html = html.replaceAll("(?is)<style.*?>.*?</style>", ""); // remove css
		html = html.replaceAll("(?is)<.*?>", "");
	}

	/**
	 * 获取所有的页面内容
	 * 
	 * @return
	 */
	private List<String> getText() {
		// 获取到以'\n' 分割后的字符串集合
		lines = Arrays.asList(html.split("\n"));
		indexDistribution.clear();
		// 将空格剔除，并且将有内容的长度存储
		deleteBlack();
		stringLines.clear();
		text.setLength(0); // 清空历史数据 
		// 获取所有的文本内容
		for (int i = 0; i < indexDistribution.size() - 1; i++) {
			// 获取到有文本内容的开始位置
			if (indexDistribution.get(i) >= threshold && !boolstart) {
				if (indexDistribution.get(i + 1).intValue() != 0
					|| indexDistribution.get(i + 2).intValue() != 0
					|| indexDistribution.get(i + 3).intValue() != 0) {
					boolstart = true;
					start = i;
					continue;
				}
			}
			// 获取有文本内容的结束位置
			if (boolstart) {
				if (indexDistribution.get(i).intValue() == 0
				|| indexDistribution.get(i + 1).intValue() == 0) {
					end = i;
					boolend = true;
				}
			}
			tmp.setLength(0); // 清空历史的数据
			if (boolend) {
				// 获取开始位置至结束位置的 文本内容
				for (int ii = start; ii <= end; ii++) {
					if (lines.get(ii).length() < threshold) { // 小于指定长度的内容，直接跳过
						continue;
					}
					tmp.append(ii + "==" + lines.get(ii) + "\n"); // 将内容拼接到tmp中
					stringLines.add(lines.get(ii));
				}
				text.append(tmp.toString());//测试显示数据   原来是tmp.toString()
				boolstart = boolend = false; // 恢复默认值
			}
		}
		return stringLines;
	}

	/**
	 * 将空格剔除 的方法
	 */
	private void deleteBlack() {
		for (int i = 0; i < lines.size() - blocksWidth; i++) {
			int wordsNum = 0;
			for (int j = i; j < i + blocksWidth; j++) {
				lines.set(j, lines.get(j).replaceAll("\\s+", ""));// 所有的剔除空格
				wordsNum += lines.get(j).length();
			}
			indexDistribution.add(wordsNum);
		}
	}
}