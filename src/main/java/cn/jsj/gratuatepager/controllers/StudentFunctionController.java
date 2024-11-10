package cn.jsj.gratuatepager.controllers;

import cn.jsj.gratuatepager.aop.RateLimiter;
import cn.jsj.gratuatepager.interceptor.RequestThreadContext;
import cn.jsj.gratuatepager.interceptor.censor.UserLoginToken;
import cn.jsj.gratuatepager.pojo.CStudent;
import cn.jsj.gratuatepager.pojo.CTeacher;
import cn.jsj.gratuatepager.pojo.CTitle;
import cn.jsj.gratuatepager.service.IUserservice;
import cn.jsj.gratuatepager.tools.HashMapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@ResponseBody
public class StudentFunctionController {

    @Autowired
    private IUserservice iUserservice;


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "start", method = {RequestMethod.GET})
    public Map<String, Object> getUserEntity(){
        CStudent student = null;
        CTeacher teacher = null;
        Map<String,Object> result = null;
        String userNumber = RequestThreadContext.getAccount();
        String identity = this.iUserservice.judgeIdentityByAccount(userNumber);
        if(identity.equals("student")){
            student = this.iUserservice.getOneStudentByNumber(userNumber.trim());
            result = HashMapBuilder.build(1,student.getName(),student);
        }else {
            teacher = this.iUserservice.getOneTeacherByNumber(userNumber);
            result = HashMapBuilder.build(1,teacher.getName(),teacher);
        }
        return result;
    }

    /**
     * 检查用户当前的选题状态，【已经选题，尚未选题】
     * @return
     */
    @CrossOrigin
    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getCheckState", method = {RequestMethod.POST})
    public Map<String,Object> getCheckState(){
        Map<String,Object> result = null;
        String studentAccount = RequestThreadContext.getAccount();
        CTitle title = this.iUserservice.getCheckedTitleOfStudent(studentAccount);
        if(title == null){
            result = HashMapBuilder.build(2,"尚未选题",null);
        }else{
            result = HashMapBuilder.build(1,"已经选题",title);
        }
        return result;
    }


    @ResponseBody
    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getAllTeachers", method = {RequestMethod.POST}, params = {"departName"})
    public Map<String,Object> getDepartTeachers(@RequestParam("departName") String departName){
        List<CTeacher> storage = this.iUserservice.getAllTeacherOfOneDepart(departName);
        return HashMapBuilder.buildList(storage,"请求成功");
    }

    
}
