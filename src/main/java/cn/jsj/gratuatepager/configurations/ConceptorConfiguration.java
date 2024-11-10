package cn.jsj.gratuatepager.configurations;

import cn.jsj.gratuatepager.interceptor.MyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private MyInterceptor myInterceptor;

    /**
     * @param interceptorRegistry   配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry){
        String[] addpath = {"/**"};
        String[] excludepath = {"/pages/**","/favicon.ico","/error"};
        interceptorRegistry.addInterceptor(this.myInterceptor).addPathPatterns(addpath).excludePathPatterns(excludepath);
    }
    
}
