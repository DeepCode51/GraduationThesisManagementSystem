package cn.jsj.gratuatepager.dao;

import cn.jsj.gratuatepager.pojo.CPlan;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPlanDao {

    @Insert("insert into tb_plan (teacher_id, plan_content) values (#{teacerID},#{planContent})")
    Integer insertOnePlan(@Param("teacerID") Integer teacerID, @Param("planContent") String planContent);

    @Delete("delete from tb_plan where plan_id = #{planID}")
    Integer deleteOnePlanByID(@Param("planID") Integer planID);

    @Results(id = "toPlan", value = {
            @Result(id = true, property = "PlanID", column = "plan_id"),
            @Result(property = "guideTeacherID", column = "teacher_id"),
            @Result(property = "content",column = "plan_content")
    })
    @Select({SelectTable.PLAN, "where teacher_id = #{teacherID}"})
    List<CPlan> getPlanListByTeacherID(@Param("teacherID") Integer teacherID);

    /**
     * 不可删除
     * @param planID
     * @return
     */
    @ResultMap("toPlan")
    @Select({SelectTable.PLAN, "where plan_id = #{planID}"})
    CPlan getOnePlanByID(@Param("planID") Integer planID);

    @Select("select teacher_id from tb_plan where plan_id = #{planID}")
    Integer getGuideTeacherIDByPlanID(@Param("planID") Integer planID);
}