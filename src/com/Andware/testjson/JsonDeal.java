package com.Andware.testjson;

import java.util.ArrayList;
import java.util.List;

import org.aa.tool.ChapterMode;
import org.aa.tool.DealFile;
import org.geometerplus.android.fbreader.ChapterList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.R.integer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

 
public class JsonDeal {

	public static ChapterMode bookChapterContent ;
	
	private List<Listmode> list;
	
	public void dealManList(String json,Handler handler) {
		list = new ArrayList<Listmode>();
		JSONArray jsonArray;
		try {
			JSONObject jo = new JSONObject(json);
			jsonArray = (JSONArray) jo.get("dataList");
			for (int i = 0; i < jsonArray.length(); ++i) { 
				Listmode man = new Listmode();
				JSONObject o = (JSONObject) jsonArray.get(i);
				int book_id = o.getInt("id");
				String bookName = o.getString("bookName");
				String authorName = o.getString("authorName");
				String picPath = o.getString("url");
				int maxSize = jo.getInt("countRow");
				man.setBook_id(book_id);
				man.setAuthor(authorName);
				man.setName(bookName);
				man.setPicPath(picPath);
				man.setMaxSize(maxSize);
				Log.i("User",man.getPicPath()+":path");
				list.add(man);
				
			}

		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public List<Listmode> getListmodes () {
		return list;
	}
	
	//书籍列表json解析
	public static List<Listmode> dealManList(String json) {
		List<Listmode> list = new ArrayList<Listmode>();
		JSONArray jsonArray;
		try {
			JSONObject jo = new JSONObject(json);
			
			jsonArray = (JSONArray) jo.get("dataList");
			
			for (int i = 0; i < jsonArray.length(); ++i) { 
				Listmode man = new Listmode();
				JSONObject o = (JSONObject) jsonArray.get(i);
				int book_id = o.getInt("id");
				String bookName = o.getString("bookName");
				String authorName = o.getString("authorName");
				String picPath = o.getString("url");
				int maxSize = jo.getInt("countRow");
				man.setBook_id(book_id);
				man.setAuthor(authorName);
				man.setName(bookName);
				man.setPicPath(picPath);
				man.setMaxSize(maxSize);
				Log.i("User",man.getPicPath()+":path");
				list.add(man);
			}

		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return list;
	}
	
	public static BookMode dealOneBook ( String json ) {
		BookMode book = null;
		try {
			JSONObject jo = new JSONObject(json);
			book = new BookMode();
			int bookId = jo.getInt("id");
			String bookName = jo.getString("bookname");
			String discribeBookBody = jo.getString("briefIntroduct");
			int firstChapterId = jo.getInt("firstChapter");
			book.setId(bookId);
			book.setName(bookName);
			book.setDiscribeBody(discribeBookBody);
			book.setFirstChapterId(firstChapterId);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return book;
	}
	
	public static void dealOneChapter ( int bookIds , int chapterIds ,Handler handler2 ) {
		final int bookId = bookIds;
		final int chapterId = chapterIds;
		final Handler handler = handler2;
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String json = GetRequest.get(URLcontent.bookPath+"achapter!chapter.e?oid="+bookId+"&id="+chapterId);
				
				try {
					bookChapterContent = new ChapterMode();
					JSONObject jo = new JSONObject(json);
					int book_id = jo.getInt("oid");
					int chapterIds = jo.getInt("id");
					int upId = jo.getInt("upId");
					int nextId = jo.getInt("nextId");
					String content = jo.getString("content");
					String chapter_name = jo.getString("cname");
					bookChapterContent.setBook_id(book_id);
					bookChapterContent.setChapter_id(chapterIds);
					bookChapterContent.setChapter_next_id(nextId);
					bookChapterContent.setChapter_up_id(upId);
					bookChapterContent.setChapter_name(chapter_name);
					bookChapterContent.setContent(DealFile.createFile(
							content,bookChapterContent.getChapter_id()));
					handler.obtainMessage(0).sendToTarget();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public static ChapterMode dealOneChapter ( int bookId , int chapterId  ) {
		String json = GetRequest.get(URLcontent.bookPath+"achapter!chapter.e?oid="+bookId+"&id="+chapterId);
		ChapterMode bookChapterContent = null;
		try {
			bookChapterContent = new ChapterMode();
			JSONObject jo = new JSONObject(json);
			int book_id = jo.getInt("oid");
			int chapterIds = jo.getInt("id");
			int upId = jo.getInt("upId");
			int nextId = jo.getInt("nextId");
			String content = jo.getString("content");
			String cName = jo.getString("cname");
			bookChapterContent.setBook_id(book_id);
			bookChapterContent.setChapter_id(chapterIds);
			bookChapterContent.setChapter_next_id(nextId);
			bookChapterContent.setChapter_up_id(upId);
			bookChapterContent.setContent(DealFile.createFile(
					content,bookChapterContent.getChapter_id()));
			bookChapterContent.setChapter_name(cName);
			Log.i("json",bookChapterContent.getContent());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookChapterContent;
	}
	
	public static List<ChapterList> getChapterList ( int book_id , int page_num , int pageCount ) {
		String json = GetRequest.get(URLcontent.getChapterList+book_id+"&page="+page_num+"&pageSize="+pageCount);
		Log.i("json",json);
		List<ChapterList> list = null;
		JSONArray jsonArray;
		try {
			list = new ArrayList<ChapterList>();
			JSONObject jo = new JSONObject(json);
			jsonArray = (JSONArray) jo.get("dataList");
			for ( int i = 0; i < jsonArray.length(); ++i ) {
				ChapterList chapterList = new ChapterList();
				JSONObject o = (JSONObject) jsonArray.get(i);
				int chapter_id = o.getInt("id");
				String cName = o.getString("cname");
				int maxSize = jo.getInt("countRow");
				chapterList.setChapter_id(chapter_id);
				chapterList.setCname(cName);
				chapterList.setMaxSize(maxSize);
				list.add(chapterList);
			}
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return list;
	}
	
}
