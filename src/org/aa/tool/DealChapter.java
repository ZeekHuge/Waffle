package org.aa.tool;

import org.geometerplus.zlibrary.ui.android.R;

import com.Andware.testjson.JsonDeal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class DealChapter {

	private Context context;

	private DBTool db;

	private Dialog mDialog = null;

	OpenFile openFile = null;
	ChapterMode chapter;

	public DealChapter(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:

				chapter = JsonDeal.bookChapterContent;
				db.UpdateChapter(chapter);
				openFile = new OpenFile(chapter.getContent(),
						chapter.getBook_id(), context,chapter.getChapter_name());
				mDialog.dismiss();
				break;

			default:
				break;
			}

		};

	};

	public void MoveToUpChapter(int book_id) {

		db = new DBTool(context);
		if (db.queryIsExists(book_id)) {
			chapter = db.query(book_id);
			if (chapter.getChapter_up_id() != -1) {

				JsonDeal.dealOneChapter(book_id, chapter.getChapter_up_id(),
						handler);
				showRoundProcessDialog(context,
						R.layout.loading_process_dialog_anim);

			} else {
				Toast.makeText(context, "已经到首页", Toast.LENGTH_SHORT).show();
			}

		} else {
			Toast.makeText(context, "已经到首页", Toast.LENGTH_SHORT).show();
		}
		db.close();
	}
	public void MoveToNextChapter(int book_id) {

		db = new DBTool(context);
		if (db.queryIsExists(book_id)) {
			chapter = db.query(book_id);
			if (chapter.getChapter_next_id() != -1) {

				JsonDeal.dealOneChapter(book_id, chapter.getChapter_next_id(),
						handler);
				
				showRoundProcessDialog(context,
						R.layout.loading_process_dialog_anim);
			} else {
				Toast.makeText(context, "等待更新", Toast.LENGTH_SHORT).show();
			}

		} else {
			Toast.makeText(context, "已经到尾页", Toast.LENGTH_SHORT).show();
		}
		db.close();
	}

	public void showRoundProcessDialog(final Context mContext, final int layout) {
		// TODO Auto-generated method stub
		mDialog = new AlertDialog.Builder(mContext).create();
		// mDialog.setOnKeyListener(keyListener);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.show();
		// 注意此处要放在show之后 否则会报异常
		mDialog.setContentView(layout);

	}

	public void onDestory() {
		if (openFile != null) {
			openFile.onDestroy();
		}
	}

}
