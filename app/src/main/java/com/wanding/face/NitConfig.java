package com.wanding.face;


/**
 * 服务地址管理类
 */
public class NitConfig {

	/**  打包前必看：
	 * 1，替换正式域名前缀(包括更新版本地址前缀)
	 */
	public static final boolean isFormal = true;//true:正式环境,false:测试环境

	/** 测试服务前缀 */
	public static final String basePath =  				"https://devpay.wandingkeji.cn";
//	public static final String authBasePath =  				"http://192.168.2.17:8081";
	public static final String querySumHistoryPath =  	"https://devdownload.wandin gkeji.cn";//
	public static final String memberBasePath =  		"https://mp.wandingkeji.cn";



	/** 正式服务器 */
	public static final String basePath1 =  					 	 "https://pay.wandingkeji.cn";
	//交易明细查询（历史）                                         https://download.wandingkeji.cn
	public static final String querySumHistoryPath1 =     		 "https://download.wandingkeji.cn";
	//会员系统业务
	public static final String memberBasePath1 =          		 "https://mp.wandingkeji.cn";


	
	
	/**
	 * 正式服务器图片前缀 
	 */
	public static final String imgUrl="";
	
	/**
	 * 本地服务器图片前缀 
	 */
	public static final String imgUrls="";

	/**
	 * 佳博云打印---》》查询打印机状态
	 */
	public static final String queryDeviceListUrl = "http://api.poscom.cn:80/apisc/getStatus";
	/**
	 * 佳博云打印---》》发送数据到云打印设备执行打印
	 */
	public static final String wifiSendMsgUrl = "http://api.poscom.cn:80/apisc/sendMsg";

	public static final String doLoginUrl = basePath +"/pay/api/qmp/face/faceLogin";

	/**
	 * 首页轮播图下载
	 */
	public static final String getBannerImgUrl = basePath + "/admin/adlaunch/getEquipADGroup";
//	public static final String getBannerImgUrl = "http://192.168.2.27:8080/admin/adlaunch/getEquipADGroup";

	/**
     * 校验操作员密码
     */
	public static final String checkPasswdUrl = basePath + "/pay/api/qmp/face/checkPwd";

	/**
     * 修改操作员密码
     */
	public static final String modifyPasswdUrl = basePath + "/pay/api/qmp/face/modifyPwd";

	/**
	 *  获取人脸SDK调用凭证authinfo
	 *  参数：人脸SDK返回的rawdata
	 */
	public static final String getAuthInfoUrl = basePath+"/pay/api/face/authinfo";
	/**
	 * 人脸支付订单支付
	 */
	public static final String brushFaceOrderPayUrl = basePath+"/pay/api/face/placeorder";
	/**
	 * 微信，支付宝（条码），刷卡支付请求
	 */
	public static final String barcodepayUrl = basePath +"/pay/api/qmp/100/1/barcodepay";
	/**
	 * 微信，支付宝（条码），刷卡支付查询请求   mer/queryOrderDetail
	 */
	public static final String queryOrderStatusUrl = basePath +"/pay/payment/query";
	//结算(交接班退出)
	public static final String summaryOrderUrl = basePath +        "/pay/api/qmp/200/1/handOver";
	//结算(交接班数据查询)
	public static final String settlementOrderUrl = basePath +        "/pay/api/qmp/200/1/getWorkOverRecord";
	//结算记录，交接班记录(查询)
	public static final String settlementRecordUrl = basePath +        "/pay/api/qmp/200/1/getWorkOverRecordInterval";
	//结算记录，交接班记录详情(查询)
	public static final String settlementRecordDetailUrl = basePath +        "/pay/api/qmp/200/1/getWorkOverRecordHistory";
	//交易明细：(查当天)入参：当前页数：pageNum 一页数量：numPerPage mid，eid，date_type（"1"=当日交易）
//	public static final String queryOrderDayListUrl = basePath +   "/pay/api/qmp/200/1/queryOrder";
	public static final String queryOrderDayListUrl = basePath +   "/admin/api/qmp/200/1/queryOrder";
	//交易明细：(查当月)入参：当前页数：pageNum 一页数量：numPerPage mid，eid，date_type（"2"=本月交易不含今天）
	public static final String queryOrderMonListUrl = querySumHistoryPath+"/download/api/qmp/200/1/queryOrderByMonth";


