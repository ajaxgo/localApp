import java.io.File;
import java.io.UnsupportedEncodingException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @ClassName XMLParse.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2011-12-15 下午10:25:25
 * @Copyright
 */

public class XMLParse {

    /**
     * @param args
     * @throws DocumentException
     */
    public static void main (String[] args) throws Exception {
        XMLParse obj = new XMLParse();
        // obj.readXml();
        obj.readUnicode();
    }

    /**
     * 解析xml将iso字符转为utf-8字符
     * 
     * @throws DocumentException
     * @throws UnsupportedEncodingException
     */
    public void readXml () throws DocumentException, UnsupportedEncodingException {
        File file = new File("C:\\Users\\TIAN\\Documents\\commit827.xml");
        SAXReader xr = new SAXReader();
        Document document = xr.read(file);
        Element root = document.getRootElement();
        String full = root.asXML();
        String iso = new String(full.getBytes("utf-8"), "ISO-8859-1");
    }

    /**
     * 这个测试将unicode转为utf-8字符,从jsp页面读出的中文，应该先在页面用encodeURI进行编码，然后到action中使用Decode进行解码
     * 注意在js中需要进行两次编码解决%作为特殊符号的问题，在jsp中只需要一次编码
     * 
     * @throws UnsupportedEncodingException
     */
    public void readUnicode () throws UnsupportedEncodingException {
        String unicode = "%E8%B4%A8%E6%8E%A7%E5%89%8D%E5%8E%9F%E5%A7%8B%E6%A0%BC%E5%BC%8F%E5%8D%95%E7%AB%99%E5%A4%9A%E6%99%AE%E5%8B%92%E9%9B%B7%E8%BE%BE%E5%9F%BA%E6%95%B0%E6%8D%AE-%E5%8C%85%E6%96%87%E4%BB%B6";
        String utf8 = new String(unicode.getBytes("unicode"), "ISO-8859-1");
        System.out.println(utf8);
    }
}
