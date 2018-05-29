package com.developmentontheedge.be5.system.queries;

import com.developmentontheedge.be5.api.services.DataSourceService;
import com.developmentontheedge.be5.queries.support.TableBuilderSupport;
import com.developmentontheedge.be5.query.model.TableModel;
import org.apache.commons.dbcp.BasicDataSource;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import javax.inject.Inject;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class DataSource extends TableBuilderSupport
{
    @Inject private DataSourceService databaseService;

    @Override
    public TableModel getTableModel()
    {
        addColumns("name", "value");

        if (databaseService.getDataSource() instanceof BasicDataSource)
        {
            Map<String, String> parameters = getParameters((BasicDataSource) databaseService.getDataSource());
            for (Map.Entry<String, String> entry : parameters.entrySet())
            {
                addRow(cells(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""));
            }

        } else
        {
            try
            {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                Set<ObjectName> objectNames = server.queryNames(null, null);
                for (ObjectName name : objectNames)
                {
                    MBeanInfo info = server.getMBeanInfo(name);
                    DefaultGroovyMethods.println(this, info.getClassName());
                    if (info.getClassName().equals("org.apache.tomcat.jdbc.pool.jmx.ConnectionPool"))
                    {

                        for (MBeanAttributeInfo mf : info.getAttributes())
                        {
                            Object attributeValue = server.getAttribute(name, mf.getName());
                            if (attributeValue != null)
                            {
                                addRow(cells(mf.getName(), attributeValue.toString()));
                            }

                        }

                        break;
                    }

                }

            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


        return table(columns, rows);
    }

    public static Map<String, String> getParameters(BasicDataSource dataSource)
    {
        Map<String, String> map = new TreeMap<>();

        map.put("DataSource class", dataSource.getClass().getCanonicalName());
        map.put("Active/Idle", StringGroovyMethods.plus(dataSource.getNumActive(), " / ") + dataSource.getNumIdle());
        map.put("max Active/max Idle", StringGroovyMethods.plus(dataSource.getMaxActive(), " / ") + dataSource.getMaxIdle());
        map.put("max wait", StringGroovyMethods.plus(dataSource.getMaxWait(), ""));
        map.put("Username", dataSource.getUsername());
        map.put("DefaultCatalog", dataSource.getDefaultCatalog());
        map.put("DriverClassName", dataSource.getDriverClassName());
        map.put("Url", dataSource.getUrl());
        map.put("ValidationQuery", dataSource.getValidationQuery());
        map.put("ConnectionInitSqls", dataSource.getConnectionInitSqls().toString());

        return map;
    }
}
