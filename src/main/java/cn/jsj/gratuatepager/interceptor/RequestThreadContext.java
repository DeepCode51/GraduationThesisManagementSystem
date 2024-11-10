package cn.jsj.gratuatepager.interceptor;

import cn.jsj.gratuatepager.pojo.CpasswordBoot;

public class RequestThreadContext {

    public static ThreadLocal<CpasswordBoot> localVar = new ThreadLocal<>();

    public static Integer getId() {
        return localVar.get().getId();
    }

    public static String getAccount() {
        return localVar.get().getUserAccount();
    }

}
