package cn.jsj.gratuatepager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.jsj.gratuatepager.dao")
public class GratuatepagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GratuatepagerApplication.class, args);
    }

}
