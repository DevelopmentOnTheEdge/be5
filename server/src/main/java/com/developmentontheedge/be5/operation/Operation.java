package com.developmentontheedge.be5.operation;

import java.io.Serializable;
import java.util.Map;


public interface Operation extends Serializable
{
	///////////////////////////////////////////////////////////////////
	// Main interface
	//

    void initialize(OperationInfo info, OperationContext context, OperationResult operationResult);

	/**
	 * Returns meta-information from project definition for this operation.
	 */
	OperationInfo getInfo();

    /**
     * Returns context of operation.
     */
    OperationContext getContext();

    /**
	 * Returns Java bean or {@link com.developmentontheedge.beans.DynamicPropertySet}.
	 * 
	 * @param presetValues - map of preset values
	 */
    Object getParameters(Map<String, Object> presetValues) throws Exception;

    /**
     * Invokes the operation with the specified parameters.
     *
     * @param parameters {@link #getParameters(Map)} result
     */
    void invoke(Object parameters) throws Exception;
    
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

    void setResult(OperationResult operationResult);

    void addRedirectParams(Map<String, ?> extra);

    void addRedirectParam(String name, Object value);

    Map<String, Object> getRedirectParams();

    //todo Map<String, String> validate( Object parameters );
}
