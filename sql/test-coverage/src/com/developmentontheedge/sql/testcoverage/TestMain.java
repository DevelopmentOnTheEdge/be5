package com.developmentontheedge.sql.testcoverage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import one.util.streamex.EntryStream;
import one.util.streamex.MoreCollectors;
import one.util.streamex.StreamEx;

import com.beanexplorer.enterprise.metadata.exception.ProcessInterruptedException;
import com.beanexplorer.enterprise.metadata.exception.ProjectElementException;
import com.beanexplorer.enterprise.metadata.exception.ProjectLoadException;
import com.beanexplorer.enterprise.metadata.exception.ReadException;
import com.beanexplorer.enterprise.metadata.model.Entity;
import com.beanexplorer.enterprise.metadata.model.Project;
import com.beanexplorer.enterprise.metadata.model.Query;
import com.beanexplorer.enterprise.metadata.serialization.LoadContext;
import com.beanexplorer.enterprise.metadata.sql.BeSqlExecutor;
import com.beanexplorer.enterprise.metadata.sql.Rdbms;
import com.beanexplorer.enterprise.metadata.sql.SqlModelReader;
import com.beanexplorer.enterprise.metadata.util.ModuleUtils;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlParser;

public class TestMain
{
    private static final Pattern BE_IF_UNLESS = Pattern.compile( "<(if|unless)" );
    private static final Pattern BE_TAG = Pattern.compile( "<\\w+" );
    private static final Set<String> allowedTypes = new HashSet<>( Arrays.asList( Query.QUERY_TYPE_1D, Query.QUERY_TYPE_1DUNKNOWN,
            Query.QUERY_TYPE_2D ) );
    
    private static final Set<String> knownNonExpanded = new HashSet<>( Arrays.asList( "FULL_BUILDING_ADDRESS_WITHOUT_REGION(",
            "CARD_LINK_MAIN(", "COMMON_WELFAREGROUP_PASSPORT_FIELDS(" ) );
    
    private static class FailData
    {
        List<String> queries = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        
        void add(Query query, String queryText)
        {
            queries.add( queryText );
            paths.add(query.getModule().getName()+"."+query.getEntity().getName()+"."+query.getName());
        }
        
        void dump(Path path)
        {
            try
            {
                Files.write( path, StreamEx.zip(queries, paths, (q, p) -> p+":\n-------------\n"+q) );
            }
            catch( IOException e )
            {
                throw new UncheckedIOException( e );
            }
        }
    }

    private static class StatContext
    {
        private final Map<String, Integer> map = new LinkedHashMap<>();
        private final String name;

        public StatContext(String name, String ... fields)
        {
            this.name = name;
            for( String f : fields )
                map.put( f, 0 );
        }

        public void inc(String parameter)
        {
            map.merge( parameter, 1, Integer::sum );
        }

        public void merge(StatContext other)
        {
            for( Entry<String, Integer> entry : other.map.entrySet() )
            {
                map.merge( entry.getKey(), entry.getValue(), Integer::sum );
            }
        }

        @Override
        public String toString()
        {
            return map
                    .entrySet()
                    .stream()
                    .map( entry -> entry.getKey().equals( "Count" ) ? String.format( Locale.ENGLISH, "%s: %4d", entry.getKey(),
                            entry.getValue() ) : String.format( Locale.ENGLISH, "%s: %4d (%5.1f%%)", entry.getKey(), entry.getValue(),
                            entry.getValue() * 100.0 / map.get( "Count" ) ) ).collect( Collectors.joining( ", ", "[", "] " + name ) );
        }

    }

    static String sanitizeValue(String rawValue)
    {
        String value = String.valueOf( rawValue )
                // != is actually the same as <>
                .replaceAll( "!=", "<>" )
                .replaceAll( "\\s+", " " )
                .replaceAll( "\\s*([,=/<>\\(\\)\\|\\-]|\\*/|/\\*)\\s*", "$1" );
                
        if( !value.trim().isEmpty() )
            value = value.trim();
        return value;
    }

