package cn.jsj.gratuatepager.dao;

import cn.jsj.gratuatepager.pojo.CPaper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface IPaperDao {

    /**
     * 根据论文Id查询论文实体对象
     *
     * @param paperID   论文Id
     * @return  论文实体对象
     */
    @Results(id = "toPaper", value = {
            @Result(id = true, property = "paperID", column = "paper_id"),
            @Result(property = "submitStudent", column = "student_id",
            one = @One(select = "cn.jsj.gratuatepager.dao.IUserEntityDao.getStudentById",fetchType = FetchType.EAGER)),
            @Result(property = "guideTeacher",column = "teacher_id",
            one = @One(select = "cn.jsj.gratuatepager.dao.IUserEntityDao.getTeacherById",fetchType = FetchType.EAGER)),
            @Result(property = "executePlan", column = "plan_id",
            one = @One(select = "cn.jsj.gratuatepager.dao.IPlanDao.getOnePlanByID", fetchType = FetchType.EAGER)),
            @Result(property = "checkState",column = "check_state"),
            @Result(property = "studentWord",column = "student_tol"),
            @Result(property = "teacherWord", column = "teacher_tol"),
            @Result(property = "paperPath",column = "paper_path"),
            @Result(property = "passState", column = "pass_state"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "judgeTime", column = "judge_time")
    })
    @Select({SelectTable.PAPER, "where paper_id = #{paperID} limit 1"})
    CPaper getOnePaperByPaperID(@Param("paperID") Integer paperID);


    @Update("update tb_paper set teacher_tol = #{judgeWord}, pass_state = #{passState},check_state = 1, judge_time = #{judgeTime} where paper_id = #{paperID} and check_state = 0")
    Integer teacherJudgeOnePaper(@Param("paperID") Integer paperID,@Param("judgeWord") String judgeWord,@Param("judgeTime") Date judgeTime, @Param("passState") Integer passState);


    /**
     * 获取用户已经提交但是尚未经过老师审批的作品。
     * @param studentID
     * @param planID
     * @return
     */
    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where student_id = #{studentID} and plan_id = #{planID} and check_state = 0 limit 1"})
    CPaper getNotJudgePaperByStudentAndPlanID(@Param("studentID") Integer studentID,@Param("planID") Integer planID);

    /**
     * 获取和教师相关的某一流程的所有等待审批的作品。
     * @param teacherID
     * @param planID
     * @return
     */
    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where teacher_id = #{teacherID} and check_state = 0 and plan_id = #{planID}"})
    List<CPaper> getNotJudgePaperByTeacherAndPlanID(@Param("teacherID") Integer teacherID, @Param("planID") Integer planID);

    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where teacher_id = #{teacherID} and check_state in (0,1,2)"})
    List<CPaper> getPaperListByTeacherID(@Param("teacherID") Integer teacherID);

    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where teacher_id = #{teacherID} and plan_id = #{planID} and check_state in (0,1,2)"})
    List<CPaper> getPaperListByTeacherAndPlanID(@Param("teacherID") Integer teacherID, @Param("planID") Integer planID);

    /**
     * 删除数据库中的一个论文对象，删除时需要保证论文对象处于审批未通过的状态。已经通过的论文无法删除。
     * @param paperID
     * @return
     */
    @Delete("delete from tb_paper where paper_id = #{paperID} and pass_state = 0")
    Integer deleteOnePaper(@Param("paperID") Integer paperID);
    

    /**
     * 根据学生主键，查询任意一条论文对象，本接口主要用于检查学生是否已经有上交的论文。
     * @param studentID
     * @return
     */
    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where student_id = #{studentID} limit 1"})
    CPaper getOneRandomPaperByStudentID(@Param("studentID") Integer studentID);

    /**
     * 根据教师ID获取该指导教师已经完成审批的所有论文对象。
     * @param teacherID
     * @return
     */
    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where teacher_id = #{teacherID} and check_state in (1,3)"})
    List<CPaper> getPaperRecordOfTeacher(@Param("teacherID") Integer teacherID);

    /**
     * 获取学生提交的左右作品，包括已经审批的，和未审批的。
     * @param studentID
     * @return
     */
    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where student_id = #{studentID} and check_state in (0,1,3)"})
    List<CPaper> getPaperListByStudentID(@Param("studentID") Integer studentID);


    @ResultMap("toPaper")
    @Select({SelectTable.PAPER, "where student_id = #{studentID} and pass_state = 1 and plan_id = #{planID}"})
    CPaper studentGetPassedPaperOfPlan(@Param("studentID") Integer studentID,@Param("planID") Integer planID);

    @Insert("insert into tb_paper (student_id, plan_id, teacher_id, student_tol, paper_path, submit_time, judge_time) values (#{studentID},#{planID}, #{teacherID},#{studentWord},#{paperPath},#{submitTime}, #{submitTime})")
    public void insertOnePaper(
        @Param("studentID") Integer studentID, @Param("teacherID") Integer teacherID,
        @Param("planID") Integer planID, @Param("studentWord") String studentWord, @Param("paperPath") String paperPath,
        @Param("submitTime") Date submitTime);

    
}
