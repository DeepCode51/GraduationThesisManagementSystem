package cn.jsj.gratuatepager.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
public class CPlan implements Serializable {

    private Integer PlanID;
    private String content;

    private Integer guideTeacherID;

}
