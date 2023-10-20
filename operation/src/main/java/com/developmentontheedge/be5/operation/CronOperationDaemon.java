package com.developmentontheedge.be5.operation;

import java.util.Map;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.quartz.JobExecutionContext;
import org.quartz.JobDataMap;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import com.developmentontheedge.be5.util.Utils;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.meta.Meta;

import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;

import com.developmentontheedge.be5.scheduling.Be5Job;

public class CronOperationDaemon extends Be5Job
{
    private static final Logger log = Logger.getLogger( CronOperationDaemon.class.getName() );

    @Inject protected CoreUtils coreUtils;
    @Inject protected Meta meta;
    @Inject protected OperationService operationService;
    @Inject protected OperationExecutor operationExecutor;

    @Override
    public void doWork(JobExecutionContext context) throws Exception
    {
        JobDataMap data = context.getJobDetail().getJobDataMap();

        String config = data.getString( "configSectionName" ); 

        Map<String, String> settings = coreUtils.getSystemSettingsInSection( config );

        String entityName = coreUtils.getSystemSettingInSection( config, "entityName" );
        String queryName = coreUtils.getSystemSettingInSection( config, "queryName" );
        String operationName = coreUtils.getSystemSettingInSection( config, "operationName" );

        try
        {
            //log.info( "Beginning execution of operation " + entityName + "::" + operationName + "..." );

            OperationInfo operationInfo = new OperationInfo( meta.getOperation( entityName, queryName, operationName ) );
                
            OperationContext operationContext = operationExecutor.getOperationContext(
                    operationInfo, queryName, new HashMap() );

            Operation operation = operationExecutor.create( operationInfo, operationContext );

            HashMap presetValues = new HashMap();
            DynamicPropertySet params = ( DynamicPropertySet )operationExecutor.generate( operation, presetValues );

            if( params != null )
            {
                for( DynamicProperty prop : params )
                {
                    String value = settings.get( prop.getName() );
                    if( value == null )
                    {
                        continue;
                    }

                    prop.setValue( Utils.changeType( value, prop.getType() ) );
                    presetValues.put( prop.getName(), Utils.changeType( value, prop.getType() ) );
                }
            }  

            operationService.execute( operation, presetValues );

            //log.info( "Operation completed successfully." );
        }
        catch ( Exception e )
        {
            log.log( Level.SEVERE, "Error executing operation", e );
        }
    }
}
