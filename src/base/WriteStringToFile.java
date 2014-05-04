package base;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * @ClassName WriteStringToFile.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2013-9-17 下午10:59:15
 * @Copyright
 */

public class WriteStringToFile {

    public void write () throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(new File("")));
        FileWriter writer = new FileWriter(new File(""));
        while (reader.ready()) {
            String str = reader.readLine();
            reader.getLineNumber();
        }
    }
}
