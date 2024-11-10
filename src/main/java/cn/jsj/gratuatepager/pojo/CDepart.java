package cn.jsj.gratuatepager.pojo;

import cn.jsj.gratuatepager.tools.CPublicJsonTranslator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CDepart {
    private int id;
    private String departName;

    @Override
    public String toString() {
        return CPublicJsonTranslator.translateWithClassName(this);
    }

}
