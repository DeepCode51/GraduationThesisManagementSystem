package cn.jsj.gratuatepager.service.impl;

import cn.jsj.gratuatepager.dao.IPaperDao;
import cn.jsj.gratuatepager.dao.IPlanDao;
import cn.jsj.gratuatepager.dao.ITitleEntityDao;
import cn.jsj.gratuatepager.dao.IUserEntityDao;
import cn.jsj.gratuatepager.exceptions.children.RuntimeImportantException;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.pojo.*;
import cn.jsj.gratuatepager.service.IIOservice;
import cn.jsj.gratuatepager.service.ITitleservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;


@Service
public class CTitleserver implements ITitleservice {


    @Autowired
    private IUserEntityDao userEntityDao;

    @Autowired
    private ITitleEntityDao titleEntityDao;

    @Autowired
    private IPaperDao paperDao;

    @Autowired
    private IPlanDao planDao;

    @Autowired
    private IIOservice iiOservice;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CTitle> getTitleListOfOneTeacher(String teacherAccount) {
        String workNumber = teacherAccount.trim();
        CTeacher teacher = this.userEntityDao.getTeacherByWorkNumber(workNumber);
        if(teacher == null) {
            throw  new RuntimeProcessException("用户信息无效");
        }
        List<CTitle> titles = this.titleEntityDao.getOneTitleByTeacherID(teacher.getId());
        return titles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CProfess> getAcceptProfessOfTitle(Integer titleID) {
        List<CProfess> professes = this.titleEntityDao.getProfessListByTitleID(titleID);
        return professes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<HashMap<String, Object>> getTeacherTitleStudents(String teacherAccount) {
        List<HashMap<String,Object>> storage = new LinkedList<HashMap<String, Object>>();
        CTeacher teacher = this.userEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        List<CStudent> students = this.userEntityDao.getTeacherTitleStudents(teacher.getId());
        for(CStudent i:students) {
            CTitle title = this.titleEntityDao.getTitleByStudentID(i.getId());
            List<CPlan> planlist = this.planDao.getPlanListByTeacherID(teacher.getId());
            boolean over = true;
            if(planlist !=null && planlist.size() > 0) {
                for(CPlan j:planlist) {
                    CPaper paper = this.paperDao.studentGetPassedPaperOfPlan(i.getId(),j.getPlanID());
                    if(paper == null) {
                        over = false;
                        break;
                    }
                }
            }
            HashMap<String,Object> tem = new HashMap<>();
            tem.put("student",i);
            tem.put("title", title);
            tem.put("finished",over);
            storage.add(tem);
        }
        return storage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CTitle> getOneDepartTitlesByTeacherAccount(String userAccount) {
        CUser user = this.userEntityDao.getUserByNumber(userAccount.trim());
        if(user == null) {
            throw new RuntimeProcessException("信息有误");
        }
        String departName = user.getDepartName();
        Integer departID = this.userEntityDao.getdepartIdByName(departName);
        return this.titleEntityDao.getTitleListByDepartID(departID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void addOneTitle(String titleName, String description, String workNumber, Date limitTime, String professStr, Integer limitNumber) {
        CTeacher teacher = this.userEntityDao.getTeacherByWorkNumber(workNumber.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("身份信息有误");
        }
        Date nowdate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = formatter.format(nowdate);
        String overTime = formatter.format(limitTime);
        String[] temp = professStr.trim().split(" ");
        LinkedList<Integer> storage = new LinkedList<>();
        for(String i: temp) {
            storage.add(Integer.parseInt(i));
        }
        Integer departid = this.userEntityDao.getdepartIdByName(teacher.getDepartName());
        this.titleEntityDao.insertOneTitle(titleName,description,teacher.getId(),startTime,overTime,departid,limitNumber);
        CTitle thistitle = this.titleEntityDao.getLastTitleByTeacher(teacher.getId());
        if(thistitle == null) {
            throw new RuntimeProcessException("程序有误");
        }
        for(Integer i:storage) {
            this.addOneProfessOfTitle(thistitle.getTitleId(),i);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void addOneProfessOfTitle(Integer titleID, Integer professID) {
        CProfess profess = this.userEntityDao.getProfessById(professID);
        if(profess == null) {
            throw new RuntimeProcessException("参数无效");
        }
        this.titleEntityDao.insertAcceptProfessByTitleId(titleID,professID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void deleteOneTitleByAccount(Integer titleID, String workNumber) {
        CTitle title = this.titleEntityDao.getOneTitleById(titleID);
        if(title == null) {
            throw new RuntimeProcessException("题目不存在");
        }
        String titleAccount = title.getGuideTeacher().getAccount();
        if(workNumber.equals(titleAccount)) {
            List<Integer> studentIDlist = this.titleEntityDao.getStudentIdOfCheckedTitle(titleID);
            if(studentIDlist != null && studentIDlist.size()!=0) {
                for(Integer i: studentIDlist) {
                    int t = this.titleEntityDao.cancelTitle(i);
                    if(t != 1) {
                        throw new RuntimeImportantException("教师删除题目时发生重大错误");
                    }
                }
                for(Integer i: studentIDlist) {
                    CStudent student = this.userEntityDao.getStudentById(i);
                    this.iiOservice.postEmail("选题失效通知","您的选题已经被指导老师删除请及时重新选题",student.getEmail());
                }
            }
            this.titleEntityDao.deleteAcceptProfessByTitleId(titleID);
            this.titleEntityDao.deleteTitleCheckInformation(titleID);
            this.titleEntityDao.deleteOneTitleByID(titleID);
        }else{
            throw new RuntimeProcessException("用户信息错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void updateOneTitle(String account,Integer titleID, String titleName, String description, Date limitTime, Integer limitNumber, String professStr) {

        CTitle title = this.titleEntityDao.getOneTitleById(titleID);
        if(title == null) {
            throw new RuntimeProcessException("信息不存在");
        }
        String titleaccount = title.getGuideTeacher().getAccount();
        if(!account.trim().equals(titleaccount)) {
            throw new RuntimeProcessException("用户信息有误");
        }
        LinkedList<Integer> profess = new LinkedList<>();
        if(professStr == null || professStr.trim().length() == 0) {
            List<CProfess> acprofess = this.titleEntityDao.getProfessListByTitleID(titleID);
            for(CProfess p: acprofess) {
                profess.add(p.getId());
            }
        }else{
            String[] pros = professStr.split(" ");
            for(String i:pros) {
                profess.add(Integer.parseInt(i));
            }
        }
        this.titleEntityDao.deleteAcceptProfessByTitleId(title.getTitleId());
        this.titleEntityDao.updateTitleInformation(title.getTitleId(),titleName.trim(),description.trim(),limitNumber,limitTime);
        for(Integer i: profess) {
            this.addOneProfessOfTitle(title.getTitleId(),i);
        }
    }
}
