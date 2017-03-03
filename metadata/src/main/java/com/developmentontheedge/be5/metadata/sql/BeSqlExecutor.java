package com.developmentontheedge.be5.metadata.sql;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

import one.util.streamex.StreamEx;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.dbms.DBMSBase;
import com.developmentontheedge.be5.metadata.exception.FreemarkerSqlException;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.freemarker.FreemarkerSqlHandler;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.dbms.SqlExecutor;

/**
 * Class helps to execute SQL queries using only query name and list of arguments.
 * All queries are stored in special properties file: <code>sql.properties</code>.
 */
public class BeSqlExecutor extends SqlExecutor
{
    private final DatabaseConnector beConnector;
    
    public interface ResultSetMapper<T>
    {
        public T map(ResultSet rs) throws SQLException;
    }
    
    /**
     * Initializes the new resource bundle and loads all needed info from file.
     *
     * @param connector Database connector containing info about DB platform.
     * @throws IOException Problem with loading of resource.
     */
    public BeSqlExecutor( DatabaseConnector connector ) throws IOException
    {
        this( connector, null );
    }
    
    public BeSqlExecutor( String connectionUrl ) throws IOException
    {
        this( DBMSBase.createConnector( connectionUrl ), null );
    }
    
    public BeSqlExecutor( DatabaseConnector connector, PrintStream log ) throws IOException
    {
        super(new BeanExplorerDbmsConnector( connector ), log, BeSqlExecutor.class.getResource( "sql.properties" ));
        this.beConnector = connector;
    }
    
    public DatabaseConnector getConnector()
    {
        return this.beConnector;
    }

    public void executeScript(FreemarkerScript script, ProcessController log) throws ProjectElementException, FreemarkerSqlException
    {
        if ( script == null || script.getSource().trim().isEmpty() )
            return;
        try
        {
            new FreemarkerSqlHandler( this, false, log ).execute( script );
        }
        catch ( ProjectElementException | FreemarkerSqlException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new ProjectElementException( script, e );
        }
    }
    
    public <T> StreamEx<T> stream(ResultSetMapper<T> mapper, String queryName, Object... args) throws ExtendedSqlException {
        ResultSet rs = executeNamedQuery( queryName, args );
        return StreamEx.of( new AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
            boolean closed = false;
            
            @Override
            public boolean tryAdvance( Consumer<? super T> action )
            {
                if(closed)
                    return false;
                try
                {
                    if(!rs.next())
                    {
                        closed = true;
                        beConnector.close( rs );
                        return false;
                    }
                    action.accept( mapper.map( rs ) );
                    return true;
                }
                catch ( SQLException e )
                {
                    BeSqlExecutor.sneakyThrow( getException( e, queryName, args ) );
                    throw new InternalError("unreachable");
                }
            }
        }).onClose( () -> beConnector.close( rs ) );
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
    
    public ExtendedSqlException getException(Exception cause, String queryName, Object... args) {
        String sql = sql( queryName, args );
        return new ExtendedSqlException( connector, sql, cause );
    }
}
