package com.developmentontheedge.be5.metadata;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.TestDB;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.RoleSet;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;

public class ReportRoles extends TestCase
{
    private static class Profile
    {
        final String name;
        final String connection;
        final List<String> roles;
        Project project;

        public Profile( String name, String connection, List<String> roles )
        {
            this.name = name;
            this.connection = connection;
            this.roles = roles;
        }
    }

    private static Profile[] PROFILES = new Profile[] {
        new Profile( "TISNSO", "jdbc:oracle:thin:@newdev:1521:orcl?user=tisnso_dev;password=tisnso", Arrays.asList( "NewUtilityOperator" ) ),
        new Profile( "CONDO", "jdbc:postgresql://localhost:5432/condo?user=condo;password=condo", Arrays.asList( "Accountant" ) ) };
    private static final Pattern REDIR_URL_PATTERN = Pattern.compile( "^(\\w+)\\.redir\\?_qn_=([^&;]+)" );
    private static final Pattern REDIR_URL_PATTERN_SIMPLE = Pattern.compile( "^(\\w+)\\.redir$" );

    public void testReportQueryRoles() throws Exception
    {
        preprocessQueries();
        reportEntityItemRoles(Entity.QUERIES);
    }

    private void preprocessQueries() throws ProjectElementException, UnsupportedEncodingException
    {
        for(Profile profile : PROFILES)
        {
            for(String entityName : profile.project.getEntityNames())
            {
                for(Query query : profile.project.getEntity( entityName ).getQueries())
                {
                    if ( query.getType().equals( Query.QUERY_TYPE_STATIC ) )
                    {
                        Set<String> roles = new HashSet<>(profile.roles);
                        roles.retainAll( query.getRoles().getIncludedValues() );
                        if(roles.isEmpty())
                            continue;
                        String url = query.getQueryCompiled().validate();
                        String newEntityName = null, newQueryName = null;
                        Matcher matcher = REDIR_URL_PATTERN.matcher( url );
                        if(matcher.find())
                        {
                            newEntityName = matcher.group( 1 );
                            newQueryName = URLDecoder.decode( matcher.group( 2 ), "UTF-8");
                        } else
                        {
                            matcher = REDIR_URL_PATTERN_SIMPLE.matcher( url );
                            if(matcher.find())
                            {
                                newEntityName = matcher.group( 1 );
                                newQueryName = "All records";
                            }
                        }
                        if(newEntityName != null && newQueryName != null)
                        {
                            Entity newEntity = profile.project.getEntity( newEntityName );
                            if(newEntity != null)
                            {
                                Query newQuery = newEntity.getQueries().get( newQueryName );
                                if(newQuery != null)
                                {
                                    System.out.println( profile.name + ": " + entityName + "." + query.getName() + " -> " + newEntityName + "."
                                        + newQueryName );
                                    newQuery.getRoles().addInclusionAll( query.getRoles().getAllIncludedValues() );
                                    query.getRoles().clear();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void testReportOperationRoles() throws Exception
    {
        reportEntityItemRoles(Entity.OPERATIONS);
    }

    private void reportEntityItemRoles(String what)
    {
        Set<String> entities = new TreeSet<>();
        System.out.print( "Entity\tModule\t"+what+"\t" );
        for ( Profile profile : PROFILES )
        {
            entities.addAll( profile.project.getEntityNames() );
            for ( String role : profile.roles )
            {
                System.out.print( profile.name + ":" + role + "\t" );
            }
        }
        System.out.println();
        for ( String entityName : entities )
        {
            Set<String> entityItems = new TreeSet<>();
            boolean entityStarted = false;
            String moduleName = null;
            for ( Profile profile : PROFILES )
            {
                Entity entity = profile.project.getEntity( entityName );
                if ( entity != null )
                {
                    moduleName = entity.getModule().getName();
                    entityItems.addAll( ((BeModelCollection<?>)entity.get(what)).getNameList() );
                }
            }
            for ( String entityItemName : entityItems )
            {
                List<String> fields = new ArrayList<>();
                boolean outputEntityItem = false;
                for ( Profile profile : PROFILES )
                {
                    Entity entity = profile.project.getEntity( entityName );
                    @SuppressWarnings( "unchecked" ) // safe
                    EntityItem entityItem = entity == null ? null : ((BeModelCollection<EntityItem>)entity.get(what)).get( entityItemName );
                    RoleSet r = entityItem.getRoles();
                    Set<String> roles = entityItem == null ? Collections.<String> emptySet() : r.getIncludedValues();
                    String status = "+";
                    if ( entityItem == null )
                        status = "absent";
                    else
                    {
                        if ( entityItem.isCustomized() )
                            status = "custom";
                        if ( entityItem instanceof Query && ((Query)entityItem).isInvisible())
                            status = "invisible";
                    }
                    for ( String role : profile.roles )
                    {
                        if ( roles.contains( role ) )
                        {
                            outputEntityItem = true;
                            fields.add( status );
                        }
                        else
                        {
                            fields.add( status.equals( "absent" ) ? status : "-" );
                        }
                    }
                }
                if ( outputEntityItem )
                {
                    if ( !entityStarted )
                    {
                        System.out.print( entityName + "\t" + moduleName + "\t" );
                        entityStarted = true;
                    } else
                    {
                        System.out.print( "\t\t" );
                    }
                    System.out.print( entityItemName + "\t" );
                    for ( String field : fields )
                    {
                        System.out.print( field + "\t" );
                    }
                    System.out.println();
                }
            }
        }
    }

    @Override
    protected void setUp() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "log4j.rootCategory", "INFO,stderr" );
        properties.setProperty( "log4j.appender.stderr", "org.apache.log4j.ConsoleAppender" );
        properties.setProperty( "log4j.appender.stderr.Threshold", "INFO" );
        properties.setProperty( "log4j.appender.stderr.Target", "System.err" );
        properties.setProperty( "log4j.appender.stderr.layout", "org.apache.log4j.PatternLayout" );
        properties.setProperty( "log4j.appender.stderr.layout.ConversionPattern", "%-5p %d [%t][%F:%L] : %m%n" );
        PropertyConfigurator.configure( properties );
        
        for(Profile profile : PROFILES)
        {
            if(profile.project == null)
            {
                DatabaseConnector connector = TestDB.getConnector( profile.connection );
                profile.project = new SqlModelReader( connector ).readProject( profile.name );
            }
        }
    }
}
