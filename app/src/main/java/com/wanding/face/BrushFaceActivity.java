package com.wanding.face;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sym.libs.usbprint.USBPrinter;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.tencent.wxpayface.WxfacePayCommonCode;
import com.wanding.face.activity.PayStateActivity;
import com.wanding.face.baidu.tts.MySyntherizer;
import com.wanding.face.bean.FacePayAuthInfoRes;
import com.wanding.face.bean.FacePayUnifiedOrderRes;
import com.wanding.face.bean.PosMemConsumeReqData;
import com.wanding.face.bean.PosPayQueryReqData;
import com.wanding.face.bean.PosPayQueryResData;
import com.wanding.face.bean.UserBean;
import com.wanding.face.httputil.HttpURLConnectionUtil;
import com.wanding.face.httputil.NetworkUtils;
import com.wanding.face.jiabo.device.bean.PosScanpayReqData;
import com.wanding.face.jiabo.device.bean.PosScanpayResData;
import com.wanding.face.payutil.PayParamsReqUtil;
import com.wanding.face.payutil.PayUtils;
import com.wanding.face.print.USBPrintTextUtil;
import com.wanding.face.usb.keyboar.USBKeyboard;
import com.wanding.face.utils.DateTimeUtil;
import com.wanding.face.utils.DecimalUtil;
import com.wanding.face.utils.FastJsonUtil;
import com.wanding.face.utils.GsonUtils;
import com.wanding.face.utils.SharedPreferencesUtil;
import com.wanding.face.utils.ToastUtil;
import com.wanding.face.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 百度语音API语音识别
 * https://cloud.baidu.com/doc/SPEECH/TTS-API.html#.E6.B5.8F.E8.A7.88.E5.99.A8.E8.B7.A8.E5.9F.9F
 */

/**
 * 刷脸支付主界面
 */
@ContentView(R.layout.activity_brush_face)
public class BrushFaceActivity extends BaseActivity implements View.OnClickListener ,USBKeyboard.OnKeyboardValueListener {

    @ViewInject(R.id.scanner_hint_imgBack)
    ImageView imgBack;
    @ViewInject(R.id.scanner_hint_imgHint)
    ImageView imgHint;

    @ViewInject(R.id.brush_face_tvSumMoney)
    TextView tvSumMoney;




    @ViewInject(R.id.brush_face_btBrushFace)
    Button btBrushFace;
    MySyntherizer synthesizer = MainActivity.synthesizer;
    USBKeyboard usbKeyboard = MainActivity.usbKeyboard;
    private boolean isKeyboardValidity = true;

    /**
     * 登录用户
     */
    private UserBean userBean;
    String totalFee = null;
    /**
     * payMode : 支付类型，刷脸支付 = "0"，扫码支付 = "1"，默认 = "0"
     */
    String payMode = "0";

    /**
     * payType : 支付方式
     */
    String payType = "";

    /**
     * 最终的扫码值
     */
    StringBuffer sb = new StringBuffer();
    /**
     * 扫码结果keyCode的值临时保存到集合
     */
    ArrayList<Integer> scannedCodes = new ArrayList<Integer>();

    /**
     * 支付成功返回实体
     */
    PosScanpayResData posScanpayResData;


    /**
     * 业务类型
     */
    private String busType = Constants.BUS_TYPE_PAY;
    /**
     * 支付状态
     */
    String payState = Constants.PAY_STATE_UNKNOWN;

    /**
     *  轮询查询订单状态标示
     */
    private int queryIndex = 1;

