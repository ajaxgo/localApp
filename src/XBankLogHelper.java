/**
 * @Copyright:Copyright (c) 2008 - 2020
 * @Company:Giantstone
 */
package com.bosent.businessactivity.gather.log.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.junit.Test;

import com.bosent.base.util.Debug;
import com.bosent.base.util.UtilValidate;
import com.bosent.base.util.ext.BusinessException;
import com.bosent.businessactivity.gather.log.handle.LogServiceHandlerHelper;
import com.bosent.service.DispatchContext;
import com.bosent.service.LocalDispatcher;
import com.bosent.util.BaConstants;
import com.bosent.util.BaConstantsParameter;
import com.bosent.util.ErrorCode;
import com.bosent.util.KindReinSafeClose;

/**
 * 
 * @Title:XBankLogHelper
 * @Description:解析XBANK日志中的字段值
 * @author 杜瑞
 * @Since:2011-1-25
 * @version V1.1.0
 *          =============================================================== 版本号
 *          修改人 日期 修改记录 V1.1.0 杜瑞 2011-1-25 创建程序 V1.1.1 丁浩亮 2011-3-24 添加代码注释
 *          V1.1.2 杜瑞 2011-3-29
 *          修改getServiceInfo方法，增加对LogServiceHandlerHelper的调用处理 V1.1.3 丁浩亮
 *          2011-3-30 设置服务调用状态 V1.1.4 丁浩亮 2011-7-06 添加方法logFileFilter()过滤日志文件
 *          ===============================================================
 */
public class XBankLogHelper {

	/** 模块 */
	private static String module = XBankLogHelper.class.getName();

	/**
	 * 
	 * @param transDate 交易日期
	 * @param organization 机构
	 * @param userCode 柜员号
	 * @return logFileList 日志文件路径
	 * @throws BusinessException 抛出异常
	 * @Description:根据交易日期、机构号、柜员号获取日志文件列表 日志文件存放规则：固定路径+8位交易日期+机构号+柜员号
	 */
	public static List<String> retriveLogFile(String transDate,
			String organization, String userCode) throws BusinessException {
		// 本地数据文件路径数组
		String[] localFilePaths = null;
		List<String> logFileList = FastList.newInstance();

		// 获取日志文件固定部分
		String localFilePath = BaConstantsParameter.getXBankLogPath();

		// 是否为多路径
		if (localFilePath != null && localFilePath.indexOf(",") != -1) {
			localFilePaths = localFilePath.split(",");
		} else {
			localFilePaths = new String[1];
			localFilePaths[0] = localFilePath;
		}

		// 交易日期不为空则拼入路径
		for (int i = 0; i < localFilePaths.length; i++) {
			if (localFilePaths[i] != null
					&& !localFilePaths[i].trim().equals("")) {
				localFilePaths[i] = localFilePaths[i].replace("\\",
						BaConstants.FILE_SEPARATOR);
				if (localFilePaths[i].lastIndexOf(BaConstants.FILE_SEPARATOR) == localFilePaths[i]
						.length() - 1) {
					localFilePaths[i] = localFilePaths[i] + transDate;
					if (Debug.infoOn()) {
						// 记录日志
						Debug.logInfo("XBANK日志固定路径: " + localFilePaths[i],
								module);
					}
				} else {
					localFilePaths[i] = localFilePaths[i]
							+ BaConstants.FILE_SEPARATOR + transDate;
					if (Debug.infoOn()) {
						// 记录日志
						Debug.logInfo("XBANK日志固定路径: " + localFilePaths[i],
								module);
					}
				}
			}
		}

		// 获取数据文件列表
		for (int i = 0; i < localFilePaths.length; i++) {
			// 获取待解析的日志文件路径列表
			File file = new File(localFilePaths[i].toString());
			// 判断是否为目录
			if (!file.isDirectory()) {
				continue;
			}
			getAllLogFilePath(localFilePaths[i].toString(), userCode,
					logFileList);
		}

		return logFileList;
	}

    /**
	 * 
	 * @param filePath 日志文件目录
	 * @param userCode 柜员
	 * @param logFileList 返回的日志文件列表
	 * @Description:读取指定目录下的所有日志文件，如果传入柜员号不为空，则指取得柜员的日志
	 */
	private static void getAllLogFilePath(String filePath, String userCode,
			List<String> logFileList) {

		// 获取日志文件目录对象
		File file = new File(filePath);
		// 获取目录下的所有文件及子目录
		String[] filelist = file.list();

		// 遍历文件列表
		for (int i = 0; i < filelist.length; i++) {
			// 获取文件对象
			File readfile = new File(filePath + BaConstants.FILE_SEPARATOR
					+ filelist[i]);
			// 判断文件是否为目录
			if (!readfile.isDirectory()) {
				// 获取文件的绝对路径
				String absolutePath = readfile.getAbsolutePath();
				// 过滤日志文件
				if (logFileFilter(absolutePath)) {
					continue;
				} else {
					// 如柜员号为空则直接保存
					if (userCode == null) {
						logFileList.add(absolutePath);
					} else if (absolutePath.indexOf(userCode) > -1) {
						// 如柜员号不为空则找指定柜员的日志
						logFileList.add(absolutePath);
					}
				}

			} else if (readfile.isDirectory()) {
				// 如果文件为目录则进入此目录并读取其中的日志文件
				getAllLogFilePath(filePath + BaConstants.FILE_SEPARATOR
						+ filelist[i], userCode, logFileList);
			}
		}

		return;
	}

