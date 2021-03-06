package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.mail.MailService;

import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;


public class TestMailService implements MailService
{
    public static final Logger log = Logger.getLogger(TestMailService.class.getName());
    public static MailService mock = mock(MailService.class);

    public static void newMock()
    {
        mock = mock(MailService.class);
    }

    @Override
    public void sendPlainEmail(String to, String subject, String body)
    {
        mock.sendPlainEmail(to, subject, body);
        log.info("to: " + to + "\nsubject: " +  subject + "\nbody: " +  body);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String body)
    {
        mock.sendHtmlEmail(to, subject, body);
        log.info("to: " + to + "\nsubject: " +  subject + "\nbody: " +  body);
    }

    @Override
    public void sendHtmlEmail(String from, String to, String subject, String body)
    {
        mock.sendHtmlEmail(from, to, subject, body);
        log.info("from: " + from + "\nto: " + to + "\nsubject: " +  subject + "\nbody: " +  body);
    }

    @Override
    public void sendEmail(InternetAddress from, InternetAddress[] to, String subject,
                          String body, String type) throws Exception
    {
        mock.sendEmail(from, to, subject, body, type);
        log.info("from: " + from + "\nto: " + Arrays.asList(to) + "\nsubject: " +  subject + "\nbody: " +  body);
    }

    @Override
    public void sendEmailReal(InternetAddress from, InternetAddress[] to, String subject,
                              String body, String type, Map locMessages) throws Exception
    {
        mock.sendEmailReal(from, to, subject, body, type, locMessages);
        log.info("from: " + from + "\nto: " + Arrays.asList(to) + "\nsubject: " +  subject + "\nbody: " +  body);
    }
}
