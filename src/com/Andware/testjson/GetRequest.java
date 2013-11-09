package com.Andware.testjson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.R.integer;
import android.os.Handler;
import android.util.Log;

public class GetRequest {

	private static String result;

	private Handler handler;
	private List<Listmode> list;

	
	
	public GetRequest ( Handler handler ) {
		this.handler = handler;
	}
	
	// 获取请求的方法返回的为json数据
	public static String get(String urlString) {
		/*
		 * try{ HttpGet request = new HttpGet(urlString); String
		 * result=getHttpClient().execute(request,new BasicResponseHandler());
		 * return result; }catch(IOException e){ throw e; }
		 */
		String result = "";
		BufferedReader in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			Log.i("User", urlString);
			HttpGet request = new HttpGet(urlString);
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			// String NL="";
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			Log.i("json", result);
			result = sb.toString().trim();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public List<Listmode> getList(){
		return list;
	}

	public void get(String urlString, Handler handler) {
		/*
		 * try{ HttpGet request = new HttpGet(urlString); String
		 * result=getHttpClient().execute(request,new BasicResponseHandler());
		 * return result; }catch(IOException e){ throw e; }
		 */
		
		// TODO Auto-generated method stub
		BufferedReader in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			Log.i("User", urlString);
			HttpGet request = new HttpGet(urlString);
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			// String NL="";
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			result = sb.toString().trim();
			handler.obtainMessage(2).sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String getResult (){
		return result;
	}
	
	// json数据解析封装，直接获取listmode的数据列表
	private void getRequest(String urlString, Handler handler) {

		get(urlString, handler);
		
		
	}

	private static List<Listmode> getRequest(String urlString) {

		String jsonDeal = get(urlString);
		List<Listmode> list = JsonDeal.dealManList(jsonDeal);
		return list;
	}

	private static BookMode getRequestOneBook(String urlString) {
		String jsonData = get(urlString);
		BookMode book = JsonDeal.dealOneBook(jsonData);
		return book;
	}

	// 外部男生列表调用接口
	public void getManList(int page, int pageSize,
			Handler handler) {
		getRequest(URLcontent.manBookList + "&page=" + page
				+ "&pageSize=" + pageSize, handler);
	}

	// 外部女生列表调用接口
	public void getWomanList(int page, int pageSize,
			Handler handler) {
		getRequest(URLcontent.womanBookList + "&page=" + page
				+ "&pageSize=" + pageSize, handler);
	}

	// 外部热门书籍列表调用接口
	public void getHostList(int page, int pageSize,
			Handler handler) {
		getRequest(URLcontent.hostBookList + "&page=" + page
				+ "&pageSize=" + pageSize, handler);
	}

	//
	public static List<Listmode> getManList(int page, int pageSize) {
		return getRequest(URLcontent.manBookList + "&page=" + page
				+ "&pageSize=" + pageSize);
	}

	// 外部女生列表调用接口
	public static List<Listmode> getWomanList(int page, int pageSize) {
		return getRequest(URLcontent.womanBookList + "&page=" + page
				+ "&pageSize=" + pageSize);
	}

	// 外部热门书籍列表调用接口
	public static List<Listmode> getHostList(int page, int pageSize) {
		return getRequest(URLcontent.hostBookList + "&page=" + page
				+ "&pageSize=" + pageSize);
	}

	public static BookMode getOneBookById(int id) {
		return getRequestOneBook(URLcontent.getOneBook + id);
	}
}
