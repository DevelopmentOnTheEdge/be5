/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import one.util.streamex.StreamEx;

public class SimpleNode implements Node, Cloneable
{
    protected SimpleNode parent;
    protected List<SimpleNode> children = new ArrayList<>();
    protected int id;
    protected Object value;
    protected SqlParser parser;
    protected Token firstToken;
    protected Token lastToken;
    protected String nodePrefix;
    protected String nodeSuffix;
    protected String nodeContent;
    protected String childrenDelimiter;
    protected Token specialPrefix;
    protected Token specialSuffix;

    public SimpleNode(int i)
    {
        id = i;
    }

    public SimpleNode(SqlParser p, int i)
    {
        this( i );
        parser = p;
    }

    @Override
    public void jjtOpen()
    {
    }

    @Override
    public void jjtClose()
    {
    }

    @Override
    public void jjtSetParent(Node n)
    {
        parent = (SimpleNode)n;
        if( parent.specialPrefix == specialPrefix )
        {
            this.specialPrefix = null;
        }
    }

    @Override
    public SimpleNode jjtGetParent()
    {
        return parent;
    }

    @Override
    public void jjtAddChild(Node n, int i)
    {
        while( i >= children.size() )
            children.add( null );
        SimpleNode sn = (SimpleNode)n;
        children.set( i, sn );
        if( i == 0 && sn.specialPrefix == specialPrefix )
        {
            sn.specialPrefix = null;
        }
        if( i > 0 )
        {
            updateSpecialSuffix( children.get( i - 1 ), sn );
        }
        if( i < children.size() - 1 )
        {
            updateSpecialSuffix( sn, children.get( i + 1 ) );
        }
    }

    /**
     * Add given node to the end of children list setting its parent to this node
     * @param node node to add
     */
    public void addChild(SimpleNode node)
    {
        node.jjtSetParent( this );
        children.add( node );
    }

    public void addChilds(List<SimpleNode> nodes)
    {
        for (SimpleNode node : nodes)
        {
            addChild(node);
        }
    }

    private void updateSpecialSuffix(SimpleNode prev, SimpleNode next)
    {
        if( prev == null || next == null )
            return;
        Token prevEnd = prev.jjtGetLastToken();
        Token curStart = next.jjtGetFirstToken();
        if( prev.specialSuffix == null && prevEnd != null && curStart != null && prevEnd.next != null && prevEnd.next.next == curStart )
        {
            // single delimiter token in-between
            prev.specialSuffix = prevEnd.next.specialToken;
            if( this.specialPrefix == prev.specialSuffix )
                this.specialPrefix = null;
        }
    }

    @Override
    public boolean removeChild(Node n)
    {
        return children.remove( n );
    }

    public SimpleNode removeChild(int idx)
    {
        return children.remove( idx );
    }

    public void moveToFront(SimpleNode node)
    {
        int pos = children.indexOf( node );
        if( pos < 0 )
            throw new NoSuchElementException();
        children.remove( pos );
        children.add( 0, node );
    }

    @Override
    public SimpleNode jjtGetChild(int i)
    {
        return children.get( i );
    }

    /**
     * Shortcut for auto-generated jjtGetChild(i)
     * @param i child number (0-based)
     * @return requested child
     * @throws IndexOutOfBoundsException
     */
    public SimpleNode child(int i)
    {
        return children.get( i );
    }

    @Override
    public int jjtGetNumChildren()
    {
        return children.size();
    }

    public void jjtSetValue(Object value)
    {
        this.value = value;
    }

    public Object jjtGetValue()
    {
        return value;
    }

    public Token jjtGetFirstToken()
    {
        return firstToken;
    }

    public void jjtSetFirstToken(Token token)
    {
        this.firstToken = token;
        if( children.isEmpty() )
            this.specialPrefix = token == null ? null : token.specialToken;
    }

    public Token jjtGetLastToken()
    {
        return lastToken;
    }

    public void jjtSetLastToken(Token token)
    {
        this.lastToken = token;
        if( !children.isEmpty() && token != null )
        {
            SimpleNode lastChild = children.get( children.size() - 1 );
            if( lastChild.jjtGetLastToken() != null && lastChild.jjtGetLastToken().next == token )
            {
                lastChild.specialSuffix = token.specialToken;
            }
        }
    }

