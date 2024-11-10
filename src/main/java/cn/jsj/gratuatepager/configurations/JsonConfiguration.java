package cn.jsj.gratuatepager.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.text.SimpleDateFormat;

@Configuration
public class JsonConfiguration {
    @Bean
    SimpleDateFormat productDataFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
