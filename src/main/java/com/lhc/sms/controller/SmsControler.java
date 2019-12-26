package com.lhc.sms.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.lhc.sms.SmsObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sms")
public class SmsControler {
    @Autowired
    @Qualifier("redisTemplate")
    //实例化
    private RedisTemplate<Object, Object> rts;

    @RequestMapping("/say")
    @ResponseBody
    public JSONObject say(){
        JSONObject res = new JSONObject(new HashMap<>());
        res.put("result", "hello world!");
        return res;
        // return "hello world!";
    }
    /**
     * 发送短信验证码
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping("/getCode")
    @ResponseBody
    public String getCode(String username)throws Exception{
        //rts.opsForValue().set("17730223870",123456);
        System.out.println("phone=" + username);
        SmsObject smsObject = new SmsObject();
        smsObject.setNewcode();
        String code = Integer.toString(smsObject.getNewcode());
        SendSmsResponse sendSms = smsObject.sendSms(username, code);//填写你需要测试的手机号码
        //将手机号和验证码存入redis,生存时间为5分钟
        rts.opsForValue().set(username, code, 1, TimeUnit.MINUTES);
        System.out.println("短信接口返回的数据----------------");
        System.out.println("Code=" + sendSms.getCode());
        System.out.println("Message=" + sendSms.getMessage());
        System.out.println("RequestId=" + sendSms.getRequestId());
        System.out.println("BizId=" + sendSms.getBizId());
        
        return sendSms.getCode();
    }


    /**
     * 检测短信验证码是否相同  登陆
     * @param username
     * @param pcode
     * @param session
     * @return
     * @throws Exception
     */
    @RequestMapping("/plogin")
    @ResponseBody
    public JSONObject plogin(String username, String pcode, HttpSession session) throws Exception {
        System.out.println("username=" + username + ";pcode=" + pcode);
        JSONObject res = new JSONObject(new HashMap<>());        
        try{
            Object code=rts.opsForValue().get(username);
            if (code.equals(pcode)) {
                res.put("result", true);
            } else {
                res.put("result", false);
            }
        }catch (Exception e){
            res.put("result", false);
            e.printStackTrace();
        }
        return res;
    }
}
