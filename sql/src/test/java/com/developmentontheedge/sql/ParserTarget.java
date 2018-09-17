package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.BasicQueryContext;
import com.developmentontheedge.sql.format.dbms.Context;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.CountApplier;
import com.developmentontheedge.sql.format.dbms.Dbms;
import com.developmentontheedge.sql.format.dbms.Formatter;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlParser;
import com.developmentontheedge.xmltest.Target;
import com.developmentontheedge.xmltest.XmlTest;
import junit.framework.ComparisonFailure;
import one.util.streamex.IntStreamEx;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class ParserTarget extends Target
{
    public static final String TAG_PARSER = "parser";
    public static final String TAG_QUERY = "query";
    public static final String TAG_DUMP = "dump";
    public static final String TAG_FORMAT = "format";
    public static final String TAG_CONTEXT = "applyContext";
    public static final String TAG_COUNT = "applyCount";
    public static final String TAG_LIMIT = "applyLimit";
    public static final String TAG_PARAMETER = "parameter";
    public static final String TAG_SESSION_VAR = "sessionVar";
    public static final String TAG_USER_NAME = "userName";
    public static final String TAG_ROLE = "role";

    public static final String ATTR_DBMS = "dbms";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_TYPE = "type";

    protected SqlParser parser = new SqlParser();
    protected String query;
    protected AstStart astStart;

    public ParserTarget(XmlTest xmlTest)
    {
        super(xmlTest);
    }

    @Override
    public void process(Element element) throws Exception
    {
//        if(!test.getName().equals( "daydiff2" ))
//            return;
        try
        {
            NodeList childs = element.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++)
            {
                if (childs.item(i) instanceof Element)
                    processChild((Element) childs.item(i));
            }
        }
        catch (Throwable t)
        {
            test.getResult().addError(test, t);
        }
    }

    protected void processChild(Element element) throws Exception
    {
        switch (element.getTagName())
        {
            case TAG_QUERY:
                loadAndParseQuery(element);
                break;

            case TAG_CONTEXT:
                context(element);
                break;

            case TAG_COUNT:
                count();
                break;

            case TAG_LIMIT:
                limit(element);
                break;

            case TAG_DUMP:
                dump();
                break;

            case TAG_FORMAT:
                format(element);
                break;
        }
    }

    protected void checkQuery(String tag)
    {
        if (query == null)
            test.getResult().addError(test, new Exception("<query> should be declared before <" + tag + "> tag."));
    }

    private void context(Element element)
    {
        BasicQueryContext.Builder builder = new BasicQueryContext.Builder();
        NodeList nodes = element.getChildNodes();
        for (Element child : IntStreamEx.range(nodes.getLength()).mapToObj(nodes::item).select(Element.class))
        {
            switch (child.getTagName())
            {
                case TAG_PARAMETER:
                    builder.parameter(child.getAttribute(ATTR_NAME), child.getAttribute(ATTR_VALUE));
                    break;
                case TAG_SESSION_VAR:
                    builder.sessionVar(child.getAttribute(ATTR_NAME), child.getAttribute(ATTR_VALUE), child.getAttribute(ATTR_TYPE));
                    break;
                case TAG_USER_NAME:
                    builder.userName(child.getAttribute(ATTR_NAME));
                    break;
                case TAG_ROLE:
                    builder.roles(child.getAttribute(ATTR_NAME));
                    break;
            }
        }
        astStart = astStart.clone();
        new ContextApplier(builder.build()).applyContext(astStart);
    }

    protected void loadAndParseQuery(Element element)
    {
        query = XmlTest.getCData(element);
        parser.parse(query);
        if (!parser.getMessages().isEmpty())
            fail(parser.getMessages().get(0));
        astStart = parser.getStartNode();
        new MacroExpander().expandMacros(astStart);
    }

    protected void dump()
    {
        checkQuery(TAG_DUMP);
        System.out.println("query=" + query + "\r\n" + "Ast:");
        System.out.println(astStart.dump());
    }

    protected void limit(Element element)
    {
        checkQuery(TAG_LIMIT);
        int count = Integer.parseInt(element.getAttribute("count"));
        astStart = astStart.clone();
        new LimitsApplier(0, count).transform(astStart);
    }

    protected void count()
    {
        checkQuery(TAG_COUNT);
        astStart = astStart.clone();
        new CountApplier().transform(parser.getContext(), astStart);
    }

    protected void format(Element element)
    {
        checkQuery(TAG_FORMAT);

        String dbmsNames = element.getAttribute(ATTR_DBMS);
        if (dbmsNames == null)
        {
            test.getResult().addError(test, new Exception("Attribute \"" + ATTR_DBMS + "\" should be specified in task <" + TAG_FORMAT + ">."));
            return;
        }

        for (String dbmsName : dbmsNames.split(","))
        {
            format(dbmsName, element);
        }
    }

    protected void format(String dbmsName, Element element)
    {
        Dbms dbms;
        if (Dbms.DB2.getName().equals(dbmsName))
            dbms = Dbms.DB2;
        else if (Dbms.MYSQL.getName().equals(dbmsName))
            dbms = Dbms.MYSQL;
        else if (Dbms.ORACLE.getName().equals(dbmsName))
            dbms = Dbms.ORACLE;
        else if (Dbms.POSTGRESQL.getName().equals(dbmsName))
            dbms = Dbms.POSTGRESQL;
        else if (Dbms.SQLSERVER.getName().equals(dbmsName))
            dbms = Dbms.SQLSERVER;
        else if (Dbms.H2.getName().equals(dbmsName))
            dbms = Dbms.H2;
        else
        {
            test.getResult().addError(test, new Exception("Unknown DBMS \"" + dbmsName + "\". DBMS should be: db2, mysql, oracle, postgresql or sqlserver."));
            return;
        }

        Formatter formatter = new Formatter();
        String formatResult = trimAllLine(formatter.format(astStart, new Context(dbms), parser.getContext()));
        String result = trimAllLine(XmlTest.getCData(element));

        assertEquals("Incorrect result, dbms=" + dbms.getName(), result, formatResult);

        if (!formatResult.equals(result))
            test.getResult().addFailure(test, new ComparisonFailure("Incorrect result, dbms=" + dbms.getName(), result, formatResult));
    }

    private String trimAllLine(String text)
    {
        return Arrays.stream(text.trim().split("[\\r?\\n]+"))
                .map(String::trim).collect(Collectors.joining("\n"));
    }

}
