package com.gjh.IPSpoof;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * qbao，刷分享任务工具 
 *
 */
public class App {
	
	private static Scanner scanner;
	public static void main(String[] args) {
		try {
			scanner = new Scanner(System.in);
			if (scanner.hasNextLine()) {
				List<String> ips = getProxyIp();
				String backUrl = "http://goods.qbao.com/goodsProduct/callBack.html";
				String srcUrl = scanner.nextLine();
				for (int i = 0; i < ips.size(); i++) {
					String agent =  "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
					String url = "http://task.qbao.com/user/activity/toNewGoods/4EB8A02C63D942C1B17B6E6BCA2DB6F6/387D3BB54D5E329599576E9632408D0B/6631C8FEF3E42A6B7557D3E37CA867E8/CD8AD4DD63B254A50A6C4219F41C4B63";
					String randomIp = ips.get(i);
					String buyNowURL = getBuyNowURL(url, randomIp);
					System.out.println(buyNowURL);
					if(buyNowURL != null){
						Jsoup.connect(buyNowURL)
						.header("X-Forwarded-For", randomIp)
						.header("X-Real-IP", randomIp)
						.header("Referer", buyNowURL)
						.header("Upgrade-Insecure-Requests", "1")
						.userAgent(agent)
						.timeout(6000)
						.get();
						
						Map<String, String> data = new HashMap<String, String>();
						Map<String, String> splitQuery = splitQuery(new URL(buyNowURL));
						for (Map.Entry<String, String> keyvalue : splitQuery.entrySet()) {
							String key = keyvalue.getKey();
							String value = keyvalue.getValue();
							if (key.equals("channel") || key.equals("param")) {
								data.put(key, value);
							}
						}
						Thread.currentThread().sleep(1500);
						Document post = Jsoup.connect(backUrl)
						.header("Accept", "*/*")
						.header("Host", "goods.qbao.com")
						.header("Origin", "goods.qbao.com")
						.header("X-Requested-With", "XMLHttpRequest")
						.ignoreContentType(true)
						.header("X-Forwarded-For", randomIp)
						.header("X-Real-IP", randomIp)
						.header("Referer", buyNowURL)
						.data("channel", data.get("channel"))
						.data("param", data.get("param"))
						.userAgent(agent)
						.timeout(6000)
						.post();
						System.out.println(post);
						System.out.println(String.format("IP %s 完成%d次, 点击购买地址：%s", randomIp, i, buyNowURL));
					} else {
						System.out.println(String.format("【失败】IP %s 完成%d次, 点击购买地址：%s", randomIp, i, buyNowURL));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getBuyNowURL(String url, String randomIp) {
		try {
			Document doc = Jsoup
					.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36")
					.timeout(6000)
					.header("X-Forwarded-For", randomIp).header("X-Real-IP", randomIp).get();
			Elements buttons = doc.select(".goodsfx-btn a");
			for (Element button : buttons) {
				if (button.attr("href") != null && button.attr("href").trim() != "") {
					return button.attr("href");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* 随机生成IP地址 */
	private static String randomIp() {
		String ip = null;
		ip = new Integer((int) (Math.random() * 128)).toString() + "." + new Integer((int) (Math.random() * 128)).toString()
				+ "." + new Integer((int) (Math.random() * 128)).toString() + "."
				+ new Integer((int) (Math.random() * 128)).toString();
		return ip;
	}
	
	/* 随机生成IP地址 */
	private static List<String> getProxyIp() {
		List<String> ips = new ArrayList<String>();
		try {
			InputStream inputStream = App.class.getResourceAsStream("ip.text");
			InputStreamReader ireader = new InputStreamReader(inputStream);
			BufferedReader breader = new BufferedReader(ireader);
			String line = breader.readLine();
			while (line != null) {
				Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");  
				Matcher m = p.matcher(line);  
				while(m.find()) {  
				  System.out.println("ip:"+m.group(1));  
				  ips.add(m.group(1));
				  line = breader.readLine();
				}  
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ips;
	}

	/* 分解url */
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}
}