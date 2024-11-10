package cn.jsj.gratuatepager.service;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Validated
public interface IIOservice {

    /**
     * 封装的邮件发送接口。
     * @param title 邮件标题
     * @param content 邮件内容
     * @param aim 收信人
     */
    void postEmail(@NotBlank String title, @NotBlank String content,@NotBlank @Email String aim);

    /**
     * 教师下载指导的学生提交的，但是已经完成审批的论文。
     * @param teacherNumber
     * @param paperID
     * @param response
     */
    void teacherDownLoadRecordPaper(@NotBlank@Length(min = 5,max = 15) String teacherNumber,@NotNull@Min(value = 1) Integer paperID, HttpServletResponse response);

    /**
     * 教师下载指导的学生提交的但是尚未审批的论文。
     * @param teacherNumber
     * @param paperID
     */
    void teacherDownLoadStudentPaper(@NotBlank@Length(min = 5,max = 15) String teacherNumber,@NotNull@Min(value = 1) Integer paperID, HttpServletResponse response);
}
