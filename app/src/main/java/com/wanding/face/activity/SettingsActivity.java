package com.wanding.face.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wanding.face.App;
import com.wanding.face.Constants;
import com.wanding.face.LoginActivity;
import com.wanding.face.ParameterSettingActivity;
import com.wanding.face.R;
import com.wanding.face.bean.UserBean;
import com.wanding.face.httputil.NetworkUtils;
import com.wanding.face.jiabo.device.DeviceConnFactoryManager;
import com.wanding.face.jiabo.device.ThreadPool;
import com.wanding.face.jiabo.device.activity.BluetoothActivity;
import com.wanding.face.jiabo.device.activity.DeviceBaseActivity;
import com.wanding.face.utils.SharedPreferencesUtil;
import com.wanding.face.utils.Utils;
import com.wanding.face.view.DevicePopupWindow;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 *  功能菜单管理界面
 */
@ContentView(R.layout.activity_setting)
public class SettingsActivity extends DeviceBaseActivity implements View.OnClickListener,DevicePopupWindow.OnSelectClickListener {

    public static final int BLUETOOTH_REQUEST_CODE = 0x001;

    @ViewInject(R.id.menu_title_imageView)
    ImageView imgBack;
    @ViewInject(R.id.menu_title_layout)
    LinearLayout titleLayout;
    @ViewInject(R.id.menu_title_tvTitle)
    TextView tvTitle;
    @ViewInject(R.id.menu_title_imgTitleImg)
    ImageView imgTitleImg;
    @ViewInject(R.id.menu_title_tvOption)
    TextView tvOption;

    @ViewInject(R.id.setting_device_content)
    LinearLayout myLayout;
    @ViewInject(R.id.setting_device_layout)
    RelativeLayout printDeviceLayout;

    @ViewInject(R.id.setting_passwd_layout)
    RelativeLayout passwdLayout;
    @ViewInject(R.id.setting_permissions_layout)
    RelativeLayout permissionsLayout;
    @ViewInject(R.id.setting_banner_layout)
    RelativeLayout bannerLayout;

    @ViewInject(R.id.setting_system_layout)
    RelativeLayout systemLayout;
    @ViewInject(R.id.setting_about_us_layout)
    RelativeLayout aboutUsLayout;
    @ViewInject(R.id.setting_exit_layout)
    RelativeLayout exitLayout;

    @ViewInject(R.id.setting_device_tvDeviceState)
    TextView tvDeviceState;


    private float alpha = 1;
    private UserBean userBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imgBack.setVisibility(View.VISIBLE);
        imgBack.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.back_icon));
        tvTitle.setText(getString(R.string.setting));
        imgTitleImg.setVisibility(View.GONE);
        tvOption.setVisibility(View.VISIBLE);
        tvOption.setText("参数设置");
        tvOption.setTextColor(ContextCompat.getColor(activity,R.color.blue_409EFF));

        initListener();
        Intent intent = getIntent();
        userBean = (UserBean) intent.getSerializableExtra("userBean");
        tvDeviceState.setText("USB");
