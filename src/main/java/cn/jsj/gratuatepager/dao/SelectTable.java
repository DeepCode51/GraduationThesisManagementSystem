package cn.jsj.gratuatepager.dao;

/**
 * 封装对表查询的SQL片段，数据库字段发生改变时，减少代码中SQL语句的修改范围
 *
 */
public class SelectTable {

    public static final String TITLE = "select tb_title.* from tb_title";

    public static final String PROFESS = "select tb_profess.* from tb_profess";

    public static final String PLAN = "select tb_plan.* from tb_plan";

    public static final String PAPER = "select tb_paper.* from tb_paper";

    public static final String DEPART = "select tb_depart.* from tb_depart";

    public static final String USER = "select tb_user.* from tb_user";
}