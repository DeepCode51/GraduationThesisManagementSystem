package cn.jsj.gratuatepager.controllerAdvice;

import cn.jsj.gratuatepager.exceptions.CustomRuntimeException;
import cn.jsj.gratuatepager.tools.HashMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.Map;


/**
 * @author 卢斌
 */
@ResponseBody
@ControllerAdvice
public class ExceptionHandleController {

    @ExceptionHandler(value = {CustomRuntimeException.class, IOException.class, ValidationException.class})
    public Map<String,Object> illegalArgumentException(Exception e){
        return HashMapBuilder.buildInException(e);
    }


    @ExceptionHandler(value = {ServletException.class})
    public Map<String,Object> illegalArgumentRequestException(HttpServletRequest request,ServletException e){
        return HashMapBuilder.build(-1,"无效请求", null);
    }

    @ExceptionHandler(value = {Exception.class})
    public Map<String,Object> programException(HttpServletRequest request, Exception e ){
        Logger logger = LoggerFactory.getLogger(this.getClass());
        String url = request.getRequestURL().toString();
        Map<String, String[]> storage = request.getParameterMap();
        StringBuilder stringBuffer = new StringBuilder("");
        stringBuffer.append("请求路径：").append(url);
        for(String i: storage.keySet()){
            stringBuffer.append("参数").append(i).append(":");
            String[] para = storage.get(i);
            if(para == null){
                continue;
            }
            for(String j:para){
                stringBuffer.append(j).append(" ");
            }
        }
        logger.error(stringBuffer.toString()+e.toString());
        return HashMapBuilder.buildInException(e);
    }
    
}
