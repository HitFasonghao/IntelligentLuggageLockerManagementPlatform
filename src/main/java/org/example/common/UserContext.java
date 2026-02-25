package org.example.common;

/**
 * pc端用户信息上下文
 * @author fasonghao
 */
public class UserContext {
    //使用ThreadLocal存储当前线程的用户信息
    private static final ThreadLocal<PcUserInfo> USER_HOLDER = new ThreadLocal<>();

    public static void set(PcUserInfo user) {
        USER_HOLDER.set(user);
    }

    public static PcUserInfo get() {
        return USER_HOLDER.get();
    }

    public static void remove() {
        USER_HOLDER.remove();
    }
}