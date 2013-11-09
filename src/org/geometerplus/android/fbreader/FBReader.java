/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader;

import java.io.Reader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import android.R.integer;
import android.app.Activity;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.aa.tool.DealChapter;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;

import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.library.*;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.*;

import org.geometerplus.android.fbreader.api.*;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.preferences.PreferenceFontActivity;

import org.geometerplus.android.util.PackageUtil;
import org.geometerplus.android.util.UIUtil;

import com.haigang.testflip.act.TestActivity;

public final class FBReader extends Activity implements OnClickListener ,OnSeekBarChangeListener,OnLongClickListener{
	public static final String ACTION_OPEN_BOOK = "android.fbreader.action.VIEW";
	public static final String BOOK_KEY = "fbreader.book";
	public static final String BOOKMARK_KEY = "fbreader.bookmark";
	private long exitTime = 0;
	private TextView bookBtn1, bookBtn2, bookBtn3, bookBtn4, bookBtn5,
			bookBtn6, bookBtn7; 
	public static final int EXIT = 1;
	private SeekBar seekBar2; 
	private int light=10;
	private WindowManager.LayoutParams lp;
	public static final int NOT_EXIT = 0;
	static final int ACTION_BAR_COLOR = Color.DKGRAY;
	private Boolean show = false;// popwindow是否显示
	public static final int REQUEST_PREFERENCES = 1;
	public static final int REQUEST_CANCEL_MENU = 2;
	private boolean isDay = true;
	public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
	public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;
	public static final String ACTION_CLOSE = "close";
	private ImageView return_booklist;
	private ImageView shuqian;
	private TextView book_name;
	private PopupWindow mPopupWindow = null;
	private View popupwindwow, toolpop;
	public static ImageView book_image;
	private LinearLayout book_image2;
	
	public static Context context;
	
	public static int READE_STATE = 0;//0 not internet ，1 internet

	public static int BOOK_ID = -1;
	
