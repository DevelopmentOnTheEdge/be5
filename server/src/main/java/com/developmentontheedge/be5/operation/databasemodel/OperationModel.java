package com.developmentontheedge.be5.operation.databasemodel;


import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.io.Writer;
import java.util.Map;

public interface OperationModel 
{
    class OperationFailedException extends RuntimeException
    {
        OperationFailedException() {
            super( "{{{Operation failed}}}" );
        }

        public OperationFailedException(Throwable e) {
            super( e );
        }
    }

    DynamicPropertySet getParameters() throws Exception;

    String invoke() throws Exception;

    //OperationModel setSessionAdapter(Operation.SessionAdapter sessionAdapter);

    OperationModel setQueueID(String queueID);

    OperationModel setRecords(String... records);

    OperationModel setFromQuery(String fromQuery);

    OperationModel setPresetValues(Map<String, String> presetValues);

    OperationModel setOut(Writer out);

    //OperationModel setOut(MessageHandler out);

    void makeTemplate(String cronMask) throws Exception;
}
