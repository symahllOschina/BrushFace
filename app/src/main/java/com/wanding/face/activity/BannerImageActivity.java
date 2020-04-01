package com.wanding.face.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.wanding.face.BaseActivity;
import com.wanding.face.Constants;
import com.wanding.face.LoginActivity;
import com.wanding.face.MainActivity;
import com.wanding.face.NitConfig;
import com.wanding.face.R;
import com.wanding.face.bean.BannerImageResData;
import com.wanding.face.bean.UserBean;
import com.wanding.face.httputil.HttpURLConnectionUtil;
import com.wanding.face.httputil.NetworkUtils;
import com.wanding.face.utils.GsonUtils;
import com.wanding.face.utils.MySerialize;
import com.wanding.face.utils.SharedPreferencesUtil;
import com.wanding.face.utils.ToastUtil;
import com.wanding.face.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Time: 2020/1/6
 * Author:Administrator
 * Description: 首页轮播广告更新
 */
public class BannerImageActivity extends BaseActivity {

    private UserBean userBean;
    private String action;
    /**
     * 轮播广告图片返回对象
     */
    private BannerImageResData bannerImageResData;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        userBean = (UserBean) intent.getSerializableExtra("userBean");
        action = intent.getStringExtra("action");
        getBannerImgList();

        Log.e(TAG,"onCreate()方法执行");
    }


    /**
     * 获取首页轮播图图片地址
     */
    private void getBannerImgList(){

        showCustomDialog("初始化参数");
        final String url = NitConfig.getBannerImgUrl;
        Log.e("请求地址：", url);
        new Thread(){
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("mType", Constants.DEVICE_TYPE);
                    userJSON.put("sid", userBean.getSid());
                    userJSON.put("mid", userBean.getMid());
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String content_type = HttpURLConnectionUtil.CONTENT_TYPE_JSON;
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content,content_type);
                    Log.e("返回字符串结果：", jsonStr);
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



    private void sendMessage(int what,String text){
        Message msg = new Message();
        msg.what = what;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String errorJsonText = "";
            switch (msg.what){
                case NetworkUtils.MSG_WHAT_ONE:
                    String getBannerImgListJson=(String) msg.obj;
                    getBannerImgListJson(getBannerImgListJson);
                    hideCustomDialog();
                    break;
                case NetworkUtils.REQUEST_JSON_CODE:
                    errorJsonText = (String) msg.obj;
                    ToastUtil.showText(activity,errorJsonText,1);
                    hideCustomDialog();
                    intoMainActivity();
                    break;
                case NetworkUtils.REQUEST_IO_CODE:
                    errorJsonText = (String) msg.obj;
                    ToastUtil.showText(activity,errorJsonText,1);
                    hideCustomDialog();
                    intoMainActivity();
                    break;
                case NetworkUtils.REQUEST_CODE:
                    errorJsonText = (String) msg.obj;
                    ToastUtil.showText(activity,errorJsonText,1);
                    hideCustomDialog();
                    intoMainActivity();
                    break;
                default:
                    break;
            }
        }
    };

    private void getBannerImgListJson(String json){
        try{
            JSONObject job = new JSONObject(json);
            String status = job.getString("status");
            String message = job.getString("message");
            if(status.equals("200")){
                String dataJson = job.getString("data");

                Gson gson = GsonUtils.getGson();
                bannerImageResData = gson.fromJson(dataJson,BannerImageResData.class);
                if(Utils.isNotEmpty(action)){
                    if(Constants.ACTION_SETTING_TO_BANNER.equals(action)){
                        ToastUtil.showText(activity,"广告资源已同步！",1);
                    }
                }



            }else{
                if(Utils.isNotEmpty(message)){
                    ToastUtil.showText(activity,message,1);
                }else{
                    ToastUtil.showText(activity,"获取数据失败！",1);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showText(activity,"请求结果返回错误！",1);
        }
        intoMainActivity();

    }




    private void intoMainActivity(){
        try{
            if(Utils.isNotEmpty(action)){
                if(Constants.ACTION_SETTING_TO_BANNER.equals(action)){
                    if(bannerImageResData!=null){

                        EventBus.getDefault().post(bannerImageResData);
                    }
                    finish();
                }else{
                    Intent in = new Intent();
                    in.setClass(activity,MainActivity.class);
                    in.putExtra("userBean",userBean);
                    in.putExtra("banner",bannerImageResData);
                    startActivity(in);
                    //跳转动画效果
                    overridePendingTransition(R.anim.in_from, R.anim.to_out);
                    this.finish();
                }
            }else{
                Intent in = new Intent();
                in.setClass(activity,MainActivity.class);
                in.putExtra("userBean",userBean);
                in.putExtra("banner",bannerImageResData);
                startActivity(in);
                //跳转动画效果
                overridePendingTransition(R.anim.in_from, R.anim.to_out);
                this.finish();
            }
        }catch(Exception e){
            e.printStackTrace();
            finish();
            ToastUtil.showText(activity,"广告资源加载失败！",1);
        }

    }
}


