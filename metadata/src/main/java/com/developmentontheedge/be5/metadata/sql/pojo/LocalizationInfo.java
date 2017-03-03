package com.developmentontheedge.be5.metadata.sql.pojo;

import java.util.ArrayList;
import java.util.List;

public class LocalizationInfo
{
    private String langcode;
    private String entity;
    private final List<String> topics = new ArrayList<>();
    private String messagekey;
    private String message;
    
    public String getLangcode()
    {
        return langcode;
    }
    public void setLangcode( String langcode )
    {
        this.langcode = langcode;
    }
    public String getEntity()
    {
        return entity;
    }
    public void setEntity( String entity )
    {
        this.entity = entity;
    }
    public List<String> getTopics()
    {
        return topics;
    }
    public void addTopic( String topic )
    {
        this.topics.add(topic);
    }
    public String getMessagekey()
    {
        return messagekey;
    }
    public void setMessagekey( String messagekey )
    {
        this.messagekey = messagekey;
    }
    public String getMessage()
    {
        return message;
    }
    public void setMessage( String message )
    {
        this.message = message;
    }
}
