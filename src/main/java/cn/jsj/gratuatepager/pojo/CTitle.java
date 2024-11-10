package cn.jsj.gratuatepager.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CTitle {

    private Integer titleId;//题目id


    private String titleName;//题目名称


    private String description;//教师要求，题目简介

    
    private Date publishTime;//发布时间


    private Date limitTime;//截止时间


    private CTeacher guideTeacher;


    private Integer limitStudentNumber;

    private CDepart depart;

    private List<CProfess> acceptProfess;

    private Integer checkStudentNumber;

}
