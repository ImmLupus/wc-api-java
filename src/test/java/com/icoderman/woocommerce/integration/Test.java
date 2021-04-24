package com.icoderman.woocommerce.integration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

	public static void main(String[] args) {

		try {
			WooCommerceClientTest woo = new WooCommerceClientTest();

			woo.setUp();

			List result = woo.apiGetAllProductsTest();

			StringWriter writer = new StringWriter();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(writer, result);
			String ans = writer.toString();
			System.out.println(ans);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
