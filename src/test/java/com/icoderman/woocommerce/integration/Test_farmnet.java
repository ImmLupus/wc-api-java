package com.icoderman.woocommerce.integration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Test_farmnet {

	static WooCommerceClientTest woo;
	static String token;
	// Хэш с номерами товаров и их описанием
	static HashMap<String, LinkedHashMap> item_map = new LinkedHashMap<String, LinkedHashMap>();
	// Хэш с номерами загруженных товаров товаров
	static HashMap<String, LinkedHashMap> woo_item_map = new LinkedHashMap<String, LinkedHashMap>();
	// Список логинов
	static HashMap<String, String> users = new LinkedHashMap<String, String>();

	public static void main(String[] args) throws ClientProtocolException, IOException {

		woo = new WooCommerceClientTest();
		woo.setUp();

		try {
			FileInputStream fis = new FileInputStream("users");
			Scanner sc = new Scanner(fis);
			String login = "";
			String password = "";
			while (sc.hasNext()) {
				login = sc.next();
				password = sc.next();
				users.put(login, password);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String login : users.keySet()) {
			String token = login(login, users.get(login));

			// Получаем список филиалов
			LinkedHashMap branch = branchList(token);
			String branchList = "";
			// Узнаем остатки по филиалам и заполняем item_map
			for (LinkedHashMap item : (List<LinkedHashMap>) branch.get("items")) {
				LinkedHashMap ost = ostByDate(token, Integer.toString((int) item.get("branchId")));
				for (LinkedHashMap ost_item : (List<LinkedHashMap>) ost.get("items")) {
					// Если в прошлом филиале такой товар был, суммируем остатки
					String id = Integer.toString((Integer) ost_item.get("regId"));
					if (!item_map.containsKey(id))
						item_map.put(id, ost_item);
					else {
						item_map.get(id).put("uQntOst", (double) (item_map.get(id).get("uQntOst"))
								+ ((double) ost_item.get("uQntOst")));
					}
				}
			}

			// Получаем список загруженных товаров
			List<LinkedHashMap> wooAllProducts = wooGetAllProducts();
			for (LinkedHashMap item : wooAllProducts) {
				woo_item_map.put((String) item.get("sku"), item);
			}

			// Сравниваем две таблицы и заполняем запрос на запись
			int count = 0;
			List<Map<String, Object>> create_list = new ArrayList<>();
			List<Map<String, Object>> update_list = new ArrayList<>();
			for (String key : item_map.keySet()) {
				count++;
				if (woo_item_map.containsKey(key)) {
					Map<String, Object> product = new HashMap<>();
					product.put("sku", item_map.get(key).get("regId"));
					product.put("name", item_map.get(key).get("tovName"));
					product.put("short_description", item_map.get(key).get("fabr"));
					product.put("stock_quantity", (int) ((double) item_map.get(key).get("uQntOst")));
					product.put("regular_price", item_map.get(key).get("priceRoznWNDS"));
					product.put("manage_stock", "true");

					update_list.add(product);
				} else {
					Map<String, Object> product = new HashMap<>();
					product.put("sku", item_map.get(key).get("regId"));
					product.put("name", item_map.get(key).get("tovName"));
					product.put("short_description", item_map.get(key).get("fabr"));
					product.put("stock_quantity", (int) ((double) item_map.get(key).get("uQntOst")));
					product.put("regular_price", item_map.get(key).get("priceRoznWNDS"));
					product.put("manage_stock", "true");

					create_list.add(product);
				}
				if (count == 99) {
					Map<String, Object> reqOptions = new HashMap<>();

					if (!create_list.isEmpty())
						reqOptions.put("create", create_list);
					if (!update_list.isEmpty())
						reqOptions.put("update", update_list);

					try {
						wooBatchProduct(reqOptions);
					} catch (Exception nohttp) {
						wooBatchProduct(reqOptions);
					}

					count = 0;
					create_list.clear();
					update_list.clear();
				}
			}

			Map<String, Object> reqOptions = new HashMap<>();

			if (!create_list.isEmpty())
				reqOptions.put("create", create_list);
			if (!update_list.isEmpty())
				reqOptions.put("update", update_list);

			try {
				wooBatchProduct(reqOptions);
			} catch (Exception nohttp) {
				wooBatchProduct(reqOptions);
			}
		}
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
		List answer = new ArrayList();
		List result = null;
		int page = 1;
		try {
			do {
				result = woo.apiGetAllProductsTest(Integer.toString(page));
				answer.addAll(result);
				page++;
			} while (!result.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	public static void wooBatchProduct(Map<String, Object> reqOptions) {
		try {
			woo.apiBatchProductTest(reqOptions);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class Login_entity {
		public String status;
		public String sessionId;
	}
}
