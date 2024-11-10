package cn.jsj.gratuatepager.interceptor;

import cn.jsj.gratuatepager.interceptor.censor.PassToken;
import cn.jsj.gratuatepager.interceptor.censor.TokenCensor;
import cn.jsj.gratuatepager.interceptor.censor.UserLoginToken;
import cn.jsj.gratuatepager.pojo.CpasswordBoot;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
public class MyInterceptor implements HandlerInterceptor {

    @Value("${login-available-time}")
    private Integer loginEffertTime;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");

        // 如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        Method method = handlerMethod.getMethod();
        //检查是否有passToken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }

        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            if(StringUtils.isBlank(token)){
                try {
                    this.userLoginTimeOut(Objects.requireNonNull(response));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
            Object o = this.redisTemplate.opsForValue().get(token);
            boolean exist = this.redisTemplate.hasKey(token);
            if(exist == true){
                String tempPassword = (String)o;
                CpasswordBoot cpasswordBoot = TokenCensor.parseToken(token, tempPassword);
                this.redisTemplate.opsForValue().set(token, tempPassword, this.loginEffertTime, TimeUnit.SECONDS);
                RequestThreadContext.localVar.set(cpasswordBoot);
                return true;
            }
        }
        PrintWriter printWriter = response.getWriter();
        printWriter.write("loginAgain");
        printWriter.close();
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }


    /**
     * 拦截器拦截到用户请求后，如果不允许用户请求访问业务逻辑层则写回给用户登录超时信息
     * @param response HttpServletResponse
     * @throws IOException 写出到用户客户端失败
     */
    private void userLoginTimeOut(HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        printWriter.write("loginAgain");
        printWriter.close();
    }

}
