package cn.jsj.gratuatepager.dao;

import cn.jsj.gratuatepager.pojo.CProfess;
import cn.jsj.gratuatepager.pojo.CTitle;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ITitleEntityDao {


    @Results(id = "toTitle", value = {
            @Result(id = true, property = "titleId", column = "title_id"),
            @Result(property = "titleName", column = "title_name"),
            @Result(property = "description", column = "title_description"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "limitTime", column = "limit_time"),
            @Result(property = "limitStudentNumber", column = "limit_stu"),
            @Result(property = "checkStudentNumber", column = "check_stu"),
            @Result(property = "guideTeacher", column = "teacher_id",
            one = @One(select = "cn.jsj.gratuatepager.dao.IUserEntityDao.getTeacherById", fetchType= FetchType.EAGER)),
            @Result(property = "depart", column = "depart_id",
            one = @One(select = "cn.jsj.gratuatepager.dao.IUserEntityDao.getDepartById", fetchType = FetchType.EAGER))
    })
    @Select({SelectTable.TITLE, "where title_id = #{titleID}"})
    CTitle getOneTitleById(@Param("titleID") Integer titleID);

    @ResultMap("toTitle")
    @Select({SelectTable.TITLE, "inner join tb_student on tb_title.title_id = tb_student.check_title where tb_student.userid = #{studentID}"})
    CTitle getTitleByStudentID(@Param("studentID") Integer studentID);

    @Insert("insert into tb_title (title_name, title_description, teacher_id, publish_time, limit_time, depart_id, limit_stu) values (#{titleName},#{titleDescription},#{teacherID},#{publishTime},#{limitTime},#{departID},#{limitNumber})")
    Integer insertOneTitle(@Param("titleName") String titleName, @Param("titleDescription") String titleDescription, @Param("teacherID") Integer teacherID, @Param("publishTime") String publishTime, @Param("limitTime") String limitTime, @Param("departID") Integer departID, @Param("limitNumber") Integer limitNumber);

    @ResultMap("toTitle")
    @Select({SelectTable.TITLE, "where teacher_id = #{teacherID}"})
    List<CTitle> getOneTitleByTeacherID(@Param("teacherID") Integer teacherID);

    @ResultMap(value = "cn.jsj.gratuatepager.dao.IUserEntityDao.toProfess")
    @Select({SelectTable.PROFESS, "inner join tb_title_profess on tb_profess.id = tb_title_profess.profess_id where title_id = #{titleID}"})
    List<CProfess> getProfessListByTitleID(@Param("titleID") Integer titleID);
    
    @Delete("delete from tb_title_profess where title_id = #{titleID}")
    Integer deleteAcceptProfessByTitleId(@Param("titleID") Integer titleID);

    @ResultMap("toTitle")
    @Select({SelectTable.TITLE, "where title_id = (select max(title_id) from tb_title where teacher_id = #{teacherID})"})
    CTitle getLastTitleByTeacher(@Param("teacherID") Integer teacherID);

    @Delete("delete from tb_title where title_id = #{titleID}")
    Integer deleteOneTitleByID(@Param("titleID") Integer titleID);

    @ResultMap("toTitle")
    @Select({SelectTable.TITLE, "where depart_id = #{departID}"})
    List<CTitle> getTitleListByDepartID(@Param("departID") Integer departID);

    @Update("update tb_student set check_title = 0 where userid = #{studentID}")
    Integer cancelTitle(@Param("studentID") Integer studentID);

    @Delete("delete from tb_title_stu where title_id = #{titleID}")
    Integer deleteTitleCheckInformation(@Param("titleID") Integer titleID);

    @Update("update tb_title set check_stu = check_stu - 1 where title_id = #{titleID}")
    Integer cutDownTheNumberOfTitleChecked(@Param("titleID") Integer titleID);

    @Update("update tb_title set check_stu = check_stu + 1 where title_id = #{titleID}")
    Integer addUpTheNumberOfTitleChecked(@Param("titleID") Integer titleID);

    @Select("select limit_stu from tb_title where title_id = #{titleID}")
    Integer getLimitNumberOfTitle(@Param("titleID") Integer titleID);

    @Select("select check_stu from tb_title where title_id = #{titleID}")
    Integer getCheckedNumberOfTitle(@Param("titleID") Integer titleID);

    @Insert("insert into tb_title_stu (title_id, student_id) values (#{titleID},#{studentID})")
    Integer addRecordToTitleStu(@Param("studentID") Integer studentID, @Param("titleID") Integer titleID);

    @Update("update tb_student set check_title = #{titleID} where userid = #{studentID}")
    Integer setStudentCheckStateAsChecked(@Param("studentID") Integer studentID, @Param("titleID") Integer titleID);

    @Delete("delete from tb_title_stu where title_id = #{titleID} and student_id = #{studentID}")
    Integer deleteTItleCheckInformationOfOneStudent(@Param("titleID") Integer titleID,@Param("studentID") Integer studentID);

    @Select("select student_id from tb_title_stu where title_id = #{titleID}")
    List<Integer> getStudentIdOfCheckedTitle(@Param("titleID") Integer titleID);

    @Update("update tb_title set title_name = #{titleName},title_description = #{description}, limit_time = #{overTime}, limit_stu = #{limitNumber} where title_id = #{titleID}")
    Integer updateTitleInformation(@Param("titleID") Integer titleID,@Param("titleName") String titleName, @Param("description") String description,
        @Param("limitNumber") Integer limitNumber,@Param("overTime") Date overTime);

    @Insert("insert into tb_title_profess (profess_id, title_id) values (#{professID}, #{titleID})")
    Integer insertAcceptProfessByTitleId(@Param("titleID") Integer titleID, @Param("professID") Integer professID);
}