	public static void openBookActivity(Context context, Book book,
			Bookmark bookmark) {
		context.startActivity(new Intent(context, FBReader.class)
				.setAction(ACTION_OPEN_BOOK)
				.putExtra(BOOK_KEY, SerializerUtil.serialize(book))
				.putExtra(BOOKMARK_KEY, SerializerUtil.serialize(bookmark))
				.putExtra("READE_STATE", 0)
				.putExtra("BOOK_ID", -1)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	public static void openBookActivityInternet(Context context, Book book,
			Bookmark bookmark,int book_id,String book_name) {
		context.startActivity(new Intent(context, FBReader.class)
				.setAction(ACTION_OPEN_BOOK)
				.putExtra(BOOK_KEY, SerializerUtil.serialize(book))
				.putExtra(BOOKMARK_KEY, SerializerUtil.serialize(bookmark))
				.putExtra("READE_STATE", 1)
				.putExtra("BOOK_ID", book_id)
				.putExtra("BOOK_NAME", book_name)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	/**
	 * 记录数据 并清空popupwindow
	 */
	private void clear() {
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		show = false;
		mPopupWindow.dismiss();
		book_image2.setVisibility(View.GONE);

	}

	private static ZLAndroidLibrary getZLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

	private FBReaderApp myFBReaderApp;
	private volatile Book myBook;

	private RelativeLayout myRootView;
	private ZLAndroidWidget myMainView;

	private int myFullScreenFlag;
	private DealChapter deal;
	private String myMenuLanguage;
	private LinearLayout layout;

	private static final String PLUGIN_ACTION_PREFIX = "___";
	private final List<PluginApi.ActionInfo> myPluginActions = new LinkedList<PluginApi.ActionInfo>();
	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(
					true).<PluginApi.ActionInfo> getParcelableArrayList(
					PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					int index = 0;
					while (index < myPluginActions.size()) {
						myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX
								+ index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						myFBReaderApp.addAction(PLUGIN_ACTION_PREFIX + index++,
								new RunPluginAction(FBReader.this,
										myFBReaderApp, info.getId()));
					}
				}
			}
		}
	};

	private synchronized void openBook(Intent intent, Runnable action,
			boolean force) {
		if (!force && myBook != null) {
			return;
		}

		myBook = SerializerUtil
				.deserializeBook(intent.getStringExtra(BOOK_KEY));
		final Bookmark bookmark = SerializerUtil.deserializeBookmark(intent
				.getStringExtra(BOOKMARK_KEY));
		if (myBook == null) {
			final Uri data = intent.getData();
			if (data != null) {
				myBook = createBookForFile(ZLFile.createFileByPath(data
						.getPath()));
			}
		}
		myFBReaderApp.openBook(myBook, bookmark, action);
	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myFBReaderApp.Collection.getBookByFile(file);
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myFBReaderApp.Collection.getBookByFile(child);
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	private Runnable getPostponedInitAction() {
		return new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {

						DictionaryUtil.init(FBReader.this);
					}
				});
			}
		};
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(
				this));
		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		myRootView = (RelativeLayout) findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget) findViewById(R.id.main_view);

		return_booklist = (ImageView) findViewById(R.id.return_booklist);
		shuqian = (ImageView) findViewById(R.id.shuqian);
		book_name = (TextView) findViewById(R.id.book_name);

		layout = (LinearLayout) findViewById(R.id.my_booklist);
		layout.setVisibility(View.GONE);
		book_image = (ImageView) findViewById(R.id.book_image);
		book_image.setOnClickListener(this);
		book_image2=(LinearLayout)findViewById(R.id.book_image2);
		book_image.setOnLongClickListener(this);
		seekBar2=(SeekBar)findViewById(R.id.seekBar2);
		seekBar2.setOnSeekBarChangeListener(this);
		lp = getWindow().getAttributes();
		lp.screenBrightness = light / 10.0f < 0.01f ? 0.01f : light / 10.0f;
		getWindow().setAttributes(lp);

		return_booklist.setOnClickListener(this); 
		shuqian.setOnClickListener(this);
		book_name.setOnClickListener(this);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		getZLibrary().setActivity(this);
		myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
		deal = new DealChapter(FBReader.this);
		if (myFBReaderApp == null) {
			myFBReaderApp = new FBReaderApp(new BookCollectionShadow(),deal);
		}
		getCollection().bindToService(this, null);
		myBook = null;

		final ZLAndroidApplication androidApplication = (ZLAndroidApplication) getApplication();
		if (androidApplication.myMainWindow == null) {
			androidApplication.myMainWindow = new ZLAndroidApplicationWindow(
					myFBReaderApp);
			myFBReaderApp.initWindow();
		}

		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary) ZLibrary
				.Instance();
		myFullScreenFlag = zlibrary.ShowStatusBarOption.getValue() ? 0
				: WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				myFullScreenFlag);

		if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myFBReaderApp);
		}
		myFBReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(
				this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES,
				new ShowPreferencesAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOK_INFO,
				new ShowBookInfoAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this,
				myFBReaderApp));
		// 目录书签
		myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS,
				new ShowBookmarksAction(this, myFBReaderApp));
		// addMenuItem(menu, ActionCode.SETFONT); 字体设置
		myFBReaderApp.addAction(ActionCode.SETFONT,
				new ShowFontPreferencesAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_NETWORK_LIBRARY,
				new ShowNetworkLibraryAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this,
				myFBReaderApp));
		// 翻看
		myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION,
				new ShowNavigationAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this,
				myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(
				this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL,
				new SelectionShowPanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL,
				new SelectionHidePanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD,
				new SelectionCopyAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_SHARE,
				new SelectionShareAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE,
				new SelectionTranslateAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK,
				new SelectionBookmarkAction(this, myFBReaderApp));
		// 目录
		// myFBReaderApp.addAction(ActionCode.SHOW_LIST, new
		// ShowLibraryAction(this, myFBReaderApp));
   
		myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK,
				new ProcessHyperlinkAction(this, myFBReaderApp));

		// myFBReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU, new
		// ShowCancelMenuAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM,
				new SetScreenOrientationAction(this, myFBReaderApp,
						ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR,
				new SetScreenOrientationAction(this, myFBReaderApp,
						ZLibrary.SCREEN_ORIENTATION_SENSOR));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT,
				new SetScreenOrientationAction(this, myFBReaderApp,
						ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE,
				new SetScreenOrientationAction(this, myFBReaderApp,
						ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			myFBReaderApp.addAction(
					ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT,
					new SetScreenOrientationAction(this, myFBReaderApp,
							ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			myFBReaderApp.addAction(
					ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
					new SetScreenOrientationAction(this, myFBReaderApp,
							ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
		
		//跳转，这里你可以通过intent跳转至欢迎界面，
		Intent intent = new Intent(this, LibraryActivity.class);
		startActivity(intent);
		//
		setTitle("book");
		setPop();
	}

	public ZLAndroidWidget getMainView() {
		return myMainView;
	}

	
	@Override
	protected void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		final Uri data = intent.getData();
		
		Log.i("image", "21");
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action)
				|| ACTION_CLOSE.equals(action)) {
			myFBReaderApp.closeWindow();

		} else if (Intent.ACTION_VIEW.equals(action) && data != null
				&& "fbreader-action".equals(data.getScheme())) {
			myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(),
					data.getFragment());
		} else if (Intent.ACTION_VIEW.equals(action)
				|| ACTION_OPEN_BOOK.equals(action)) {
			int type = intent.getIntExtra("READE_STATE", -1);
			Log.i("Page","READE_STATE:"+READE_STATE);
			if ( type != -1 && type != 0 ) {
				READE_STATE = type;
			} else {
				READE_STATE = 0;
			}
			int book_id = intent.getIntExtra("BOOK_ID", -1);
			Log.i("TEST",book_id+"");
			if ( book_id != -1 ) {
				BOOK_ID = book_id;
			} else {
				BOOK_ID = -1;
			}
			if ( intent.getStringExtra("BOOK_NAME") != null ) {
				TextView tv = (TextView) findViewById(R.id.book_name);
				tv.setText(intent.getStringExtra("BOOK_NAME"));
			}
			Log.i("TEST",BOOK_ID+"");
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					openBook(intent, null, true);
				}
			});
		} else if (Intent.ACTION_SEARCH.equals(action)) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup) myFBReaderApp
							.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					myFBReaderApp.TextSearchPatternOption.setValue(pattern);
					if (myFBReaderApp.getTextView().search(pattern, true,
							false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								myFBReaderApp.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FBReader.this,
										"textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		getCollection().bindToService(this, new Runnable() {
			public void run() {
				new Thread() {
					public void run() {
						openBook(getIntent(), getPostponedInitAction(), false);
						myFBReaderApp.getViewWidget().repaint();
					}
				}.start();

				myFBReaderApp.getViewWidget().repaint();
			}
		});

		initPluginActions();

		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary) ZLibrary
				.Instance();

		final int fullScreenFlag = zlibrary.ShowStatusBarOption.getValue() ? 0
				: WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, getClass()));
		}

		SetScreenOrientationAction.setOrientation(this, zlibrary
				.getOrientationOption().getValue());

		((PopupPanel) myFBReaderApp.getPopupById(TextSearchPopup.ID))
				.setPanelInfo(this, myRootView);
		((PopupPanel) myFBReaderApp.getPopupById(NavigationPopup.ID))
				.setPanelInfo(this, myRootView);
		((PopupPanel) myFBReaderApp.getPopupById(SelectionPopup.ID))
				.setPanelInfo(this, myRootView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		switchWakeLock(hasFocus
				&& getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < myFBReaderApp
						.getBatteryLevel());
	}

	private void initPluginActions() {
		Log.i("image", " " + 20);
		synchronized (myPluginActions) {
			int index = 0;
			while (index < myPluginActions.size()) {
				myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
			}
			myPluginActions.clear();
		}

		sendOrderedBroadcast(new Intent(PluginApi.ACTION_REGISTER), null,
				myPluginInfoReceiver, null, RESULT_OK, null, null);
	}

	@Override
	protected void onResume() {
		super.onResume();

		myStartTimer = true;
		final int brightnessLevel = getZLibrary().ScreenBrightnessLevelOption
				.getValue();
		if (brightnessLevel != 0) {
			setScreenBrightness(brightnessLevel);
		} else {
			setScreenBrightnessAuto();
		}
		if (getZLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(false);
		}

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

		PopupPanel.restoreVisibilities(myFBReaderApp);
		ApiServerImplementation.sendEvent(this,
				ApiListener.EVENT_READ_MODE_OPENED);

		getCollection().bindToService(this, new Runnable() {
			public void run() {
				final BookModel model = myFBReaderApp.Model;
				if (model == null || model.Book == null) {
					return;
				}
				onPreferencesUpdate(myFBReaderApp.Collection
						.getBookById(model.Book.getId()));
			}
		});
	}

	@Override
	protected void onPause() {
		try {
			unregisterReceiver(myBatteryInfoReceiver);
		} catch (IllegalArgumentException e) {
			// do nothing, this exception means myBatteryInfoReceiver was not
			// registered
		}
		myFBReaderApp.stopTimer();
		if (getZLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(true);
		}
		myFBReaderApp.onWindowClosing();
		super.onPause();
	}

	@Override
	protected void onStop() {
		ApiServerImplementation.sendEvent(this,
				ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(myFBReaderApp, this);
		if ( mPopupWindow != null ) {
			clear();
		}
		
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getCollection().unbind();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		myFBReaderApp.onWindowClosing();
		super.onLowMemory();
	}

	@Override
	public boolean onSearchRequested() {
		Log.i("image", "a10");
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		myFBReaderApp.hideActivePopup();
		final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					myFBReaderApp.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(myFBReaderApp.TextSearchPatternOption.getValue(), true,
				null, false);
		return true;
	}

	public void showSelectionPanel() {
		Log.i("image", "a9");
		final ZLTextView view = myFBReaderApp.getTextView();
		((SelectionPopup) myFBReaderApp.getPopupById(SelectionPopup.ID)).move(
				view.getSelectionStartY(), view.getSelectionEndY());
		myFBReaderApp.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		Log.i("image", "a8");
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			myFBReaderApp.hideActivePopup();
		}
	}

	private void onPreferencesUpdate(Book book) {
		Log.i("image", "a7");
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("image", "a6");
		switch (requestCode) {
		case REQUEST_PREFERENCES:
			if (resultCode != RESULT_DO_NOTHING) {
				final Book book = BookInfoActivity.bookByIntent(data);
				if (book != null) {
					getCollection().bindToService(this, new Runnable() {
						public void run() {
							onPreferencesUpdate(book);
						}
					});
				}
			}
			break;
		case REQUEST_CANCEL_MENU:
			myFBReaderApp.runCancelAction(resultCode - 1);
			break;
		}
	}

	public void navigate() {
		Log.i("image", "a5");
		((NavigationPopup) myFBReaderApp.getPopupById(NavigationPopup.ID))
				.runNavigation();
	}

	private Menu addSubMenu(Menu menu, String id) {
		final ZLAndroidApplication application = (ZLAndroidApplication) getApplication();
		return application.myMainWindow.addSubMenu(menu, id);
	}

	private void addMenuItem(Menu menu, String actionId, String name) {
		final ZLAndroidApplication application = (ZLAndroidApplication) getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, name);
	}

	private void addMenuItem(Menu menu, String actionId, int iconId) {
		final ZLAndroidApplication application = (ZLAndroidApplication) getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, iconId, null);
	}

	private void addMenuItem(Menu menu, String actionId) {
		final ZLAndroidApplication application = (ZLAndroidApplication) getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(FBReader.this,LibraryActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 添加对menu按钮的监听
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (show) {
				show = false;
				mPopupWindow.dismiss();
				layout.setVisibility(View.GONE);
				book_image2.setVisibility(View.GONE);
				ZLAndroidWidget.isRead2=true;

			} else {
				ZLAndroidWidget.isRead2=false;
				show = true;
				pop();
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	private void setButtonLight(boolean enabled) {
		Log.i("image", "a4");
		try {
			final WindowManager.LayoutParams attrs = getWindow()
					.getAttributes();
			final Class<?> cls = attrs.getClass();
			final Field fld = cls.getField("buttonBrightness");
			if (fld != null && "float".equals(fld.getType().toString())) {
				fld.setFloat(attrs, enabled ? -1.0f : 0.0f);
				getWindow().setAttributes(attrs);
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
	}

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;

	public final void createWakeLock() {
		Log.i("image", "a3");
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
							.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
									"FBReader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			myFBReaderApp.startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		Log.i("image", "a2");
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			final int level = intent.getIntExtra("level", 100);
			final ZLAndroidApplication application = (ZLAndroidApplication) getApplication();
			application.myMainWindow.setBatteryLevel(level);
			switchWakeLock(hasWindowFocus()
					&& getZLibrary().BatteryLevelToTurnScreenOffOption
							.getValue() < level);
		}
	};

	private void setScreenBrightnessAuto() {

		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	public void setScreenBrightness(int percent) {

		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
	}

	public int getScreenBrightness() {

		final int level = (int) (100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private BookCollectionShadow getCollection() {

		return (BookCollectionShadow) myFBReaderApp.Collection;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.return_booklist:
			Intent intent = new Intent();
			intent.setClass(FBReader.this, LibraryActivity.class);
			startActivity(intent);
			break;
		case R.id.shuqian:
			ShowBookmarksAction action = new ShowBookmarksAction(this,
					myFBReaderApp);
			action.run(null);
			break;
		// 点击中间 出现菜单
		case R.id.book_image:

			menuVisibleGone();
			break;

		// 字体按钮
		case R.id.bookBtn1:
			ShowFontPreferencesAction action2 = new ShowFontPreferencesAction(
					this, myFBReaderApp);
			action2.run(null);
			menuVisibleGone();
			break;
		// 黑白按钮
		case R.id.bookBtn2:
			if (isDay) {
				myFBReaderApp.setColorProfileName(ColorProfile.NIGHT);
				myFBReaderApp.getViewWidget().reset();
				myFBReaderApp.getViewWidget().repaint();
				isDay = false;
			} else {
				myFBReaderApp.setColorProfileName(ColorProfile.DAY);
				myFBReaderApp.getViewWidget().reset();
				myFBReaderApp.getViewWidget().repaint();

				isDay = true;
			}
			seekBar2.setVisibility(View.VISIBLE);
			menuVisibleGone();
			break;
		// 书签按钮
		case R.id.bookBtn3:
			// Toast.makeText(getApplicationContext(), "3",
			// Toast.LENGTH_SHORT).show();
			ShowBookmarksAction action4 = new ShowBookmarksAction(this,
					myFBReaderApp);
			action4.run(null);
			menuVisibleGone();
			break;
		// 跳转按钮
		case R.id.bookBtn4:
			// Toast.makeText(getApplicationContext(), "4",
			// Toast.LENGTH_SHORT).show();
			ShowNavigationAction action5 = new ShowNavigationAction(this,
					myFBReaderApp);
			action5.run(null);
			menuVisibleGone();
			break;
		// 内容查找
		case R.id.bookBtn6:
			// Toast.makeText(getApplicationContext(), "5",
			// Toast.LENGTH_SHORT).show();
			SearchAction action6 = new SearchAction(this, myFBReaderApp);
			action6.run(null);
			menuVisibleGone();
			break;
		// 设置
		case R.id.bookBtn7:
			// Toast.makeText(getApplicationContext(), "6",
			// Toast.LENGTH_SHORT).show();
			ShowPreferencesAction action3 = new ShowPreferencesAction(this,
					myFBReaderApp);
			action3.run(null);
			menuVisibleGone();

			break;
		default:
			break;
		}
	}

	
	/**
	 * popupwindow的弹出,工具栏
	 */
	public void pop() {

		mPopupWindow.showAtLocation(myMainView, Gravity.BOTTOM, 0, 0);
		bookBtn1 = (TextView) popupwindwow.findViewById(R.id.bookBtn1);
		bookBtn2 = (TextView) popupwindwow.findViewById(R.id.bookBtn2);
		bookBtn3 = (TextView) popupwindwow.findViewById(R.id.bookBtn3);
		bookBtn4 = (TextView) popupwindwow.findViewById(R.id.bookBtn4);
		bookBtn6 = (TextView) popupwindwow.findViewById(R.id.bookBtn6);
		bookBtn7 = (TextView) popupwindwow.findViewById(R.id.bookBtn7);

		bookBtn1.setOnClickListener(this);
		bookBtn2.setOnClickListener(this);
		bookBtn3.setOnClickListener(this);
		bookBtn4.setOnClickListener(this);
		bookBtn6.setOnClickListener(this);
		bookBtn7.setOnClickListener(this);

		layout.setVisibility(View.VISIBLE);
		book_image2.setVisibility(View.VISIBLE);

	}

	/**
	 * 初始化所有POPUPWINDOW
	 */
	private void setPop() {
		popupwindwow = this.getLayoutInflater().inflate(R.layout.bookpop, null);
		toolpop = this.getLayoutInflater().inflate(R.layout.toolpop, null);
		mPopupWindow = new PopupWindow(popupwindwow, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
	}

	private void menuVisibleGone() {
		if (show) {
			show = false;
			mPopupWindow.dismiss();
			layout.setVisibility(View.GONE);
			book_image2.setVisibility(View.GONE);
			ZLAndroidWidget.isRead2=true;
		} else {
			show = true;
			pop();
			ZLAndroidWidget.isRead2=false;
		}
	}

	public static void closeApp(Context context) {
		context.startActivity(new Intent(context, FBReader.class)
				.setAction(ACTION_CLOSE).putExtra("exit", EXIT)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
		
		// 亮度进度条
		case R.id.seekBar2:
			light = seekBar2.getProgress();
			lp.screenBrightness = light / 10.0f < 0.01f ? 0.01f : light / 10.0f;
			getWindow().setAttributes(lp);
			break;
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		seekBar2.setVisibility(View.GONE);
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.book_image)
		{
			book_image.setEnabled(false);
			book_image.setVisibility(View.GONE);
			ZLAndroidWidget.isRead=false;
		    Display display = getWindowManager().getDefaultDisplay();
	        int width = display.getWidth();
	        int height = display.getHeight();
	        final ZLView view = ZLApplication.Instance().getCurrentView();
			return view.onFingerLongPress(width/2, height/2);
		}
		return false;

	}
	

}
