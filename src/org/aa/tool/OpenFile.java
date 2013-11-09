package org.aa.tool;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;


import android.content.Context;
import android.util.Log;

public class OpenFile implements IBookCollection.Listener {

	public String filePath;
	
	private Book myBook;

	private int book_id;
	
	private String book_name;
	
	private BookCollectionShadow myCollection = new BookCollectionShadow();

	public OpenFile(String path, int book_id,Context context,String book_name) {
		// TODO Auto-generated constructor stub
		
		this.book_id = book_id;
		this.book_name = book_name;
		filePath = path;
		final Context fileContext = context;
		myCollection.addListener(this);
		myCollection.bindToService(context, new Runnable() {

			@Override
			public synchronized void run() {
				// TODO Auto-generated method stub
				Log.i("openBook","openStart");
				readBook(fileContext,
						filePath);
			}
			
		});
		
	}

	@Override
	public void onBookEvent(BookEvent event, Book book) {
		// TODO Auto-generated method stub
		if (event == BookEvent.Updated && book.equals(myBook)) {
			myBook.updateFrom(book);
		}
	}

	public void readBook(Context context, String path) {
		Log.i("BOOK",path);
		myBook = myCollection.getBookByFile(getFile(path));
		
		FBReader.openBookActivityInternet(context, myBook, null,book_id,book_name);
	}

	public static ZLFile getFile ( String path ) {
		ZLFile file = ZLPhysicalFile.createFileByPath(path);

		return file;
	}
	
	public void onDestroy() {
		// TODO Auto-generated method stub
		myCollection.removeListener(this);
		myCollection.unbind();
	}

	@Override
	public void onBuildEvent(Status status) {
		// TODO Auto-generated method stub

	}

}
