package cn.jsj.gratuatepager.controllers;

import cn.jsj.gratuatepager.aop.RateLimiter;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.interceptor.RequestThreadContext;
import cn.jsj.gratuatepager.interceptor.censor.PassToken;
import cn.jsj.gratuatepager.interceptor.censor.TokenCensor;
import cn.jsj.gratuatepager.interceptor.censor.UserLoginToken;
import cn.jsj.gratuatepager.pojo.CPaper;
import cn.jsj.gratuatepager.pojo.CPlan;
import cn.jsj.gratuatepager.pojo.CpasswordBoot;
import cn.jsj.gratuatepager.service.IIOservice;
import cn.jsj.gratuatepager.service.IPaperService;
import cn.jsj.gratuatepager.service.IPlanService;
import cn.jsj.gratuatepager.tools.HashMapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@ResponseBody
public class PaperController {

    @Autowired
    private IPaperService paperService;

    @Autowired
    private IPlanService planService;

    @Autowired
    private IIOservice iiOservice;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @PassToken
    @RateLimiter(value = 0.5, timeout = 50000)
    @RequestMapping(value = "submitPaper", method = {RequestMethod.POST}, params = {"token","studentAccount","studentWord"})
    public String uploadStudentPaper(@RequestParam("token") String token,@RequestParam("studentAccount") String studentAccount,@RequestParam("studentWord") String studentWord,@RequestParam("file") MultipartFile paper){
        Map<String,Object> result = null;
        try {
            if(token == null || token.trim().length() == 0){
                return null;
            }
            Object o = this.redisTemplate.opsForValue().get(token);
            boolean exist = this.redisTemplate.hasKey(token);
            if(!exist){
                return null;
            }
            CpasswordBoot cpasswordBoot = TokenCensor.parseToken(token, (String)o);
            if(!cpasswordBoot.getUserAccount().equals(studentAccount.trim())){
                return null;
            }
            Integer planID = this.planService.getRedisPlanID(studentAccount);
            this.paperService.uploadOnePaper(studentAccount,planID,studentWord,paper);
            result = HashMapBuilder.build(1,"提交成功",null);
        }catch (ConstraintViolationException | RuntimeProcessException | IOException e){
            result = HashMapBuilder.build(0,e.getMessage(),null);
        }catch (Exception e){
            e.printStackTrace();
            result = HashMapBuilder.build(0,"发生重大错误",null);
        }finally {
            return (String)result.get("message");
        }
    }


    @ResponseBody
    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "studentGetPaperList", method = {RequestMethod.GET})
    public Map<String,Object> getStudentPapers(){
        String studentAccount = RequestThreadContext.getAccount();
        List<CPaper> paperList = this.paperService.getStudentPapers(studentAccount);
        if(paperList != null && paperList.size()>0){
            Collections.sort(paperList);
        }
        for(CPaper i: paperList){
            i.setPaperPath("");
        }
        return HashMapBuilder.buildList(paperList,"加载成功");
    }


    @ResponseBody
    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "teacherGetNotJudgePapers", method = {RequestMethod.POST}, params = {"planID"})
    public Map<String,Object> getNotJudge(@RequestParam("planID") Integer planID){
        String teacherAccount = RequestThreadContext.getAccount();
        List<CPaper> papers = this.paperService.getTeacherNotJudgePapersByPlanID(teacherAccount, planID);
        if(papers!= null && papers.size()>0){
            Collections.sort(papers);
        }
        for(CPaper i: papers){
            i.setPaperPath("");
        }
        return HashMapBuilder.buildList(papers,"加载完成");
    }


    @ResponseBody
    @UserLoginToken
    @RateLimiter(value = 0.5, timeout = 150)
    @RequestMapping(value = "paperJudgeResult", method = {RequestMethod.POST}, params = {"judgeWord","passed","paperID"})
    public Map<String,Object> judgePaper(@RequestParam("judgeWord") String judgeWord,@RequestParam("passed") Boolean passed,@RequestParam("paperID") Integer paperID){
        String teacherAccount = RequestThreadContext.getAccount();
        CPlan plan = this.paperService.teacherJudgePaper(teacherAccount,paperID,judgeWord,passed);
        return HashMapBuilder.build(1,"批阅完成",plan);
    }


    @PassToken
    @RateLimiter(value = 0.5, timeout = 50000)
    @RequestMapping(value = "teacherDownLoad", method = {RequestMethod.GET}, params = {"account","paperID"})
    public void teacherDownLoadStudentPaper(@RequestParam("account") String teacherAccount, @RequestParam("paperID") Integer paperID, HttpServletResponse response){
        try {
            this.iiOservice.teacherDownLoadStudentPaper(teacherAccount,paperID,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @PassToken
    @RateLimiter(value = 0.5, timeout = 50000)
    @RequestMapping(value = "teacherDownloadRecord", method = {RequestMethod.GET}, params = {"account","paperID"})
    public void teacherDownLoadRecordOnePaper(@RequestParam("account") String teacherAccount,@RequestParam("paperID") Integer paperID, HttpServletResponse response){
        try {
            this.iiOservice.teacherDownLoadRecordPaper(teacherAccount,paperID,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @ResponseBody
    @UserLoginToken
    @RateLimiter(value = 1, timeout = 150)
    @RequestMapping(value = "teacherGetDownPower", method = {RequestMethod.POST}, params = {"paperID"})
    public Map<String,Object> teacherGetTheDownLoadPaperPower(@RequestParam("paperID") Integer paperID){
        String teacherAccount = RequestThreadContext.getAccount();
        this.paperService.setPowerOfDownLoadFile(teacherAccount, paperID);
        return HashMapBuilder.build(1,"可以下载",null);
    }


    @ResponseBody
    @UserLoginToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "teacherRecordlist", method = {RequestMethod.GET})
    public Map<String,Object> teacherGetRecordList(){
        String teacherAccount = RequestThreadContext.getAccount();
        List<CPaper> cPaperList = this.paperService.teacherGetJudgeRecords(teacherAccount);
        if(cPaperList != null && cPaperList.size()>0){
            Collections.sort(cPaperList);
        }
        return HashMapBuilder.buildList(cPaperList,"加载成功");
    }

}
