/* Parser.java */
/* Generated By:JavaCC: Do not edit this line. Parser.java */
package com.developmentontheedge.be5.metadata.model.selectors.parser;

import java.util.*;

import com.developmentontheedge.be5.metadata.model.selectors.*;

@SuppressWarnings({"unchecked", "unused", "rawtypes", "null"})
public class Parser implements ParserConstants
{

    final public SelectorRule parse() throws ParseException
    {
        UnionSelectorRule rule;
        rule = getUnionSelector();
        jj_consume_token(0);
        {
            if ("" != null) return rule;
        }
        throw new Error("Missing return statement in function");
    }

    final public UnionSelectorRule getUnionSelector() throws ParseException
    {
        List rules = new ArrayList();
        HierarchySelectorRule rule;
        label_1:
        while (true)
        {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
            {
                case SPACE:
                {
                    ;
                    break;
                }
                default:
                    jj_la1[0] = jj_gen;
                    break label_1;
            }
            jj_consume_token(SPACE);
        }
        rule = getHierarchySelector();
        label_2:
        while (true)
        {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
            {
                case COMMA:
                {
                    ;
                    break;
                }
                default:
                    jj_la1[1] = jj_gen;
                    break label_2;
            }
            jj_consume_token(COMMA);
            label_3:
            while (true)
            {
                switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
                {
                    case SPACE:
                    {
                        ;
                        break;
                    }
                    default:
                        jj_la1[2] = jj_gen;
                        break label_3;
                }
                jj_consume_token(SPACE);
            }
            rules.add(rule);
            rule = getHierarchySelector();
        }
        rules.add(rule);
        {
            if ("" != null) return new UnionSelectorRule(rules);
        }
        throw new Error("Missing return statement in function");
    }

    final public HierarchySelectorRule getHierarchySelector() throws ParseException
    {
        List rules = new ArrayList();
        ComplexSelectorRule rule;
        rule = getComplexSelector();
        label_4:
        while (true)
        {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
            {
                case SPACE:
                {
                    ;
                    break;
                }
                default:
                    jj_la1[3] = jj_gen;
                    break label_4;
            }
            label_5:
            while (true)
            {
                jj_consume_token(SPACE);
                switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
                {
                    case SPACE:
                    {
                        ;
                        break;
                    }
                    default:
                        jj_la1[4] = jj_gen;
                        break label_5;
                }
            }
            rules.add(rule);
            rule = getComplexSelector();
        }
        rules.add(rule);
        {
            if ("" != null) return new HierarchySelectorRule(rules);
        }
        throw new Error("Missing return statement in function");
    }

