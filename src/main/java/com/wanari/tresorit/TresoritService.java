package com.wanari.tresorit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wanari.tresorit.request.RequestValidateUser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zcsongor on 2017.02.27..
 */
public class TresoritService {

	private static final String BASE_URL = "api/v4/admin/";
	private static final String ADMIN_UID = "admin@ney7xi56o2.tresorit.io";
	private static final String ADMIN_KEY = "f6bffcf5ba34a1b2b614ca348020a16fda36b1a9e824be0938d32bcda5258976";
	private static final String API_ROOT = "https://host-s9m5bg49sm.api.tresorit.io/tenant-ney7xi56o2";
	private static final String PATH = API_ROOT.substring(API_ROOT.indexOf("/", "https://host".length()) + 1);
	private static final String USERS_PATH = "src/main/resources/users.json";

	private static ObjectMapper objectMapper = new ObjectMapper();

	public static String administrativeCall(final String url) {
		return administrativeCall(url, null);
	}

	public static String administrativeCall(final String url, final String body) {
		HttpURLConnection urlConnection = null;
		OutputStream outputStream = null;
		BufferedInputStream bufferedInputStream;
		String response;
		String urlToUse = BASE_URL + url;

		try {
			boolean hasBody = body != null;
			byte[] contentBytes = hasBody ? TresoritUtils.string_to_bytes(body) : null;

			Map<String, String> headers = TresoritUtils.adminPostAuth(ADMIN_UID, ADMIN_KEY, PATH + "/" + urlToUse, contentBytes);
			urlConnection = (HttpURLConnection) new URL(API_ROOT + "/" + urlToUse).openConnection();
			urlConnection.setRequestMethod(hasBody ? "POST" : "GET");
			if(hasBody) urlConnection.setDoOutput(true);

			for(String key : headers.keySet())
				urlConnection.setRequestProperty(key, headers.get(key));

			if(hasBody) {
				outputStream = urlConnection.getOutputStream();
				outputStream.write(contentBytes);
			}

			int responseCode = urlConnection.getResponseCode();
			boolean isOK = responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE;

			StringBuilder stringBuilder = new StringBuilder();
			bufferedInputStream = new BufferedInputStream(isOK ? urlConnection.getInputStream() : urlConnection.getErrorStream());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

			String inputLine;
			while((inputLine = bufferedReader.readLine()) != null)
				stringBuilder.append(inputLine);
			response = stringBuilder.toString();
		} catch(Exception e) {
			response = e.getMessage();
		} finally {
			if(outputStream != null)
				try {
					outputStream.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			if(urlConnection != null)
				urlConnection.disconnect();
		}
		return response;
	}

	public static void saveUserIdToFile(RequestValidateUser user) throws IOException {
		List<RequestValidateUser.RequestValidateUserDTO> users;
		try {
			users = getUsers();
		} catch(FileNotFoundException e) {
			users = new ArrayList<>();
		}
		users.add(RequestValidateUser.createDTOFromEntity(user));
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_PATH), users);
	}

	public static List<RequestValidateUser.RequestValidateUserDTO> getUsers() throws IOException {
		FileInputStream fis = new FileInputStream(TresoritService.USERS_PATH);
		List<RequestValidateUser.RequestValidateUserDTO> users = objectMapper.readValue(fis, TypeFactory.defaultInstance()
				.constructCollectionType(List.class, RequestValidateUser.RequestValidateUserDTO.class));
		fis.close();
		return users;
	}
}
