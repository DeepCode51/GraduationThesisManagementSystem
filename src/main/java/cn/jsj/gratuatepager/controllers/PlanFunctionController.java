package cn.jsj.gratuatepager.controllers;

import cn.jsj.gratuatepager.aop.RateLimiter;
import cn.jsj.gratuatepager.interceptor.RequestThreadContext;
import cn.jsj.gratuatepager.interceptor.censor.UserLoginToken;
import cn.jsj.gratuatepager.pojo.CPlan;
import cn.jsj.gratuatepager.service.IPlanService;
import cn.jsj.gratuatepager.tools.HashMapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@ResponseBody
public class PlanFunctionController {

    @Autowired
    private IPlanService planService;


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getTeacherPlanList", method = {RequestMethod.POST})
    public Map<String,Object> getAllPlanOfTeacher(){
        String teacherAccount = RequestThreadContext.getAccount();
        List<CPlan> plans = this.planService.getTeacherPlans(teacherAccount);
        return HashMapBuilder.buildList(plans,"加载成功");
    }


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "addOnePlan", method = {RequestMethod.POST}, params = {"newPlan"})
    public Map<String,Object> addNewPlan(@RequestParam("newPlan") String newPlan){
        String teacherAccount = RequestThreadContext.getAccount();
        this.planService.addOnePlan(teacherAccount, newPlan);
        return HashMapBuilder.build(1,"添加成功",null);
    }


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "deleteOnePlan", method = {RequestMethod.POST}, params = {"planID"})
    public Map<String,Object> deletePlan(@RequestParam("planID") Integer planID){
        String teacherAccount = RequestThreadContext.getAccount();
        this.planService.deleteOnePlanByID(teacherAccount, planID);
        return HashMapBuilder.build(1,"删除成功",null);
    }


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "studentGetPlans", method = {RequestMethod.GET})
    public Map<String,Object> getStudentPlans(){
        String studentAccount = RequestThreadContext.getAccount();
        List<CPlan> storage = this.planService.getStudentPlans(studentAccount);
        return HashMapBuilder.buildList(storage,"加载成功");
    }


    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "judgeFinishedPlan", method = {RequestMethod.POST}, params = {"planID"})
    public Map<String,Object> judgeStudentPlanFinished(@RequestParam("planID") Integer planID){
        Map<String,Object> result = null;
        String studentAccount = RequestThreadContext.getAccount();
        boolean over = this.planService.checkStudentPlanIsOver(studentAccount, planID);
        if(over == true){
            result = HashMapBuilder.build(1,"您已经通过该审批流程，无需重复提交",null);
        }else {
            result = HashMapBuilder.build(2,"您还没有通过此流程，请提交作品",null);
        }
        return result;
    }

}