	/**
	 * 当天汇总查询
	 * 入参：eid，mid，startTime，endTime
	 */
//	public static final String querySummaryUrl = basePath +   "/pay/api/qmp/200/1/queryOrderSum";
	public static final String querySummaryUrl = basePath +   "/admin/api/qmp/200/1/queryOrderSum";

	/**
	 * 历史汇总查询
	 * 入参：eid，mid，startTime，endTime
	 */
	public static final String queryHistorySummaryUrl = querySumHistoryPath +   "/download/api/qmp/200/1/queryOrderSumHistory";

	/**
	 * 查实时订单详情：
	 *  入参：
	 * orderId（订单号）
	 */
	public static final String getOrderDetailsUrl = basePath+"/pay/api/app/200/1/queryOrderDetail";

	/**
	 * 查历史订单详情
	 * 测试：http://test.weupay.com:8080/download/api/app/200/1/queryOrderDetail
	 * 正式：http://download.weupay.com/download/api/app/200/1/queryOrderDetail
	 */
	public static final String getOrderHistoryDetailsUrl = querySumHistoryPath +"/download/api/app/200/1/queryOrderDetail";


	/**
	 * 退款获取验证码
	 * 入参：orderId，sid， mid
	 */
	public static final String getVerCodeUrl = basePath + "/admin/api/app/200/sendVerCodeT";

	/**
	 * 退款：api/app/200/1/refund
	 *  入参：
	 * orderId（订单号），
	 * amount（退款金额），
	 * verCode(验证码)
	 * desc（备注）
	 */
	public static final String refundRequestUrl = basePath+"/admin/api/app/200/1/refund";



	/**
	 *  刷卡扫码预授权
	 *  http://192.168.2.17:8090
	 */
	public static final String scanAuthUrl = basePath + "/pay/payment/deposit/barcodepay";
//	public static final String scanAuthUrl = "http://192.168.2.17:8081/pay/payment/deposit/barcodepay";

	/**
	 *  刷脸预授权
	 *  http://192.168.2.63:8090
	 */
	public static final String faceAuthUrl = basePath + "/pay/payment/deposit/facepay";

	/**
	 *  预授权完成
	 *  http://192.168.2.63:8090
	 */
	public static final String authConfirmUrl = basePath + "/pay/payment/deposit/consume";

	/**
	 *  预授权撤销
	 *  http://192.168.2.63:8090
	 */
	public static final String authCancelUrl = basePath + "/pay/payment/deposit/reverse";

	/**
	 *  预授权完成撤销
	 *  http://192.168.2.63:8090
	 */
	public static final String authConfirmCancelUrl = basePath + "/pay/payment/deposit/refund";

	/**
	 *  预授权交易记录
	 *  http://192.168.2.63:8090
	 */
	public static final String authRecodeListUrl = basePath + "/pay/payment/deposit/queryOrder";

	/**
	 *  预授权交易记录
	 *  http://192.168.2.63:8090
	 */
	public static final String authOrderDetailtUrl = basePath + "/pay/payment/deposit/query";

	/**
	 *  预授权支付中轮询
	 *  http://192.168.2.63:8090
	 */
	public static final String refreshOrderStateUrl = basePath + "/pay/payment/deposit/query";
//	public static final String refreshOrderStateUrl = "http://192.168.2.17:8081/pay/payment/deposit/query";

	/**
	 * 消费有礼领劵
	 * member-mall/android/getConsumActivityAfterPay
	 */
	public static final String getCardStockUrl = memberBasePath+"/member-mall/android/getConsumActivityAfterPay";

	/**
	 *  会员卡劵核销(查询自己的订单号)
	 */
//	public static final String queryCodeUrl = memberBasePath+"/admin/api/qmp/2000/queryCode";
	public static final String queryCodeUrl = memberBasePath+"/member-console/console/android/queryCodepos";

	/**
	 *  会员卡劵核销(核销卡劵)
	 */
//	public static final String consumeCodeUrl = memberBasePath+"/admin/api/qmp/2000/consumeCode";
	public static final String consumeCodeUrl = memberBasePath+"/member-console/console/android/consumeCodepos";

	/**
	 * 会员消费
	 */
	public static final String micropayUrl = memberBasePath+"/member-mall/member/pos/micropay";


	
}
