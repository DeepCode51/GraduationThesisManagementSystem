package cn.jsj.gratuatepager.service.impl;

import cn.jsj.gratuatepager.dao.IPaperDao;
import cn.jsj.gratuatepager.dao.IPlanDao;
import cn.jsj.gratuatepager.dao.ITitleEntityDao;
import cn.jsj.gratuatepager.dao.IUserEntityDao;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.pojo.*;
import cn.jsj.gratuatepager.service.IPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

@Service
public class CPlanService implements IPlanService {

    @Autowired
    private IUserEntityDao userEntityDao;

    @Autowired
    private IPlanDao planDao;

    @Autowired
    private IPaperDao paperDao;

    @Autowired
    private ITitleEntityDao titleEntityDao;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CPlan> getTeacherPlans(String teacherAccount) {
        CTeacher teacher = this.userEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("信息错误");
        }
        return this.planDao.getPlanListByTeacherID(teacher.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CPlan> getStudentPlans(String studentAccount) {
        CStudent student = this.userEntityDao.getStudentByNumber(studentAccount.trim());
        if(student == null) {
            throw new RuntimeProcessException("用户信息异常");
        }
        CTitle title = this.titleEntityDao.getTitleByStudentID(student.getId());
        if(title == null) {
            throw new RuntimeProcessException("用户尚未选题");
        }
        return this.planDao.getPlanListByTeacherID(title.getGuideTeacher().getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void deleteOnePlanByID(String teacherAccount, Integer planID) {
        CTeacher teacher = this.userEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        Integer teacherid = this.planDao.getGuideTeacherIDByPlanID(planID);
        if(teacherid == null || teacherid<1) {
            throw new RuntimeProcessException("数据异常");
        }
        if(teacher.getId().equals(teacherid)) {
            List<CPaper> papers = this.paperDao.getPaperListByTeacherAndPlanID(teacher.getId(),planID);
            if(papers != null && papers.size()>0) {
                throw new RuntimeProcessException("该审批流程已开始，无法删除");
            }
            this.planDao.deleteOnePlanByID(planID);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void addOnePlan(String teacherAccount, String planContent) {
        CTeacher teacher = this.userEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("信息错误");
        }
        this.planDao.insertOnePlan(teacher.getId(),planContent);
    }


    @Override
    public void duradePlanID(CStudent student, Integer planID, Integer second) {
        this.redisTemplate.opsForValue().set(student.getAccount()+"|"+"choosePlan",planID);
        this.redisTemplate.expire(student.getAccount()+"|"+"choosePlan",second, TimeUnit.SECONDS);
    }

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer getRedisPlanID(String studentAccount) {

        Object value = this.redisTemplate.opsForValue().get(studentAccount.trim()+"|"+"choosePlan");
        if(value == null) {
            throw new RuntimeProcessException("请先选择要提交的审批流程");
        }
        this.redisTemplate.delete(studentAccount.trim()+"|"+"choosePlan");
        return (Integer)value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public boolean checkStudentPlanIsOver(String studentAccount, Integer planID) {
        CStudent student = this.userEntityDao.getStudentByNumber(studentAccount.trim());
        if(student == null) {
            throw new RuntimeProcessException("用户信息有误");
        }
        CPaper paper = this.paperDao.studentGetPassedPaperOfPlan(student.getId(),planID);
        if(paper == null) {
            this.duradePlanID(student,planID,120);
            return false;
        }else {
            return true;
        }
    }
}
