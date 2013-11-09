package com.haigang.testflip.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aa.tool.ChapterMode;
import org.aa.tool.DBTool;
import org.aa.tool.DealChapter;
import org.aa.tool.DealFile;
import org.aa.tool.OpenFile;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.zlibrary.ui.android.R;
import com.Andware.testjson.BookMode;
import com.Andware.testjson.GetRequest;
import com.Andware.testjson.JsonDeal;
import com.Andware.testjson.Listmode;
import com.demo.GetConnection;
import com.demo.MyListView;
import com.demo.MyListView.OnRefreshListener;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class Lay1Act extends Activity {
	private BaseAdapter adapter;
	SimpleAdapter listItemAdapter;
	private ArrayList<HashMap<String, Object>> listItem, listItem2, listItem3;
	private MyListView listView, listView2, listView3;
	int i = 0;
	int page = 1;
	private int MaxDateNum;
	private View moreView;
	private Handler handler;
	int pageSize = 5;
	private Button bt;
	private ProgressBar pg;
	List<Listmode> modes;
	DBTool db;
	private JsonDeal jsonDeal;
	GetRequest getRequest;
	private Dialog mDialog = null;
	int Position;
	DealChapter dealChapter = null;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.lay1);
		db = new DBTool(this);
		listView = (MyListView) findViewById(R.id.list);
		moreView = getLayoutInflater().inflate(R.layout.moredata, null);
		listItem = new ArrayList<HashMap<String, Object>>();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				getRequest = new GetRequest(handler2);
				getRequest.getHostList(page, pageSize, handler2);
			}
		});
		thread.start();
		showRoundProcessDialog(Lay1Act.this,
				R.layout.loading_process_dialog_anim);

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

	private void list1() {

		bt = (Button) moreView.findViewById(R.id.bt_load);
		pg = (ProgressBar) moreView.findViewById(R.id.pg);
		handler = new Handler();

		MaxDateNum = modes.get(0).getMaxSize();
		for (Listmode mode : modes) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", getImage(mode.getPicPath()));
			map.put("ItemName", "" + mode.getName());
			map.put("ItemPrice", "" + mode.getAuthor());
			listItem.add(map);
		}
		page++;
		listItemAdapter = new SimpleAdapter(this, listItem,// data
				// source
				R.layout.itemcity,// the xml of listTtem;
									// the subItem of array
				new String[] { "ItemImage", "ItemName", "ItemPrice" },
				// add xml of the subItem;
				new int[] { R.id.ItemImage, R.id.ItemName, R.id.ItemPrice });

		// set the ImageView can use the method - setImageBitmap;
		listItemAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				// judge the object is or not we want;
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView iv = (ImageView) view;
					iv.setImageBitmap((Bitmap) data);
					return true;
				} else
					return false;
			}
		});

		listView.setAdapter(listItemAdapter);

		listView.setonRefreshListener(listener);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pg.setVisibility(View.VISIBLE);// 将进度条可见
				bt.setVisibility(View.GONE);// 按钮不可见

				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						loadMoreDate();// 加载更多数据
						bt.setVisibility(View.VISIBLE);
						pg.setVisibility(View.GONE);
						handler2.obtainMessage(1).sendToTarget();
					}

				}, 2000);
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {

				Position = position;
				Thread thread = new Thread(new Runnable() {

					@Override
					public synchronized void run() {
						// TODO Auto-generated method stub
						BookMode book;
						book = GetRequest.getOneBookById(modes
								.get(Position - 1).getBook_id());
						
						if (db.queryIsExists(book.getId())) {
							
							if (book.getFirstChapterId() != -1) {
								ChapterMode chapter = db.query(book.getId());
								
								if (chapter.getChapter_id() != -1) {
									Intent intent = new Intent(Lay1Act.this,
											TestActivity.class);
									intent.putExtra("book_id",
											chapter.getBook_id());
									startActivity(intent);
								}
							}
						} else {
							Log.i("name","1");
							ChapterMode bookChapterContent;
							bookChapterContent = JsonDeal.dealOneChapter(
									book.getId(), book.getFirstChapterId());
							
							db.Insert(book.getId(),
									bookChapterContent.getChapter_id(),
									bookChapterContent.getChapter_up_id(),
									bookChapterContent.getChapter_next_id(),
									bookChapterContent.getContent(),bookChapterContent.getChapter_name());
							Intent intent = new Intent(Lay1Act.this,
									TestActivity.class);
							intent.putExtra("book_id", book.getId());
							startActivity(intent);
						}

					}
				});
				thread.start();

			}
		});
		listView.addFooterView(moreView);
	}

	private void loadMoreDate() {
		int count = listItemAdapter.getCount();
		if (count + 5 < MaxDateNum) {

			modes = GetRequest.getHostList(page, pageSize);
			// 每次加载5条
			for (Listmode mode : modes) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ItemImage", getImage(mode.getPicPath()));
				map.put("ItemName", "" + mode.getName());
				map.put("ItemPrice", "" + mode.getAuthor());
				listItem.add(map);
			}
			page++;
		} else {
			// 数据已经不足5条
			modes = GetRequest.getHostList(page, pageSize);
			for (Listmode mode : modes) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ItemImage", getImage(mode.getPicPath()));
				map.put("ItemName", "" + mode.getName());
				map.put("ItemPrice", "" + mode.getAuthor());
				listItem.add(map);
			}
			page++;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		db.close();
		super.onDestroy();
	}

	private Handler handler2 = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:
				list1();
				listItemAdapter.notifyDataSetChanged();
				listView.onRefreshComplete();
				break;

			case 1:
				listItemAdapter.notifyDataSetChanged();
				listView.onRefreshComplete();
				break;

			case 2:
				jsonDeal = new JsonDeal();
				Log.i("Log", getRequest.getResult());
				jsonDeal.dealManList(getRequest.getResult(), handler);
				modes = jsonDeal.getListmodes();
				list1();
				if (mDialog != null) {
					mDialog.dismiss();
				}
				break;
			}
		};
	};

	private OnRefreshListener listener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			handler2.obtainMessage(0).sendToTarget();
		}
	};

	private OnRefreshListener listener2 = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			handler2.obtainMessage(1).sendToTarget();
		}
	};

	private OnRefreshListener listener3 = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			handler2.obtainMessage(2).sendToTarget();
		}
	};

	// get image source for service;
	public Bitmap getImage(String urlString) {
		Bitmap bitmap = null;
		try {
			byte[] data = GetConnection.getimage(urlString);
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (Exception e) {

		}
		return bitmap;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			i++;
			if (i == 1) {
				Toast.makeText(getApplicationContext(), "再点击一次退出程序",
						Toast.LENGTH_SHORT).show();
				i++;
			}
			if (i == 2) {
				System.exit(0);
			}

		}
		return false;
	}
}