//        initDate();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //注册获取USB连接设备实时状态广播
        IntentFilter filter = new IntentFilter(CONN_STATE_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String state = intent.getExtras().getString("data");
            if(Utils.isNotEmpty(state)){
                if(state.equals(Constants.DEVICE_STATE_DISCONNET)){

                    tvDeviceState.setText(getResources().getString(R.string.str_conn_state_disconnect));

                }else if(state.equals(Constants.DEVICE_STATE_CONNING)){

                    tvDeviceState.setText(getResources().getString(R.string.str_conn_state_connecting));

                }else if(state.equals(Constants.DEVICE_STATE_CONNETED)){

                    tvDeviceState.setText(getResources().getString(R.string.str_conn_state_connected));

                }else if(state.equals(Constants.DEVICE_STATE_CONN_FAIL)){

                    tvDeviceState.setText(getResources().getString(R.string.str_conn_fail));
                }

            }

        }
    };



    private void initListener(){
        imgBack.setOnClickListener(this);
        tvOption.setOnClickListener(this);
//        printDeviceLayout.setOnClickListener(this);
        passwdLayout.setOnClickListener(this);
        permissionsLayout.setOnClickListener(this);
        bannerLayout.setOnClickListener(this);
        systemLayout.setOnClickListener(this);
        aboutUsLayout.setOnClickListener(this);
        exitLayout.setOnClickListener(this);
    }

    private void initDate(){
        if(sharedPreferencesUtil == null){
            sharedPreferencesUtil = new SharedPreferencesUtil(activity, Constants.DEVICE_FILE_NAME);
        }
        if(sharedPreferencesUtil.contain(Constants.DEVICE_STATE_KEY)){
            String deviceState = (String) sharedPreferencesUtil.getSharedPreference(Constants.DEVICE_STATE_KEY,"");
            String deviceType = (String) sharedPreferencesUtil.getSharedPreference(Constants.DEVICE_TYPE_KEY,"");

            updateDeviceState(deviceType,deviceState);
        }

    }

    private void updateDeviceState(String type,String state){
        String str = getResources().getString(R.string.str_conn_state_disconnect);
        if(state.equals(Constants.DEVICE_STATE_DISCONNET)){

            str = getResources().getString(R.string.str_conn_state_disconnect);
            tvDeviceState.setText(type + str);

        }else if(state.equals(Constants.DEVICE_STATE_CONNING)){

            str = getResources().getString(R.string.str_conn_state_connecting);
            tvDeviceState.setText(type + str);

        }else if(state.equals(Constants.DEVICE_STATE_CONNETED)){

            str = getResources().getString(R.string.str_conn_state_connected);
            tvDeviceState.setText(type + str);

        }else if(state.equals(Constants.DEVICE_STATE_CONN_FAIL)){

            str = getResources().getString(R.string.str_conn_fail);
            tvDeviceState.setText(type + str);
        }
    }

    /**
     * 背景渐变暗
     */
    private void setWindowBackground(){
        DevicePopupWindow popupWindow = new DevicePopupWindow(this);
        popupWindow.showPhotoWindow();
        popupWindow.setOnSelectClickListener(this);
        popupWindow.showAtLocation(myLayout, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
        popupWindow.setOnDismissListener(new poponDismissListener());
        backgroundAlpha(1f);
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(alpha > 0.5f){
                    try {
                        //4是根据弹出动画时间和减少的透明度计算
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = NetworkUtils.MSG_WHAT_ONE;
                    //每次减少0.01，精度越高，变暗的效果越流畅
                    alpha-=0.01f;
                    msg.obj = alpha ;
                    handler.sendMessage(msg);
                }
            }

        }).start();
    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /**
     * 返回或者点击空白位置的时候将背景透明度改回来
     */
    class poponDismissListener implements PopupWindow.OnDismissListener{

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            new Thread(new Runnable(){
                @Override
                public void run() {
                    //此处while的条件alpha不能<= 否则会出现黑屏
                    while(alpha<1f){
                        try {
                            Thread.sleep(4);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("HeadPortrait","alpha:"+alpha);

                        Message msg = handler.obtainMessage();
                        msg.what = NetworkUtils.MSG_WHAT_ONE;
                        alpha+=0.01f;
                        msg.obj =alpha ;
                        handler.sendMessage(msg);
                    }
                }

            }).start();
        }

    }

    private Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            String errorJsonText = "";
            String url = "";
            switch (msg.what){
                case NetworkUtils.MSG_WHAT_ONE:
                    backgroundAlpha((float)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK )
        {
            switch ( requestCode )
            {
                /*蓝牙连接*/
                case BLUETOOTH_REQUEST_CODE: {
                    closeport();
                    /*获取蓝牙mac地址*/
                    String macAddress = data.getStringExtra("address");
                    /* 初始化话DeviceConnFactoryManager */
                    new DeviceConnFactoryManager.Build()
                            .setId( id )
                            /* 设置连接方式 */
                            .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH )
                            /* 设置连接的蓝牙mac地址 */
                            .setMacAddress( macAddress )
                            .build();
                    /* 打开端口 */
                    Log.d(TAG, "onActivityResult: 连接蓝牙"+id);
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    } );

                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public void selectListener(int type) {
        if(Utils.isFastClick()){
            return;
        }
        if(type == 1){
//            tvConnectType.setText(getResources().getString(R.string.setting_usb_connect_type));
            getUsbDeviceList();
        }else if(type == 2){
//            tvConnectType.setText(getResources().getString(R.string.setting_bluetooth_connect_type));
            startActivityForResult(new Intent(activity,BluetoothActivity.class),BLUETOOTH_REQUEST_CODE);
        }else if(type == 3){
//            tvConnectType.setText(getResources().getString(R.string.setting_serialport_connect_type));
        }else if(type == 4){
//            tvConnectType.setText(getResources().getString(R.string.setting_wifi_connect_type));
            startActivity(new Intent(activity,CloudPrintAuthActivity.class));
        }else{
//            tvConnectType.setText(getResources().getString(R.string.setting_no_connect_type));
        }

    }

    /**
     *  显示确认提示框
     **/
    private void showConfirmDialog(){
        View view = LayoutInflater.from(activity).inflate(R.layout.confirm_hint_dialog, null);
        TextView btok = (TextView) view.findViewById(R.id.confirm_hint_dialog_tvOk);
        TextView btCancel = (TextView) view.findViewById(R.id.confirm_hint_dialog_tvCancel);
        final Dialog myDialog = new Dialog(activity,R.style.dialog);
        Window dialogWindow = myDialog.getWindow();
        WindowManager.LayoutParams params = myDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
        dialogWindow.setAttributes(params);
        myDialog.setContentView(view);
        btok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //清除保存的用户信息
                sharedPreferencesUtil = new SharedPreferencesUtil(activity,"userInfo");
                sharedPreferencesUtil.clear();


                Intent intent=new Intent();
                intent.setClass(activity, LoginActivity.class);
                startActivity(intent);
                //关闭应用
                App.getInstance().exit();
                myDialog.dismiss();

            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    /**
     * 连续点击多次后的事件实现
     */
    int CLICK_NUM = 20;
    //点击时间间隔5秒
    int CLICK_INTERVER_TIME = 5000;
    //上一次的点击时间
    long lastClickTime = 0;
    //记录点击次数
    int clickNum = 0;
    private void continuClick(){

        //点击的间隔时间不能超过5秒
        long currentClickTime = SystemClock.uptimeMillis();
        if (currentClickTime - lastClickTime <= CLICK_INTERVER_TIME || lastClickTime == 0) {
            lastClickTime = currentClickTime;
            clickNum = clickNum + 1;
        } else {
            //超过5秒的间隔
            //重新计数 从1开始
            clickNum = 1;
            lastClickTime = 0;
            return;
        }
        if (clickNum == CLICK_NUM) {
            //重新计数
            clickNum = 0;
            lastClickTime = 0;
            /*实现点击多次后的事件*/
            startActivity(new Intent(activity,ParameterSettingActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.menu_title_imageView:
                finish();
                break;
            case R.id.menu_title_tvOption:
                continuClick();
                break;
            case R.id.setting_device_layout:
                if(Utils.isFastClick()){
                    return;
                }
                setWindowBackground();
                break;
            case R.id.setting_passwd_layout:
                intent.setClass(activity,EnterPasswdActivity.class);
                intent.putExtra("userBean",userBean);
                intent.putExtra("action",Constants.ACTION_PASSWD_MODIFY_PASSWD);
                startActivity(intent);
                break;
            case R.id.setting_permissions_layout:
                intent.setClass(activity,EnterPasswdActivity.class);
                intent.putExtra("action",Constants.ACTION_PASSWD_PERMISSIONS);
                startActivity(intent);
                break;
            case R.id.setting_banner_layout:
                intent.setClass(activity,BannerImageActivity.class);
                intent.putExtra("userBean",userBean);
                intent.putExtra("action",Constants.ACTION_SETTING_TO_BANNER);
                startActivity(intent);
                break;
            case R.id.setting_system_layout:
                //单独进入网络设置(ACTION_WIFI_SETTINGS：wifi设置)
//                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                //进入系统设置
                intent.setAction(Settings.ACTION_SETTINGS);
                startActivity(intent);
                break;
            case R.id.setting_about_us_layout:
                intent.setClass(activity,AboutUsActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_exit_layout:
                if(Utils.isFastClick()){
                    return;
                }
                showConfirmDialog();
                break;
                default:
                    break;
        }
    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL){
            *//*0键*//*
            Log.e(TAG,"按下键盘回退/删除键");
            finish();
        }
        return true;
    }*/
}
