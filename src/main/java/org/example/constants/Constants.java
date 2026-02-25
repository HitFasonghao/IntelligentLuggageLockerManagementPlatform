package org.example.constants;

/**
 * @author fasonghao
 */
public interface Constants {
    //PC端访问token在Redis中的前缀
    String TOKEN_PREFIX = "token:pc:";
    //PC端访问Token过期时间
    int TOKEN_EXPIRE_TIME = 1800;
    //PC端登录token长度
    int TOKEN_BYTE_LENGTH=32;
    //短信验证码发送url
    String SMS_CODE_URL="https://push.spug.cc/sms/ZGB9gWWHR3usXSwmO0IO2w";
    //短信验证码发送署名
    String SMS_CODE_NAME="行李寄存柜管理平台";
    //登录所用短信验证码在Redis中前缀
    String LOGIN_SMS_CODE_PREFIX="login:smsCode:";
    //修改密码所用短信验证码在Redis中前缀
    String UPDATE_PASSWORD_SMS_CODE_PREFIX="updatePassword:smsCode:";
    //更换绑定手机号所用短信验证码在Redis中前缀
    String UPDATE_PHONE_SMS_CODE_PREFIX="updatePhone:smsCode:";
    //短信验证码有效期
    int SMS_CODE_EXPIRE_TIME=300;
    //短信验证码相邻两次发送间隔
    int SMS_CODE_SPAN=60;
}
