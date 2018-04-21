package cn.itcast.lucene;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Lucene {

	@Test
	public void createIndex() throws Exception {
		// 3）创建一个Directory对象，指定索引库保存的位置。可以是内存也可以是磁盘。一般都是磁盘。
		//保存到内存中
		//Directory directory = new RAMDirectory();
		//保存到磁盘
		Directory directory = FSDirectory.open(new File("F:/java/index"));
		// 4）创建一个IndexWriter对象，两个参数，一个Directory，第二个IndexWriterConfig对象（两个参数：Version，分析器对象）
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, conf);
		// 5）读取磁盘上的文件。
		File srcPath = new File("E:/就业班/上课视频/day99-lucene/参考资料/searchsource");
		File[] files = srcPath.listFiles();
		for (File file : files) {
			//文件名
			String fileName = file.getName();
			//文件的路径
			String filePath = file.getPath();
			//文件的内容
			String fileContent = FileUtils.readFileToString(file);
			//文件的大小
			long fileSize = FileUtils.sizeOf(file);
			// 6）对应每个文件创建一个文档对象。
			Document document = new Document();
			// 7）向文档对象中添加域， 把文件的属性保存到域中。
			//参数1：域的名称 参数2：域的内容， 参数3：是否存储
			Field fieldName = new TextField("name", fileName, Store.YES);
			Field fieldPath = new TextField("path", filePath, Store.YES);
			Field fieldContent = new TextField("content", fileContent, Store.YES);
			Field fieldSize = new TextField("size", fileSize + "", Store.YES);
			document.add(fieldName);
			document.add(fieldPath);
			document.add(fieldContent);
			document.add(fieldSize);
			// 8）把文档对象写入索引库
			indexWriter.addDocument(document);
		}
		// 9）关闭IndexWriter对象。
		indexWriter.close();
	}
	
	@Test
	public void searchIndex() throws Exception{
		// 1）创建一个Directory对象，指定索引库的位置。
		Directory directory = FSDirectory.open(new File("F:/java/index"));
		// 2）创建一个IndexReader对象，以读的方式打开索引库
		IndexReader indexReader = DirectoryReader.open(directory);
		// 3）创建一个IndexSearcher对象，需要基于IndexReader创建。
		//参数1：要搜索的域， 参数2：要搜索的关键词
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		// 4）创建一个Query对象，TermQuery，需要指定要搜索的域及要搜索的关键词。
		//参数1：查询对象， 参数2：查询结果返回的最大条数。
		Query query = new TermQuery(new Term("content", "方"));
		// 5）执行查询。
		TopDocs topDocs = indexSearcher.search(query, 10);
		// 6）取查询结果的总记录数。
		System.out.println("查询结果总记录数"+topDocs.totalHits);
		// 7）遍历文档id列表，根据id取文档对象。
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			//取文档id
			int docId = scoreDoc.doc;
			//根据id取文档对象
			Document document = indexSearcher.doc(docId);
			// 8）从文档中取域的内容，并打印
			System.out.println(document.get("name"));
			System.out.println(document.get("path"));
			//System.out.println(document.get("content"));
			System.out.println(document.get("size"));
		}
		// 9）关闭IndexReader对象。
		indexReader.close();
	}
	
	@Test
	public void testTokenStream() throws Exception{
		// 1）创建一个Analyzer对象。创建一个标准分析器对象
		//Analyzer analyzer = new StandardAnalyzer();
		//Analyzer analyzer = new CJKAnalyzer();   //CJK中文分析器
		//Analyzer analyzer = new SmartChineseAnalyzer();   //智能中文分析器
		Analyzer analyzer = new IKAnalyzer();
		// 2）使用Analyzer对象的tokenStream方法，参数要分析的内容。得到一个TokenStream对象
		//参数1：域的名称，目前没有可以是空串或者是null
		//参数2：要分析的文本内容
		TokenStream tokenStream = analyzer.tokenStream(null, "共产党是否对叙动武?特朗普:可能很快也可能根本不快");
		// 3）调用TokenStream对象的reset方法。
		tokenStream.reset();
		// 4）设置一个引用，相当于指针，指向当前的关键词。
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		// 5）遍历TokenStream对象，把关键打印出来
		while(tokenStream.incrementToken()){
			System.out.println(charTermAttribute.toString());
		}
		// 6）关闭TokenStream对象
		tokenStream.close();
	}
}
