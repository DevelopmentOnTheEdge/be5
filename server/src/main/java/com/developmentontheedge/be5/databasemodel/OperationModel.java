package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;


public interface OperationModel 
{
//    class OperationFailedException extends RuntimeException
//    {
//        OperationFailedException() {
//            super( "{{{Operation failed}}}" );
//        }
//
//        public OperationFailedException(Throwable e) {
//            super( e );
//        }
//    }

    DynamicPropertySet getParameters() throws Exception;

    Either<FormPresentation, OperationResult> execute();

    //OperationModel setSessionAdapter(Operation.SessionAdapter sessionAdapter);

    //OperationModel setQueueID(String queueID);

    OperationModel setRecords(String... records);

    OperationModel setFromQuery(String fromQuery);

    OperationModel setPresetValues(Map<String, Object> presetValues);

    //OperationModel setOut(Writer out);

    //OperationModel setOut(MessageHandler out);

    //void makeTemplate(String cronMask) throws Exception;
}
