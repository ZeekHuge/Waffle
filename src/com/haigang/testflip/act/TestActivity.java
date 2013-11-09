package com.haigang.testflip.act;

import org.aa.tool.ChapterMode;
import org.aa.tool.DBTool;
import org.aa.tool.DealFile;
import org.aa.tool.OpenFile;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;
import org.w3c.dom.ls.LSInput;

import com.Andware.testjson.BookMode;
import com.Andware.testjson.GetRequest;
import com.Andware.testjson.JsonDeal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class TestActivity extends Activity {

	private OpenFile openFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Intent intent = getIntent();
		
		int id = intent.getIntExtra("book_id", 0);
		if (id != 0 ) {
			DBTool db = new DBTool(this);
			ChapterMode chapter = db.query(id);
			openFile = new OpenFile(chapter.getContent(),chapter.getBook_id(),
					TestActivity.this,chapter.getChapter_name());
			db.close();
			this.finish();
		} else {
			finish();
		}

	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		openFile.onDestroy();
		super.onDestroy();
	}

}
