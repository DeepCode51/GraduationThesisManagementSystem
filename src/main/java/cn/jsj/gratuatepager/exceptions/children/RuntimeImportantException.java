package cn.jsj.gratuatepager.exceptions.children;

import cn.jsj.gratuatepager.exceptions.CustomRuntimeException;

public class RuntimeImportantException extends CustomRuntimeException {
    public RuntimeImportantException(String message) {
        super(message);// 父类的构造函数；调用底层的Throwable的构造函数，将参数message赋值到detailMessage (Throwable的属性)
    }
}