    final public ComplexSelectorRule getComplexSelector() throws ParseException
    {
        List rules = new ArrayList();
        SelectorRule rule;
        rule = getSimpleSelector();
        label_6:
        while (true)
        {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
            {
                case IDENT:
                case DOT:
                case HASH:
                case LEFT_BRACKET:
                case NOT_START:
                case MATCH_START:
                {
                    ;
                    break;
                }
                default:
                    jj_la1[5] = jj_gen;
                    break label_6;
            }
            rules.add(rule);
            rule = getSimpleSelector();
        }
        rules.add(rule);
        {
            if ("" != null) return new ComplexSelectorRule(rules);
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getSimpleSelector() throws ParseException
    {
        SelectorRule rule;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
        {
            case IDENT:
            {
                rule = getClassSelector();
                break;
            }
            case HASH:
            {
                rule = getNameSelector();
                break;
            }
            case DOT:
            {
                rule = getTypeSelector();
                break;
            }
            case NOT_START:
            {
                rule = getNotSelector();
                break;
            }
            case MATCH_START:
            {
                rule = getMatchesSelector();
                break;
            }
            case LEFT_BRACKET:
            {
                rule = getAttributeSelector();
                break;
            }
            default:
                jj_la1[6] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if ("" != null) return rule;
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getClassSelector() throws ParseException
    {
        Token t;
        t = jj_consume_token(IDENT);
        {
            if ("" != null) return new ElementClassRule(t.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getNameSelector() throws ParseException
    {
        Token t;
        jj_consume_token(HASH);
        t = jj_consume_token(IDENT);
        {
            if ("" != null) return new AttributeRule("name", t.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getTypeSelector() throws ParseException
    {
        Token t;
        jj_consume_token(DOT);
        t = jj_consume_token(IDENT);
        {
            if ("" != null) return new AttributeRule("type", t.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getAttributeSelector() throws ParseException
    {
        Token op, name, value;
        jj_consume_token(LEFT_BRACKET);
        name = jj_consume_token(IDENT);
        op = jj_consume_token(OPERATOR);
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk)
        {
            case IDENT:
            {
                value = jj_consume_token(IDENT);
                break;
            }
            case STRING:
            {
                value = jj_consume_token(STRING);
                break;
            }
            default:
                jj_la1[7] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(RIGHT_BRACKET);
        {
            if ("" != null) return new AttributeRule(name.image, value.image, op.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getNotSelector() throws ParseException
    {
        SelectorRule rule;
        jj_consume_token(NOT_START);
        rule = getComplexSelector();
        jj_consume_token(RIGHT_PARENTHESIS);
        {
            if ("" != null) return new NotRule(rule);
        }
        throw new Error("Missing return statement in function");
    }

    final public SelectorRule getMatchesSelector() throws ParseException
    {
        SelectorRule rule;
        jj_consume_token(MATCH_START);
        rule = getUnionSelector();
        jj_consume_token(RIGHT_PARENTHESIS);
        {
            if ("" != null) return new MatchesRule(rule);
        }
        throw new Error("Missing return statement in function");
    }

    /**
     * Generated Token Manager.
     */
    public ParserTokenManager token_source;
    SimpleCharStream jj_input_stream;
    /**
     * Current token.
     */
    public Token token;
    /**
     * Next token.
     */
    public Token jj_nt;
    private int jj_ntk;
    private int jj_gen;
    final private int[] jj_la1 = new int[8];
    static private int[] jj_la1_0;

    static
    {
        jj_la1_init_0();
    }

    private static void jj_la1_init_0()
    {
        jj_la1_0 = new int[]{0x2, 0x8000, 0x2, 0x2, 0x2, 0x6b1000, 0x6b1000, 0x3000,};
    }

    /**
     * Constructor with InputStream.
     */
    public Parser(java.io.InputStream stream)
    {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
     */
    public Parser(java.io.InputStream stream, String encoding)
    {
        try
        {
            jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        token_source = new ParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream)
    {
        ReInit(stream, null);
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream, String encoding)
    {
        try
        {
            jj_input_stream.ReInit(stream, encoding, 1, 1);
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    }

    /**
     * Constructor.
     */
    public Parser(java.io.Reader stream)
    {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new ParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.Reader stream)
    {
        if (jj_input_stream == null)
        {
            jj_input_stream = new SimpleCharStream(stream, 1, 1);
        }
        else
        {
            jj_input_stream.ReInit(stream, 1, 1);
        }
        if (token_source == null)
        {
            token_source = new ParserTokenManager(jj_input_stream);
        }

        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    }

    /**
     * Constructor with generated Token Manager.
     */
    public Parser(ParserTokenManager tm)
    {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    }

    /**
     * Reinitialise.
     */
    public void ReInit(ParserTokenManager tm)
    {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 8; i++) jj_la1[i] = -1;
    }

    private Token jj_consume_token(int kind) throws ParseException
    {
        Token oldToken;
        if ((oldToken = token).next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind)
        {
            jj_gen++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }


    /**
     * Get the next Token.
     */
    final public Token getNextToken()
    {
        if (token.next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    final public Token getToken(int index)
    {
        Token t = token;
        for (int i = 0; i < index; i++)
        {
            if (t.next != null) t = t.next;
            else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    private int jj_ntk_f()
    {
        if ((jj_nt = token.next) == null)
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        else
            return (jj_ntk = jj_nt.kind);
    }

    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
    private int[] jj_expentry;
    private int jj_kind = -1;

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException()
    {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[23];
        if (jj_kind >= 0)
        {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 8; i++)
        {
            if (jj_la1[i] == jj_gen)
            {
                for (int j = 0; j < 32; j++)
                {
                    if ((jj_la1_0[i] & (1 << j)) != 0)
                    {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 23; i++)
        {
            if (la1tokens[i])
            {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++)
        {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /**
     * Enable tracing.
     */
    final public void enable_tracing()
    {
    }

    /**
     * Disable tracing.
     */
    final public void disable_tracing()
    {
    }

}
