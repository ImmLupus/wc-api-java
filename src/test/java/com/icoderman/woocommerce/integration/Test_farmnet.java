package com.icoderman.woocommerce.integration;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Test_farmnet {

	static WooCommerceClientTest woo = new WooCommerceClientTest();
	static String token;
	//Хэш с номерами товаров и их описанием
	static HashMap<String, LinkedHashMap> item_map = new LinkedHashMap<String, LinkedHashMap>();
	//Хэш с номерами товаров и признаком наличия
	static HashMap<String, Boolean> item_exists = new LinkedHashMap<String, Boolean>(); 

	Test_farmnet() {
		try {
			token = login("32201", "DA2IWMLS");
			woo.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ClientProtocolException, IOException {

		//String token = login("32201", "DA2IWMLS");
		WooCommerceClientTest woo = new WooCommerceClientTest();
/*
		// Получаем список филиалов
		LinkedHashMap branch = branchList(token);
		String branchList = "";
		// Узнаем остатки по филиалам и заполняем item_map
		for (LinkedHashMap item : (List<LinkedHashMap>) branch.get("items")) {
			LinkedHashMap ost = ostByDate(token, Integer.toString((int) item.get("branchId")));
			for (LinkedHashMap ost_item : (List<LinkedHashMap>) ost.get("items")) {
				item_map.put(Integer.toString((Integer)ost_item.get("regId")), ost_item);
			}
		}*/
		
		List wooProducts = wooGetAllProducts();
		int i = 0;
	}

	public static String login(String customerId, String password) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String answer = null;
		try {
			URIBuilder builder = new URIBuilder("https://farmnet.ru:1988/Login");
			builder.setParameter("customerId", customerId).setParameter("password", password);

			HttpGet request = new HttpGet(builder.build());

			CloseableHttpResponse response = httpClient.execute(request);

			try {
				System.out.println(response.getStatusLine().toString());

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String result = EntityUtils.toString(entity);
					StringReader reader = new StringReader(result);
					ObjectMapper mapper = new ObjectMapper();
					Login_entity login = mapper.readValue(reader, Login_entity.class);
					answer = login.sessionId;
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception exe) {
			exe.printStackTrace();
		} finally {
			httpClient.close();
		}
		return answer;
	}

	public static LinkedHashMap sprGoods(String token) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		LinkedHashMap answer = null;
		try {
			HttpGet request = new HttpGet("https://farmnet.ru:1988/SprGoods");

			request.addHeader("WebApiSession", token);

			CloseableHttpResponse response = httpClient.execute(request);

			try {
				System.out.println(response.getStatusLine().toString());

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ObjectMapper mapper = new ObjectMapper();
					Object result = mapper.readValue(entity.getContent(), Object.class);
					answer = (LinkedHashMap) result;
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception exe) {
			exe.printStackTrace();
		} finally {
			httpClient.close();
		}
		return answer;
	}

	public static LinkedHashMap branchList(String token) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		LinkedHashMap answer = null;
		try {
			HttpGet request = new HttpGet("https://farmnet.ru:1988/BranchList");

			request.addHeader("WebApiSession", token);

			CloseableHttpResponse response = httpClient.execute(request);

			try {
				System.out.println(response.getStatusLine().toString());

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ObjectMapper mapper = new ObjectMapper();
					Object result = mapper.readValue(entity.getContent(), Object.class);
					answer = (LinkedHashMap) result;
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception exe) {
			exe.printStackTrace();
		} finally {
			httpClient.close();
		}
		return answer;
	}

	public static LinkedHashMap ostByDate(String token, String branchId) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		LinkedHashMap answer = null;
		try {
			URIBuilder builder = new URIBuilder("https://farmnet.ru:1988/OstByDate");
			builder.setParameter("branchId", branchId);

			HttpGet request = new HttpGet(builder.build());

			request.addHeader("WebApiSession", token);

			CloseableHttpResponse response = httpClient.execute(request);

			try {
				System.out.println(response.getStatusLine().toString());

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ObjectMapper mapper = new ObjectMapper();
					Object result = mapper.readValue(entity.getContent(), Object.class);
					answer = (LinkedHashMap) result;
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception exe) {
			exe.printStackTrace();
		} finally {
			httpClient.close();
		}
		return answer;
	}

	public static List wooGetAllProducts() {
		List result = null;
		try {
			result = woo.apiGetAllProductsTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	

	static class Login_entity {
		public String status;
		public String sessionId;
	}
}
