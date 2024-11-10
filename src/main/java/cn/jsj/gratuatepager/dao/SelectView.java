package cn.jsj.gratuatepager.dao;

/**
 * 封装对视图查询的SQL片段，数据库字段发生改变时，减少代码中SQL语句的修改范围
 */
public class SelectView {

    public static final String STUDENT = "select vi_student.* from vi_student";

    public static final String TEACHER = "select vi_teacher.* from vi_teacher";
}