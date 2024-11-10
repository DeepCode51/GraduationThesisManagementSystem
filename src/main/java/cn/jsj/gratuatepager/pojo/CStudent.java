package cn.jsj.gratuatepager.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class CStudent extends CUser implements Serializable {


    //专业名称
    private String professName;


    public CStudent(Integer id, @NotBlank @Length(min = 2, max = 4) String name, @NotBlank @Length(min = 3, max = 11) String departName, String discription, @NotBlank @Email String email, @NotBlank @Pattern(regexp = "^1[0-9]{10}$") String phone, @NotBlank @Length(min = 5, max = 15) String studentNumber, String professName) {
        super(id,0,studentNumber,name,departName,discription,email,phone);
        this.professName = professName;
    }

}
