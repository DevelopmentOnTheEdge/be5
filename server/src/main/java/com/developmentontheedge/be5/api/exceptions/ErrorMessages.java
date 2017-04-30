package com.developmentontheedge.be5.api.exceptions;

import static com.developmentontheedge.be5.api.exceptions.Be5ErrorCode.*;

import java.util.EnumMap;

public class ErrorMessages
{
	private static final EnumMap<Be5ErrorCode, String> MESSAGES = new EnumMap<>(Be5ErrorCode.class);
	
	static
	{
		MESSAGES.put(UNKNOWN_ENTITY, "Entity not found: $1");
		MESSAGES.put(UNKNOWN_QUERY, "Query not found: $1.$2");
		MESSAGES.put(UNKNOWN_OPERATION, "Operation not found: $1.$2");
		MESSAGES.put(NO_OPERATION_IN_QUERY, "Operation $1.$3 is not available in query $2");
		MESSAGES.put(UNKNOWN_COMPONENT, "Component not found: $1");
		MESSAGES.put(INTERNAL_ERROR, "Internal error occured: $1");
		MESSAGES.put(PARAMETER_ABSENT, "Invalid request: parameter $1 is missing");
		MESSAGES.put(PARAMETER_EMPTY, "Invalid request: parameter $1 is empty");
		MESSAGES.put(PARAMETER_INVALID, "Invalid request: parameter $1 has unacceptable value $2");
		MESSAGES.put(STATE_INVALID, "Invalid state: $1");
		MESSAGES.put(NOT_INITIALIZED, "Server is not properly initialized: try again later");
		MESSAGES.put(ACCESS_DENIED, "Access denied");
		MESSAGES.put(ACCESS_DENIED_TO_QUERY, "Access denied to query $1.$2");
		MESSAGES.put(ACCESS_DENIED_TO_OPERATION, "Access denied to operation $1.$2");
		MESSAGES.put(INTERNAL_ERROR_IN_OPERATION, "Internal error occured during operation $1.$2: $3");
        MESSAGES.put(INTERNAL_ERROR_IN_QUERY, "Internal error occured during query $1.$2: $3");
        MESSAGES.put(NOT_FOUND, "Element not found: $1");
		
        if (MESSAGES.size() < Be5ErrorCode.values().length)
        {
            throw new InternalError("Not all error codes have messages!");
        }
	}
	
	public static String formatMessage(Be5ErrorCode code, Object... parameters)
	{
		String message = MESSAGES.get(code);
        
		if (message == null)
        {
            message = code.toString();
        }
        
        for (int i = 0; i < parameters.length; i++)
        {
            message = message.replace("$" + (i + 1), String.valueOf(parameters[i]));
        }
        
		return message;
	}
	
}
