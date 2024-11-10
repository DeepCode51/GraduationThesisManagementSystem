package cn.jsj.gratuatepager.configurations;

import cn.jsj.gratuatepager.tools.SimpleMailFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfiguration {
    @Bean
    public SimpleMailFactory simpleMailSenderFactory() {
        return new SimpleMailFactory();
    }
}
