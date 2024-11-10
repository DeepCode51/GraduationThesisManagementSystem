package cn.jsj.gratuatepager.service.impl;

import cn.jsj.gratuatepager.dao.IPaperDao;
import cn.jsj.gratuatepager.exceptions.children.RuntimeImportantException;
import cn.jsj.gratuatepager.exceptions.children.RuntimeProcessException;
import cn.jsj.gratuatepager.pojo.CPaper;
import cn.jsj.gratuatepager.service.IIOservice;
import cn.jsj.gratuatepager.tools.SimpleMailFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

import java.io.*;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

@Service
public class CIOservice implements IIOservice {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${paper.storage-path}")
    private String filePath;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private IPaperDao paperDao;

    @Autowired
    private SimpleMailFactory simpleMailFactory;

    @Override
    public void postEmail(String title, String content, String aim) {
        SimpleMailMessage simpleMailMessage = this.simpleMailFactory.getSimpleMailMessage(aim,content,title);
        this.javaMailSender.send(simpleMailMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void teacherDownLoadRecordPaper(String teacherAccount,Integer paperID, HttpServletResponse response) {
        //首先验证用户上传的论文主键是否存在。
        CPaper paper = this.paperDao.getOnePaperByPaperID(paperID);
        if(paper == null) {
            throw new RuntimeProcessException("参数不合法");
        }
        //保证论文存在后，检查论文是否已经完成审核，如果已经完成审核，则直接跳转到下载完成审核论文的方法中，本方法立刻结束。
        if(paper.getPassState() == 1) {
            this.teacherDownLoadStudentPaper(teacherAccount,paperID,response);
            return;
        }
        //检查用户是否已经完成申请下载的权限。
        String key = teacherAccount.trim()+"|teacherDownload";
        Object o = this.redisTemplate.opsForValue().get(key);
        boolean exist = this.redisTemplate.hasKey(key);
        if(exist == false) {
            throw new RuntimeProcessException("未取得下载权限");
        }
        this.redisTemplate.delete(key);
        Integer paperStorageID = (Integer)o;
        if(!paperStorageID.equals(paperID)) {
            throw new RuntimeProcessException("未取得下载权限");
        }
        String tmpath = paper.getPaperPath();
        String[] storages = tmpath.split("\\.");
        File aimFile = new File(this.filePath+paper.getSubmitStudent().getAccount()+"/history/"+paper.getPaperID()+"."+storages[storages.length-1]);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(aimFile);
            //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("multipart/form-data");
            //2.设置文件头：最后一个参数是设置下载文件名
            response.setHeader("Content-Disposition", "attachment;filename="+aimFile.getName());
            //3.通过response获取ServletOutputStream对象(out)
            int b = 0;
            byte[] buffer = new byte[1024];
            while (b != -1) {
                b = inputStream.read(buffer);
                if(b != -1) {
                    response.getOutputStream().write(buffer,0,b);//4.写到输出流(out)中
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream!= null) {
                    inputStream.close();
                }
                response.getOutputStream().flush();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = REPEATABLE_READ)
    public void teacherDownLoadStudentPaper(String teacherAccount, Integer paperID, HttpServletResponse response) {
        //生成一个key字符串作为Redis检查用户是否申请下载权限的标识。
        String key = teacherAccount.trim()+"|teacherDownload";
        Object o = this.redisTemplate.opsForValue().get(key);
        boolean exist = this.redisTemplate.hasKey(key);
        if(exist == false) {
            throw new RuntimeProcessException("未取得下载权限");
        }
        //一旦开始检查用户是否有下载权限，不管是否验证成功，原有的申请权限都会失效。
        this.redisTemplate.delete(key);
        Integer paperStorageID = (Integer)o;
        if(!paperStorageID.equals(paperID)) {
            throw new RuntimeProcessException("未取得下载权限");
        }
        //权限验证成功之后，表明用户正确的完成了权限申请，此时检查用户上传的论文主键是否存在。
        CPaper paper = this.paperDao.getOnePaperByPaperID(paperID);
        if(paper == null) {
            throw new RuntimeProcessException("参数不合法");
        }
        //论文主键如果存在，则必定属于正确的指导教师，因为论文和指导教师之间的关系，在下载权限申请时就已经完成验证。
        //根据数据库中保存的路径，结合application.properties中论文总路径进行拼接，获取目标论文的绝对路径准备下载。
        String fileReadyPath = this.filePath+paper.getPaperPath();
        //下载之前主动检查文件是否存在，用自定义异常替换将要抛出的FileNotFoundException
        File file = new File(fileReadyPath);
        if(!file.exists()) {
            throw new RuntimeImportantException("重大错误：文件丢失");
        }
        //保证文件存在后，程序必定不会抛出FileNotFoundException.于是开始下载学生上传的论文。
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("multipart/form-data");
            //2.设置文件头：最后一个参数是设置下载文件名
            response.setHeader("Content-Disposition", "attachment;filename="+file.getName());
            //3.通过response获取ServletOutputStream对象(out)
            int b = 0;
            byte[] buffer = new byte[1024];
            while (b != -1) {
                b = inputStream.read(buffer);
                if(b != -1) {
                    response.getOutputStream().write(buffer,0,b);//4.写到输出流(out)中
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                response.getOutputStream().flush();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
