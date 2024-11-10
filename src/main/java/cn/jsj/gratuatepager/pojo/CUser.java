package cn.jsj.gratuatepager.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class CUser implements Serializable {
    //用户id
    private Integer id;
    
    private Integer identity;

    private String account;

    //学生姓名
    private String name;


    //所属院系名称
    private String departName;

    //个人介绍
    private String discription;


    //电子邮箱
    private String email;


    //电话号码
    private String phone;

    public CUser(Integer id, Integer identity, String account, String name, String departName, String discription, String email, String phone) {
        this.id = id;
        this.identity = identity;
        this.account = account;
        this.name = name;
        this.departName = departName;
        this.discription = discription;
        this.email = email;
        this.phone = phone;
    }
}