    private PosPayQueryReqData posPayQueryReqData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        userBean = (UserBean) intent.getSerializableExtra("userBean");
        totalFee = intent.getStringExtra("totalFee");
        tvSumMoney.setText("￥"+totalFee);
        initListener();
        //默认开启扫码支付
        WxPayFace.getInstance().startCodeScanner(new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                if (info == null) {
                    showToast("扫码失败,扫码返回为空！");
                    finish();
                    return;
                }
                String code = (String)info.get(Constants.RETURN_CODE);
                String msg = (String)info.get(Constants.RETURN_MSG);
                Log.e(TAG, "response | getWxpayfaceRawdata " + code + " | " + msg);
                if (code == null || !code.equals(WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)) {
                    showToast("扫码失败！");
                    finish();
                    return;
                }
                //{return_code=SUCCESS, code_msg=134665614656016576, return_msg=scan code success}
                Log.e("获取扫码返回:",info.toString());
                String code_msg = info.get("code_msg").toString();
                if(Utils.isNotEmpty(code_msg)){
//                    speak("扫码成功！");

                    int what = NetworkUtils.MSG_WHAT_ONEHUNDRED;
                    String text = code_msg;
                    sendMessage(what,text);

                }else{
                    ToastUtil.showText(activity,"扫码返回结果为空！",1);
                }


            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {



            }
        });

        speak("支付"+totalFee+"元，请出示付款码或点击刷脸支付！");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"onStart....");
        AnimationSet animationSet = (AnimationSet)AnimationUtils.loadAnimation(this, R.anim.up_anim);
        imgHint.startAnimation(animationSet);


    }


    @Override
    protected void onResume() {
        super.onResume();
        isKeyboardValidity = true;
        Log.e(TAG,"onResume....");
    }



    @Override
    protected void onStop() {
        super.onStop();
        isKeyboardValidity = false;
        Log.e(TAG,"onStop....");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            stopScanner();
        }catch (Exception e){
            e.printStackTrace();
            showToast("调用停止摄像头SDK失败！");
        }
        try{
            USBPrinter.getInstance().close();
        }catch (Exception e){
            e.printStackTrace();
            showToast("关闭打印机出错！");
        }
        if(usbKeyboard!=null){

            usbKeyboard.release();
            usbKeyboard = null;
        }
        Log.e(TAG,"onDestroy....");
    }



    /**
     * 定制键盘
     */
    @Override
    public void getKeyboardValue(int keyCode, String keyName, double money, String text) {
        Log.e(TAG,"接收的金额："+String.valueOf(money));
        Log.e(TAG,"屏幕更新的值："+text);
        if(isKeyboardValidity) {
            if (USBKeyboard.KEY_CODE_ESC == keyCode && USBKeyboard.KEY_NAME_ESC.equals(keyName)) {
                //点击取消键显示
                speak("取消");
                finish();


            }
        }
    }

    /**
     * 青蛙自带键盘
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ADD){
            /*0键*/
            Log.e(TAG,"按下+号键，定义为取消操作");
            speak("取消");
            finish();

        }
        /**
         * 这里直接return true，防止事件向上传递，改变最终输入结果
         */
