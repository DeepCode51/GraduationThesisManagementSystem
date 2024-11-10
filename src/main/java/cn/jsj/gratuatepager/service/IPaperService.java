package cn.jsj.gratuatepager.service;

import cn.jsj.gratuatepager.pojo.CPaper;
import cn.jsj.gratuatepager.pojo.CPlan;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;


@Validated
public interface IPaperService {
    
    List<CPaper> getStudentPapers(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String studentAccount);


    List<CPaper> teacherGetJudgeRecords(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String teacherAccount);


    CPlan teacherJudgePaper(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String teacherAccount, @NotNull@Min(value = 1) Integer paperID, @NotBlank@Length(min = 1,max = 600, message = "字数应小于600字") String judgeStr, boolean passed);

    void setPowerOfDownLoadFile(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String teacherAccount,@NotNull@Min(value = 1) Integer paperID);

    List<CPaper> getTeacherNotJudgePapersByPlanID(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String teacherAccount,@NotNull@Min(value = 1) Integer planID);

    String fileAbsolutePathBuilder(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String studentAccount,@NotNull@Min(value = 1) Integer planID, MultipartFile file);

    void uploadOnePaper(@NotBlank@Length(min = 5,max = 15, message = "账号长度应为5-15") String studentAccount,@NotNull@Min(value = 1) Integer planID,@Length(max = 600, message = "字数应小于600字") String studentWord,@NotNull MultipartFile paper) throws IOException;
}
