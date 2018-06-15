package com.developmentontheedge.be5.server.services.mail;

import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.util.Utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownServiceException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


public class MailService
{
    public static final Logger log = Logger.getLogger(MailService.class.getName());

    private final CoreUtils coreUtils;

    @Inject
    public MailService(CoreUtils coreUtils)
    {
        this.coreUtils = coreUtils;
    }

    /**
     * Send email with parameter type="text/plain".
     * <p>
     * <br/><br/>Same as,
     * {@link #sendEmail(InternetAddress, InternetAddress[], String, String, String)}
     * only parameter "from" - "MAIL_FROM_ADDRESS" or "MAIL_FROM_NAME", parameter "to" as string.
     *
     * @param to
     * @param subject
     * @param body
     * @throws Exception
     */
    public void sendPlainEmail(String to, String subject, String body)
            throws Exception
    {
        sendEmail(null,
                new InternetAddress[]{new InternetAddress(to)}, subject, body, "text/plain");
    }

    /**
     * Send email with parameter type="text/html".
     * <p>
     * <br/><br/>Same as,
     * {@link #sendEmail(InternetAddress, InternetAddress[], String, String, String)}
     * only parameter "from" - "MAIL_FROM_ADDRESS" or "MAIL_FROM_NAME", parameter "to" as string.
     *
     * @param to
     * @param subject
     * @param body
     * @throws Exception
     */
    public void sendHtmlEmail(String to, String subject, String body)
            throws Exception
    {
        sendEmail(null,
                new InternetAddress[]{new InternetAddress(to)}, subject, body, "text/html");
    }

    /**
     * Same as,
     * {@link #sendEmailReal(InternetAddress, InternetAddress[], String, String, String, Map) sendEmailReal(InternetAddress, InternetAddress[], String, String, String, Map, Locale)}
     * only map of localized messages extract from a current operation.
     *
     * @param from
     * @param to
     * @param subject
     * @param body
     * @param type
     * @throws Exception
     */
    public void sendEmail(InternetAddress from, InternetAddress[] to, String subject, String body, String type)
            throws Exception
    {
//        Map locMessages = Utils.readLocalizedMessages(
//                userInfo.getLocale(), entity, getName() );

        sendEmailReal(from, to, subject, body, type, Collections.emptyMap());
    }

    /**
     * Send email passed localized.
     *
     * @param from
     * @param to
     * @param subject
     * @param body
     * @param type
     * @param locMessages
     * @throws Exception
     */
    public void sendEmailReal(InternetAddress from, InternetAddress[] to, String subject, String body, String type,
                              Map locMessages)
            throws Exception
    {
        String enc = "UTF-8";
        MimeMessage2 message = createMimeMessage();


        StringWriter writer = new StringWriter();
        //LocalizingWriter lWriter = new LocalizingWriter( locale, writer, locMessages );

//        if( subject != null && subject.indexOf( LOC_MSG_PREFIX ) >= 0 )
//        {
//            writer.write( subject );
//            writer.flush();
//            writer.close();
//            subject = writer.getBuffer().toString();
//
//            writer = new StringWriter();
//            lWriter = new LocalizingWriter( locale, writer, locMessages );
//        }
//        else
//        {
//            subject = Utils.getMessage( locMessages, subject );
//        }

//        lWriter.write( body );
//        lWriter.flush();
//        lWriter.close();

        if(from != null)
        {
            message.setFrom(from);
        }

        message.setSubject(subject, enc);
        message.addRecipients(Message.RecipientType.TO, to);

        message.setDataHandler(
                getEmailBodyDataHandler(
                        writer.getBuffer().toString().getBytes(enc),
                        type + "; charset=" + enc)
        );

        Transport.send(message);
    }

