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

import java.util.*;

import android.R.integer;
import android.app.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.content.*;

import org.aa.tool.ChapterMode;
import org.aa.tool.OpenFile;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;

import com.Andware.testjson.JsonDeal;
import com.haigang.testflip.act.TestActivity;

/**
 * ��ǩ
 * 
 * @author Administrator
 * 
 */
public class BookmarksActivity extends TabActivity implements
		MenuItem.OnMenuItemClickListener, OnScrollListener { 
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;

	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();

	private volatile BookmarksAdapter myThisBookAdapter;
	private volatile BookmarksAdapter myAllBooksAdapter;
	private volatile BookmarksAdapter mySearchResultsAdapter;
	private int MaxDateNum;
	private ListView lv;
	private int book_id;
	private Handler handler;
	private Button bt;
	private ProgressBar pg;
	private View moreView;
	private List<ChapterList> chapterLists;
	int i = 1;
	SimpleAdapter mSimpleAdapter;
	private int lastVisibleIndex;
	private ArrayList<HashMap<String, String>> list;
	private OpenFile openFile;

	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private final ZLStringOption myBookmarkSearchPatternOption = new ZLStringOption(
			"BookmarkSearch", "Pattern", "");

	private ListView createTab(String tag, int id) {
		final TabHost host = getTabHost();
		final String label = myResource.getResource(tag).getValue();
		host.addTab(host.newTabSpec(tag).setIndicator(label).setContent(id));
		return (ListView) findViewById(id);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(
				this));
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		book_id = getIntent().getIntExtra("BOOK_ID", -1);
		final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.bookmarks,
				host.getTabContentView(), true);

		Log.i("TEST",book_id+":test");
		
		if (book_id != -1) {
			Log.i("TEST","1");
			lv =createTab(
					"muluBooks", R.id.this_mulu);
			moreView = getLayoutInflater().inflate(R.layout.moredata, null);
			handler = new Handler();
			bt = (Button) moreView.findViewById(R.id.bt_load);
			pg = (ProgressBar) moreView.findViewById(R.id.pg);
			chapterLists = JsonDeal.getChapterList(book_id, i, 10);
			MaxDateNum = chapterLists.get(0).getMaxSize();
			list = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < 10; i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("ItemTitle", chapterLists.get(i).getCname());
				map.put("Chapter", chapterLists.get(i).getChapter_id()+"");
				list.add(map);
			}
			for ( HashMap<String, String> map : list) {
				Log.i("json",2+"");
			}
			i += 2;
			mSimpleAdapter = new SimpleAdapter(this, list,
					R.layout.chapteritem, new String[] { "ItemTitle" },
					new int[] { R.id.tv_title });
			lv.addFooterView(moreView);
			lv.setAdapter(mSimpleAdapter);
			lv.setOnScrollListener(this);
			bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					pg.setVisibility(View.VISIBLE);// ���������ɼ�
					bt.setVisibility(View.GONE);// ��ť���ɼ�

					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							loadMoreDate();// ���ظ�������
							bt.setVisibility(View.VISIBLE);
							pg.setVisibility(View.GONE);
							mSimpleAdapter.notifyDataSetChanged();// ֪ͨlistViewˢ������
						}

					}, 2000);
				}
			});
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public synchronized void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					// TODO Auto-generated method stub
					int chapterId = Integer.parseInt(list.get(position-1).get("Chapter"));
					ChapterMode chapter = JsonDeal.dealOneChapter(book_id, chapterId);
					if ( chapter.getContent() !=null ) {
						openFile = new OpenFile(chapter.getContent(),chapter.getBook_id(),
								BookmarksActivity.this,chapter.getChapter_name());
					}
				
				}
			});
		}
		myBook = SerializerUtil.deserializeBook(getIntent().getStringExtra(
				FBReader.BOOK_KEY));
		setTitle("book");

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if ( openFile != null ) {
			openFile.onDestroy();
		}
		
		super.onDestroy();
	}

	

	// ---------��ʼ��
	private class Initializer implements Runnable {
		public void run() {
			long id = 0;
			if (myBook != null) {
				for (BookmarkQuery query = new BookmarkQuery(myBook, 20);; query = query
						.next()) {
					final List<Bookmark> thisBookBookmarks = myCollection
							.bookmarks(query);
					if (thisBookBookmarks.isEmpty()) {
						break;
					}
					myThisBookAdapter.addAll(thisBookBookmarks);
					myAllBooksAdapter.addAll(thisBookBookmarks);
				}
			}
			id = 0;
			for (BookmarkQuery query = new BookmarkQuery(20);; query = query
					.next()) {
				final List<Bookmark> allBookmarks = myCollection
						.bookmarks(query);
				if (allBookmarks.isEmpty()) {
					break;
				}
				myAllBooksAdapter.addAll(allBookmarks);
			}

			runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		runOnUiThread(new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(true);
			}
		});

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				if (myAllBooksAdapter != null) {
					return;
				}

				if (myBook != null) {
					myThisBookAdapter = new BookmarksAdapter(createTab(
							"thisBook", R.id.this_book), true);
				} else {
					findViewById(R.id.this_book).setVisibility(View.GONE);
				}
				myAllBooksAdapter = new BookmarksAdapter(createTab("allBooks",
						R.id.all_books), false);
				findViewById(R.id.search_results).setVisibility(View.GONE);
				// ---------Ŀ¼

				new Thread(new Initializer()).start();
			}
		});

		OrientationUtil.setOrientation(this, getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);

		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		myBookmarkSearchPatternOption.setValue(pattern);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
		for (Bookmark b : myAllBooksAdapter.bookmarks()) {
			if (MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				bookmarks.add(b);
			}
		}
		if (!bookmarks.isEmpty()) {
			showSearchResultsTab(bookmarks);
		} else {
			UIUtil.showErrorMessage(this, "bookmarkNotFound");
		}
	}

	@Override
	protected void onStop() {
		myCollection.unbind();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuItem item = menu
				.add(0, 1, Menu.NONE, myResource.getResource("menu")
						.getResource("search").getValue());
		item.setOnMenuItemClickListener(this);
		item.setIcon(R.drawable.ic_menu_search);
		return true;
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	void showSearchResultsTab(LinkedList<Bookmark> results) {
		if (mySearchResultsAdapter == null) {
			mySearchResultsAdapter = new BookmarksAdapter(createTab("found",
					R.id.search_results), false);
		} else {
			mySearchResultsAdapter.clear();
		}
		mySearchResultsAdapter.addAll(results);
		getTabHost().setCurrentTabByTag("found");
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			return onSearchRequested();
		default:
			return true;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo()).position;
		final ListView view = (ListView) getTabHost().getCurrentView();
		final Bookmark bookmark = ((BookmarksAdapter) view.getAdapter())
				.getItem(position);
		switch (item.getItemId()) {
		case OPEN_ITEM_ID:
			gotoBookmark(bookmark);
			return true;
		case EDIT_ITEM_ID:
			final Intent intent = new Intent(this, BookmarkEditActivity.class);
			OrientationUtil.startActivityForResult(this, intent, 1);
			// TODO: implement
			return true;
		case DELETE_ITEM_ID:
			myCollection.deleteBookmark(bookmark);
			if (myThisBookAdapter != null) {
				myThisBookAdapter.remove(bookmark);
			}
			if (myAllBooksAdapter != null) {
				myAllBooksAdapter.remove(bookmark);
			}
			if (mySearchResultsAdapter != null) {
				mySearchResultsAdapter.remove(bookmark);
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void addBookmark() {
		final Bookmark bookmark = SerializerUtil
				.deserializeBookmark(getIntent().getStringExtra(
						FBReader.BOOKMARK_KEY));
		if (bookmark != null) {
			myCollection.saveBookmark(bookmark);
			myThisBookAdapter.add(bookmark);
			myAllBooksAdapter.add(bookmark);
		}
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.getBookId());
		if (book != null) {
			FBReader.openBookActivity(this, book, bookmark);
		} else {
			UIUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements
			AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
		private final List<Bookmark> myBookmarks = Collections
				.synchronizedList(new LinkedList<Bookmark>());
		private final boolean myShowAddBookmarkItem;

		BookmarksAdapter(ListView listView, boolean showAddBookmarkItem) {
			myShowAddBookmarkItem = showAddBookmarkItem;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}

		public List<Bookmark> bookmarks() {
			return Collections.unmodifiableList(myBookmarks);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarks) {
						for (Bookmark b : bookmarks) {
							final int position = Collections.binarySearch(
									myBookmarks, b, myComparator);
							if (position < 0) {
								myBookmarks.add(-position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void add(final Bookmark b) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarks) {
						final int position = Collections.binarySearch(
								myBookmarks, b, myComparator);
						if (position < 0) {
							myBookmarks.add(-position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void remove(final Bookmark b) {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarks.remove(b);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarks.clear();
					notifyDataSetChanged();
				}
			});
		}

		public void onCreateContextMenu(ContextMenu menu, View view,
				ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
			if (getItem(position) != null) {
				menu.setHeaderTitle(getItem(position).getText());
				menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("open")
						.getValue());
				// menu.add(0, EDIT_ITEM_ID, 0,
				// myResource.getResource("edit").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("delete")
						.getValue());
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView
					: LayoutInflater.from(parent.getContext()).inflate(
							R.layout.bookmark_item, parent, false);
			final ImageView imageView = (ImageView) view
					.findViewById(R.id.bookmark_item_icon);
			final TextView textView = (TextView) view
					.findViewById(R.id.bookmark_item_text);
			final TextView bookTitleView = (TextView) view
					.findViewById(R.id.bookmark_item_booktitle);

			final Bookmark bookmark = getItem(position);
			if (bookmark == null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.ic_list_plus);
				textView.setText(myResource.getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
			} else {
				imageView.setVisibility(View.GONE);
				textView.setText(bookmark.getText());
				if (myShowAddBookmarkItem) {
					bookTitleView.setVisibility(View.GONE);
				} else {
					bookTitleView.setVisibility(View.VISIBLE);
					bookTitleView.setText(bookmark.getBookTitle());
				}
			}
			return view;
		}

		@Override
		public final boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public final boolean isEnabled(int position) {
			return true;
		}

		public final long getItemId(int position) {
			return position;
		}

		public final Bookmark getItem(int position) {
			if (myShowAddBookmarkItem) {
				--position;
			}
			return (position >= 0) ? myBookmarks.get(position) : null;
		}

		public final int getCount() {
			return myShowAddBookmarkItem ? myBookmarks.size() + 1 : myBookmarks
					.size();
		}

		public final void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			final Bookmark bookmark = getItem(position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			} else {
				addBookmark();
			}
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		lastVisibleIndex = arg1 + arg2 - 1;

		// ���е���Ŀ�Ѿ������������ȣ����Ƴ��ײ���View
		if (arg3 == MaxDateNum + 1) {
			lv.removeFooterView(moreView);
			Toast.makeText(this, "����ȫ��������ɣ�û�и������ݣ�", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub
		if (arg1 == OnScrollListener.SCROLL_STATE_IDLE
				&& lastVisibleIndex == mSimpleAdapter.getCount()) {
			// �������ײ�ʱ�Զ�����
			// pg.setVisibility(View.VISIBLE);
			// bt.setVisibility(View.GONE);
			// handler.postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// loadMoreDate();
			// bt.setVisibility(View.VISIBLE);
			// pg.setVisibility(View.GONE);
			// mSimpleAdapter.notifyDataSetChanged();
			// }
			//
			// }, 2000);

		}
	}
	
	private void loadMoreDate() {
		int count = mSimpleAdapter.getCount();
		if (count + 5 < MaxDateNum) {
			
			chapterLists = JsonDeal.getChapterList(book_id, i, 5);
			int j = 0;
			// ÿ�μ���5��
			for (int i = count; i < count + 5; i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("ItemTitle", chapterLists.get(j).getCname());
				map.put("Chapter", chapterLists.get(j).getChapter_id()+"");
				list.add(map);
				j++;
			}
			i++;
		} else {
			// �����Ѿ�����5��
			chapterLists = JsonDeal.getChapterList(book_id, i, 5);
			int j = 0;
			for (int i = count; i < MaxDateNum; i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("ItemTitle", chapterLists.get(j).getCname());
				map.put("Chapter", chapterLists.get(j).getChapter_id()+"");
				list.add(map);
				j++;
			}
			i++;
		}

	}

	
	
	
}
