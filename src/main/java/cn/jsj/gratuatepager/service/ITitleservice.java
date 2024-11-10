package cn.jsj.gratuatepager.service;

import cn.jsj.gratuatepager.pojo.CProfess;
import cn.jsj.gratuatepager.pojo.CTitle;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Validated
public interface ITitleservice {

    List<CTitle> getTitleListOfOneTeacher(@NotBlank@Length(min = 5,max = 15) String teacherAccount);

    List<CProfess> getAcceptProfessOfTitle(@NotNull@Min(value = 1) Integer titleID);

    List<HashMap<String,Object>> getTeacherTitleStudents(@NotBlank@Length(min = 5,max = 15) String teacherAccount);

    List<CTitle> getOneDepartTitlesByTeacherAccount(@NotBlank@Length(min = 5,max = 15) String userAccount);

    void addOneTitle(@NotBlank String titleName, @NotBlank String description, @NotBlank@Length(min = 5,max = 15) String workNumber, @NotNull@Future Date limitTime, @NotBlank String professStr,@NotNull@Min(value = 1)@Max(value = 9999) Integer limitNumber);

    void addOneProfessOfTitle(@NotNull@Min(value = 1) Integer titleID,@NotNull@Min(value = 1) Integer professID);

    void deleteOneTitleByAccount(@NotNull@Min(value = 1) Integer titleID,@NotBlank@Length(min = 5,max = 15) String workNumber);

    void updateOneTitle(@NotBlank String account,@NotNull@Min(value = 1) Integer titleID,@NotBlank String titleName, @NotBlank String description,@Future Date limitTime, @NotNull@Min(value = 1)@Max(value = 9999) Integer limitNumber,String professstr);
    
}
