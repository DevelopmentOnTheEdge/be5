package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.databasemodel.OperationModel;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

//todo need Request
class OperationModelBase implements OperationModel
{
    private OperationService operationService;
    private OperationInfo operationInfo;
    //private DynamicPropertySet parameters;
    //private SessionAdapter sessionAdapter;

    private String[] records = new String[]{ };

    private String queueID;
    private String fromQuery;
    private String tcloneId;

    private Writer out = new StringWriter();
    //private MessageHandler output = new OutputMessageHandler( out );
    private Map<String, Object> presetValues = Collections.emptyMap();

    OperationModelBase( OperationService operationService, OperationInfo operationInfo )
    {
        this.operationService = operationService;
        this.operationInfo = operationInfo;
    }

//        @Override
//        public OperationModel setSessionAdapter( SessionAdapter sessionAdapter ) { this.sessionAdapter = sessionAdapter; return this; }

//    @Override
//    public OperationModel setQueueID( String queueID ) { this.queueID = queueID; return this; }

    @Override
    public OperationModel setRecords( String ... records ) { this.records = records; return this; }

    @Override
    public OperationModel setFromQuery( String fromQuery ) { this.fromQuery = fromQuery; return this; }

    @Override
    public OperationModel setPresetValues( Map<String, Object> presetValues ) { this.presetValues = presetValues; return this; }

//    @Override
//    public OperationModel setOut( Writer out ) { this.out = out; return this; }

//        @Override
//        public OperationModel setOut( MessageHandler out ) { this.output = out; return this; }

    @Override
    public DynamicPropertySet getParameters() throws Exception
    {
        return null;//operationService.generate()
    }

//    private DynamicPropertySet getParametersImpl() throws Exception
//    {
//        Operation operation = loadOperation();
////            DynamicPropertySet parameters = ( DynamicPropertySet )operation.getParameters( out, connector, presetValues );
////            OperationFragmentHelper.invokeExtenders( "getParameters", out, connector, operation, parameters, presetValues );
//        return parameters;
//    }

    @Override
    public Either<FormPresentation, OperationResult> execute()
    {
        return null;//
    }

//    private void invokeImpl()
//    {
//        try
//        {
            //parameters = getParameters();
//                OperationFragmentHelper.invokeExtenders( "preInvoke", out, connector, operation, null, null );
//                if( !Boolean.TRUE.equals( OperationFragmentHelper.invokeExtenders( "skipInvoke", out, connector, operation, null, null ) ) )
//                {
//                    if( operation instanceof OfflineOperation )
//                    {
//                        ( ( OfflineOperation )operation ).invoke( output, connector );
//                    }
//                    else
//                    {
//                        operation.invoke( out, connector );
//                    }
//                    OperationFragmentHelper.invokeExtenders( "postInvoke", out, connector, operation, null, null );
//                }
//                else
//                {
//                    OperationFragmentHelper.invokeExtenders( "getSkipInvokeReason", out, connector, operation, null, null );
//                }
//        }
//        catch( Exception e )
//        {
//            throw new OperationFailedException( e );
//        }
//            if( Operation.Status.ERROR == operation.getResult() )
//            {
//                throw new OperationFailedException();
//            }
//    }

//    @Override
//    public void makeTemplate( String cronMask ) throws Exception
//    {
//            String description = this.name + " [" + this.name + "]"; // TODO: origName
//            QRec template = null;
//            try
//            {
//                template = new QRec( connector, "SELECT ID FROM operationQueue " +
//                                                "WHERE description = " + Utils.safeIdValue( connector, "operationQueue", "description", description ) +
//                                                " AND status = " + Utils.safeIdValue( connector, "operationQueue", "status", Operation.Status.TEMPLATE ) );
//            }
//            catch( NoRecord noRecord )
//            {
//                // it`s ok
//            }
//
//            if( template == null )
//            {
//                String opLogId = Utils.insert( connector, userInfo, "operationLogs", ImmutableMap.of(
//                        "table_name", getTableName(),
//                        "operation_name", this.name,
//                        "user_name", userInfo.getUserName(),
//                        "localeString", userInfo.getLocale(),
//                        "executeDat", Utils.currentDate()
//                ) );
//
//                Utils.insert( connector, userInfo, "operationLogParams", ImmutableMap.of(
//                        "type", "preset",
//                        "paramName", HttpConstants.TABLE_NAME_PARAM,
//                        "paramValue", getTableName(),
//                        "operLogID", opLogId
//                ) );
//
//                for( Map.Entry<?, ?> pvItem : presetValues.entrySet() )
//                {
//                    Utils.insert( connector, userInfo, "operationLogParams", ImmutableMap.of(
//                            "type", "preset",
//                            "paramName", pvItem.getKey(),
//                            "paramValue", pvItem.getValue(),
//                            "operLogID", opLogId
//                    ) );
//                }
//
//                Utils.insert( connector, userInfo, "operationQueue", ImmutableMap.of(
//                        "description", description,
//                        "operLogId", opLogId,
//                        "cronMaskForTemplate", cronMask,
//                        "status", Operation.Status.TEMPLATE,
//                        "slaveNo", WebAppInitializer.getSlaveNo()
//                ) );
//            }
//            else
//            {
//                Utils.update( connector, userInfo, "operationQueue", new String[]{ template.getString() }, ImmutableMap.of(
//                        "cronMaskForTemplate", cronMask,
//                        "user_name", userInfo.getUserName()
//                ) );
//            }
//    }
}