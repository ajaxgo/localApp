package parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * @ClassName ParseLog.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2013-9-20 下午9:42:06
 * @Copyright 
 */

public class ParseLog {

    /**
     * 
     * @param logFilePath
     *            文件路径
     * @param encoding
     *            文件编码格式
     * @param dscx
     *            引擎
     * @param content
     *            上下文
     * @param logProcessId
     *            文件路径标识
     * @return 返回文件格式保存的行
     * @throws BusinessException
     *             异常
     * @Description 检查日志文件格式是否正确
     */
    public String locateError (String filePath, String encoding, Map<String, Object> content) throws IOException {

        // 解析错误标志
        boolean isFindBegin = false;
        // 流水号
        String startBusinessFlow = null;
        String endBusinessFlow = null;
        // 获取日志文件对象
        File file = new File(filePath);
        InputStreamReader is = null;
        FileInputStream fis = null;
        LineNumberReader br = null;
        StringBuilder resultStr = new StringBuilder();
        int serviceStartLineNumber = 0;
        int serviceEndLineNumber = 0;
        int errorStart = 0;
        int errorEnd = 0;
        Stack<ServiceFlag> logFlagStack = new Stack<ServiceFlag>();
        try {
            // 获取日志文件输入流
            fis = new FileInputStream(file);
            is = new InputStreamReader(fis, encoding);
            br = new LineNumberReader(is);
            // 循环读取并解析日志文件中的数据
            while (br.ready()) {
                // 读取日志文件中的一行数据
                String str = br.readLine();
                // 判断读取的数据
                if (str == null) {
                    continue;
                }
                // AS服务格式的检查
                if (str.indexOf("Service BEGIN AS ==") != -1) {
                    // 服务开始，取得业务流水号
                    String serviceBeginPatterString = "== (\\S*) Service BEGIN AS ==";
                    Matcher serviceBeginMatcher = Pattern.compile(serviceBeginPatterString).matcher(str);
                    // AS判断流水是否为空
                    if (serviceBeginMatcher.find() && logFlagStack.isEmpty()) {
                        serviceStartLineNumber = br.getLineNumber();
                        logFlagStack.push(ServiceFlag.ASBegin);
                    } else {
                        logFlagStack.pop();
                        int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                        errorStart = serviceStartLineNumber;
                        errorEnd = nextServiceNumber - 1;
                        resultStr.append(errorStart + ":" + errorEnd + ",");
                    }
                } else if ( (str.indexOf("ASName") != -1)) {
                    if (logFlagStack.peek() == ServiceFlag.ASBegin) {
                        errorStart = br.getLineNumber();
                        int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                        errorEnd = nextServiceNumber - 1;
                        resultStr.append(errorStart + ":" + errorEnd + ",");
                    } else {
                        String[] asName = str.split(":");
                        if (asName.length < 2 || StringUtils.isEmpty(asName[1])) {
                            errorStart = serviceStartLineNumber;
                            int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                            errorEnd = nextServiceNumber - 1;
                            resultStr.append(errorStart + ":" + errorEnd + ",");
                        }
                    }

                } else if (str.indexOf("Service END AS ==") != -1) {

                    // 服务开始，取得业务流水号
                    String serviceBeginPatterString = "== (\\S*) Service END AS ==";
                    Matcher serviceBeginMatcher = Pattern.compile(serviceBeginPatterString).matcher(str);
                    // AS判断流水是否为空
                    if (serviceBeginMatcher.find()) {
                        if (logFlagStack.peek() == ServiceFlag.ASBegin) {
                            logFlagStack.pop();
                        } else if (logFlagStack.peek() != ServiceFlag.ASBegin) {
                            errorStart = serviceStartLineNumber;
                            errorEnd = br.getLineNumber();
                            resultStr.append(errorStart + ":" + errorEnd + ",");
                        } else if (logFlagStack.isEmpty()) {
                            int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                            errorStart = serviceStartLineNumber;
                            errorEnd = nextServiceNumber - 1;
                            resultStr.append(errorStart + ":" + errorEnd + ",");
                        }
                    } else {
                        int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                        errorStart = serviceStartLineNumber;
                        errorEnd = nextServiceNumber - 1;
                        resultStr.append(errorStart + ":" + errorEnd + ",");
                    }
                } else if (str.indexOf("Service BEGIN ES ==") != -1) {
                    // 服务开始，取得业务流水号
                    String serviceBeginPatterString = "== (\\S*) Service BEGIN ES ==";
                    Matcher serviceBeginMatcher = Pattern.compile(serviceBeginPatterString).matcher(str);
                    // AS判断流水是否为空
                    if (serviceBeginMatcher.find() && logFlagStack.isEmpty()) {
                        serviceStartLineNumber = br.getLineNumber();
                        logFlagStack.push(ServiceFlag.ASBegin);
                    } else {
                        logFlagStack.pop();
                        int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                        errorStart = serviceStartLineNumber;
                        errorEnd = nextServiceNumber - 1;
                        resultStr.append(errorStart + ":" + errorEnd + ",");
                    }
                } else if ( (str.indexOf("ESName") != -1)) {
                    if (logFlagStack.peek() == ServiceFlag.ESBegin) {
                        errorStart = br.getLineNumber();
                        int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                        errorEnd = nextServiceNumber - 1;
                        resultStr.append(errorStart + ":" + errorEnd + ",");
                    } else {
                        String[] asName = str.split(":");
                        if (asName.length < 2 || StringUtils.isEmpty(asName[1])) {
                            errorStart = serviceStartLineNumber;
                            int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                            errorEnd = nextServiceNumber - 1;
                            resultStr.append(errorStart + ":" + errorEnd + ",");
                        }
                    }
                } else if (str.indexOf("Service END ES ==") != -1) {

                    // 服务开始，取得业务流水号
                    String serviceBeginPatterString = "== (\\S*) Service END ES ==";
                    Matcher serviceBeginMatcher = Pattern.compile(serviceBeginPatterString).matcher(str);
                    // AS判断流水是否为空
                    if (serviceBeginMatcher.find()) {
                        if (logFlagStack.peek() == ServiceFlag.ESBegin) {
                            logFlagStack.pop();
                        } else if (logFlagStack.peek() != ServiceFlag.ESBegin) {
                            logFlagStack.pop();
                            errorStart = serviceStartLineNumber;
                            errorEnd = br.getLineNumber();
                            resultStr.append(errorStart + ":" + errorEnd + ",");
                        } else if (logFlagStack.isEmpty()) {
                            int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                            errorStart = serviceStartLineNumber;
                            errorEnd = nextServiceNumber - 1;
                            resultStr.append(errorStart + ":" + errorEnd + ",");
                        }
                    } else {
                        int nextServiceNumber = skipToNextServiceBegin(br, logFlagStack);
                        errorStart = serviceStartLineNumber;
                        errorEnd = nextServiceNumber - 1;
                        resultStr.append(errorStart + ":" + errorEnd + ",");
                    }
                }
            }
        } catch (IOException e) {
            // if (Debug.errorMinorOn()) {
            // StringBuilder buf = new StringBuilder(1024);
            // Debug.logErrorMinor(e, buf.toString(), module);
            // }
            // 日志文件解析异常
            // throw new BusinessException(ErrorCode.ERROR_03011);
        } finally {
            // if (null != is) {
            // KindReinSafeClose.inputStreamReaderSafeClose(is, module);
            // }
            // if (null != br) {
            // KindReinSafeClose.bufferedReaderSafeClose(br, module);
            // }
            // if (null != fis) {
            // KindReinSafeClose.fileInputStreamSafeClose(fis, module);
            // }
        }

        return resultStr.toString();
    }