//        return super.dispatchKeyEvent(event);
        return true;
    }



    private void initListener(){
        imgBack.setOnClickListener(this);
        btBrushFace.setOnClickListener(this);
        usbKeyboard.getOnKeyboardValueListener(this);
    }



    /**
     * 获取RawData
     */
    private void getRawData(){
        try{
            WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
                @Override
                public void response(Map info) throws RemoteException {

                    if (info == null) {
                        showToast("获取RawData参数失败,请联系技术人员！");
                        finish();
                        return;
                    }
                    String code = (String)info.get(Constants.RETURN_CODE);
                    String msg = (String)info.get(Constants.RETURN_MSG);
                    Log.e(TAG, "response | getWxpayfaceRawdata " + code + " | " + msg);
                    if (code == null || !code.equals(WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)) {
                        showToast("获取RawData参数失败,请联系技术人员！");
                        finish();
                        return;
                    }
                    Log.e("获取rawdata返回:",info.toString());
                    String rawdata = info.get("rawdata").toString();

                    //获取人脸SDK调用凭证authinfo
                    try {
                        String encodeRawdata = URLEncoder.encode(rawdata,"UTF-8");
                        Map<String,String> map = PayParamsReqUtil.getAuthinfoReq(userBean,encodeRawdata);
                        getAuthInfo(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("获取authinfo参数配置失败！");
                        finish();
                        btBrushFace.setClickable(true);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            showToast("获取RawData参数失败，请联系技术人员！");
            finish();
            btBrushFace.setClickable(true);
        }
    }

    /**
     * 准备获取人脸凭证（1，调用摄像头，展示UI，2，完成人脸识别，3，返回人脸识别结果face_code,openid）
     *  ask_face_permit: 支付成功页是否需要展示人脸识别授权项。展示：1。不展示：0。
     *                   人脸识别授权项：用户授权后用于1:N识别，可返回用户信息openid，建议商户有自己会员系统时，填1。
     *  ask_ret_page: 是否展示微信支付成功页，可选值："0"，不展示；"1"，展示
     *
     *  total_fee: 订单金额(数字), 单位分，该字段在在face_code_type为"1"时可不填，为"0"时必填
     */
    private void startBrushFace(FacePayAuthInfoRes authInfo){
        stop();
        //语音播报提示
        speak("请面向屏幕开始刷脸！");
        try{
            //支付时金额以分为单位
            String totalFeeStr = DecimalUtil.elementToBranch(totalFee);
            final Map params = PayParamsReqUtil.getBrushFaceParams(totalFeeStr,payMode,authInfo);
            Log.e("刷脸SDK参数：",params.toString());

            WxPayFace.getInstance().getWxpayfaceCode(params, new IWxPayfaceCallback() {
                @Override
                public void response(Map info) throws RemoteException {
                    if (info == null) {
                        showToast("刷脸失败或已取消！");
                        finish();
                        return;
                    }
                    String code = (String)info.get(Constants.RETURN_CODE);
                    String msg = (String)info.get(Constants.RETURN_MSG);
                    Log.e(TAG, "response | getWxpayfaceRawdata " + code + " | " + msg);
                    if (code == null || !code.equals(WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)) {
                        showToast("刷脸失败或已取消！");
                        finish();
                        return;
                    }


                    Log.e("获取人脸凭证返回:",info.toString());
                    String face_code = info.get("face_code").toString();
                    String openid = info.get("openid").toString();

                    try {
                        Map<String,String> map = PayParamsReqUtil.getPlaceorderParams(userBean,face_code,openid,params);
                        startBrushFaceOrderPay(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("刷脸支付API参数配置失败！");
                        finish();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            showToast("人脸识别SDK调起失败！");
            finish();
        }
    }

    /**
     * 停止扫码
     */
    private void stopScanner(){
        try{
            WxPayFace.getInstance().stopCodeScanner();
        }catch(Exception e){
            e.printStackTrace();
            showToast("停止扫码SDK调用失败！！");
        }
    }


    /**
     * 停止人脸识别
     */
    private void stopBrushFace(){
        try{
            HashMap map = new HashMap();
            WxPayFace.getInstance().stopWxpayface(map, new IWxPayfaceCallback() {
                @Override
                public void response(Map info) throws RemoteException {
                    if (info == null) {
                        showToast("停止人脸识别调起失败！");
                        finish();
                        return;
                    }
                    String code = (String)info.get(Constants.RETURN_CODE);
                    String msg = (String)info.get(Constants.RETURN_MSG);
                    Log.e(TAG, "response | getWxpayfaceRawdata " + code + " | " + msg);
                    if (code == null || !code.equals(WxfacePayCommonCode.VAL_RSP_PARAMS_SUCCESS)) {
                        showToast("停止人脸识别调起失败");
                        finish();
                        return;
                    }
                    showToast("停止人脸识别！");
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            showToast("停止人脸识别调起失败！！");
        }
    }


    /**
     *  获取人脸SDK调用凭证authinfo
     */
    private void getAuthInfo(final Map<String,String> map){
        showCustomDialog();
        final String url = NitConfig.getAuthInfoUrl;
//        final String url = "http://hd2m27.natappfree.cc/pay/api/face/authinfo";
        Log.e(TAG,"获取人脸凭证接口地址："+url);
        new Thread(){
            @Override
            public void run() {
                try {
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("face_code","AAAAAA");
                    userJSON.put("orderinfo","BBBBBBBBB");
                    String reqJson = String.valueOf(userJSON);
                    Log.e("获取authinfo示例请求参数：", reqJson);

                    String content = FastJsonUtil.toJSONString(map);
                    String content_type = HttpURLConnectionUtil.CONTENT_TYPE_JSON;
                    Log.e("获取authinfo真实请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content,content_type);
                    Log.e("获取authinfo返回结果：", jsonStr);
                    int msg = NetworkUtils.MSG_WHAT_ONE;
                    String text = jsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_JSON_CODE,NetworkUtils.REQUEST_JSON_TEXT);
                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_IO_CODE,NetworkUtils.REQUEST_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_CODE,NetworkUtils.REQUEST_TEXT);
                }
            }
        }.start();

    }

    /**
     * 发起订单人脸支付
     */
    private void startBrushFaceOrderPay(final Map<String,String> map){
        final String url = NitConfig.brushFaceOrderPayUrl;
//        final String url = "http://hd2m27.natappfree.cc/pay/api/face/placeorder";
        Log.e(TAG,"刷脸接口地址："+url);
        new Thread(){
            @Override
            public void run() {
                try {
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("face_code","");
                    userJSON.put("openid","");
                    String reqJson = String.valueOf(userJSON);
                    Log.e("人脸支付API示例请求参数：", reqJson);


                    String content = FastJsonUtil.toJSONString(map);
                    String content_type = HttpURLConnectionUtil.CONTENT_TYPE_JSON;
                    Log.e("人脸支付API真实请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content,content_type);
                    Log.e("人脸支付API返回结果：", jsonStr);
                    int msg = NetworkUtils.MSG_WHAT_TWO;
                    String text = jsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_JSON_CODE,NetworkUtils.REQUEST_JSON_TEXT);
                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_IO_CODE,NetworkUtils.REQUEST_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_CODE,NetworkUtils.REQUEST_TEXT);
                }
            }
        }.start();
    }

    /**
     * 发起订单刷卡支付
     */
    private void scannerCodePay(final String totalFeeStr,final String auth_no){
        showCustomDialog();
        new Thread(){
            @Override
            public void run() {
                try {
                    String url = "";
                    String content = "";
                    if(payType.equals(Constants.PAY_TYPE_800MEMBER)){
                        int cardId = 0;
                        PosMemConsumeReqData posBean = PayParamsReqUtil.consumeReq(payType, auth_no, totalFeeStr,userBean,cardId);
                        url = NitConfig.micropayUrl;
                        Log.e(TAG,"会员消费接口地址："+url);
                        content = FastJsonUtil.toJSONString(posBean);
                    }else{
                        PosScanpayReqData posBean = PayParamsReqUtil.payReq(userBean,payType,totalFeeStr, auth_no);
                        url = NitConfig.barcodepayUrl;
                        Log.e(TAG,"刷卡接口地址："+url);
                        content = FastJsonUtil.toJSONString(posBean);
                    }



                    String content_type = HttpURLConnectionUtil.CONTENT_TYPE_JSON;
                    Log.e("刷卡请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content,content_type);
                    Log.e("刷卡支付API返回结果：", jsonStr);
                    int msg = NetworkUtils.MSG_WHAT_THREE;
                    String text = jsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_JSON_CODE,NetworkUtils.REQUEST_JSON_TEXT);
                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_IO_CODE,NetworkUtils.REQUEST_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_CODE,NetworkUtils.REQUEST_TEXT);
                }
            }
        }.start();
    }

    /**
     * 查询消费有礼
     */
    private void getCardStock(){
        showCustomDialog("查询消费有礼！");
        final String url = NitConfig.getCardStockUrl;
        Log.e(TAG,"刷卡接口地址："+url);
        new Thread(){
            @Override
            public void run() {
                try {
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("terminalId",userBean.getTerminal_id());
                    userJSON.put("merchantNo",userBean.getMerchant_no());
                    userJSON.put("amount",DecimalUtil.branchToElement(posScanpayResData.getTotal_fee()));
                    userJSON.put("orderId",posScanpayResData.getOut_trade_no());
                    userJSON.put("payWay",Constants.getPayWay(posScanpayResData.getPay_type()));
                    userJSON.put("payTime",DateTimeUtil.dateToStamp(posScanpayResData.getEnd_time()));
                    String content = String.valueOf(userJSON);
                    Log.e("刷卡请求参数：", content);
                    String content_type = HttpURLConnectionUtil.CONTENT_TYPE_JSON;
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content,content_type);
                    Log.e("刷卡支付API返回结果：", jsonStr);
                    int msg = NetworkUtils.MSG_WHAT_FOUR;
                    String text = jsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_JSON_CODE,NetworkUtils.REQUEST_JSON_TEXT);
                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_IO_CODE,NetworkUtils.REQUEST_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_CODE,NetworkUtils.REQUEST_TEXT);
                }
            }
        }.start();
    }

    /**
     * 获取订单状态
     */
    private void getPayOrderStatus(){

        final String url = NitConfig.queryOrderStatusUrl;
        Log.e(TAG,"获取订单状态接口调用地址："+url);
        new Thread(){
            @Override
            public void run() {
                try {
                    String content = FastJsonUtil.toJSONString(posPayQueryReqData);
                    Log.e("获取订单状态请求参数：", content);
                    String content_type = HttpURLConnectionUtil.CONTENT_TYPE_JSON;
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content,content_type);
                    Log.e("获取订单状态返回结果：", jsonStr);
                    int msg = NetworkUtils.MSG_WHAT_FIVE;
                    String text = jsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_JSON_CODE,NetworkUtils.REQUEST_JSON_TEXT);
                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_IO_CODE,NetworkUtils.REQUEST_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.REQUEST_CODE,NetworkUtils.REQUEST_TEXT);
                }
            }
        }.start();
    }

    /**
     * 支付中时查询订单状态
     * 轮询方案：https://www.cnblogs.com/ygj0930/p/7657194.html
     */
    private void queryOrderStatus(){
        if(posScanpayResData != null){
            posPayQueryReqData = PayParamsReqUtil.paymentStateQueryReq(payType,posScanpayResData,userBean);
            //发起查询请求
            getPayOrderStatus();

        }else{
            ToastUtil.showText(activity,"支付中订单信息为空！",1);
        }
    }

    public void sendMessage(int what,String text){
        Message msg = new Message();
        msg.what = what;
        msg.obj = text;
        handler.sendMessage(msg);
    }




    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String errorJsonText = "";
            switch (msg.what){
                case NetworkUtils.MSG_WHAT_ONEHUNDRED:
                    String code_msg = (String) msg.obj;
                    //获取支付类型
                    payType = PayUtils.getPayType(code_msg);
                    //支付时金额以分为单位
                    String totalFeeStr = DecimalUtil.elementToBranch(totalFee);
                    scannerCodePay(totalFeeStr,code_msg);
                    break;
                case NetworkUtils.MSG_WHAT_ONE:
                    String authInfoJson = (String) msg.obj;
                    authInfoJson(authInfoJson);
                    hideCustomDialog();
                    btBrushFace.setClickable(true);
                    break;
                case NetworkUtils.MSG_WHAT_TWO:
                    //{"return_code":"SUCCESS","return_msg":"OK","result_code":"SUCCESS","mch_no":null,"term_no":null,"nonce_str":null}
                    String orderPayResult = (String) msg.obj;
                    orderPayResult(orderPayResult);
                    break;
                case NetworkUtils.MSG_WHAT_THREE:
                    //关闭扫码
                    stopScanner();
                    String scanPayJsonStr=(String) msg.obj;
                    scanPayJsonStr(scanPayJsonStr);
                    hideCustomDialog();
                    break;
                case NetworkUtils.MSG_WHAT_FOUR:
                    String cardStockStr=(String) msg.obj;
                    cardStockStr(cardStockStr);
                    hideCustomDialog();
                    break;
                case NetworkUtils.MSG_WHAT_FIVE:
                    String queryOrderStatusStr=(String) msg.obj;
                    queryOrderStatusStr(queryOrderStatusStr);
                    break;
                case NetworkUtils.MSG_WHAT_SIX:
                    showCustomDialog("支付状态查询中！");
                    queryOrderStatus();
                    break;
                case NetworkUtils.MSG_WHAT_SEVEN:

                    getPayOrderStatus();

                    break;
                case NetworkUtils.REQUEST_JSON_CODE:
                    errorJsonText = (String) msg.obj;
                    ToastUtil.showText(activity,errorJsonText,1);

                    btBrushFace.setClickable(true);
                    hideCustomDialog();
                    break;
                case NetworkUtils.REQUEST_IO_CODE:
                    errorJsonText = (String) msg.obj;
                    ToastUtil.showText(activity,errorJsonText,1);

                    btBrushFace.setClickable(true);
                    hideCustomDialog();
                    break;
                case NetworkUtils.REQUEST_CODE:
                    errorJsonText = (String) msg.obj;
                    ToastUtil.showText(activity,errorJsonText,1);

                    btBrushFace.setClickable(true);
                    hideCustomDialog();
                    break;
                default:
                    finish();
                    break;
            }
        }
    };





    private void authInfoJson(String json){
        FacePayAuthInfoRes authInfoRes = null;
        try {
            authInfoRes = (FacePayAuthInfoRes) FastJsonUtil.jsonToObject(json,FacePayAuthInfoRes.class);
            if(authInfoRes != null){
                String return_code = authInfoRes.getReturn_code();
                String return_msg = authInfoRes.getReturn_msg();
                String result_code = authInfoRes.getResult_code();
                String err_code_des = authInfoRes.getErr_code_des();
                if(Constants.RETURN_SUCCESS.equals(return_code)){
                    if(Constants.RETURN_SUCCESS.equals(result_code)){
                        //准备获取人脸凭证（1，调用摄像头，展示UI，2，完成人脸识别，3，返回人脸识别结果face_code,openid）
                        startBrushFace(authInfoRes);
                    }else{
                        ToastUtil.showText(activity,err_code_des,1);
                    }
                }else{
                    ToastUtil.showText(activity,return_msg,1);
                }
            }else{
                ToastUtil.showText(activity,"获取authinfo失败！",1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showText(activity,"获取authinfo失败！",1);
        }
    }

    private void scanPayJsonStr(String jsonStr){
        Gson gjson  =  GsonUtils.getGson();
        posScanpayResData = gjson.fromJson(jsonStr, PosScanpayResData.class);
        //return_code	响应码：“01”成功 ，02”失败，请求成功不代表业务处理成功
        String return_codeStr = posScanpayResData.getReturn_code();
        //return_msg	返回信息提示，“支付成功”，“支付中”，“请求受限”等
        String return_msgStr = posScanpayResData.getReturn_msg();
        //result_code	业务结果：“01”成功 ，02”失败 ，“03”支付中
        String result_codeStr = posScanpayResData.getResult_code();
        if("01".equals(return_codeStr)) {
            if("01".equals(result_codeStr)){
                payState = Constants.PAY_STATE_SUCCESS;
                speak("付款成功！"+totalFee+"元！");
                //打印小票
                scannerPayPrint();
                //API支付成功（调用微信更新订单状态接口）
                WxPayFace.getInstance().updateWxpayfacePayResult(new HashMap(), new IWxPayfaceCallback() {
                    @Override
                    public void response(Map info) throws RemoteException {
                        Log.e("支付成功！",info.toString());
                    }
                });

            }else if("03".equals(result_codeStr)){
                handler.sendEmptyMessageDelayed(NetworkUtils.MSG_WHAT_SIX,0);
            }else{
                payState = Constants.PAY_STATE_FAIL;
                toActivity(payState);
                ToastUtil.showText(activity,"支付失败",1);
            }
        }else{
            payState = Constants.PAY_STATE_FAIL;
            toActivity(payState);
            ToastUtil.showText(activity,return_msgStr,1);

        }


    }


    private void orderPayResult(String jsonStr){

        FacePayUnifiedOrderRes orderRes = null;
        try {
            orderRes = (FacePayUnifiedOrderRes) FastJsonUtil.jsonToObject(jsonStr,FacePayUnifiedOrderRes.class);
            if(orderRes != null){
                String return_code = orderRes.getReturn_code();
                String return_msg = orderRes.getReturn_msg();
                String result_code = orderRes.getResult_code();
                String err_code_des = orderRes.getErr_code_des();
                if(Constants.RETURN_SUCCESS.equals(return_code)){
                    if(Constants.RETURN_SUCCESS.equals(result_code)){
                        payState = Constants.PAY_STATE_SUCCESS;
                        speak("付款成功"+totalFee+"元！");
                        //API支付成功（调用微信更新订单状态接口）
                        WxPayFace.getInstance().updateWxpayfacePayResult(new HashMap(), new IWxPayfaceCallback() {
                            @Override
                            public void response(Map info) throws RemoteException {
                                Log.e("支付成功！",info.toString());
                            }
                        });
                        facePayPrint(orderRes);

                    }else{
                        payState = Constants.PAY_STATE_FAIL;
                        toActivity(payState);
                        ToastUtil.showText(activity,err_code_des,1);
                    }
                }else{
                    payState = Constants.PAY_STATE_FAIL;
                    toActivity(payState);
                    ToastUtil.showText(activity,return_msg,1);
                }
            }else{
                payState = Constants.PAY_STATE_FAIL;
                toActivity(payState);
                ToastUtil.showText(activity,"刷脸支付失败！",1);
            }
        } catch (Exception e) {
            payState = Constants.PAY_STATE_FAIL;
            toActivity(payState);
            e.printStackTrace();
            ToastUtil.showText(activity,"刷脸支付失败！",1);
        }



    }

    private void cardStockStr(String jsonStr){
        try {
            if(Utils.isNotEmpty(jsonStr)){
                JSONObject job = new JSONObject(jsonStr);
                String code = job.getString("code");
                String msg = job.getString("msg");
                if("000000".equals(code)){
                    String subCode = job.getString("subCode");
                    String subMsg = job.getString("subMsg");
                    if("000000".equals(subCode)){
                        JSONObject dataJob = new JSONObject(job.getString("data"));
                        String url = dataJob.getString("url");
                        if(Utils.isNotEmpty(url)){
                            //打印
                            posScanpayResData.setUrl(url);
                            try {
                                Thread.sleep(USBPrintTextUtil.TIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            printCardStockUrl(url);
                        }
                    }else{
                        ToastUtil.showText(activity,subMsg,1);
                    }
                }else{
                    ToastUtil.showText(activity,msg,1);
                }
            }else{
                ToastUtil.showText(activity,"查询失败！",1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        toActivity(payState);
    }

    /**
     * 查询支付结果
     */
    private void queryOrderStatusStr(String json){
        try{
            Gson gjson  =  GsonUtils.getGson();
            PosPayQueryResData posResult = gjson.fromJson(json, PosPayQueryResData.class);
            //return_code	响应码：“01”成功 ，02”失败，请求成功不代表业务处理成功
            String return_codeStr = posResult.getReturn_code();
            //return_msg	返回信息提示，“支付成功”，“支付中”，“请求受限”等
            String return_msgStr = posResult.getReturn_msg();
            //result_code	业务结果：“01”支付成功 ，02”支付失败 ，“03”支付中,"04"含退款，
            String result_codeStr = posResult.getResult_code();
            if(return_codeStr.equals("01")){
                if(result_codeStr.equals("01")){
                    posScanpayResData = new PosScanpayResData();
                    posScanpayResData.setPay_type(posResult.getPay_type());
                    posScanpayResData.setMerchant_name(posResult.getMerchant_name());
                    posScanpayResData.setMerchant_no(posResult.getMerchant_no());
                    posScanpayResData.setTerminal_id(posResult.getTerminal_id());
                    posScanpayResData.setTerminal_trace(posResult.getTerminal_trace());
                    posScanpayResData.setTerminal_time(posResult.getTerminal_time());
                    posScanpayResData.setTotal_fee(posResult.getTotal_fee());
                    posScanpayResData.setEnd_time(posResult.getEnd_time());
                    posScanpayResData.setOut_trade_no(posResult.getOut_trade_no());

                    scannerPayPrint();

                    hideCustomDialog();
                }else if(result_codeStr.equals("02")){

                    hideCustomDialog();
                    payState = Constants.PAY_STATE_FAIL;
                    toActivity(payState);
                    ToastUtil.showText(activity,"支付失败",1);
                }else if(result_codeStr.equals("03")){
                    queryIndex ++;
                    if(queryIndex <= 5 ) {

                        handler.sendEmptyMessageDelayed(NetworkUtils.MSG_WHAT_SEVEN,5000);

                    }else{
                        hideCustomDialog();
                        speak("支付处理中，请等待收银员确认支付结果！");
                        finish();
                    }

                }else{

                    hideCustomDialog();
                    payState = Constants.PAY_STATE_FAIL;
                    toActivity(payState);
                    ToastUtil.showText(activity,"支付失败",1);
                }
            }else{

                hideCustomDialog();
                payState = Constants.PAY_STATE_FAIL;
                toActivity(payState);
                ToastUtil.showText(activity,return_msgStr,1);
            }
        }catch (Exception e){
            e.printStackTrace();
            hideCustomDialog();
            payState = Constants.PAY_STATE_FAIL;
            toActivity(payState);
            ToastUtil.showText(activity,"支付返回结果错误！",1);
        }

    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak(String text) {

        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    /**
     * 暂停播放
     */
    private void stop() {

        int result = synthesizer.stop();
        checkResult(result, "speak");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
//            toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
            Log.e("error code :", result+" method:" + method );
        }
    }

    /**
     * 刷卡支付成功执行打印
     */
    private void scannerPayPrint(){

        //打印
        USBPrinter.initPrinter(activity,false);
        USBPrintTextUtil.payOrderText(userBean,posScanpayResData);
        boolean isQueryCoupons = userBean.isQueryCoupons();
        if(isQueryCoupons){
            getCardStock();
        }else{
            toActivity(payState);
        }

       /* if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState() )
        {
            ToastUtil.showText( activity, getString( R.string.str_cann_printer ),1 );
            toActivity(payState);
            return;
        }
        threadPool = ThreadPool.getInstantiation();
        threadPool.addTask( new Runnable()
        {
            @Override
            public void run()
            {
                if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC )
                {
                    JiaBoPrintTextUtil.payOrderText(activity,id,userBean,posScanpayResData);
                    boolean isQueryCoupons = userBean.isQueryCoupons();
                    if(isQueryCoupons){
                        getCardStock();
                    }else{
                        toActivity(payState);
                    }
                } else {
                    mHandler.obtainMessage( PRINTER_COMMAND_ERROR ).sendToTarget();
                    toActivity(payState);
                }
            }
        } );*/


    }

    /**
     * 打印消费有礼
     */
    private void printCardStockUrl(final String url){
        //打印
//        USBPrinter.initPrinter(this);
        USBPrintTextUtil.cardStockQRcode(url);

        /*if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState() )
        {
            ToastUtil.showText( activity, getString( R.string.str_cann_printer ),1 );
            toActivity(payState);
            return;
        }
        threadPool = ThreadPool.getInstantiation();
        threadPool.addTask( new Runnable()
        {
            @Override
            public void run()
            {
                if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC )
                {
                    JiaBoPrintTextUtil.cardStockQRcode(id,url);

                } else {
                    mHandler.obtainMessage( PRINTER_COMMAND_ERROR ).sendToTarget();
                    toActivity(payState);
                }
            }
        } );*/


    }


    /**
     * 刷脸支付成功执行打印
     */
   private void facePayPrint(final FacePayUnifiedOrderRes orderRes){
       posScanpayResData = new PosScanpayResData();
       posScanpayResData.setPay_type(orderRes.getPay_type());
       posScanpayResData.setOut_trade_no(orderRes.getTrasaction_no());
       posScanpayResData.setEnd_time(orderRes.getTrasaction_time());
       posScanpayResData.setTotal_fee(orderRes.getTotal_fee());
       //打印
//       USBPrinter.initPrinter(this);
       USBPrintTextUtil.payOrderText(userBean,posScanpayResData);
       boolean isQueryCoupons = userBean.isQueryCoupons();
       if(isQueryCoupons){
           getCardStock();
       }else{
           toActivity(payState);
       }


       /*if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
               !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState() )
       {
           ToastUtil.showText( activity, getString( R.string.str_cann_printer ),1 );
           toActivity(payState);
           return;
       }
       threadPool = ThreadPool.getInstantiation();
       threadPool.addTask( new Runnable()
       {
           @Override
           public void run()
           {
               if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC )
               {
                   posScanpayResData = new PosScanpayResData();
                   posScanpayResData.setPay_type(orderRes.getPay_type());
                   posScanpayResData.setOut_trade_no(orderRes.getTrasaction_no());
                   posScanpayResData.setEnd_time(orderRes.getTrasaction_time());
                   posScanpayResData.setTotal_fee(orderRes.getTotal_fee());
                   JiaBoPrintTextUtil.payOrderText(activity,id,userBean,posScanpayResData);
                   boolean isQueryCoupons = userBean.isQueryCoupons();
                   if(isQueryCoupons){
                       getCardStock();
                   }else{
                       toActivity(payState);
                   }
               } else {
                   mHandler.obtainMessage( PRINTER_COMMAND_ERROR ).sendToTarget();
                   toActivity(payState);
               }
           }
       } );*/

    }

    /**
     * 展示支付状态页
     */
    private void toActivity(String state){
        Intent intent = new Intent();
        intent.setClass(activity,PayStateActivity.class);
        intent.putExtra("busType",busType);
        intent.putExtra("state",state);
        intent.putExtra("totalFee",totalFee);
        intent.putExtra("userBean",userBean);
        startActivity(intent);
        finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.scanner_hint_imgBack:
                finish();
                break;
            case R.id.brush_face_btBrushFace:
                if(Utils.isFastClick()){
                    return;
                }
                payMode = "1";
                SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(activity,SharedPreConstants.FACE_CODE_TYPE_FILE_NAME);
                if(sharedPreferencesUtil.contain(SharedPreConstants.FACE_CODE_TYPE_KEY_NAME)){
                    boolean isFaceCode = (boolean) sharedPreferencesUtil.getSharedPreference(SharedPreConstants.FACE_CODE_TYPE_KEY_NAME,false);
                    if(isFaceCode){
                        payMode = "0";
                    }else{
                        payMode = "1";
                    }
                }else{
                    //默认
                    payMode = "1";
                }
                //获取RawData
                getRawData();
                btBrushFace.setClickable(false);
                break;
                default:
                    break;
        }
    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.e("event的值", "event= "+event);

        //扫码枪扫码以ENTER键结束
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() != KeyEvent.KEYCODE_ENTER){
            scannedCodes.add(event.getKeyCode());
        }
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            //hasShift判断输入字符大小写
            boolean hasShift = false;
            for(int i = 0; i < scannedCodes.size(); i++ ){
                int keyCode = scannedCodes.get(i);
                sb.append(USBkeyboardUtil.keyCodeToChar(keyCode,hasShift));
                hasShift = (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT);
                if(i == scannedCodes.size()-1){
                    String value = sb.toString();
                    Log.e("扫码值：",value);
                }

            }
        }

        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL){
            *//*0键*//*
            Log.e(TAG,"按下键盘回退/删除键");
            //停止刷脸识别
            stopBrushFace();
            finish();

        }
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            *//*0键*//*
            Log.e(TAG,"按下back键");
            //停止刷脸识别
            stopBrushFace();
            finish();

        }
        return true;
    }*/
}
