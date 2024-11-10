package cn.jsj.gratuatepager.service;

import cn.jsj.gratuatepager.pojo.CPlan;
import cn.jsj.gratuatepager.pojo.CStudent;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
public interface IPlanService {


    List<CPlan> getTeacherPlans(@NotBlank@Length(min = 5,max = 15) String teacherAccount);

    List<CPlan> getStudentPlans(@NotBlank@Length(min = 5,max = 15) String studentAccount);

    void deleteOnePlanByID(@NotBlank@Length(min = 5,max = 15)String teacherAccount,@NotNull@Min(value = 1) Integer planID);

    void addOnePlan(@NotBlank@Length(min = 5,max = 15) String teacherAccount,@NotBlank String planContent);
    
    Integer getRedisPlanID(@NotBlank@Length(min = 5,max = 15) String studentAccount);

    void duradePlanID(@NotNull CStudent student,@NotNull@Min(value = 1) Integer planID,@NotNull@Min(value = 1) Integer second);

    /**
     * 检查学生用户的某一执行计划是否已经完成，如果用户暂时没有执行计划或者没有选题，则返回true。
     * @param studentAccount
     * @param planID
     * @return
     */
    boolean checkStudentPlanIsOver(@NotBlank@Length(min = 5,max = 15) String studentAccount,@NotNull@Min(value = 1) Integer planID);
}
