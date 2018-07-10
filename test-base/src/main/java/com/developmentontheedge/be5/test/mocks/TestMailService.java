package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.base.services.MailService;

import javax.mail.internet.InternetAddress;
import java.util.Map;

import static org.mockito.Mockito.mock;


public class TestMailService implements MailService
{
    public static MailService mock = mock(MailService.class);

    public static void newMock()
    {
        mock = mock(MailService.class);
    }

    @Override
    public void sendPlainEmail(String to, String subject, String body) throws Exception
    {
        mock.sendPlainEmail(to, subject, body);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String body) throws Exception
    {
        mock.sendHtmlEmail(to, subject, body);
    }

    @Override
    public void sendEmail(InternetAddress from, InternetAddress[] to, String subject, String body, String type) throws Exception
    {
        mock.sendEmail(from, to, subject, body, type);
    }

    @Override
    public void sendEmailReal(InternetAddress from, InternetAddress[] to, String subject, String body, String type, Map locMessages) throws Exception
    {
        mock.sendEmailReal(from, to, subject, body, type, locMessages);
    }
}
