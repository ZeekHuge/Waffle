/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.tree;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.R.string;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.library.LibraryActivity;

import com.haigang.testflip.act.MainAct;

public abstract class TreeActivity2<T extends FBTree> extends ListActivity implements OnClickListener{
	private static final String OPEN_TREE_ACTION = "android.fbreader.action.OPEN_TREE";

	public static final String TREE_KEY_KEY = "TreeKey";
	public static final String SELECTED_TREE_KEY_KEY = "SelectedTreeKey";
	public static final String HISTORY_KEY = "HistoryKey";
	public static  LinearLayout my_list;
	

	public T myCurrentTree;
	// we store the key separately because
	// it will be changed in case of myCurrentTree.removeSelf() call
	public FBTree.Key myCurrentKey;
	public ArrayList<FBTree.Key> myHistory;
	
	private ImageButton imageButton1,imageButton2;
	private ProgressDialog mpDialog = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
//		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.book2);
		my_list=(LinearLayout)findViewById(R.id.my_list);
		imageButton1 = (ImageButton) findViewById(R.id.shugui);
		imageButton2 = (ImageButton) findViewById(R.id.shucheng);
		imageButton1.setOnClickListener(this);
		imageButton2.setOnClickListener(this);
		setTitle("book");
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());
	}

	@Override
	public TreeAdapter2 getListAdapter() {
//		Log.i("sss", "getListAdapter");
		if(myHistory!=null)
		{
				if(myHistory.size()==0)
				{
					my_list.setVisibility(View.VISIBLE);
				}
				else 
				{
					my_list.setVisibility(View.GONE);
				}
				
		}
		return (TreeAdapter2)super.getListAdapter();
	}

	protected T getCurrentTree() {
		return myCurrentTree;
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		OrientationUtil.setOrientation(this, intent);
		if (OPEN_TREE_ACTION.equals(intent.getAction())) {
			runOnUiThread(new Runnable() {
				public void run() {
					init(intent);
				}
			});
		} else {
			super.onNewIntent(intent);
		}
	}

	protected abstract T getTreeByKey(FBTree.Key key);
	public abstract boolean isTreeSelected(FBTree tree);

	protected boolean isTreeInvisible(FBTree tree) {
		return false;
	}
	
	public void onClose () {
		TreeActivity2.this.finish();
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			FBTree parent = null;
//			while (parent == null && !myHistory.isEmpty()) {
//				parent = getTreeByKey(myHistory.remove(myHistory.size() - 1));
//			}
//			
////			if(myHistory.size()==0)
////			{
////				Log.i("sss", "history"+myHistory.size());
////				my_list.setVisibility(View.VISIBLE);
////			}
////			else {
////				my_list.setVisibility(View.GONE);
////			}
//			
//			if (parent == null) {
//				parent = myCurrentTree.Parent;
//			}
//			if (parent != null && !isTreeInvisible(parent)) {
//				openTree(parent, myCurrentTree, false);
//				return true;
//			}
//			
//		}
//
//		return false;
//	}

	// TODO: change to protected
	public void openTree(final FBTree tree) {
		LibraryActivity.i=0;
		openTree(tree, null, true);
	}

	protected void onCurrentTreeChanged() {
	}

	public void openTree(final FBTree tree, final FBTree treeToSelect, final boolean storeInHistory) {
		switch (tree.getOpeningStatus()) {
			case WAIT_FOR_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
			
				final String messageKey = tree.getOpeningStatusMessage();
				if (messageKey != null) {
					UIUtil.runWithMessage(
						TreeActivity2.this, messageKey,
						new Runnable() {
							public void run() {
								tree.waitForOpening();
							}
						},
						new Runnable() {
							public void run() {
								openTreeInternal(tree, treeToSelect, storeInHistory);
							}
						},
						true
					);
					
					
				} else {
					Log.i("sss", "book2");
				
					tree.waitForOpening();
					openTreeInternal(tree, treeToSelect, storeInHistory);
				}
				break;
			default:
				openTreeInternal(tree, treeToSelect, storeInHistory);
				break;
		}
	}

	protected void init(Intent intent) {
		final FBTree.Key key = (FBTree.Key)intent.getSerializableExtra(TREE_KEY_KEY);
		final FBTree.Key selectedKey = (FBTree.Key)intent.getSerializableExtra(SELECTED_TREE_KEY_KEY);
		myCurrentTree = getTreeByKey(key);
		// not myCurrentKey = key
		// because key might be null
		myCurrentKey = myCurrentTree.getUniqueKey();
		final TreeAdapter2 adapter = getListAdapter();
		adapter.replaceAll(myCurrentTree.subTrees());
		setTitle(myCurrentTree.getTreeTitle());
		final FBTree selectedTree =
			selectedKey != null ? getTreeByKey(selectedKey) : adapter.getFirstSelectedItem();
		final int index = adapter.getIndex(selectedTree);
		if (index != -1) {
			setSelection(index);
			getListView().post(new Runnable() {
				public void run() {
					setSelection(index);
				}
			});
		}

		myHistory = (ArrayList<FBTree.Key>)intent.getSerializableExtra(HISTORY_KEY);
		if (myHistory == null) {
			myHistory = new ArrayList<FBTree.Key>();
		}
		onCurrentTreeChanged();
	}

	private void openTreeInternal(FBTree tree, FBTree treeToSelect, boolean storeInHistory) {
		Log.i("sss", "book3");
		switch (tree.getOpeningStatus()) {
			case READY_TO_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				if (storeInHistory && !myCurrentKey.equals(tree.getUniqueKey())) {
					Log.i("sss", "book4");
					myHistory.add(myCurrentKey);
				}
				onNewIntent(new Intent(this, getClass())
					.setAction(OPEN_TREE_ACTION)
					.putExtra(TREE_KEY_KEY, tree.getUniqueKey())
					.putExtra(
						SELECTED_TREE_KEY_KEY,
						treeToSelect != null ? treeToSelect.getUniqueKey() : null
					)
					.putExtra(HISTORY_KEY, myHistory)
				);
				break;
			case CANNOT_OPEN:
				Log.i("sss", "book5");
				UIUtil.showErrorMessage(TreeActivity2.this, tree.getOpeningStatusMessage());
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if ( mpDialog !=null ){
			mpDialog.dismiss();
		}
	}
	
	public void showProgressDialog(String msg) {
		mpDialog = new ProgressDialog(TreeActivity2.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mpDialog.setMessage(msg);
		mpDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mpDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		mpDialog.show();
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.shugui:
			
			break;
		case R.id.shucheng:
			showProgressDialog("正在进入书城......");
			Intent intent1 = new Intent();
			intent1.setClass(TreeActivity2.this, MainAct.class );
			startActivity(intent1);
			overridePendingTransition(0, 0);
			TreeActivity2.this.finish();
			
			break;
		default:
			break;
		}
	}
}