    /*
     * You can override these two methods in subclasses of SimpleNode to
     * customize the way the node appears when the tree is dumped. If your
     * output uses more than one line you should override toString(String),
     * otherwise overriding toString() is probably all you need to do.
     */

    @Override
    public String toString()
    {
        return this.format();
    }

    public String toString(String prefix)
    {
        return prefix + toString();
    }

    /*
     * Override this method if you want to customize how the node dumps out its
     * children.
     */

    public String dump()
    {
        StringBuilder sb = new StringBuilder();
        dump( sb, "" );
        return sb.toString();
    }

    protected void dump(StringBuilder ps, String prefix)
    {
        ps.append( toString( prefix ) ).append( "\n" );
        if( children != null )
        {
            for( Node child : children )
            {
                SimpleNode n = (SimpleNode)child;
                if( n != null )
                {
                    n.dump( ps, prefix + " " );
                }
            }
        }
    }

    public String format()
    {
        StringBuilder sb = new StringBuilder();
        format( sb, Collections.newSetFromMap( new IdentityHashMap<>() ) );
        return sb.toString();
    }

    protected static boolean isColliding(char left, char right)
    {
        if( Character.isSpaceChar( right ) || Character.isSpaceChar( left ) || right == '\n' || left == '\n' || left == '.' || right == '.' )
            return false;
        if( left == '(' || right == ')' )
            return false;
        if( Character.isDigit( right ) || Character.isAlphabetic( right ) || right == '_' )
            return true;
        if( Character.isDigit( left ) || Character.isAlphabetic( left ) || left == '_' )
            return right != ',' && right != ':';
        if( left == ',' )
            return true;
        if( left == '"' && right == '"' )
            return true;
        return false;
    }

    protected void append(StringBuilder sb, String token)
    {
        if( token == null || token.isEmpty() )
            return;
        if( sb.length() > 0 && isColliding( sb.charAt( sb.length() - 1 ), token.charAt( 0 ) )
                && ! ( token.length() > 2 && token.startsWith( "<" ) ) )
        {
            sb.append( ' ' );
        }
        sb.append( token );
    }

    protected void printSpecials(StringBuilder sb, Set<Token> printedSpecials, Token start)
    {
        if( printedSpecials == null )
            return;
        List<Token> specials = new ArrayList<>();
        while( start != null )
        {
            if( printedSpecials.add( start ) )
                specials.add( start );
            start = start.specialToken;
        }
        for( int i = specials.size() - 1; i >= 0; i-- )
            append( sb, specials.get( i ).image );
    }

    protected void format(StringBuilder sb, Set<Token> printedSpecials)
    {
        printSpecials( sb, printedSpecials, getSpecialPrefix() );
        formatBody( sb, printedSpecials );
        printSpecials( sb, printedSpecials, getSpecialSuffix() );
    }

    protected void formatBody(StringBuilder sb, Set<Token> printedSpecials)
    {
        append( sb, getNodePrefix() );
        append( sb, getNodeContent() );
        if( children != null )
        {
            SimpleNode prev = null;
            for( SimpleNode child : children )
            {
                if( prev != null )
                {
                    append( sb, getChildrenDelimiter( prev, child ) );
                }
                prev = child;
                child.format( sb, printedSpecials );
            }
        }
        append( sb, getNodeSuffix() );
    }

    @Override
    public int getId()
    {
        return id;
    }

    public StreamEx<SimpleNode> children()
    {
        return StreamEx.of( children );
    }

    public StreamEx<SimpleNode> tree()
    {
        return StreamEx.ofTree( this, node -> node.jjtGetNumChildren() == 0 ? null : node.children() );
    }

    @Override
    public SimpleNode clone()
    {
        try
        {
            SimpleNode clone = (SimpleNode)super.clone();
            clone.children = new ArrayList<>( children.size() );
            for( Node child : children )
            {
                SimpleNode childClone = ( (SimpleNode)child ).clone();
                childClone.jjtSetParent( clone );
                clone.children.add( childClone );
            }
            return clone;
        }
        catch( CloneNotSupportedException e )
        {
            throw new InternalError();
        }
    }

    public String getNodePrefix()
    {
        return nodePrefix;
    }

    public String getNodeSuffix()
    {
        return nodeSuffix;
    }

    public String getNodeContent()
    {
        return nodeContent;
    }