    enum ServiceFlag {
        ASBegin, ESBegin
    }

    public int skipToNextServiceBegin (LineNumberReader reader, Stack<ServiceFlag> stack) throws IOException {
        while (reader.ready()) {
            String currentLine = reader.readLine();
            if (currentLine.matches("== (\\S*) Service BEGIN (\\S*) ==")) {
                String serviceBeginPatterString = "== (\\S*) Service BEGIN (\\S*) ==";
                Matcher serviceBeginMatcher = Pattern.compile(serviceBeginPatterString).matcher(
                        serviceBeginPatterString);
                // AS判断流水是否为空
                if (serviceBeginMatcher.find()) {
                    if ("AS".equals(serviceBeginMatcher.group(1).trim())) {
                        stack.push(ServiceFlag.ASBegin);
                    } else if ("ES".equals(serviceBeginMatcher.group(1).trim())) {
                        stack.push(ServiceFlag.ESBegin);
                    }
                    return reader.getLineNumber();
                } else {
                    skipToNextServiceBegin(reader, stack);
                }
            }
        }
        return -1;
    }

    @Test
    public void testErrorLocate () throws IOException {
        String filepath = "D:/temp/fa_group_fetch/XBANK/20121001/0001/0000012345.log";
        System.out.println(locateError(filepath, "UTF-8", null));
    }
}
