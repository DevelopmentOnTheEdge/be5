package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.impl.GroovyOperationLoader;
import com.developmentontheedge.be5.api.ulit.HtmlUtils;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.github.benmanes.caffeine.cache.Cache;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


public class GroovyRegister
{
    private Cache<String, Class> groovyClasses;
    private GroovyClassLoader classLoader;

    private Provider<GroovyOperationLoader> groovyOperationLoaderProvider;

    public GroovyRegister(ProjectProvider projectProvider, Provider<GroovyOperationLoader> groovyOperationLoaderProvider, Be5Caches be5Caches)
    {
        this.groovyOperationLoaderProvider = groovyOperationLoaderProvider;

        projectProvider.addToReload(this::initClassLoader);
        initClassLoader();

        groovyClasses = be5Caches.createCache("Groovy classes");
    }

    private GroovyClassLoader getClassLoader()
    {
        return classLoader;
    }

    private Class parseClass( String text, String name )
    {
        return getClassLoader().parseClass( text, name );
    }

    public Class getClass( String key, String code, String fileName )
    {
        return groovyClasses.get(key, k -> parseClass( code, fileName ));
    }

    public Cache<String, Class> getGroovyClasses()
    {
        return groovyClasses;
    }

    public void initClassLoader()
    {
        classLoader = new GroovyClassLoader();
        addClassPaths();
    }

    private void addClassPaths()
    {
        ModuleLoader2.getPathsToProjectsToHotReload().forEach(
                (name, path) -> classLoader.addClasspath(path.resolve("src/groovy/operations").toString())
        );
    }

//    public static Class parseClassWithCache( String name, String text )
//    {
//        Cache cache = GroovyRegisterCache.getInstance();
//        Pair<String, Class> pair = ( Pair<String, Class> )cache.get( name );
//        if( pair != null )
//        {
//            if( !pair.getFirst().equals( text ) )
//            {
//                pair.setSecond( classLoader.parseClass( text ) );
//            }
//        }
//        else
//        {
//            pair = new Pair<>(  );
//            pair.setFirst( text );
//            pair.setSecond( classLoader.parseClass( text ) );
//            cache.put( name, pair );
//        }
//        return pair.getSecond();
//    }


    public static void registerMetaClass(Class<? extends MetaClass> metaClazz, Class<?> clazz )
    {
        try
        {
            Constructor<? extends MetaClass> constructor = metaClazz.getDeclaredConstructor( Class.class );
            MetaClass metaClass;
            try
            {
                metaClass = constructor.newInstance( clazz );
            }
            catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
            {
                throw new RuntimeException( e );
            }
            metaClass.initialize();
            InvokerHelper.getMetaRegistry().setMetaClass( clazz, metaClass );
        }
        catch( NoSuchMethodException e )
        {
            throw new RuntimeException( e );
        }
    }

    public String getErrorCodeLine(Throwable e)
    {
        Set<String> printedGroovyClasses = new HashSet<>();
        Throwable err = e;

        Stack<Throwable> throwables = new Stack<>();
        throwables.add(err);
        while (err.getCause() != null)
        {
            throwables.add(err.getCause());
            err = err.getCause();
        }

        StringBuilder sb = new StringBuilder();
        while (!throwables.empty())
        {
            err = throwables.pop();

            StackTraceElement[] stackTrace = err.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++)
            {
                if(stackTrace[i].getFileName() != null && stackTrace[i].getFileName().endsWith(".groovy")
                        && !printedGroovyClasses.contains(stackTrace[i].getFileName()))
                {
                    printedGroovyClasses.add(stackTrace[i].getFileName());
                    sb.append(getErrorCodeLinesForClass(stackTrace[i]));
                    break;
                }
            }
        }

        return sb.toString();
    }

    private String getErrorCodeLinesForClass(StackTraceElement e)
    {
        int lineID = e.getLineNumber();
        StringBuilder sb = new StringBuilder("\n" + Be5Exception.getFullStackTraceLine(e));

        String className = e.getClassName().indexOf('$') == -1
                                ? e.getClassName()
                                : e.getClassName().substring(0, e.getClassName().indexOf('$'));

        String code = groovyOperationLoaderProvider.get()
                .getByFullName(className + ".groovy")
                .getCode();
        String lines[] = HtmlUtils.escapeHTML(code).split("\\r?\\n");

        sb.append("\n\n<code>");
        for (int i = Math.max(0, lineID - 4); i < Math.min(lineID + 3, lines.length); i++)
        {
            String lineNumber = String.format("%4d", i+1)+" | ";
            if(lineID == i+1){
                sb.append("<span style=\"color: #e00000;\">").append(lineNumber).append(lines[i]).append("</span>\n");
            }else{
                sb.append(lineNumber).append(lines[i]).append("\n");
            }
        }
        sb.append("</code>");

        return sb.toString();
    }

//
//    @SuppressWarnings( "unchecked" )
//    public static List<String> toCompilationMessages(List errors0)
//    {
//        List<Message> errors = (List<Message>)errors0;
//        List<String> messages = new ArrayList<>();
//        if (errors != null) {
//            for (Message error : errors) {
//                if (error instanceof SyntaxErrorMessage) {
//                    SyntaxErrorMessage syntaxError = (SyntaxErrorMessage) error;
//                    messages.add(syntaxError.getCause().getMessage());
//                }
//            }
//        }
//        return messages;
//    }

//    public static boolean classHook( DatabaseConnector connector, Map<String, ?> map )
//    {
//        try
//        {
//            if( Utils.getBooleanModuleSetting( connector, "GROOVY", "GROOVY_HOOKS", false ) )
//            {
//                Exception e = new Exception();
//                StackTraceElement s = e.getStackTrace()[ 1 ];
//                String className = s.getClassName();
//
//                // if static block: methodName = "<clinit>"
//                // if constructor or initialize block:methodName = "<init>"
//                String methodName = s.getMethodName();
//
//                String code = new QRec( connector, "SELECT code FROM groovyHooks WHERE className = '" + className + "' AND methodName = '" + methodName + "'" ).getString();
//                Object o = eval( code, map );
//                return o instanceof Boolean && ( ( Boolean )o );
//            }
//        }
//        catch( NoRecord e )
//        {
//            // no hooks for that class with that method name
//        }
//        catch( Exception e )
//        {
//            Logger.error( cat, e.getMessage(), e );
//            return false;
//        }
//        return false;
//    }
//
//    public static Object eval( String code, Map<String, ?> params )
//    {
//        GroovyScope scope = new GroovyScope();
//        scope.setVariables( params );
//        return scope.evaluate( code );
//    }
//
//    public static GroovyScope getScope()
//    {
//        return new GroovyScope();
//    }
//
//    public static class GroovyScope
//    {
//        private final GroovyShell shell = new GroovyShell();
//
//        public void setVariable( String name, Object value )
//        {
//            shell.setVariable( name, value );
//        }
//
//        public void setVariables( Map<String, ?> vars )
//        {
//            for( Entry<String, ?> e : vars.entrySet() )
//            {
//                setVariable( e.getKey(), e.getValue() );
//            }
//        }
//
//        public Object getVariable( String name )
//        {
//            return shell.getVariable( name );
//        }
//
//        public Object evaluate( String code )
//        {
//            return shell.evaluate( code );
//        }
//    }
}
