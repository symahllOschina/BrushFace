package com.wanding.face.bean;



import com.wanding.face.utils.MD5;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员消费
 */
public class PosMemConsumeReqData implements Serializable {

    private String pay_ver = "100";//版本号

    private String pay_type;//请求类型

    private String service_id = "010";//接口类型

    private String merchant_no;//商户号

    private String terminal_id;//终端号

    private String terminal_trace;//终端流水号

    private String terminal_time;//终端交易时间

    private String auth_no;//授权码

    private String total_fee;//金额

    private String memberCode;//会员卡号

    private String order_body;//订单描述

    private String key_sign; //签名检验串

    private String member_type;

    /**
     * 设备类型：machineType
     */
    private String machineType;

    public PosMemConsumeReqData() {
    }

    public String getPay_ver() {
        return pay_ver;
    }

    public void setPay_ver(String pay_ver) {
        this.pay_ver = pay_ver;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getMerchant_no() {
        return merchant_no;
    }

    public void setMerchant_no(String merchant_no) {
        this.merchant_no = merchant_no;
    }

    public String getTerminal_id() {
        return terminal_id;
    }

    public void setTerminal_id(String terminal_id) {
        this.terminal_id = terminal_id;
    }

    public String getTerminal_trace() {
        return terminal_trace;
    }

    public void setTerminal_trace(String terminal_trace) {
        this.terminal_trace = terminal_trace;
    }

    public String getTerminal_time() {
        return terminal_time;
    }

    public void setTerminal_time(String terminal_time) {
        this.terminal_time = terminal_time;
    }

    public String getAuth_no() {
        return auth_no;
    }

    public void setAuth_no(String auth_no) {
        this.auth_no = auth_no;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getOrder_body() {
        return order_body;
    }

    public void setOrder_body(String order_body) {
        this.order_body = order_body;
    }

    public String getKey_sign() {
        return key_sign;
    }

    public void setKey_sign(String key_sign) {
        this.key_sign = key_sign;
    }

    public String getMember_type() {
        return member_type;
    }

    public void setMember_type(String member_type) {
        this.member_type = member_type;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getSignStr(String access_token){
        final StringBuilder sb = new StringBuilder("");
        sb.append("pay_ver=").append(pay_ver).append("&");
        sb.append("pay_type=").append(pay_type).append("&");
        sb.append("service_id=").append(service_id).append("&");
        sb.append("memberCode='").append(memberCode).append("&");
        sb.append("merchant_no=").append(merchant_no).append("&");
        sb.append("terminal_id=").append(terminal_id).append("&");
        sb.append("terminal_trace=").append(terminal_trace).append("&");
        sb.append("terminal_time=").append(terminal_time).append("&");
        sb.append("auth_no=").append(auth_no).append("&");
        sb.append("total_fee=").append(total_fee).append("&");
        sb.append("access_token=").append(access_token);
        String keySign = MD5.MD5Encode(sb.toString());
        return keySign;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if (obj != null) {
                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