    public static StatContext testModule5DB(String name, List<String> okQueries) throws ProjectLoadException, ReadException
    {
        LoadContext ctx = new LoadContext();
        Project project = ModuleUtils.loadModule( name, ctx );
        ModuleUtils.addModuleScripts( project );
        ctx.check();
        SqlParser parser = new SqlParser();
        Formatter formatter = new Formatter();
        DefaultParserContext parserContext = new DefaultParserContext();
        parser.setContext( parserContext );
        Rdbms[] dbms = {Rdbms.MYSQL, Rdbms.DB2, Rdbms.ORACLE, Rdbms.SQLSERVER, Rdbms.POSTGRESQL};
        StatContext stat = new StatContext( name + "/5DB", StreamEx.of( dbms ).map( Rdbms::name ).prepend( "Count", "PF" )
                .append( "Success" ).toArray( String[]::new ) );
        for( String entityName : project.getEntityNames() )
        {
            Entity entity = project.getEntity( entityName );
            for( Query query : entity.getQueries() )
            {
                if( !allowedTypes.contains( query.getType() ) )
                    continue;
                if( query.getName().equals( "Table definition" ) )
                    continue;
                if( query.getQuery().trim().isEmpty() )
                    continue;
                project.setDatabaseSystem( Rdbms.BESQL );
                String queryText;
                try
                {
                    queryText = query.getQueryCompiled().validate();
                }
                catch( ProjectElementException e )
                {
                    continue;
                }
                stat.inc( "Count" );
                parser.parse( queryText );
                if( !parser.getMessages().isEmpty() )
                {
                    stat.inc( "PF" );
                    continue;
                }
                try
                {
                    formatter.format( parser.getStartNode(), new Context( Dbms.POSTGRESQL ), parserContext );
                }
                catch( IllegalArgumentException e )
                {
                    System.out.println(entityName+"."+query.getName()+": "+e.getMessage());
                    stat.inc( "PF" );
                    continue;
                }
                boolean success = true;
                for( Rdbms db : dbms )
                {
                    project.setDatabaseSystem( db );
                    String expected;
                    try
                    {
                        expected = sanitizeValue( query.getQueryCompiled().validate() );
                    }
                    catch( ProjectElementException e )
                    {
                        continue;
                    }
                    String formatted;
                    try
                    {
                        formatted = formatter.format( parser.getStartNode(), new Context( Dbms.valueOf( db.name() ) ), parserContext );
                    }
                    catch( IllegalArgumentException e )
                    {
                        System.out.println(entityName+"."+query.getName()+": "+e.getMessage());
                        stat.inc( db.name() );
                        success = false;
                        continue;
                    }
                    String actual = sanitizeValue(formatted);
                    if(!expected.equals( actual ))
                    {
                        if(db == Rdbms.ORACLE)
                        {
                            System.out.println(entityName+"."+query.getName());
                            System.out.println("BESQL: "+queryText);
                            System.out.println("Actual: "+actual);
                            System.out.println("Expected: "+expected);
                        }
                        stat.inc( db.name() );
                        success = false;
                    }
                }
                if(success)
                {
                    stat.inc( "Success" );
                    //okQueries.add( entityName+"."+query.getName() );
                }
            }
        }

        return stat;
    }

    public static StatContext testModule(String name, Rdbms dbms, FailData parseFailQueries) throws ProjectLoadException, ReadException
    {
        LoadContext ctx = new LoadContext();
        Project project = ModuleUtils.loadModule( name, ctx );
        ModuleUtils.addModuleScripts( project );
        ctx.check();
        project.setDatabaseSystem( dbms );
        return testProject( project, parseFailQueries );
    }

