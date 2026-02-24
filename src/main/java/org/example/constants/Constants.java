package org.example.constants;

/**
 * @author fasonghao
 */
public interface Constants {
    //管理员访问token在Redis中的前缀
    String TOKEN_PREFIX = "token:admin:";
    //管理员访问Token过期时间
    int TOKEN_EXPIRE_TIME = 30;
    //短信验证码发送url
    String SMS_CODE_URL="https://push.spug.cc/sms/ZGB9gWWHR3usXSwmO0IO2w";
    //短信验证码发送署名
    String SMS_CODE_NAME="行李寄存柜管理平台";
    //短信验证码在Redis中前缀
    String SMS_CODE_PREFIX="smsCode:";
    //短信验证码有效期
    int SMS_CODE_EXPIRE_TIME=5;
    //短信验证码相邻两次发送间隔
    int SMS_CODE_SPAN=1;
}
