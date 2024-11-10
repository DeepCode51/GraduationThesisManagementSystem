package cn.jsj.gratuatepager.dao;

import cn.jsj.gratuatepager.pojo.*;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IUserEntityDao {


    @Results(id = "toStudent", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "account", column = "schid"),
            @Result(property = "name", column = "name"),
            @Result(property = "departName", column = "departname"),
            @Result(property = "discription", column = "discription"),
            @Result(property = "email", column = "mail"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "professName", column = "professname")
    })
    @Select({SelectView.STUDENT, "where id = #{id} limit 1"})
    CStudent getStudentById(@Param("id") Integer id);

    /**
     *
     * @param number
     * @return 通过学号或者工号查询该用户在数据库中的id
     */
    @Select("select id from tb_user where schid = #{number} limit 1")
    Integer getUserIDByNumber(@Param("number") String number);

    @Results(id = "toUser", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "account", column = "schid"),
            @Result(property = "name", column = "name"),
            @Result(property = "departName", column = "departid",
                    one = @One(select = "cn.jsj.gratuatepager.dao.IUserEntityDao.getDepartNameByID", fetchType= FetchType.EAGER)),
            @Result(property = "discription", column = "discription"),
            @Result(property = "email", column = "mail"),
            @Result(property = "phone", column = "phone"),
    })
    @Select({SelectTable.USER, "where schid = #{number} limit 1"})
    CUser getUserByNumber(@Param("number")String number);


    @ResultMap("toStudent")
    @Select({SelectView.STUDENT, "where schid = #{number} limit 1"})
    CStudent getStudentByNumber(@Param("number")String number);


    /**
     * 向数据库user插入教师的部分信息，添加一个用户时还需要向教师表添加另外一部分信息
     * @param teacher
     * @return 插入成功返回1失败返回0
     */
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @Insert("insert into tb_user (schid, name,departid,phone,mail,identify) values (#{account},#{name},#{departName},#{phone},#{email},1)")
    Integer insertOneTeacherToUser(CTeacher teacher);

    @Results(id = "toDepart", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "departName", column = "departname")
    })
    @Select({SelectTable.DEPART})
    List<CDepart> getdepartList();

    @ResultMap("toProfess")
    @Select({SelectTable.PROFESS, "inner join tb_user on tb_profess.departid = tb_user.departid where schid=#{workNumber}"})
    List<CProfess> getoringProfessOfOneTeacher(@Param("workNumber") String workNumber);

    @ResultMap("toDepart")
    @Select({SelectTable.DEPART, "where id = #{departID} limit 1"})
    CDepart getDepartById(@Param("departID") Integer departID);

    @Select("select departname from tb_depart where id = #{departID} limit 1")
    public String getDepartNameByID(@Param("departID") Integer departID);
    /**
     *
     * @param departName
     * @return 返回院系名称对应的id
     */
    @Select("select id from tb_depart where departname = #{departName} limit 1")
    Integer getdepartIdByName(@Param("departName") String departName);

    @ResultMap(value = "toStudent")
    @Select({SelectView.STUDENT, "where phone = #{phoneNumber} limit 1"})
    CStudent getStudentByPhoneNumber(@Param("phoneNumber") String phoneNumber);


    /**
     * @param account
     * @param password
     * @return 向登录认证表中插入账号密码信息。
     */
    @Insert("insert into tb_usersecurty (schid, password, identify, user_id) values (#{account}, #{password}, #{identity}, #{userId})")
    Integer insertIntoSecurity(@Param("account") String account, @Param("password") String password, @Param("identity") Integer identity, @Param("userId") Integer userId);

    @ResultMap(value = "toStudent")
    @Select({SelectView.STUDENT, "where mail = #{email} limit 1"})
    CStudent getStudentByEmail(@Param("email") String email);

    @Insert("insert into tb_student (userid, profeid) values (#{studentID},#{professId})")
    public int insertProfessInformationByStudentId(@Param("studentID") Integer studentid, @Param("professId") Integer professid);

    @Results(id = "toPasswordBoot", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "userAccount", column = "schid"),
            @Result(property = "userPassword", column = "password"),
            @Result(property = "userIdentity", column = "identify"),
            @Result(property = "userId", column = "user_id")
    })
    @Select("select * from tb_usersecurty where schid = #{account} limit 1")
    CpasswordBoot getpasswordBoot(@Param("account") String account);

    @Results(id = "toProfess", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "departid", column = "departid"),
            @Result(property = "professname", column = "professname")
    })
    @Select({SelectTable.PROFESS, "where departid = #{departid}"})
    List<CProfess> getProfessByDepartId(@Param("departid") Integer departid);

    @ResultMap("toProfess")
    @Select({SelectTable.PROFESS, "where id = #{professID}"})
    CProfess getProfessById(@Param("professID") Integer professID);

    /**
     * 检查用户是否已经选题，返回0表示用户尚未选题，返回非零数则为选题的id
     * @param studentID
     * @return
     */
    @Select("select check_title from tb_student where userid = #{studentID}")
    Integer getCheckedTitle(@Param("studentID") Integer studentID);

    /**
     * 根据教师ID查询何其相关的所有学生信息
     * @param teacherID
     * @return
     */
    @ResultMap("toStudent")
    @Select({SelectView.STUDENT, "where id in (select student_id from tb_title_stu where title_id in (select title_id from tb_title where teacher_id = #{teacherID}))"})
    List<CStudent> getTeacherTitleStudents(@Param("teacherID") Integer teacherID);

    @Results(id = "toTeacher", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "account", column = "schid"),
            @Result(property = "name", column = "name"),
            @Result(property = "departName", column = "departname"),
            @Result(property = "discription", column = "discription"),
            @Result(property = "email", column = "mail"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "direction", column = "direction"),
            @Result(property = "protitle", column = "protitle")
    })
    @Select({SelectView.TEACHER, "where phone = #{phoneNumber} limit 1"})
    CTeacher getTeacherByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @ResultMap("toTeacher")
    @Select({SelectView.TEACHER, "where mail = #{email} limit 1"})
    CTeacher getTeacherByEmail(@Param("email") String email);

    @ResultMap("toTeacher")
    @Select({SelectView.TEACHER, "where id = #{teacherID} limit 1"})
    CTeacher getTeacherById(@Param("teacherID") Integer teacherID);

    @ResultMap(value = "toTeacher")
    @Select({SelectView.TEACHER, "where schid = #{workNumber} limit 1"})
    CTeacher getTeacherByWorkNumber(@Param("workNumber") String workNumber);

    @Insert("insert into tb_teacher (userid, protitle,direction) values (#{userid},#{protitle},#{direction})")
    Integer insertProtitleInformationByTeacherId(@Param("userid") Integer userid, @Param("protitle") String protitle, @Param("direction") String direction);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into tb_user (schid, name,departid,phone,mail,identify) values (#{account},#{name},#{departName},#{phone},#{email},0)")
    Integer insertOneStudentToUser(CUser student);

    @Select("select identify from tb_usersecurty where schid = #{account} limit 1")
    Integer getIdentityByAccount(@Param("account") String account);

    @ResultMap(value = "toTeacher")
    @Select({SelectView.TEACHER, "where vi_teacher.id in (select tb_user.id from tb_user where identify = 1 and departid = #{departID})"})
    List<CTeacher> getOneDepartAllTeachers(@Param("departID") Integer departID);

    @Update("update tb_user set name = #{newName} where schid = #{userAccount}")
    Integer changeName(@Param("userAccount") String userAccount, @Param("newName")String newName);

    @Update("update tb_user set discription = #{content} where schid = #{userAccount}")
    Integer changeDescription(@Param("userAccount") String userAccount, @Param("content") String newContent);
}