	/**
	 * 
	 * @param logFile 日志文件路径
	 * @param encoding 文件编码格式
	 * @return List<ServiceLogParameter> 解析后的有效信息集合
	 * @param dscx 实体引擎
	 * @param content 数据值
	 * @throws BusinessException 抛出异常
	 * @throws IOException IO异常
	 * @Description:解析日志文件 1. 从日志文件中解析服务参数
	 *                     2.解析ES服务为ServiceLogParameter对象，以业务流水号为key，放入map中
	 *                     3.解析AS服务为ServiceLogParameter对象
	 *                     ,放入list中，同事根据业务流水号，取得ES服务的入参出参放入AS参数对象中
	 *                     4.为每个AS服务设置其他属性
	 */
	public static List<ServiceLogParameter> saveBusinessData(String logFile,
			String encoding, DispatchContext dscx, Map<String, Object> content,
			String logProcessId) throws BusinessException, IOException {

		// 取得要解析的服务列表
		Map<String, String> needParseServiceNameMap = (Map<String, String>) content
				.get("needParseServiceNameMap");

		// 1. 从日志文件中解析ES服务参数
		Map<String, String> esStringMap = FastMap.newInstance();
		// 1.1 记录ES解析失败的流水号
		Map<String, Object> logErrorMap = FastMap.newInstance();
		// 1.2 从日志文件解析得到ES服务内容
		parseEsParameterString(logFile, encoding, esStringMap, content);

		// 2. 解析ES服务为ServiceLogParameter对象，以业务流水号为key，放入map中
		Map<String, ServiceLogParameter> esServiceLogParameterMap = FastMap
				.newInstance();
		XBankLogESParser xbankLogESParser = new XBankLogESParser();

		Iterator<Entry<String, String>> itemEntity = esStringMap.entrySet()
				.iterator();
		while (itemEntity.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) itemEntity
					.next();
			String businessFlow = (String) entry.getKey();
			String contentStr = (String) entry.getValue();
			ServiceLogParameter serviceLogParameter = xbankLogESParser
					.parseLog(businessFlow, contentStr, encoding, logErrorMap);

			if (serviceLogParameter != null) {
				// 2.1如果需要将ES服务转换为AS服务来进行存储处理，则将AsName参数设置为与EsName相同，并存储数据库
				if (LogServiceHandlerHelper.isEsConvert2As(serviceLogParameter,
						needParseServiceNameMap)) {
					serviceLogParameter.setAsName(serviceLogParameter
							.getEsName());

					// 2.2webservice 查看returnCode中的type字段
					// 当且仅当type不为空且其值为"E"时返回失败状态
					Map webServiceReturnCode = (Map) serviceLogParameter
							.getResponseStructMap().get("ES_RETURNCODE");

					// 设置服务调用状态
					if (null != webServiceReturnCode
							&& null != webServiceReturnCode.get("ES_TYPE")
							&& ((String) webServiceReturnCode.get("ES_TYPE"))
									.equalsIgnoreCase("E")) {
						// MESSAGE=客户签约成功, DOMAIN=527, TYPE=S, CODE=00000111
						serviceLogParameter.isSuccess = false;
					}

					// 解析到一个ES服务，立即保存数据到数据库
					saveServiceParameter(logFile, esServiceLogParameterMap,
							serviceLogParameter, dscx, content);
				} else {
					// 2.3如果不需要转换,以业务流水号为key，放入map中
					esServiceLogParameterMap.put(
							serviceLogParameter.getBusinessFlow(),
							serviceLogParameter);
				}
			}
		}
		saveAsBusinessData(logFile, encoding, esServiceLogParameterMap, dscx,
				content, logProcessId, esStringMap, logErrorMap);

