package com.dd.dda.thread;

import com.dd.dda.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class MailSendingThread  extends Thread {

    String EMAIL_TO;
    String EMAIL_TO_CC;
    String EMAIL_SUBJECT;
    String EMAIL_TEXT;
    MailService mailService;

    public void init(String EMAIL_TO, String EMAIL_TO_CC, String EMAIL_SUBJECT, String EMAIL_TEXT, MailService mailService) {
        this.EMAIL_TO = EMAIL_TO;
        this.EMAIL_TO_CC = EMAIL_TO_CC;
        this.EMAIL_SUBJECT = EMAIL_SUBJECT;
        this.EMAIL_TEXT = EMAIL_TEXT;
        this.mailService = mailService;

    }

    @Override
    public void run() {
        mailService.sendMail(EMAIL_TO, EMAIL_TO_CC, EMAIL_SUBJECT, EMAIL_TEXT);

    }

}
