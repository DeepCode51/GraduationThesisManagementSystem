package cn.jsj.gratuatepager.controllers;

import cn.jsj.gratuatepager.aop.RateLimiter;
import cn.jsj.gratuatepager.interceptor.RequestThreadContext;
import cn.jsj.gratuatepager.interceptor.censor.PassToken;
import cn.jsj.gratuatepager.interceptor.censor.UserLoginToken;
import cn.jsj.gratuatepager.pojo.CDepart;
import cn.jsj.gratuatepager.pojo.CProfess;
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
public class UserSecurityController {

    @Autowired
    private IUserservice iUserservice;


    @PassToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "departs",method = {RequestMethod.GET,RequestMethod.POST})
    public Map<String,Object> departs() {
        List<CDepart> list = this.iUserservice.getDeparts();
        return HashMapBuilder.buildList(list,"请求成功");
    }


    @PassToken
    @RateLimiter(value = 0.5, timeout = 150)
    @RequestMapping(value = "registerStudent",method = {RequestMethod.POST},params = {"studentNumber","password","email","phoneNumber","name","departid","securitycode","professid"})
    public Map<String,Object> registerStudent(@RequestParam("studentNumber") String studentNumber,@RequestParam("password") String password,@RequestParam("email") String email, @RequestParam("phoneNumber") String phoneNumber,@RequestParam("name") String name,@RequestParam("departid") Integer departid, @RequestParam("securitycode") String code,@RequestParam("professid") Integer professid){
        Map<String,String> infor = this.iUserservice.registerOneStudent(studentNumber, password, email, phoneNumber, name, departid,code,professid);
        return HashMapBuilder.build(1,"注册成功",infor);
    }


    @PassToken
    @RateLimiter(value = 0.5, timeout = 150)
    @RequestMapping(value = "registerTeacher",method = {RequestMethod.POST},params = {"workNumber","password","email","phoneNumber","name","departid","securitycode","protitle","direction"})
    public Map<String,Object> registerTeacher(@RequestParam("workNumber") String workNumber,@RequestParam("password") String password,@RequestParam("email") String email,@RequestParam("phoneNumber") String phoneNumber, @RequestParam("name") String name,@RequestParam("departid") Integer departid, @RequestParam("securitycode") String code, @RequestParam("protitle") String protitle,@RequestParam("direction") String direction){
        Map<String,String> infor = this.iUserservice.registerOneTeacher(workNumber,password,email,phoneNumber,name,departid,code,protitle,direction);
        return HashMapBuilder.build(1,"注册成功",infor);
    }

    @PassToken
    @RateLimiter(value = 1.5, timeout = 150)
    @RequestMapping(value = "getprofess", method = {RequestMethod.POST,RequestMethod.GET},params = {"departID"})
    public Map<String, Object> getProfess(@RequestParam("departID") Integer departId){
        List<CProfess> storage = this.iUserservice.getProfessByDepartId(departId);
        return HashMapBuilder.buildList(storage,"请求成功");
    }


    @PassToken
    @RateLimiter(value = 0.8, timeout = 500)
    @RequestMapping(value = "getsecuritycode",method = {RequestMethod.POST}, params = {"account","email"})
    public Map<String, Object> getSecurityCode(@RequestParam("account") String account,@RequestParam("email") String mail){
        String inf = this.iUserservice.productsecuritycode(account,mail);
        return HashMapBuilder.build(1,"请及时输入验证码",inf);
    }


    @UserLoginToken
    @RateLimiter(value = 0.8, timeout = 150)
    @RequestMapping(value = "rename", method = {RequestMethod.POST}, params = {"newname"})
    public Map<String,Object> changeName(@RequestParam("newname") String newName) {
        String userAccount = RequestThreadContext.getAccount();
        this.iUserservice.userReName(userAccount, newName);
        return HashMapBuilder.build(1,"修改成功",null);
    }



    @UserLoginToken
    @RateLimiter(value = 0.8, timeout = 150)
    @RequestMapping(value = "reInformation", method = {RequestMethod.POST}, params = {"content"})
    public Map<String,Object> changeDescription(@RequestParam("content") String content) {
        String userAccount = RequestThreadContext.getAccount();
        this.iUserservice.userReDescription(userAccount, content);
        return HashMapBuilder.build(1,"修改成功",null);
    }


    @PassToken
    @RateLimiter(value = 0.5, timeout = 150)
    @RequestMapping(value = "login", method = {RequestMethod.POST}, params = {"account","password"})
    public Map<String,Object> userLogin(@RequestParam("account") String account,@RequestParam("password") String password){
        Map<String,String> storage = this.iUserservice.login(account,password);
        return HashMapBuilder.build(1,storage.get("identity"),storage.get("token"));
    }


    @PassToken
    @RateLimiter(value = 1.5, timeout = 150)
    @GetMapping(value = "test")
    public Map<String, Object> testAPI() {
        return HashMapBuilder.build(1, "测试完成", null);
    }

    
}