		return null;
	}

	/**
	 * 
	 * @param logFile
	 * @param esServiceLogParameterMap
	 * @param asString
	 * @param dscx
	 * @throws BusinessException
	 * @Description: 保存AS数据到数据库
	 */
	private static void saveServiceParameter(String logFile,
			Map<String, ServiceLogParameter> esServiceLogParameterMap,
			ServiceLogParameter serviceLogParameter, DispatchContext dscx,
			Map<String, Object> content) throws BusinessException {

		// 取得要解析的服务列表
		Map<String, String> needParseServiceNameMap = (Map<String, String>) content
				.get("needParseServiceNameMap");

		// 解析AS服务为ServiceLogParameter对象,放入list中，同时根据业务流水号，取得ES服务的入参出参放入AS参数对象中

		ServiceLogParameter esParameter = null;
		if (UtilValidate.isNotEmpty(serviceLogParameter.getBusinessFlow())) {
			esParameter = esServiceLogParameterMap.get(serviceLogParameter
					.getBusinessFlow());
		}

		if (esParameter != null) {
			// 入参 paramter类型
			serviceLogParameter.getRequestParameterMap().putAll(
					esParameter.getRequestParameterMap());
			// 入参struct类型
			serviceLogParameter.getRequestStructMap().putAll(
					esParameter.getRequestStructMap());
			// 入参table类型
			serviceLogParameter.getRequestTableMap().putAll(
					esParameter.getRequestTableMap());

			// 出参 paramter类型
			serviceLogParameter.getResponseParameterMap().putAll(
					esParameter.getResponseParameterMap());
			// 出参struct类型
			serviceLogParameter.getResponseStructMap().putAll(
					esParameter.getResponseStructMap());
			// 出参table类型
			serviceLogParameter.getResponseTableMap().putAll(
					esParameter.getResponseTableMap());
			// 出参服务处理成功状态类型
			serviceLogParameter.getResponseSuccessList().addAll(
					esParameter.getResponseSuccessList());
			// 出参服务处理失败状态类型
			serviceLogParameter.getResponseErrorList().addAll(
					esParameter.getResponseErrorList());
		}

		// 授权柜员
		String accreditCode = "";
		if (null != serviceLogParameter.getRequestStructMap()) {

			Map<String, String> journalInfoMap = (Map<String, String>) serviceLogParameter
					.getRequestStructMap().get("JOURNAL_INFO");
			if (null != journalInfoMap) {
				accreditCode = journalInfoMap.get("strauthoperaternum");
			}
			if (UtilValidate.isEmpty(accreditCode)) {
				journalInfoMap = (Map<String, String>) serviceLogParameter
						.getRequestStructMap().get(
								XBankLogESParser.ES_PARAMETER_PREFIX
										+ "JOURNAL_INFO");
				if (null != journalInfoMap) {
					accreditCode = journalInfoMap
							.get(XBankLogESParser.ES_PARAMETER_PREFIX
									+ "strauthoperaternum".toUpperCase());
				}
			}
		}

		// 柜员号
		String userCode = getUserCodeFromFilePath(logFile);
		// 机构号
		String branchCode = getBranchCodeFromFilePath(logFile);
		// 区分是否为再次解析标识
		boolean reResolvingFlag = (Boolean) content.get("reResolvingFlag");

		// 先对服务数据进行处理，如果返回值是false，则该条服务不需要存储
		if (!LogServiceHandlerHelper.handle(serviceLogParameter,
				needParseServiceNameMap)) {
			return;
		}

		// 交易柜员号
		serviceLogParameter.setUserCode(userCode);
		// 授权柜员号
		serviceLogParameter.setAccreditCode(accreditCode);
		// 机构号
		serviceLogParameter.setBranchCode(branchCode);
		// 渠道
		serviceLogParameter.setChannel(BaConstants.CHANNELS_XBANK);
		// 角色
		serviceLogParameter.setRoleTypeId(BaConstants.BA_OPERATE);

		// 获取服务调用状态
		setServiceStatus(serviceLogParameter);

		// String tranDate = serviceLogParameter.getTranDate();
		//
		// // 判断交易日期是否为空
		// if (tranDate != null) {
		// tranDate = tranDate.substring(1, tranDate.length() - 1);
		// serviceLogParameter.setTranDate(tranDate);
		// }

		// 判断AsName是否为空
		if (serviceLogParameter.getAsName() != null) {
			if ("ZCS_BP_PER_GET_DETAIL"
					.equals(serviceLogParameter.getService())
					&& !"TS0000000100"
							.equals(serviceLogParameter.getFuncCode())
					&& !"SC00000001".equals(serviceLogParameter
							.getSenarioCode())) {
				return;
			}
			// 第一次解析日志，直接保存
			if (!reResolvingFlag) {
				LogHelper.businessLogProcessWithTransaction(dscx,
						serviceLogParameter, content);
			} else {
				// 获取查询结果状态。当再次解析时，数据库中有此条数据，就不在保存。
				boolean resultStatus = CheckDateHelper
						.queryBaDetailData(serviceLogParameter);
				if (!resultStatus) {
					// 重新解析日志，并且库中无指定“业务流水号”和“渠道流水号”的记录，则直接插入数据库
					LogHelper.businessLogProcessWithTransaction(dscx,
							serviceLogParameter, content);
				}
			}
		}
	}

	/**
	 * 
	 * @param serviceLogParameter 服务参数对象
	 * @Description:获取服务调用成功、失败 1.BAPI 查看BAPIRET2表中的type字段
	 *                          2.webservice查看returnCode中的type字段
	 */
	private static void setServiceStatus(ServiceLogParameter serviceLogParameter) {

		// 1.BAPI 查看BAPIRET2表中的type字段 当且仅当type不为空且其值为“E”时返回失败状态
		List bapiRet2 = (List) serviceLogParameter.getResponseTableMap().get(
				"BAPIRET2");

		// 设置服务调用状态
		if (null != bapiRet2 && bapiRet2.size() > 0) {
			Map returnCode = (Map) bapiRet2.get(0);
			if (null != returnCode && null != returnCode.get("TYPE")
					&& ((String) returnCode.get("TYPE")).equalsIgnoreCase("E")) {
				serviceLogParameter.isSuccess = false;
			}
		}

		// 2.webservice 查看returnCode中的type字段 当且仅当type不为空且其值为“E”时返回失败状态
		Map webServiceReturnCode = (Map) serviceLogParameter
				.getResponseStructMap().get("RETURNCODE");

		// 设置服务调用状态
		if (null != webServiceReturnCode
				&& null != webServiceReturnCode.get("TYPE")
				&& ((String) webServiceReturnCode.get("TYPE"))
						.equalsIgnoreCase("E")) {
			// MESSAGE=客户签约成功, DOMAIN=527, TYPE=S, CODE=00000111
			serviceLogParameter.isSuccess = false;
		}
	}

	/**
	 * 
	 * @param filePath 日志文件路径
	 * @param encoding 文件编码格式
	 * @param asStringList AS服务
	 * @param esStringList ES服务
	 * @throws IOException
	 * @throws BusinessException
	 * @Description: 解析日志中的数据
	 */
	private static void saveAsBusinessData(String filePath, String encoding,
			Map<String, ServiceLogParameter> esServiceLogParameterMap,
			DispatchContext dscx, Map<String, Object> content,
			String logProcessId, Map<String, String> esStringMap,
			Map<String, Object> logErrorMap) throws IOException,
			BusinessException {
		// 取得要解析的服务列表
		Map<String, String> needParseServiceNameMap = (Map<String, String>) content
				.get("needParseServiceNameMap");

		LocalDispatcher dispatcher = dscx.getDispatcher();
		// 解析错误标志
		Boolean isSuccess = true;
		// 解析的内容
		StringBuffer sb = null;
		StringBuffer tmpSb = null;

		// 服务参数Bean对象
		ServiceLogParameter serviceLogParameter = null;

		// 渠道流水号
		String businessFlow = null;
		// 错误文件内容
		StringBuffer esLogContent = new StringBuffer();
		// AS 对象集合
		Map<String, List<String>> asMap = FastMap.newInstance();

		// 获取日志文件对象
		File file = new File(filePath);
		InputStreamReader is = null;
		BufferedReader br = null;
		FileInputStream fis = null;

		boolean isFind = false;

		// AS日志解析器
		XBankLogASParser xbankLogASParser = new XBankLogASParser();

		try {
			// 获取日志文件输入流
			fis = new FileInputStream(file);
			is = new InputStreamReader(fis, encoding);
			br = new BufferedReader(is);
			// 循环读取并解析日志文件中的数据
			while (br.ready()) {
				// 读取日志文件中的一行数据
				String str = br.readLine();
				// 判断读取的数据
				if (str == null) {
					continue;
				}
				if (str.indexOf("BEGIN AS") != -1) {
					if (str.indexOf("Service BEGIN AS ==") != -1) {
						// 服务开始，取得业务流水号
						String serviceBeginPatterString = "== (\\S*) Service BEGIN AS ==";
						Matcher serviceBeginMatcher = Pattern.compile(
								serviceBeginPatterString).matcher(str);
						while (serviceBeginMatcher.find()) {
							businessFlow = serviceBeginMatcher.group(1);
						}
					}
					tmpSb = new StringBuffer();
					tmpSb.append(str);
					tmpSb.append("\r\n");
				} else if ((str.indexOf("ASName") != -1)) {
					String asName = str.split(":")[1];
					if (needParseServiceNameMap.containsKey(asName)) {
						isFind = true;
						tmpSb.append(str);
						tmpSb.append("\r\n");
					}
				} else if (str.indexOf("END AS") != -1) {
					if (isFind) {
						sb = new StringBuffer();
						sb.append(tmpSb.toString());
						sb.append(str);
						sb.append("\r\n");
						// ES服务的错误流水号
						String esErrorBusinessFlow = null;
						if (businessFlow != null) {
							Map<String, String> logErrorStrMap = (Map<String, String>) logErrorMap
									.get(businessFlow);
							if (UtilValidate.isNotEmpty(logErrorStrMap)) {
								Iterator<Entry<String, String>> errorEntity = (Iterator<Entry<String, String>>) logErrorStrMap
										.entrySet().iterator();
								;
								while (errorEntity.hasNext()) {
									Entry<String, String> entryMap = (Entry<String, String>) errorEntity
											.next();
									esErrorBusinessFlow = (String) entryMap
											.getKey();
								}
							}
							if (esErrorBusinessFlow == null) {
								try {
									// 解析到一个AS服务，立即保存数据到数据库
									serviceLogParameter = xbankLogASParser
											.parseLog(sb.toString(), encoding);
									// 判断服务参数对象是否为空
									if (serviceLogParameter != null) {
										// 对解析到得参数进行特殊处理
										LogServiceHandlerHelper
												.handlAsParamter(
														serviceLogParameter,
														sb.toString(),
														needParseServiceNameMap);
										saveServiceParameter(filePath,
												esServiceLogParameterMap,
												serviceLogParameter, dscx,
												content);
									}
								} catch (Throwable e) {
									isSuccess = false;
									List<String> asList = asMap
											.get(businessFlow);
									if (UtilValidate.isEmpty(asList)) {
										asList = FastList.newInstance();
										asList.add(sb.toString()
												+ "\r\n== ERRORMESSAGE BINGE ==\r\n"
												+ CheckDateHelper
														.transExceptionString(e)
												+ "\r\n== ERRORMESSAGE END ==\r\n");
										asMap.put(businessFlow, asList);
									} else {
										asList.add(sb.toString()
												+ "\r\n== ERRORMESSAGE BINGE ==\r\n"
												+ CheckDateHelper
														.transExceptionString(e)
												+ "\r\n== ERRORMESSAGE END ==\r\n");
									}
									// 清空对象
									serviceLogParameter = null;
								}
							} else {
								isSuccess = false;
								List<String> asList = asMap.get(businessFlow);
								if (UtilValidate.isEmpty(asList)) {
									asList = FastList.newInstance();
									asList.add("\r\n" + sb.toString() + "\r\n");
									asMap.put(businessFlow, asList);
								} else {
									asList.add(sb.toString() + "\r\n");
								}
							}
						}
					}
					tmpSb = null;
					isFind = false;
				} else {
					if (tmpSb != null) {
						tmpSb.append(str);
						tmpSb.append("\r\n");
					}
				}
			}
			// 判断解析中是否出现错误日志内容
			if (!isSuccess) {
				// 判断AS服务集合是否为空
				if (UtilValidate.isNotEmpty(asMap)) {
					Iterator<Entry<String, List<String>>> asStrEntity = asMap
							.entrySet().iterator();
					while (asStrEntity.hasNext()) {
						// ES错误内容
						String errorESContent = "";
						// AS 内容
						Entry<String, List<String>> entryMap = (Entry<String, List<String>>) asStrEntity
								.next();
						String busFlow = (String) entryMap.getKey();
						List<String> asEntity = (List<String>) entryMap
								.getValue();
						for (String asStr : asEntity) {
							esLogContent.append("\r\n" + asStr + "\r\n");
						}
						if (esStringMap.get(busFlow) != null
								&& !esStringMap.get(busFlow).equals("")) {
							esLogContent.append(esStringMap.get(busFlow)
									+ "\r\n");
						}
						// ES 异常错误
						Map<String, String> logErrorStrMap = (Map<String, String>) logErrorMap
								.get(busFlow);
						if (UtilValidate.isNotEmpty(logErrorStrMap)) {
							Iterator<Entry<String, String>> errorEntity = (Iterator<Entry<String, String>>) logErrorStrMap
									.entrySet().iterator();
							while (errorEntity.hasNext()) {
								Entry<String, String> errorEntryMap = (Entry<String, String>) errorEntity
										.next();
								errorESContent = (String) errorEntryMap
										.getValue();
							}
						}
						if (errorESContent != null
								&& !errorESContent.equals("")) {
							esLogContent
									.append("\r\n== ERRORMESSAGE BINGE ==\r\n"
											+ errorESContent
											+ "\r\n== ERRORMESSAGE END ==\r\n");
						}
					}
				}
				// 解析标志为失败
				Map<String, Object> cont = FastMap.newInstance();
				// 文件路径
				cont.put("logFilePath", filePath);
				// 文件内容
				cont.put("tmpSb", esLogContent);
				// 编码格式
				cont.put("fileEncoding", BaConstants.FILE_ENCODING_GBK);
				// ES错误信息
				cont.put("exception", new Throwable(new BusinessException(
						ErrorCode.ERROR_02008)));
				// 解析路径
				cont.put("oldPath", BaConstantsParameter.getXBankLogPath());
				// 解析错误路径
				cont.put("newPath", BaConstantsParameter.getErrorXBankLogPath());
				CheckDateHelper.recordErrorLog(dispatcher, cont);
				// 异常处理
				throw new BusinessException(ErrorCode.ERROR_03011);
			}
		} catch (IOException e) {
			if (Debug.errorMinorOn()) {
				StringBuilder buf = new StringBuilder(1024);
				buf.append("日志信息解析、转换存储服务异常，当前正在执行方法XBankLogHelper.saveAsBusinessData,方法入参：");
				buf.append("filePath=");
				buf.append(filePath);
				buf.append(",encoding=");
				buf.append(encoding);
				buf.append(",esServiceLogParameterMap=");
				buf.append(esServiceLogParameterMap.toString());
				buf.append("content=");
				buf.append(content.toString());
				Debug.logErrorMinor(e, buf.toString(), module);
			}
			// 日志文件解析异常
			throw new BusinessException(ErrorCode.ERROR_03011);
		} finally {
			if (null != is) {
				KindReinSafeClose.inputStreamReaderSafeClose(is, module);
			}
			if (null != br) {
				KindReinSafeClose.bufferedReaderSafeClose(br, module);
			}
			if (null != fis) {
				KindReinSafeClose.fileInputStreamSafeClose(fis, module);
			}
		}

		return;
	}

	/**
	 * 
	 * @param logFilePath 文件路径
	 * @param encoding 文件编码格式
	 * @param dscx 引擎
	 * @param content 上下文
	 * @param logProcessId 文件路径标识
	 * @return 返回文件格式保存的行
	 * @throws BusinessException 异常
	 * @Description 检查日志文件格式是否正确
	 */
	public static String checkFileFormat(String filePath, String encoding,
			DispatchContext dscx, Map<String, Object> content,
			String logProcessId) throws IOException, BusinessException {

		// 解析错误标志
		boolean isFindBegin = false;
		// 流水号
		String startBusinessFlow = null;
		String endBusinessFlow = null;
		// 获取日志文件对象
		File file = new File(filePath);
		InputStreamReader is = null;
		FileInputStream fis = null;
		BufferedReader br = null;
		// 记录解析数据行数
		int count = 0;
		try {
			// 获取日志文件输入流
			fis = new FileInputStream(file);
			is = new InputStreamReader(fis, encoding);
			br = new BufferedReader(is);
			// 循环读取并解析日志文件中的数据
			while (br.ready()) {
				count = count + 1;
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
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// AS判断流水是否为空
					if (serviceBeginMatcher.find()) {
						startBusinessFlow = serviceBeginMatcher.group(1);
					} else {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
					// 判断更改标识位
					if (isFindBegin) {
						return "" + count + " 交易流水:" + startBusinessFlow;
					} else {
						isFindBegin = true;
					}
				} else if ((str.indexOf("ASName") != -1)) {
					String[] asName = str.split(":");
					if (asName.length < 2 || UtilValidate.isEmpty(asName[1])) {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
				} else if (str.indexOf("Service END AS ==") != -1) {

					// 服务开始，取得业务流水号
					String serviceBeginPatterString = "== (\\S*) Service END AS ==";
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// AS判断流水是否为空
					if (serviceBeginMatcher.find()) {
						endBusinessFlow = serviceBeginMatcher.group(1);
					} else {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
					// 判断标识位和开始流水和结束流水是否相同
					if (isFindBegin
							&& endBusinessFlow.equals(startBusinessFlow)) {
						isFindBegin = false;
					} else {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
				} else if (str.indexOf("Service BEGIN ES ==") != -1) {
					// 服务开始，取得业务流水号
					String serviceBeginPatterString = "== (\\S*) Service BEGIN ES ==";
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// ES判断流水是否为空
					if (serviceBeginMatcher.find()) {
						startBusinessFlow = serviceBeginMatcher.group(1);
					} else {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
					// 判断标识位
					if (isFindBegin) {
						return "" + count + " 交易流水:" + startBusinessFlow;
					} else {
						isFindBegin = true;
					}
				} else if ((str.indexOf("ESName") != -1)) {
					String[] esName = str.split(":");
					if (esName.length < 2 || UtilValidate.isEmpty(esName[1])) {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
				} else if (str.indexOf("Service END ES ==") != -1) {

					// 服务开始，取得业务流水号
					String serviceBeginPatterString = "== (\\S*) Service END ES ==";
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					if (serviceBeginMatcher.find()) {
						endBusinessFlow = serviceBeginMatcher.group(1);
					} else {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
					// 判断标识位和开始流水和结束流水是否相同
					if (isFindBegin
							&& endBusinessFlow.equals(startBusinessFlow)) {
						isFindBegin = false;
					} else {
						return "" + count + " 交易流水:" + startBusinessFlow;
					}
				}
			}

		} catch (IOException e) {
			if (Debug.errorMinorOn()) {
				StringBuilder buf = new StringBuilder(1024);
				Debug.logErrorMinor(e, buf.toString(), module);
			}
			// 日志文件解析异常
			throw new BusinessException(ErrorCode.ERROR_03011);
		} finally {
			if (null != is) {
				KindReinSafeClose.inputStreamReaderSafeClose(is, module);
			}
			if (null != br) {
				KindReinSafeClose.bufferedReaderSafeClose(br, module);
			}
			if (null != fis) {
				KindReinSafeClose.fileInputStreamSafeClose(fis, module);
			}
		}

		return null;
	}
	
	/**
	 * 
	 * @param logFilePath 文件路径
	 * @param encoding 文件编码格式
	 * @param dscx 引擎
	 * @param content 上下文
	 * @param logProcessId 文件路径标识
	 * @return 返回文件格式保存的行
	 * @throws BusinessException 异常
	 * @Description 检查日志文件格式是否正确
	 */
	public String locateError(String filePath, String encoding,
			Map<String, Object> content)
			throws IOException, BusinessException {

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
		int serviceStartLineNumber =0;
		int serviceEndLineNumber= 0;
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
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// AS判断流水是否为空
					if (serviceBeginMatcher.find()&&logFlagStack.isEmpty()) {
						serviceStartLineNumber = br.getLineNumber();
						logFlagStack.push(ServiceFlag.ASBegin);
					} else {
						logFlagStack.pop();
						int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
						errorStart = serviceStartLineNumber;
						errorEnd = nextServiceNumber-1;
						resultStr.append(errorStart+":"+errorEnd+",");
					}
				} else if ((str.indexOf("ASName") != -1)) {
					if(logFlagStack.peek()==ServiceFlag.ASBegin){
						errorStart = br.getLineNumber();
						int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
						errorEnd = nextServiceNumber-1;
						resultStr.append(errorStart+":"+errorEnd+",");
					}else{
						String[] asName = str.split(":");
						if (asName.length < 2 || UtilValidate.isEmpty(asName[1])) {
							errorStart = serviceStartLineNumber;
							int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
							errorEnd = nextServiceNumber-1;
							resultStr.append(errorStart+":"+errorEnd+",");
						}
					}
					
				} else if (str.indexOf("Service END AS ==") != -1) {

					// 服务开始，取得业务流水号
					String serviceBeginPatterString = "== (\\S*) Service END AS ==";
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// AS判断流水是否为空 
					if (serviceBeginMatcher.find()) {
						if(logFlagStack.peek()==ServiceFlag.ASBegin){
							logFlagStack.pop();
						}
						else if(logFlagStack.peek()!=ServiceFlag.ASBegin){
							errorStart = serviceStartLineNumber;
							errorEnd = br.getLineNumber();
							resultStr.append(errorStart+":"+errorEnd+",");
						}else if(logFlagStack.isEmpty()){
							int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
							errorStart = serviceStartLineNumber;
							errorEnd = nextServiceNumber-1;
							resultStr.append(errorStart+":"+errorEnd+",");
						}
					} else {
						int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
						errorStart = serviceStartLineNumber;
						errorEnd = nextServiceNumber-1;
						resultStr.append(errorStart+":"+errorEnd+",");
					}
				} else if (str.indexOf("Service BEGIN ES ==") != -1) {
					// 服务开始，取得业务流水号
					String serviceBeginPatterString = "== (\\S*) Service BEGIN ES ==";
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// AS判断流水是否为空
					if (serviceBeginMatcher.find()&&logFlagStack.isEmpty()) {
						serviceStartLineNumber = br.getLineNumber();
						logFlagStack.push(ServiceFlag.ASBegin);
					} else {
						logFlagStack.pop();
						int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
						errorStart = serviceStartLineNumber;
						errorEnd = nextServiceNumber-1;
						resultStr.append(errorStart+":"+errorEnd+",");
					}
				} else if ((str.indexOf("ESName") != -1)) {
					if(logFlagStack.peek()==ServiceFlag.ESBegin){
						errorStart = br.getLineNumber();
						int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
						errorEnd = nextServiceNumber-1;
						resultStr.append(errorStart+":"+errorEnd+",");
					}else{
						String[] asName = str.split(":");
						if (asName.length < 2 || UtilValidate.isEmpty(asName[1])) {
							errorStart = serviceStartLineNumber;
							int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
							errorEnd = nextServiceNumber-1;
							resultStr.append(errorStart+":"+errorEnd+",");
						}
					}
				} else if (str.indexOf("Service END ES ==") != -1) {


					// 服务开始，取得业务流水号
					String serviceBeginPatterString = "== (\\S*) Service END ES ==";
					Matcher serviceBeginMatcher = Pattern.compile(
							serviceBeginPatterString).matcher(str);
					// AS判断流水是否为空 
					if (serviceBeginMatcher.find()) {
						if(logFlagStack.peek()==ServiceFlag.ESBegin){
							logFlagStack.pop();
						}
						else if(logFlagStack.peek()!=ServiceFlag.ESBegin){
							logFlagStack.pop();
							errorStart = serviceStartLineNumber;
							errorEnd = br.getLineNumber();
							resultStr.append(errorStart+":"+errorEnd+",");
						}else if(logFlagStack.isEmpty()){
							int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
							errorStart = serviceStartLineNumber;
							errorEnd = nextServiceNumber-1;
							resultStr.append(errorStart+":"+errorEnd+",");
						}
					} else {
						int nextServiceNumber = skipToNextServiceBegin(br,logFlagStack);
						errorStart = serviceStartLineNumber;
						errorEnd = nextServiceNumber-1;
						resultStr.append(errorStart+":"+errorEnd+",");
					}
				}
			}
		} catch (IOException e) {
			if (Debug.errorMinorOn()) {
				StringBuilder buf = new StringBuilder(1024);
				Debug.logErrorMinor(e, buf.toString(), module);
			}
			// 日志文件解析异常
			throw new BusinessException(ErrorCode.ERROR_03011);
		} finally {
			if (null != is) {
				KindReinSafeClose.inputStreamReaderSafeClose(is, module);
			}
			if (null != br) {
				KindReinSafeClose.bufferedReaderSafeClose(br, module);
			}
			if (null != fis) {
				KindReinSafeClose.fileInputStreamSafeClose(fis, module);
			}
		}

		return resultStr.toString();
	}

	/**
	 * 
	 * @param filePath 日志文件路径
	 * @param encoding 文件编码格式
	 * @param asStringList AS服务
	 * @param esStringList ES服务
	 * @throws IOException
	 * @throws BusinessException
	 * @Description: 解析日志中的数据
	 */
	private static void parseEsParameterString(String filePath,
			String encoding, Map<String, String> esStringMap,
			Map<String, Object> content) throws IOException, BusinessException {

		// 取得要解析的服务列表
		Map<String, String> needParseServiceNameMap = (Map<String, String>) content
				.get("needParseServiceNameMap");
		// 日志内容
		StringBuffer sb = null;
		StringBuffer tmpSb = null;
		// 业务流水号
		String businessFlow = null;

		// 获取日志文件对象
		File file = new File(filePath);
		InputStreamReader is = null;
		FileInputStream fis = null;

		BufferedReader br = null;
		boolean isFind = false;
		boolean isES = false;

		try {
			// 获取日志文件输入流
			fis = new FileInputStream(file);
			is = new InputStreamReader(fis, encoding);
			br = new BufferedReader(is);

			// 循环读取并解析日志文件中的数据
			while (br.ready()) {
				// 读取日志文件中的一行数据
				String str = br.readLine();

				// 判断读取的数据
				if (str == null) {
					continue;
				}
				if (str.indexOf("BEGIN ES") != -1) {
					isES = true;
					if (str.indexOf("Service BEGIN ES ==") != -1) {
						// 获得业务流水号
						String serviceBeginPatterString = "== (\\S*) Service BEGIN ES ==";
						Matcher serviceBeginMatcher = Pattern.compile(
								serviceBeginPatterString).matcher(str);
						while (serviceBeginMatcher.find()) {
							businessFlow = serviceBeginMatcher.group(1);
						}
					}
					tmpSb = new StringBuffer();
					tmpSb.append(str);
					tmpSb.append("\r\n");
				} else if (isES && str.indexOf("ESName") != -1) {
					String esName = str.split(":")[1];
					if (needParseServiceNameMap.containsKey(esName)) {
						isFind = true;
					}
					tmpSb.append(str);
					tmpSb.append("\r\n");
				} else if (str.indexOf("Service END ES ==") != -1) {

					sb = new StringBuffer();
					sb.append(tmpSb.toString());
					sb.append(str);
					String esString = sb.toString();
					// 判断是否存在授权柜员
					boolean isContainAuthorOperator = false;
					if (esString.indexOf("strauthoperaternum") != -1
							&& esString.indexOf("JOURNAL_INFO") != -1) {
						isContainAuthorOperator = true;
					}
					// 如果存在授权柜员或者需要解析该es服务
					if (isFind || isContainAuthorOperator) {
						if (businessFlow != null) {
							esStringMap.put(businessFlow, esString);
						}
					}
					tmpSb = null;
					isFind = false;
					isES = false;
				} else {
					if (tmpSb != null) {
						tmpSb.append(str);
						tmpSb.append("\r\n");
					}
				}
			}
		} catch (IOException e) {
			if (Debug.errorMinorOn()) {
				StringBuilder buf = new StringBuilder(1024);
				buf.append("日志信息解析、转换存储服务异常，");
				buf.append("当前正在执行方法XBankLogHelper.parseEsParameterString,方法入参：");
				buf.append("filePath=");
				buf.append(filePath);
				buf.append(",encoding=");
				buf.append(encoding);
				buf.append(",esStringList=");
				buf.append(esStringMap.toString());
				Debug.logErrorMinor(e, buf.toString(), module);
			}
			// 日志文件解析异常
			throw new BusinessException(ErrorCode.ERROR_03011);
		} finally {
			if (null != is) {
				KindReinSafeClose.inputStreamReaderSafeClose(is, module);
			}
			if (null != br) {
				KindReinSafeClose.bufferedReaderSafeClose(br, module);
			}
			if (null != fis) {
				KindReinSafeClose.fileInputStreamSafeClose(fis, module);
			}
		}

		return;
	}

	/**
	 * 
	 * @param logFile 日志文件路径
	 * @return String 柜员号
	 * @Description: 从日志路径中获取柜员号
	 */
	private static String getUserCodeFromFilePath(String logFile) {

		// 如果日志文件为空则直接返回
		if (logFile == null) {
			return "";
		}

		// Xbank日志文件名即为柜员号
		String userCode = logFile.substring(
				logFile.lastIndexOf(BaConstants.FILE_SEPARATOR) + 1,
				logFile.lastIndexOf(".log"));

		return userCode;
	}

	/**
	 * 
	 * @param logFile 日志文件路径
	 * @return String 机构号
	 * @Description: 从日志路径中获取机构号
	 */
	private static String getBranchCodeFromFilePath(String logFile) {

		// 如果日志文件为空则直接返回
		if (logFile == null) {
			return "";
		}

		// Xbank日志文件名上一层目录名为机构号
		String branchCode = logFile.substring(0,
				logFile.lastIndexOf(BaConstants.FILE_SEPARATOR));
		branchCode = branchCode.substring(branchCode
				.lastIndexOf(BaConstants.FILE_SEPARATOR) + 1);

		return branchCode;
	}

	/**
	 * 1.过滤掉不以".log"结尾的文件 2.过滤掉大于1G的日志 3.过滤掉非10位员工号的日志 4.过滤掉8开头的日志
	 * 
	 * @param logPath 日志文件路径
	 * @return true：过滤掉此日志文件，false:不过滤掉此日志文件
	 */
	private static boolean logFileFilter(String logPath) {

		// 判断日志路径是否为空
		if (null == logPath || logPath.trim().equals("")) {
			return true;
		}

		// 获取日志文件名
		String fileName = null;
		if (logPath.indexOf(BaConstants.FILE_SEPARATOR) != -1) {
			fileName = logPath.substring(logPath.lastIndexOf(System
					.getProperty("file.separator")) + 1);
		}
		// 获取日志文件对象
		FileInputStream file = null;
		try {
			// 获取日志文件对象
			file = new FileInputStream(logPath);
			Long logFileLength = BaConstants.LOG_FILE_LENGTH;
			// 获取日志文件大小,单位为:byte
			long length = file.available();

			// 判断日志文件名是否为空
			if (null == fileName || fileName.trim().equals("")) {
				return true;
			}

			// 去掉日志文件扩展名
			if (fileName.indexOf(".") != -1) {
				fileName = fileName.substring(0, fileName.indexOf("."));
			}

			// 1.过滤掉不以".log"结尾的文件 2.过滤掉大于1G的日志
			if (!logPath.endsWith(".log") || length > logFileLength) {
				return true;
			}

			// 过滤柜员不等于10或者不等于8日志文件
			if (!(fileName.length() == 10 || fileName.length() == 8)) {
				return true;
			}

		} catch (IOException e) {
			if (Debug.errorMinorOn()) {
				StringBuilder buf = new StringBuilder(1024);
				buf.append("日志信息解析、转换存储服务异常，日志过滤表查询失败；");
				buf.append("当前正在执行方法XBankLogHelper.logFileFilter,方法入参：");
				buf.append("logPath=");
				buf.append(logPath);
				Debug.logErrorMinor(e, buf.toString(), module);
			}
		} finally {
			try {
				if (null != file) {
					file.close();
				}
			} catch (IOException e) {
				if (Debug.errorMinorOn()) {
					StringBuilder buf = new StringBuilder(1024);
					buf.append("日志信息解析、转换存储服务异常，'BaDetail'表查询file.close()失败；");
					buf.append("当前正在执行方法XBankLogHelper.logFileFilter,方法入参：");
					buf.append("logPath=");
					buf.append(logPath);
					Debug.logErrorMinor(e, buf.toString(), module);
				}
			}
		}
		return false;
	}
	
	public int skipToNextServiceBegin(LineNumberReader reader,Stack<ServiceFlag> stack) throws IOException{
		while(reader.ready()){
			String currentLine = reader.readLine();
			if(currentLine.matches("== (\\S*) Service BEGIN (\\S*) ==")){
				String serviceBeginPatterString = "== (\\S*) Service BEGIN (\\S*) ==";
				Matcher serviceBeginMatcher = Pattern.compile(
						serviceBeginPatterString).matcher(serviceBeginPatterString);
				// AS判断流水是否为空
				if (serviceBeginMatcher.find()) {
					if("AS".equals(serviceBeginMatcher.group(1).trim())){
						stack.push(ServiceFlag.ASBegin);
					}else if("ES".equals(serviceBeginMatcher.group(1).trim())){
						stack.push(ServiceFlag.ESBegin);
					}
					return reader.getLineNumber();
				}else{
					skipToNextServiceBegin(reader,stack);
				}
			}
		}
		return -1;
	}
	
	enum ServiceFlag{
		ASBegin,ESBegin
	}
	
	@Test
	public void testErrorLocate() throws IOException, BusinessException{
		String filepath= "D:/temp/fa_group_fetch/XBANK/20121001/0001/0000012345.log";
		System.out.println(locateError(filepath, "UTF-8", null));
	}

}