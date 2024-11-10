package cn.jsj.gratuatepager.tools;

import com.alibaba.fastjson.JSON;

public class CPublicJsonTranslator {
    public static String translateWithClassName(Object t){
        return JSON.toJSONString(t);
    }
}
