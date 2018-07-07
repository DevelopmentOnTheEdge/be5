/* Generated By:JJTree: Do not edit this line. AstStart.java */
package com.developmentontheedge.sql.model;

import java.util.List;

public class AstStart extends SimpleNode
{
    public AstStart(int id)
    {
        super(id);
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer("Start, status=" + status + "\r\n");

        // print errors and warnings
        if (status > Parser.STATUS_OK && messages != null)
        {
            for (int i = 0; i < messages.size(); i++)
                buf.append("  " + messages.get(i) + "\r\n");
        }

        return buf.toString();
    }

    /**
     * Status of AST tree. It can be:
     * <ul>
     * <li>{@link Parser#STATUS_OK}</li>
     * <li>{@link Parser#STATUS_WARNING}</li>
     * <li>{@link Parser#STATUS_ERROR}</li>
     * </ul>
     *
     * @see Parser
     */
    protected int status;

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    @Override
    public void jjtAddChild(Node n, int i)
    {
        super.jjtAddChild(n, i);
        pushDownPrefix();
    }

    /**
     * List of messages (warnings and errors) generated by parser
     * for corresponding math expression.
     */
    protected List<String> messages;

    public List<String> getMessages()
    {
        return messages;
    }

    public void setMessages(List<String> messages)
    {
        this.messages = messages;
    }

    public AstQuery getQuery()
    {
        if (children.get(children.size() - 1) instanceof AstQuery)
            return ((AstQuery) children.get(children.size() - 1));
        return null;
    }

    @Override
    public AstStart clone()
    {
        return (AstStart) super.clone();
    }
}