    /**
     * Returns delimiter between given nodes
     * @param prev previous node
     * @param next next node
     * @return the delimiter string
     */
    public String getChildrenDelimiter(SimpleNode prev, SimpleNode next)
    {
        return childrenDelimiter;
    }

    public Token getSpecialPrefix()
    {
        return specialPrefix;
    }

    public Token getSpecialSuffix()
    {
        return specialSuffix;
    }

    public void removeSpecialPrefix()
    {
        this.specialPrefix = null;
    }

    public void removeSpecialSuffix()
    {
        this.specialSuffix = null;
    }

    /**
     * Replace this node in its parent with given other nodes
     * @param nodes
     */
    public void replaceWith(SimpleNode ... nodes)
    {
        if( parent == null )
            throw new IllegalStateException( this + ": No parent available" );
        for( int i = 0; i < parent.jjtGetNumChildren(); i++ )
        {
            if( parent.child( i ) == this )
            {
                if( nodes.length == 0 )
                {
                    parent.removeChild( i );
                    return;
                }
                SimpleNode node = nodes[0];
                parent.jjtAddChild( node, i );
                if( specialPrefix != null && node.specialPrefix == null )
                {
                    node.specialPrefix = specialPrefix;
                    specialPrefix = null;
                }
                node.jjtSetParent( parent );
                for( int j = 1; j < nodes.length; j++ )
                {
                    node = nodes[j];
                    parent.children.add( i + 1, node );
                    node.jjtSetParent( parent );
                }
                return;
            }
        }
        throw new IllegalStateException( this + ": Parent does not contain me" );
    }

    /**
     * Insert given node to this node parent right after this node
     * @param node to insert
     */
    public void appendSibling(SimpleNode node)
    {
        if( parent == null )
            throw new IllegalStateException( this + ": No parent available" );
        for( int i = 0; i < parent.jjtGetNumChildren(); i++ )
        {
            if( parent.child( i ) == this )
            {
                parent.children.add( i + 1, node );
                node.jjtSetParent( parent );
                return;
            }
        }
        throw new IllegalStateException( this + ": Parent does not contain me" );
    }

    /**
     * Replace this node from its parent
     */
    public void remove()
    {
        if( parent == null )
            throw new IllegalStateException( this + ": No parent available" );
        if( !parent.removeChild( this ) )
            throw new IllegalStateException( this + ": Parent does not contain me" );
    }

    /**
     * Wraps current node with given node
     * @param node wrapper node
     */
    public void wrapWith(SimpleNode node)
    {
        replaceWith( node );
        node.addChild( this );
    }

    /**
     * Call when the supplied node is removed from the tree and replaced with this node
     * @param node to inherit from
     */
    public void inheritFrom(SimpleNode node)
    {
        this.jjtSetParent( node.jjtGetParent() );
        this.specialPrefix = node.specialPrefix;
        this.specialSuffix = node.specialSuffix;
    }

    public void removeChildren()
    {
        children = new ArrayList<>();
    }

    public int indexOf(SimpleNode node)
    {
        return children.indexOf( node );
    }

    /**
     * Returns other child of this node assuming that this node contains exactly two children
     *
     * @return other node
     * @throws IllegalStateException if this node contains not two children
     * @throws IllegalArgumentException if the supplied argument is not the child of this node
     */
    public SimpleNode other(SimpleNode child)
    {
        if( children.size() != 2 )
            throw new IllegalStateException( this + ": expected exactly two children" );
        if( children.get( 1 ) == child )
            return children.get( 0 );
        if( children.get( 0 ) == child )
            return children.get( 1 );
        throw new IllegalArgumentException( child + ": is not the child of " + this );
    }

    public void pullUpPrefix()
    {
        if( specialPrefix != null && parent != null && parent.specialPrefix == null )
        {
            parent.specialPrefix = specialPrefix;
            this.specialPrefix = null;
        }
    }

    public void pushDownPrefix()
    {
        if( specialPrefix != null && !children.isEmpty() && children.get( 0 ).specialPrefix == null )
        {
            children.get( 0 ).specialPrefix = specialPrefix;
            this.specialPrefix = null;
        }
    }

    public boolean isActual()
    {
        return this.parent.children.contains( this );
    }
}

/*
 * JavaCC - OriginalChecksum=57b2bf49a5caa762b1ef7dcd93a81dde (do not edit this
 * line)
 */
