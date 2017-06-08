package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.env.Injector;

import java.io.Serializable;
import java.util.Map;

public interface Operation extends Serializable
{
	
	///////////////////////////////////////////////////////////////////
	// Main interface
	//

    void initialize(Injector injector, OperationInfo meta,
                    OperationResult operationResult);

	/**
	 * Returns meta-information from project definition for this operation.
	 */
	OperationInfo getInfo();
	
	/**
	 * Returns Java bean or {@link com.developmentontheedge.beans.DynamicPropertySet}.
	 * 
	 * @param presetValues - map of preset values
	 */
    Object getParameters(Map<String, String> presetValues) throws Exception;

    /**
     * Invokes the operation with the specified parameters.
     *
     * @param parameters
     * @param context
     */
    void invoke(Object parameters, OperationContext context) throws Exception;
    
    /**
     * Set flag (Thread.interrupt) to interrupt the operation.
     */
    void interrupt();

    /**
     * Returns current status of the operation 
     */
    OperationStatus getStatus();

    /**
     * Returns {@link OperationResult}.
     * This function can be called several times. If operation is not completed, 
     * then it returns  
     *
     */
    OperationResult getResult();

    OperationContext getContext();

    void setResult(OperationResult operationResult);
}
