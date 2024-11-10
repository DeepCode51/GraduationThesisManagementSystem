package cn.jsj.gratuatepager.tools;

import cn.jsj.gratuatepager.exceptions.CustomRuntimeException;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HashMapBuilder工具类主要负责在控制层将后台程序需要响应给用户的信息进行封装
 */
public class HashMapBuilder {
    /**
     * build 主要处理返回值信息为单一对象的情况
     * @param state 状态数字表示
     * @param message 返回信息
     * @param o 数据对象
     * @return 一个HashMap
     */
    public static HashMap<String,Object> build(int state, String message, Object o){
        HashMap<String,Object> m =  new HashMap<>();
        m.put("state", state);
        m.put("message",message);
        m.put("jsonData", o);
        return m;
    }

    /**
     * buildList 主要用户处理响应数据对象为一个列表的情况
     * @param list
     * @param message
     * @return
     */
    public static Map<String, Object> buildList(List list, String message){
        HashMap<String,Object> m = new HashMap<>();
        if(list == null || list.size() == 0){
            m.put("total",0);
        }else{
            m.put("total",list.size());
        }
        m.put("rows", list);
        m.put("message", message);
        return m;
    }

    public static Map<String,Object> buildInException(Exception e){
        HashMap<String,Object> m = new HashMap<>();
        m.put("total",0);
        m.put("rows", null);
        m.put("jsonData", null);
        if((e instanceof CustomRuntimeException) || (e instanceof IOException)){
            m.put("message", e.getMessage());
            m.put("state", 0);
            return m;
        }else if(e instanceof ConstraintViolationException){
            String ts = e.getMessage();
            if(ts != null && ts.contains(": ")){
                String[] tq = ts.split(":");
                if(tq.length==2 && tq[1].length()>1){
                    ts = tq[1].trim();
                }
            }
            m.put("message",ts);
            m.put("state", 0);
            return m;
        }else{
            m.put("message","无效请求，请勿尝试攻击服务器");
            m.put("state",-1);
            return m;
        }
    }
    
}
