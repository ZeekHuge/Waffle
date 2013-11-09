package org.aa.tool;

public class ChapterMode {

	private int book_id;
	private int chapter_id;
	private int chapter_up_id;
	private int chapter_next_id;

	private String chapter_name;
	
	private String content = null;
	
	
	public int getBook_id() {
		return book_id;
	}
	public void setBook_id(int book_id) {
		this.book_id = book_id;
	}
	public int getChapter_id() {
		return chapter_id;
	}
	public void setChapter_id(int chapter_id) {
		this.chapter_id = chapter_id;
	}
	public int getChapter_up_id() {
		return chapter_up_id;
	}
	public void setChapter_up_id(int chapter_up_id) {
		this.chapter_up_id = chapter_up_id;
	}
	public int getChapter_next_id() {
		return chapter_next_id;
	}
	public void setChapter_next_id(int chapter_next_id) {
		this.chapter_next_id = chapter_next_id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getChapter_name() {
		return chapter_name;
	}
	public void setChapter_name(String chapter_name) {
		this.chapter_name = chapter_name;
	}
	
}
