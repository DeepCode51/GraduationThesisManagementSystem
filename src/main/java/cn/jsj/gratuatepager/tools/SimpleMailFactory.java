package cn.jsj.gratuatepager.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;

public class SimpleMailFactory {

    @Value("${spring.mail.username}")
    private String hostmailaddress;

    /**
     * @param aimAddress 目的邮箱（用户邮箱）
     * @param text 邮件的文字内容
     * @param subject 邮件标题
     * @return 传入三个重要参数，生成一个可以提供给Javamail发送的邮件对象。
     */
    public SimpleMailMessage getSimpleMailMessage(String aimAddress, String text, String subject){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(this.hostmailaddress);
        simpleMailMessage.setTo(aimAddress);
        simpleMailMessage.setText(text);
        simpleMailMessage.setSubject(subject);
        return simpleMailMessage;
    }
}
