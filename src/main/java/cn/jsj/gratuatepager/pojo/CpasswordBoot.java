package cn.jsj.gratuatepager.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;


/**
 * 用户账号密码
 */
@Getter
@Setter
@NoArgsConstructor
public class CpasswordBoot implements Serializable {

    private Integer id;

    private String userAccount;

    private String userPassword;

    private Integer userIdentity;//用户个人身份标识，区分用户身份，详见数据库

    private Integer userId;


}