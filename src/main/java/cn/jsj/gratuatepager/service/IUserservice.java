package cn.jsj.gratuatepager.service;

import cn.jsj.gratuatepager.pojo.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Validated
public interface IUserservice {

    /**
     * 从数据库中查询一个Student对象，如果数据库中不存在该用户，则抛出自定义异常。
     * @param id  学生id
     * @return CStudent
     */
    CStudent getOneStudentByID(@NotNull(message = "参数为空") @Min(value = 1) Integer id);

    /**
     * 从数据库中查询一个Student对象，如果数据库中不存在该用户，则抛出自定义异常。
     * @param account
     * @return CStudent
     */
    CStudent getOneStudentByNumber(@NotBlank@Length(min = 5,max = 15) String account);

    /**
     * 从数据库中查询一个Student对象，如果数据库中不存在该用户，则抛出自定义异常。
     * @param account
     * @return CStudent
     */
    CTeacher getOneTeacherByNumber(@NotBlank@Length(min = 5,max = 15) String account);


    List<CProfess> getProfessByDepartId(@NotNull@Min(value = 1) Integer departid);

    /**
     * 学生账号注册
     * 首先检查用户提交的验证码是否有效以及验证码是否正确，
     * 检查用户账号，手机号，邮箱是否已经被其他用户注册，检查用户上传的院系信息和专业信息是否合法。
     * @param studentNumber 学生学号
     * @param password 密码
     * @param email 邮箱
     * @param phoneNumber 手机号
     * @param name 姓名
     * @param departid 所属院系
     * @param code 验证码
     * @return
     */
    Map<String,String> registerOneStudent(@NotBlank(message = "请输入您要注册的账号")@Length(min = 5,max = 15,message = "请输入格式正确的账号")@Pattern(regexp = "^[a-z0-9A-Z]+$",message = "请输入格式正确的账号") String studentNumber, @NotBlank(message = "密码不能为空")@Length(min = 6,max = 20, message = "密码长度应为6-20位！") String password, @NotBlank(message = "请输入格式正确的邮箱")@Email(message = "请输入格式正确的邮箱") String email,@NotBlank(message = "请输入正确的手机号")@Length(min = 11,max = 11,message = "请输入正确的手机号")@Pattern(regexp = "^[1-9]\\d*|0$",message = "请输入正确的手机号") String phoneNumber, @NotBlank@Length(min = 2,max = 4,message = "这个姓名一看就知道不是你的") String name, @NotNull@Min(value = 1)@Max(value = 16) Integer departid,@NotBlank@Length(min = 6,max = 6)String code, @NotNull@Min(value = 1) Integer professid);


    /**
     * 获取所有的院系信息，用户用户注册页面的二级联动。
     * @return
     */
    List<CDepart> getDeparts();


    CTitle getCheckedTitleOfStudent(@NotBlank@Length(min = 5,max = 15) String studentAccount);
    

    void cancelTitleCheck(@NotBlank@Length(min = 5,max = 15) String studentAccount);

    void teacherCancelStudentTitleCheck(@NotBlank@Length(min = 5,max = 15)@Pattern(regexp = "^[a-z0-9A-Z]+$") String teacherAccount,@NotBlank@Length(min = 5,max = 15) String studentAccount);

    /**
     * 学生用户选题
     * @param studentAccount
     * @param titleID
     */
    void checkTitle(@NotBlank@Length(min = 5,max = 15) String studentAccount,@NotNull@Min(value = 1) Integer titleID);


    List<CProfess> getOneTeacherProfess(@NotBlank@Length(min = 5,max = 15) String workNumber);

    /**
     * 注册验证码处理
     * @param account 用户要申请的账号
     * @param email 邮箱
     * @return 返回生成的验证码，同时将邮件发送到用户邮箱中。
     */
    String productsecuritycode(@NotBlank@Length(min = 5) String account,@NotBlank@Email String email);

    /**检查用户验证法是否正确以及合法,一旦调用此方法，不论检验是否通过，都会删除redis相关键值对
     * @param account
     * @param code
     * @return
     */

    boolean checkResistorCode(@NotBlank String account,@NotBlank@Length(min = 6,max = 6) String code);

    List<CTeacher> getAllTeacherOfOneDepart(@NotBlank String departName);

    void userReDescription(@NotBlank@Length(min = 5,max = 15) String userAccount, @NotBlank String newContent);

    void userReName(@NotBlank@Length(min = 5,max = 15) String userAccount,@NotBlank@Length(min = 2,max = 4) String newName);

    Map<String,String> login(@NotBlank@Length(min = 5,max = 15) String account, @NotBlank@Length(min = 6,max = 20) String password);

    /**
     * 教师账号注册
     * 首先检查用户提交的验证码是否有效以及验证码是否正确，
     * 检验用户账号，邮箱，手机号是否已经被其他用户注册，检查院系ID信息是否有效。
     * @param workNumber
     * @param password
     * @param email
     * @param phoneNumber
     * @param name
     * @param departid
     * @param code
     * @param protitle
     * @param direction
     * @return Map<String,String>
     */
    Map<String,String> registerOneTeacher(@NotBlank(message = "请输入工号")@Length(min = 5,max = 15,message = "您输入的工号长度不符合要求")@Pattern(regexp = "^[a-z0-9A-Z]+$",message = "请注意您的工号格式") String workNumber, @NotBlank@Length(min = 6,max = 20,message = "密码长度应为6-20位！") String password, @NotBlank@Email String email,@NotBlank@Length(min = 11,max = 11)@Pattern(regexp = "^[1-9]\\d*|0$") String phoneNumber, @NotBlank@Length(min = 2,max = 4,message = "这一看就不是你的真实姓名") String name, @NotNull@Min(value = 1)@Max(value = 16) Integer departid,@NotBlank@Length(min = 6,max = 6)String code, @NotBlank String protitle,@NotBlank String direction);

    /**
     * 检查账号是否有效，如果无效则抛出自定义异常。
     * 返回值为字符串，student或者teacher
     * @param account
     * @return String
     */
    String judgeIdentityByAccount(@NotBlank@Length(min = 5,max = 15) String account);
    
}
