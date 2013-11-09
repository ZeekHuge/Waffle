package com.Andware.testjson;

public class URLcontent {

	// 服务器路径
	public static String bookPath = "http://120.197.95.200/";

	// 请求方式
	public static String wapBook = "wapbook?";

	public static String manBookList = bookPath + wapBook
			+ "rpt=classlist&typeid=43";

	public static String womanBookList = bookPath + wapBook
			+ "rpt=classlist&typeid=42";

	public static String hostBookList = bookPath + wapBook
			+ "rpt=classlist&typeid=23";

	public static String getOneBook = bookPath + wapBook
			+ "rpt=book&tagid=57&oid=";

	public static String getChapterList = bookPath + wapBook + "rpt=mart&oid=";

}
