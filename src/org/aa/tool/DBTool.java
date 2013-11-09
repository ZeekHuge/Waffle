package org.aa.tool;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.style.UpdateAppearance;

public class DBTool extends SQLiteOpenHelper {
	private static final String DB_NAME = "chapter.db";

	private static final String TABLE_NAME = "chapters";

	private SQLiteDatabase db;
	
	public DBTool(Context context) {
		super(context, DB_NAME, null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		this.db = db;
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME
				+ " (book_id INTEGER, chapter_id INTEGER,chapter_up_id INTEGER,chapter_next_id INTEGER,chapter_content text, chapter_name text,PRIMARY KEY (chapter_id)) ");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void Insert(int book_id,int chapter_id, int chapter_up_id,
			int chapter_next_id, String chapter_content,String chapter_name) {
		ContentValues values = new ContentValues();
		values.put("book_id", book_id);
		values.put("chapter_id", chapter_id);
		values.put("chapter_up_id", chapter_up_id);
		values.put("chapter_next_id", chapter_next_id);
		values.put("chapter_content", chapter_content);
		values.put("chapter_name", chapter_name);
		SQLiteDatabase db = getWritableDatabase();
		db.replace(TABLE_NAME, null, values);
		values.clear();
		
	}

	public boolean queryIsExists(int chapter_id) {
		boolean bool;
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, "chapter_id = ?",
				new String[] { String.valueOf(chapter_id) }, null, null, null);
		bool = cursor.moveToFirst();
		cursor.close();
		return bool;
	}
	
//	public boolean queryChapterUpIsExists(int chapter_id) {
//		boolean bool;
//		SQLiteDatabase db = getWritableDatabase();
//		Cursor cursor = db.query(TABLE_NAME, null, "chapter_id = ?",
//				new String[] { String.valueOf(chapter_id) }, null, null, null);
//		bool = cursor.moveToFirst();
//		cursor.close();
//		return bool;
//	}

	public void UpdateChapter(ChapterMode chapter) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("book_id", chapter.getBook_id());
		values.put("chapter_up_id", chapter.getChapter_up_id());
		values.put("chapter_next_id", chapter.getChapter_next_id());
		values.put("chapter_content", chapter.getContent());
		values.put("chapter_name", chapter.getChapter_name());
		db.update(TABLE_NAME, values, "chapter_id = ?",
				new String[] { String.valueOf(chapter.getChapter_id()) });
		values.clear();
	}

	public ChapterMode query(int chapter_id) {
		
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_NAME, null, "chapter_id = ?",
				new String[] { String.valueOf(chapter_id) }, null, null, null);
		if (c.moveToFirst()) {
			ChapterMode chapter = new ChapterMode();
			chapter.setBook_id(c.getInt(c.getColumnIndex("book_id")));
			chapter.setChapter_id(c.getInt(c.getColumnIndex("chapter_id")));
			chapter.setChapter_up_id(c.getInt(c.getColumnIndex("chapter_up_id")));
			chapter.setChapter_next_id(c.getInt(c
					.getColumnIndex("chapter_next_id")));
			chapter.setContent(c.getString(c.getColumnIndex("chapter_content")));
			chapter.setChapter_name(c.getString(c.getColumnIndex("chapter_name")));
			c.close();
			return chapter;
		} else {
			c.close();
			return null;
		}

	}

	public void close() {
		if (db != null)
			db.close();
	}

}
