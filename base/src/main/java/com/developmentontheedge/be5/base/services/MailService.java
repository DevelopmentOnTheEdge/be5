package com.developmentontheedge.be5.base.services;

import javax.mail.internet.InternetAddress;
import java.util.Map;


public interface MailService
{
    void sendPlainEmail(String to, String subject, String body) throws Exception;

    void sendHtmlEmail(String to, String subject, String body) throws Exception;

    void sendEmail(InternetAddress from, InternetAddress[] to,
                   String subject, String body, String type) throws Exception;

    void sendEmailReal(InternetAddress from, InternetAddress[] to,
                       String subject, String body, String type, Map locMessages) throws Exception;
}