    /**
     * Retrieves data handler for the Mime message.
     *
     * @param bodyBytes message body, as byte array
     * @param mimeType  body text parameter
     * @return
     */
    public static DataHandler getEmailBodyDataHandler(final byte[] bodyBytes, final String mimeType)
    {
        return new DataHandler(
                new DataSource()
                {
                    @Override
                    public String getContentType()
                    {
                        return mimeType;
                    }

                    @Override
                    public String getName()
                    {
                        return "";
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException
                    {
                        throw new UnknownServiceException();
                    }

                    @Override
                    public InputStream getInputStream()
                    {
                        return new ByteArrayInputStream(bodyBytes);
                    }
                });
    }

    static class SmtpAuthenticator extends Authenticator
    {
        private String user = null;
        private String pwd = null;

        public SmtpAuthenticator(String user, String pwd)
        {
            this.user = user;
            this.pwd = pwd;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(this.user, this.pwd);
        }
    }

    /**
     * Creates session for the mail host. Mail host is passed parameter "host" or host is fetching from the system settings
     * ( {@link CoreUtils#getSystemSetting(String) getSystemSetting(DatabaseConnector, String)} ), parameter "MAIL_HOST".
     * If mail host wasn't specified or is empty, then "localhost" will be taken instead.
     *
     * @param host mail host
     * @return
     */
    public Session getMailSession(String host)
    {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");

        if(host == null)
        {
            host = coreUtils.getSystemSetting("MAIL_HOST");
            if(Utils.isEmpty(host))
            {
                host = "localhost";
                log.warning("No mail host defined (MAIL_HOST). Using localhost.");
            }
        }
        props.setProperty("mail.host", host);

        String login = coreUtils.getSystemSetting("SMTP_AUTH_USER");
        String password = coreUtils.getSystemSetting("SMTP_AUTH_PASSWORD");
        if(!Utils.isEmpty(login) && !Utils.isEmpty(password))
        {
            props.put("mail.smtp.user", login);
            props.put("mail.smtp.auth", "true");
            if(host.endsWith(".gmail.com"))
            {
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.starttls.enable", "true");
            }
            SmtpAuthenticator auth = new SmtpAuthenticator(login, password);
            Session session = Session.getInstance(props, auth);
            if(Boolean.parseBoolean(coreUtils.getSystemSetting("SMTP_DEBUG", "false")))
            {
                session.setDebug(true);
                session.setDebugOut(new MimeMessage2.PrintStream2(new ByteArrayOutputStream(), true));
            }
            return session;
        }
        return Session.getInstance(props);
    }

    /**
     * Use {@link #createMimeMessage(String, InputStream) createMimeMessage(DatabaseConnector, String, InputStream)},
     * with host=null and stream=null
     *
     * @return created mime message
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public MimeMessage2 createMimeMessage()
            throws MessagingException, UnsupportedEncodingException
    {
        return createMimeMessage(null, null);
    }

    /**
     * Creates mime message from passed stream, for sending it as letter with the specified host. If system settings contains parameter
     * "MAIL_FROM_ADDRESS" or "MAIL_FROM_NAME", then mime message will contain from address and name.
     *
     * @param host   smtp host
     * @param stream message body
     * @return created mime message
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public MimeMessage2 createMimeMessage(String host, InputStream stream)
            throws MessagingException, UnsupportedEncodingException
    {
        MimeMessage2 message = stream == null
                ? new MimeMessage2(getMailSession(host))
                : new MimeMessage2(getMailSession(host), stream);

        if(stream != null)
        {
            return message;
        }

        String fromAddr = coreUtils.getSystemSetting("MAIL_FROM_ADDRESS");
        String fromName = coreUtils.getSystemSetting("MAIL_FROM_NAME");

        if(Utils.isEmpty(fromAddr))
        {
            return message;
        }

        if(!Utils.isEmpty(fromName))
        {
            message.setFrom(new InternetAddress(fromAddr, fromName, "UTF-8"));
        } else
        {
            message.setFrom(new InternetAddress(fromAddr));
        }

        return message;
    }

}