    public static StatContext testSqlDatabase(String name, String connectionURL, FailData failedQueries) throws IOException,
            ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        BeSqlExecutor executor = new BeSqlExecutor( connectionURL );
        SqlModelReader reader = new SqlModelReader( executor, SqlModelReader.READ_META );
        Project project = reader.readProject( name );
        return testProject( project, failedQueries );
    }

    private static StatContext testProject(Project project, FailData failedQueries)
    {
        String name = project.getName();
        Rdbms dbms = project.getDatabaseSystem();
        StatContext stat = new StatContext( name + "/" + dbms, "Count", "FMF", "PE", "FE", "PF", "RFE", "RFM", "Success" );
        SqlParser parser = new SqlParser();
        parser.setContext( new DefaultParserContext() );
        Formatter formatter = new Formatter();
        Context context = new Context( Arrays.stream( Dbms.values() ).filter( d -> d.getType() == dbms.getType() ).findFirst().get() );
        for( String entityName : project.getEntityNames() )
        {
            Entity entity = project.getEntity( entityName );
            for( Query query : entity.getQueries() )
            {
                if( !allowedTypes.contains( query.getType() ) )
                    continue;
                if( query.getName().equals( "Table definition" ) )
                    continue;
                if( query.getQuery().trim().isEmpty() )
                    continue;
                stat.inc( "Count" );
                String queryText;
                try
                {
                    queryText = query.getQueryCompiled().validate();
                }
                catch( ProjectElementException e )
                {
                    stat.inc( "FMF" );
                    continue;
                }
                parser.parse( queryText );
                if( !parser.getMessages().isEmpty() )
                {
                    if(knownNonExpanded.stream().anyMatch( queryText::contains ))
                    {
                        stat.inc( "FMF" );
                        continue;
                    }
                    failedQueries.add( query, queryText );
                    //System.out.println("----------------------\nQuery: "+entity.getName()+"."+query.getName()+"\n"+queryText+"\n\n"+String.join("\n", parser.getMessages()));
                    stat.inc( "PE" );
                    continue;
                }
                AstStart astStart = parser.getStartNode();
                String format;
                try
                {
                    format = formatter.format( astStart, context, parser.getContext() );
                    Objects.requireNonNull( format );
                }
                catch( Exception e )
                {
                    failedQueries.add( query, queryText );
                    System.out.println("Err: "+entity.getName()+"."+query.getName()+": "+e.getMessage());
                    stat.inc( "FE" );
                    continue;
                }
                parser.parse( format );
                if( !parser.getMessages().isEmpty() )
                {
                    failedQueries.add( query, queryText );
//                    System.out.println("PF: "+queryText+"\n"+"Formatted: "+format+"\n"+parser.getMessages());
                    stat.inc( "PF" );
                    continue;
                }
                astStart = parser.getStartNode();
                String format2;
                try
                {
                    format2 = formatter.format( astStart, context, parser.getContext() );
                    Objects.requireNonNull( format2 );
                }
                catch( Exception e )
                {
                    stat.inc( "RFF" );
                    continue;
                }
                if( !format.equals( format2 ) )
                {
                    failedQueries.add( query, queryText );
//                    System.out.println("Orig : "+queryText);
//                    System.out.println("Form1: "+format);
//                    System.out.println("Form2: "+format2);
                    stat.inc( "RFM" );
                    continue;
                }
                stat.inc( "Success" );
            }
        }
        return stat;
    }

    public static Function<String, String> getQueryClassifier()
    {
        Map<String, Predicate<String>> rules = new LinkedHashMap<>();
        //rules.put( "Sql-Auto", s -> s.startsWith( "<sql auto=\"true\"" ) );
        //rules.put("Incorrect selection", s -> s.startsWith( "SELECT ID AS \"Code\",  AS \"Name\" FROM " ));
        //rules.put("Unknown macro", s -> s.startsWith( "BROWSE_BY_OVERDUE_STATUS_FILTER" ));
        //rules.put( "_nl_query", s -> s.startsWith( "_nl" ) );
        //rules.put("_tcloneid_", s -> s.contains( "<parameter:_tcloneid_" ) );
        rules.put( "1. AS CHAR(x)", s -> s.toLowerCase().contains( "as char(" ) );
        rules.put( "2. Strange unless", s -> s.contains( "/*big-selection-view*/</unless>" ));
        rules.put( "3. FROM AGE", s -> s.contains( "FROM AGE(" ));
        rules.put( "4. exec-include", s -> s.contains( "<sql exec=\"include\"" ));
        rules.put( "5.0. exec-pre in-quotes", s -> s.contains( "'<sql exec=\"pre\"" ));
        rules.put( "5.1. exec-pre filterKey", s -> s.contains( "<sql exec=\"pre\"" ) && s.contains( " filterKey=" ));
        rules.put( "5.2. exec-pre other", s -> s.contains( "<sql exec=\"pre\"" ));
        rules.put( "6. ::INTERVAL", s -> s.contains( "::INTERVAL" ));
        rules.put( "7. TO_CHAR(", Pattern.compile( "TO_CHAR\\s*\\(", Pattern.CASE_INSENSITIVE ).asPredicate() );
        rules.put( "8. BE if/unless", s -> BE_IF_UNLESS.matcher( s ).find() );
        rules.put( "9. BE tags", s -> BE_TAG.matcher( s ).find() );
        //rules.put( "COUNT", s -> s.toLowerCase().contains( "count" ) );
        //rules.put( "FROM (", Pattern.compile( "FROM\\s*\\(", Pattern.CASE_INSENSITIVE ).asPredicate() );
        return EntryStream.of( rules ).foldRight( s -> "Other",
                (entry, fn) -> s -> entry.getValue().test( s ) ? entry.getKey() : fn.apply( s ) );
    }

    public static Map<String, List<String>> classifyFailures(List<String> failures)
    {
        return StreamEx.of( failures ).distinct().groupingBy( getQueryClassifier(), TreeMap::new, Collectors.toList() );
    }

    public static void reportFailedQueries(FailData failedQueries)
    {
        failedQueries.dump( Paths.get( "failed.txt" ) );
        System.out.println( "Shortest failed queries:" );
        System.out.println( "------------------------" );
        Map<String, List<String>> failures = classifyFailures( failedQueries.queries );
        failures.forEach( (className, list) -> {
            String info = className+" (total: "+list.size()+")";
            System.out.println( info );
            System.out.println( StreamEx.constant( "=", info.length() ).joining() );
            List<String> shortestQueries = list.stream().distinct()
                    .collect( MoreCollectors.least( Comparator.comparingInt( String::length ), 10 ) );
            EntryStream.of( shortestQueries ).mapKeyValue( (i, q) -> "    " + ( i + 1 ) + ". " + q ).forEach( System.out::println );
        } );
    }

    static void formatTest() throws ProjectLoadException, ReadException
    {
        String[] modules = {"realty", "security", "financial", "socialaid"};
        List<String> okQueries = new ArrayList<>();
        StatContext total = new StatContext( "total" );
        for( String module : modules )
        {
            StatContext stat = testModule5DB( module, okQueries );
            System.out.println( stat );
            total.merge( stat );
        }
        System.out.println( "------------" );
        System.out.println( total );
        okQueries.stream().sorted().forEach( System.out::println );
    }

    static void parseTest() throws ProjectLoadException, IOException, ExtendedSqlException, SQLException,
            ProcessInterruptedException, ReadException
    {
        StatContext total = new StatContext( "total" );
        FailData failedQueries = new FailData();
        String[] modules = {"realty", "security", "financial", "socialaid"};
        for( String module : modules )
        {
            StatContext stat = testModule( module, Rdbms.POSTGRESQL, failedQueries );
            System.out.println( stat );
            total.merge( stat );
        }
        StatContext stat = testSqlDatabase( "tikregion",
                "jdbc:postgresql://pg02.dote.ru:5432/tisnso_dev_pg?user=tisnso_dev_pg;password=tisnso", failedQueries );
        System.out.println( stat );
        total.merge( stat );
        StatContext statI = testSqlDatabase( "invalids",
                "jdbc:postgresql://localhost:5433/invalids?user=postgres;password=postgres", failedQueries );
        System.out.println( statI );
        total.merge( statI );
        StatContext statB = testSqlDatabase( "biostore", "jdbc:mysql://localhost:3306/biostore?user=biostore;password=biostore",
                failedQueries );
        System.out.println( statB );
        total.merge( statB );
        System.out.println( "------------" );
        System.out.println( total );
    
        reportFailedQueries( failedQueries );
    }

    public static void main(String[] args) throws Exception
    {
        parseTest();
        formatTest();
    }
}
