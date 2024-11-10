package cn.jsj.gratuatepager.service.impl;

import cn.jsj.gratuatepager.dao.IPaperDao;
import cn.jsj.gratuatepager.dao.IPlanDao;
import cn.jsj.gratuatepager.dao.ITitleEntityDao;
import cn.jsj.gratuatepager.dao.IUserEntityDao;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.pojo.*;
import cn.jsj.gratuatepager.service.IPaperService;
import cn.jsj.gratuatepager.tools.FileCopyTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;


@Service
public class CPaperService implements IPaperService {

    @Autowired
    private IUserEntityDao iUserEntityDao;

    @Autowired
    private IPaperDao paperDao;

    @Value("${downloadPowerTimeSecond}")
    private Integer downloadPowerTimeSecond;

    @Autowired
    private IPlanDao planDao;

    @Autowired
    private FileCopyTool fileCopyTool;

    @Autowired
    private ITitleEntityDao titleEntityDao;

    @Autowired
    private CIOservice ciOservice;

    @Value("${paper.storage-path}")
    private String paperStoragePath;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CPaper> getStudentPapers(String studentAccount) {
        CStudent student = this.iUserEntityDao.getStudentByNumber(studentAccount.trim());
        if(student == null) {
            throw new RuntimeProcessException("用户身份异常");
        }
        return this.paperDao.getPaperListByStudentID(student.getId());
    }



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CPaper> teacherGetJudgeRecords(String teacherAccount) {
        CTeacher teacher = this.iUserEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("用户身份错误");
        }
        return this.paperDao.getPaperRecordOfTeacher(teacher.getId());
    }



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public CPlan teacherJudgePaper(String teacherAccount,Integer paperID, String judgeStr, boolean passed) {
        CTeacher teacher = this.iUserEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("用户身份错误");
        }
        CPaper paper = this.paperDao.getOnePaperByPaperID(paperID);
        if(paper == null) {
            throw new RuntimeProcessException("参数无效");
        }
        if(!teacher.getId().equals(paper.getGuideTeacher().getId())) {
            throw new RuntimeProcessException("不可告人的错误");
        }
        Date nowdate = new Date();
        Integer passState = 0;
        String passInfor = "未通过";
        if(passed == true) {
            passState = 1;
            passInfor = "已通过";
        }
        Integer result = this.paperDao.teacherJudgeOnePaper(paper.getPaperID(),judgeStr.trim(),nowdate,passState);
        if(result == 0) {
            throw new RuntimeProcessException("该作品已经完成审阅，请选择其他作品，或刷新页面");
        }
        this.ciOservice.postEmail("您的一个作品"+passInfor, paper.getSubmitStudent().getName()+"同学：您的作品已经完成评审。"+paper.getGuideTeacher().getName()+" 老师:"+judgeStr.trim(),paper.getSubmitStudent().getEmail());
        if(passed == false) {
            File file = new File(this.paperStoragePath+paper.getSubmitStudent().getAccount()+"/history");
            if(!file.exists()) {
                file.mkdirs();
            }
            if(file.isDirectory()) {
                String temp = paper.getPaperPath();
                String[] spaths = temp.split("\\.");
                try {
                    this.fileCopyTool.copyFile(new File(this.paperStoragePath+temp),new File(this.paperStoragePath+paper.getSubmitStudent().getAccount()+"/history/"+paper.getPaperID()+"."+spaths[spaths.length-1]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return paper.getExecutePlan();
    }



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void setPowerOfDownLoadFile(String teacherAccount, Integer paperID) {
        CTeacher teacher = this.iUserEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("用户身份错误");
        }
        CPaper paper = this.paperDao.getOnePaperByPaperID(paperID);
        if(paper == null) {
            throw new RuntimeProcessException("参数不合法");
        }
        if(!paper.getGuideTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeProcessException("用户身份异常");
        }
        this.redisTemplate.opsForValue().set(teacher.getAccount()+"|teacherDownload",paperID);
        this.redisTemplate.expire(teacher.getAccount()+"|teacherDownload",this.downloadPowerTimeSecond, TimeUnit.SECONDS);
    }



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CPaper> getTeacherNotJudgePapersByPlanID(String teacherAccount, Integer planID) {
        CTeacher teacher = this.iUserEntityDao.getTeacherByWorkNumber(teacherAccount.trim());
        if(teacher == null) {
            throw new RuntimeProcessException("用户身份异常");
        }
        return this.paperDao.getNotJudgePaperByTeacherAndPlanID(teacher.getId(),planID);
    }



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public String fileAbsolutePathBuilder(String studentAccount, Integer planID,MultipartFile file) {
        String result = null;
        if(file != null) {
            String fileName = file.getOriginalFilename();
            String[] fileNames = fileName.split("\\.");
            result = studentAccount.trim()+"/"+planID+"."+fileNames[fileNames.length-1];
        }else {
            result = studentAccount.trim()+"/"+planID;
        }
        return result;
    }



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void uploadOnePaper(String studentAccount, Integer planID, String studentWord,MultipartFile paper) throws IOException {
        if(paper == null || paper.isEmpty() || paper.getSize() == 0) {
            throw new RuntimeProcessException("请上传文件");
        }
        CStudent student = this.iUserEntityDao.getStudentByNumber(studentAccount.trim());
        if(student == null) {
            throw new RuntimeProcessException("用户身份错误");
        }
        CTitle title = this.titleEntityDao.getTitleByStudentID(student.getId());
        if(title == null) {
            throw  new RuntimeProcessException("用户选题信息有误");
        }
        Integer teacherID = this.planDao.getGuideTeacherIDByPlanID(planID);
        if(teacherID == null || teacherID<1 || (!teacherID.equals(title.getGuideTeacher().getId()))) {
            throw new RuntimeProcessException("请重新选择流程信息");
        }
        CPaper paperEntity = this.paperDao.studentGetPassedPaperOfPlan(student.getId(),planID);
        if(paperEntity != null) {
            throw new RuntimeProcessException("学生已通过该审批流程，无需再次提交");
        }
        Date nowDate = new Date();
        String absolutePath = this.fileAbsolutePathBuilder(student.getAccount(),planID,paper);//key
        File file = new File(this.paperStoragePath+student.getAccount());
        if(!file.exists()) {
            file.mkdir();
        }else {
            File[] lastfiles = file.listFiles();
            if(lastfiles != null) {
                for(File k: lastfiles) {
                    if(k.isDirectory()) {
                        continue;
                    }
                    String kname = k.getName();
                    String[] teem = kname.split("\\.");
                    if(teem[0].equals(planID+"")) {
                        k.delete();
                    }
                }
            }
        }
        file = new File(this.paperStoragePath+absolutePath);
        CPaper oldpaper = this.paperDao.getNotJudgePaperByStudentAndPlanID(student.getId(),planID);
        if(oldpaper != null) {
            File oldfeile = new File(this.paperStoragePath+oldpaper.getPaperPath());
            if(oldfeile.exists()) {
                oldfeile.delete();
            }
            this.paperDao.deleteOnePaper(oldpaper.getPaperID());
        }
        paper.transferTo(file);
        this.paperDao.insertOnePaper(student.getId(),title.getGuideTeacher().getId(),planID,studentWord.trim(),absolutePath,nowDate);
        this.ciOservice.postEmail(student.getName()+"同学已提交论文",student.getName()+"刚刚提交了论文："+studentWord.trim(),title.getGuideTeacher().getEmail());
    }

}