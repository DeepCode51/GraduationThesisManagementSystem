package cn.jsj.gratuatepager.service.impl;

import cn.jsj.gratuatepager.dao.IPaperDao;
import cn.jsj.gratuatepager.dao.ITitleEntityDao;
import cn.jsj.gratuatepager.dao.IUserEntityDao;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.interceptor.censor.TokenCensor;
import cn.jsj.gratuatepager.pojo.*;
import cn.jsj.gratuatepager.service.IIOservice;
import cn.jsj.gratuatepager.service.IUserservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

@Service
public class CUserService implements IUserservice {

    @Value("${securitycodetime}")
    private Integer securitysecond;

    @Value("${login-available-time}")
    private Integer loginAvailableTime;//设置无操作登录有效时长

    @Autowired
    private IPaperDao paperDao;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private IUserEntityDao iUserEntityDao;

    @Autowired
    private ITitleEntityDao titleEntityDao;

    @Autowired
    private IIOservice iiOservice;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public CStudent getOneStudentByID(Integer id) {
        CStudent student = this.iUserEntityDao.getStudentById(id);
        return student;
    }

    @Override
    public CStudent getOneStudentByNumber(String number) {
        CStudent student = this.iUserEntityDao.getStudentByNumber(number);
        return  student;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public CTeacher getOneTeacherByNumber(String account) {
        CTeacher teacher = this.iUserEntityDao.getTeacherByWorkNumber(account);
        return teacher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CProfess> getProfessByDepartId(Integer departid) {
        List<CProfess> storage = this.iUserEntityDao.getProfessByDepartId(departid);
        return storage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public Map<String,String> registerOneStudent(String studentNumber,String password,String email,String phoneNumber,String name,Integer departid,String code, Integer professid) {
        CStudent student = new CStudent(0,name.trim(),departid+"","",email.trim(),phoneNumber.trim(),studentNumber.trim(),professid+"");
        boolean ready = false;
        ready = this.checkResistorCode(student.getAccount(), code.trim());
        if (ready == false) {
            throw new RuntimeProcessException("验证码错误");
        }
        CStudent studenttest = null;
        studenttest = this.iUserEntityDao.getStudentByPhoneNumber(student.getPhone());
        if(studenttest != null) {
            throw new RuntimeProcessException("手机号已注册");
        }
        studenttest = this.iUserEntityDao.getStudentByEmail(student.getEmail());
        if(studenttest != null) {
            throw  new RuntimeProcessException("邮箱已经注册");
        }
        Integer flag = this.iUserEntityDao.getUserIDByNumber(student.getAccount());
        if (flag != null && flag > 0) {
            throw new RuntimeProcessException("学号已注册");
        }
        int tem = Integer.parseInt(student.getDepartName());
        CDepart depart = this.iUserEntityDao.getDepartById(tem);
        if(depart == null) {
            throw new RuntimeProcessException("注册信息有误");
        }
        this.iUserEntityDao.insertOneStudentToUser(student);
        this.iUserEntityDao.insertIntoSecurity(student.getAccount(), password.trim(),0, student.getId());
        CProfess profess = this.iUserEntityDao.getProfessById(Integer.parseInt(student.getProfessName()));
        if(profess == null) {
            throw new RuntimeProcessException("注册信息有误");
        }
        this.iUserEntityDao.insertProfessInformationByStudentId(student.getId(),Integer.parseInt(student.getProfessName()));
        Map<String,String> storage = this.login(student.getAccount(),password.trim());
        return storage;
    }

    @Override
    public List<CDepart> getDeparts() {
        return this.iUserEntityDao.getdepartList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public CTitle getCheckedTitleOfStudent(String studentAccount) {
        CStudent student = this.iUserEntityDao.getStudentByNumber(studentAccount.trim());
        Integer titleID = this.iUserEntityDao.getCheckedTitle(student.getId());
        if(titleID == 0) {
            return null;
        }
        CTitle title = this.titleEntityDao.getOneTitleById(titleID);
        return title;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void cancelTitleCheck(String studentAccount) {
        CTitle title = this.getCheckedTitleOfStudent(studentAccount.trim());
        if(title == null) {
            throw new RuntimeProcessException("用户尚未选题");
        }
        CStudent student = this.iUserEntityDao.getStudentByNumber(studentAccount.trim());
        this.titleEntityDao.cancelTitle(student.getId());
        this.titleEntityDao.deleteTItleCheckInformationOfOneStudent(title.getTitleId(),student.getId());
        this.titleEntityDao.cutDownTheNumberOfTitleChecked(title.getTitleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void teacherCancelStudentTitleCheck(String teacherAccount, String studentAccount) {
        CStudent student = this.iUserEntityDao.getStudentByNumber(studentAccount.trim());
        if(student == null) {
            throw new RuntimeProcessException("用户信息有误");
        }
        CTitle title = this.titleEntityDao.getTitleByStudentID(student.getId());
        if(title == null) {
            throw new RuntimeProcessException("用户信息有误");
        }
        CPaper paper = this.paperDao.getOneRandomPaperByStudentID(student.getId());
        if(paper != null) {
            throw new RuntimeProcessException("该同学已经开始本题目，无法强制退选");
        }
        if(title.getGuideTeacher().getAccount().equals(teacherAccount.trim())) {
            this.cancelTitleCheck(student.getAccount());
            this.iiOservice.postEmail("强制退选通知","您的选题："+title.getTitleName()+" 已经被指导教师强制退选，请重新选题",student.getEmail());
        }else{
            throw new RuntimeProcessException("用户信息有误");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void checkTitle(String studentAccount, Integer titleID) {
        CStudent student = this.iUserEntityDao.getStudentByNumber(studentAccount.trim());
        CTitle title = this.titleEntityDao.getOneTitleById(titleID);
        if(student == null || title == null) {
            throw new RuntimeProcessException("信息有误");
        }
        Integer titlestate = this.iUserEntityDao.getCheckedTitle(student.getId());
        if(titlestate != 0) {
            throw new RuntimeProcessException("用户已有选择的题目");
        }
        List<CProfess> candidantesProfess = this.titleEntityDao.getProfessListByTitleID(title.getTitleId());
        String professName = student.getProfessName();
        boolean tkey = false;
        for(CProfess i:candidantesProfess) {
            if(i.getProfessname().equals(professName)) {
                tkey = true;
                break;
            }
        }
        if(tkey == false) {
            throw new RuntimeProcessException("您的专业不符合该选题要求");
        }
        Integer limit = this.titleEntityDao.getLimitNumberOfTitle(title.getTitleId());
        Integer checked = this.titleEntityDao.getCheckedNumberOfTitle(title.getTitleId());
        if(limit <= checked) {
            throw new RuntimeProcessException("该选题人数已满，请选择其他题目");
        }
        this.titleEntityDao.setStudentCheckStateAsChecked(student.getId(),title.getTitleId());
        this.titleEntityDao.addRecordToTitleStu(student.getId(),title.getTitleId());
        this.titleEntityDao.addUpTheNumberOfTitleChecked(title.getTitleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CProfess> getOneTeacherProfess(String workNumber) {
        return this.iUserEntityDao.getoringProfessOfOneTeacher(workNumber.trim());
    }

    @Override
    public String productsecuritycode(String account, String email) {
        boolean exitscode = this.redisTemplate.hasKey(account.trim());
        if(exitscode == true) {
            throw new RuntimeProcessException("验证太过频繁，请稍后再试");
        }
        String se = UUID.randomUUID().toString();
        StringBuffer secode = new StringBuffer("");
        for(int i = 0;i<=5;i++) {
            secode.append(se.charAt(se.length()/6*i));
        }
        this.redisTemplate.opsForValue().set(account.trim(), secode.toString());
        this.redisTemplate.expire(account.trim(),this.securitysecond, TimeUnit.SECONDS);
        this.iiOservice.postEmail("论文管理系统注册验证码","验证码："+secode+" 请在"+this.securitysecond+"秒内完成验证。",email.trim());
        return secode.toString();
    }

    @Override
    public boolean checkResistorCode(String account, String code) {
        Object o = this.redisTemplate.opsForValue().get(account.trim());
        boolean exist = this.redisTemplate.hasKey(account.trim());
        if(exist == false) {
            return false;
        }
        String codecy = (String)o;
        this.redisTemplate.delete(account.trim());//已经验证过一次的验证码，不管成功与否，立刻失效。
        if(code.trim().equals(codecy.trim())) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public List<CTeacher> getAllTeacherOfOneDepart(String  departName) {
        Integer departID = this.iUserEntityDao.getdepartIdByName(departName);
        CDepart depart = this.iUserEntityDao.getDepartById(departID);
        if(depart == null) {
            throw new RuntimeProcessException("院系信息有误");
        }
        return this.iUserEntityDao.getOneDepartAllTeachers(depart.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void userReDescription(String userAccount, String newContent) {
        this.iUserEntityDao.changeDescription(userAccount.trim(), newContent.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void userReName(String userAccount, String newName) {
        this.iUserEntityDao.changeName(userAccount.trim(), newName.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public Map<String, String> login(String account, String password) {
        CpasswordBoot cpasswordBoot = this.iUserEntityDao.getpasswordBoot(account.trim());
        if(cpasswordBoot == null) {
            throw new RuntimeProcessException("登录失败，请检查账号密码是否正确");
        }
        if(password.trim().equals(cpasswordBoot.getUserPassword())) {
            //String token = UUID.randomUUID().toString();
            String[] tokenInfo = TokenCensor.getToken(cpasswordBoot.getUserId(), cpasswordBoot.getUserAccount());
            String token = tokenInfo[0];
            String tempPasword = tokenInfo[1];
            this.redisTemplate.opsForValue().set(token, tempPasword);
            this.redisTemplate.expire(token,this.loginAvailableTime,TimeUnit.SECONDS);
            Map<String,String> result = new HashMap<>();
            result.put("identity",cpasswordBoot.getUserIdentity().toString());
            result.put("token", token);
            return result;
        }else{
            throw new RuntimeProcessException("登陆失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public Map<String, String> registerOneTeacher(String teacherNumber,String password, String email, String phoneNumber, String name, Integer departid, String code, String protitle, String direction) {
        CTeacher teacher = new CTeacher(0,name.trim(),departid+"","",email.trim(),phoneNumber.trim(),teacherNumber.trim(),direction.trim(),protitle.trim());
        boolean ready = false;
        ready = this.checkResistorCode(teacher.getAccount(), code.trim());
        if (ready == false) {
            throw new RuntimeProcessException("验证码错误");
        }
        CTeacher teachertest = null;
        teachertest = this.iUserEntityDao.getTeacherByPhoneNumber(teacher.getPhone());
        if(teachertest != null) {
            throw new RuntimeProcessException("您输入的手机号已注册");
        }
        teachertest = this.iUserEntityDao.getTeacherByEmail(teacher.getEmail());
        if(teachertest != null) {
            throw  new RuntimeProcessException("您输入的邮箱已经注册");
        }
        Integer flag = this.iUserEntityDao.getUserIDByNumber(teacher.getAccount());
        if (flag != null && flag > 0) {
            throw new RuntimeProcessException("您输入的工号已注册");
        }
        CDepart depart = this.iUserEntityDao.getDepartById(Integer.parseInt(teacher.getDepartName()));
        if(depart == null) {
            throw new RuntimeProcessException("您的注册信息无效");
        }
        this.iUserEntityDao.insertOneTeacherToUser(teacher);
        this.iUserEntityDao.insertIntoSecurity(teacher.getAccount(), password.trim(),1, teacher.getId());
        this.iUserEntityDao.insertProtitleInformationByTeacherId(teacher.getId(),protitle.trim(),direction.trim());
        Map<String,String> storage = this.login(teacher.getAccount(),password.trim());
        return storage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public String judgeIdentityByAccount(String account) {
        Integer sto = this.iUserEntityDao.getIdentityByAccount(account);
        if(sto == null) {
            throw new RuntimeProcessException("无效用户账号");
        }
        if(sto == 0) {
            return "student";
        }
        return "teacher";
    }

}
