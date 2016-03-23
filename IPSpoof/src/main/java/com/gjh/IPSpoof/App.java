package com.gjh.IPSpoof;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

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
	private static String agent =  "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
	private static String backUrl = "http://goods.qbao.com/goodsProduct/callBack.html";
	public static void main(String[] args) {
		try {
			scanner = new Scanner(System.in);
			if (scanner.hasNextLine()) {
				String srcUrl = scanner.nextLine();
				for (int i = 0; i < 100; i++) {
					String randomIp = randomIp();
					String buyNowURL = getBuyNowURL(srcUrl, randomIp);
					System.out.println(buyNowURL);
					if(buyNowURL != null){
						Jsoup.connect(buyNowURL)
						.header("X-Forwarded-For", randomIp)
						.header("X-Real-IP", randomIp)
						.header("Referer", buyNowURL)
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
						
						Jsoup.connect(backUrl)
						.header("Accept", "*/*")
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
					.userAgent(agent)
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