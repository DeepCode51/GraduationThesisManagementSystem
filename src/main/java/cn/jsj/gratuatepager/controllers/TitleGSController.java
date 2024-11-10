package cn.jsj.gratuatepager.controllers;

import cn.jsj.gratuatepager.aop.RateLimiter;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.interceptor.RequestThreadContext;
import cn.jsj.gratuatepager.interceptor.censor.UserLoginToken;
import cn.jsj.gratuatepager.pojo.CProfess;
import cn.jsj.gratuatepager.pojo.CTitle;
import cn.jsj.gratuatepager.service.ITitleservice;
import cn.jsj.gratuatepager.service.IUserservice;
import cn.jsj.gratuatepager.tools.HashMapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@ResponseBody
public class TitleGSController {

    @Autowired
    private ITitleservice titleservice;

    @Autowired
    private IUserservice userservice;


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getTeacherTitles", method = {RequestMethod.POST})
    public Map<String,Object> getTitleList(){
        String teacherNumber = RequestThreadContext.getAccount();
        List<CTitle> titleList = this.titleservice.getTitleListOfOneTeacher(teacherNumber);
        return HashMapBuilder.buildList(titleList,"请求成功");
    }


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getProfessAcceptedTitle", method = {RequestMethod.POST}, params = {"titleID"})
    public Map<String,Object> getAcceptProfessListOfOneTitle(@RequestParam("titleID") Integer titleID){
        List<CProfess> professes = this.titleservice.getAcceptProfessOfTitle(titleID);
        return HashMapBuilder.buildList(professes,"加载成功");
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getAllTitle", method = {RequestMethod.POST})
    public Map<String,Object> getAllTitle(){
        String workNumber = RequestThreadContext.getAccount();
        List<CTitle> titles = this.titleservice.getOneDepartTitlesByTeacherAccount(workNumber);
        return HashMapBuilder.buildList(titles,"请求成功");
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getTeacherProfess", method = {RequestMethod.POST})
    public Map<String,Object> getTeacherDeaprtProfess(){
        String workNumber = RequestThreadContext.getAccount();
        List<CProfess> professes = this.userservice.getOneTeacherProfess(workNumber);
        if(professes == null){
            throw new RuntimeProcessException("信息无效");
        }
        return HashMapBuilder.buildList(professes,"加载成功");
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "publishTitle", method = {RequestMethod.POST}, params = {"titleName","description","limitTime","limitNumber","acceptProfessStr"})
    public Map<String,Object> addTitle(@RequestParam("titleName") String titleName,@RequestParam("description") String description, @RequestParam("limitTime") Date limitTime,@RequestParam("limitNumber") Integer limitNumber, @RequestParam("acceptProfessStr") String acceptProfessStr){
        String teacherAccount = RequestThreadContext.getAccount();
        this.titleservice.addOneTitle(titleName,description,teacherAccount, limitTime, acceptProfessStr, limitNumber);
        return HashMapBuilder.build(1,"发布成功",null);
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "updateTitle", method = {RequestMethod.POST},params = {"titleId","titleName","description","limitTime","limitStudentNumber","professlistStr"})
    public Map<String,Object> updateTitle(@RequestParam("titleId") Integer titleId, @RequestParam("titleName") String titleName,@RequestParam("description") String description,@RequestParam(value = "limitTime") Date limitTime,@RequestParam("limitStudentNumber") Integer limitStudentNumber,@RequestParam("professlistStr") String professlistStr) {
        String account = RequestThreadContext.getAccount();
        this.titleservice.updateOneTitle(account, titleId, titleName, description, limitTime, limitStudentNumber, professlistStr);
        return HashMapBuilder.build(1,"添加成功",null);
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "deleteTitle", method = {RequestMethod.POST}, params = {"deleteTitleID"})
    public Map<String, Object> deleteTitle(@RequestParam("deleteTitleID") Integer titleID) {
        String account = RequestThreadContext.getAccount();
        this.titleservice.deleteOneTitleByAccount(titleID, account);
        return HashMapBuilder.build(1,"删除成功",null);
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "cancelTitle", method = {RequestMethod.POST})
    public Map<String,Object> cancelTitleCheck(){
        String studentAccount = RequestThreadContext.getAccount();
        this.userservice.cancelTitleCheck(studentAccount);
        return HashMapBuilder.build(1,"取消成功",null);
    }



    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "studentTitleList", method = {RequestMethod.GET})
    public Map<String,Object> getStudentsListAndTitleInformationByTeacherAccount() {
        String teacherAccount = RequestThreadContext.getAccount();
        List<HashMap<String, Object>> storage = this.titleservice.getTeacherTitleStudents(teacherAccount);
        return HashMapBuilder.buildList(storage,"请求成功");
    }



    @UserLoginToken
    @RateLimiter(value = 1, timeout = 150)
    @RequestMapping(value = "checkOneTitle", method = {RequestMethod.POST}, params = {"titleID"})
    public Map<String, Object> checkTitleByAccount(@RequestParam("titleID") Integer titleID) {
        String studentAccount = RequestThreadContext.getAccount();
        this.userservice.checkTitle(studentAccount, titleID);
        return HashMapBuilder.build(1,"选题成功",null);
    }



    @UserLoginToken
    @RateLimiter(value = 1, timeout = 150)
    @RequestMapping(value = "teacherCancelStudentTitle", method = {RequestMethod.POST}, params = {"studentAccount"})
    public Map<String,Object> teacherExistStudentTitle(@RequestParam("studentAccount") String studentAccount) {
        String teacherAccount = RequestThreadContext.getAccount();
        this.userservice.teacherCancelStudentTitleCheck(teacherAccount, studentAccount);
        return HashMapBuilder.build(1,"退选成功！",null);
    }

}
