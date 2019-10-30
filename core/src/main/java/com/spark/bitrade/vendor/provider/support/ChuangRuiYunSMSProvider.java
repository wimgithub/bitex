package com.spark.bitrade.vendor.provider.support;

import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vendor.provider.SMSProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.net.URLEncoder;

/** 创瑞云短信
 * @auther Cain
 * @date 2018/10/24
 * @time 9:04
 */
@Slf4j
public class ChuangRuiYunSMSProvider implements SMSProvider {
    private String gateway;
    private String accessKey;
    private String secret;
    private String sign;
    private String templateId;

    public ChuangRuiYunSMSProvider(String gateway, String accessKey, String secret, String sign, String templateId){
        this.gateway=gateway;
        this.accessKey=accessKey;
        this.secret=secret;
        this.sign=sign;
        this.templateId=templateId;
    }

    public static String getName() {
        return "chuangruiyun";
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String code) throws Exception {
        return sendSingleMessage(mobile,code);
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(gateway);
        postMethod.getParams().setContentCharset("UTF-8");
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());
        NameValuePair[] data = {
                new NameValuePair("accesskey", accessKey),
                new NameValuePair("secret", secret),
                new NameValuePair("sign", sign),
                new NameValuePair("templateId", templateId),
                new NameValuePair("mobile", mobile),
                new NameValuePair("content", URLEncoder.encode(content, "utf-8"))
        };
        postMethod.setRequestBody(data);
        postMethod.setRequestHeader("Connection", "close");
        int statusCode = httpClient.executeMethod(postMethod);
        System.out.println("statusCode: " + statusCode + ", body: "+ postMethod.getResponseBodyAsString());
        return parseResult(statusCode);
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, Long advertiseId, String userName) throws Exception {
        return null;
    }

    @Override
    public MessageResult sendLoginMessage(String ip, String phone) throws Exception {
        return null;
    }

    private MessageResult parseResult(int statusCode) {
        if(statusCode==200){
            return MessageResult.success();
        }else{
            return MessageResult.error("发送短信失败");
        }
    }
}
