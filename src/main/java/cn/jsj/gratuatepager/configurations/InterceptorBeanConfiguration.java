package cn.jsj.gratuatepager.configurations;

import cn.jsj.gratuatepager.interceptor.MyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorBeanConfiguration {

    @Bean
    public MyInterceptor interceptor(){
        return new MyInterceptor();
    }
}
