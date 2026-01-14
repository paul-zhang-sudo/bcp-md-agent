package com.bsi.md.agent.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class AgEmailService {
	
	@Autowired
	private JavaMailSenderImpl javaMailSenderImpl;

	
	/**
	 * 发送邮件
	 * @param email
	 */
	public void sendEmail(AgEmailEntity email){
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setFrom(javaMailSenderImpl.getJavaMailProperties().getProperty("bcp.mail.from"));
		String receiver = email.getReceiver();
		String[] receivers = receiver.split(";");
		simpleMailMessage.setTo(receivers);
		simpleMailMessage.setSubject(email.getSubject());
		simpleMailMessage.setText(email.getContent());
		javaMailSenderImpl.send(simpleMailMessage);
	}
}
