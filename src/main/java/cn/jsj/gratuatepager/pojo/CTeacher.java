package cn.jsj.gratuatepager.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class CTeacher extends CUser implements Serializable {

    //研究方向，研究领域
    private String direction;


    //职称，荣誉称号，头衔
    private String protitle;

    public CTeacher(Integer id, String name, String departName, String discription, String email, String phone, String workNumber, String direction, String protitle) {
        super(id,1,workNumber,name,departName,discription,email,phone);
        this.direction = direction;
        this.protitle = protitle;
    }

}
