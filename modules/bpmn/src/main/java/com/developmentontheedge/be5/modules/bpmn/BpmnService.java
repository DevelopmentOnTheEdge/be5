package com.developmentontheedge.be5.modules.bpmn;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;

public interface BpmnService
{
    /**
     * Deploys BPMN model.
     *
     * @return key for deployed model.
     */
    public String deployModel(String name, String model);

    /**
     * Deploys BPMN model from the stream.
     *
     * @return key for deployed model.
     */
    public String deployModel(String name, InputStream is);

    /**
     * Delete BPMN model with the specified id and all related resources.
     */
    public void deleteModel(String key);

    /**
     * Starts BPMN model with the specified id and variables.
     *
     * @return key for started process instance.
     */
    public String startProcess(String processDefinitionKey, Map<String, Object> variables);

    /**
     * Starts BPMN model with the specified id and variables.
     *
     * @return key for started process instance.
     */
    public String startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    /**
     * Starts BPMN model with the specified id and variables.
     *
     * @return key for started process instance.
     */
    public ProcessDefinition getProcess(String key);

    /**
     * @return list of all processes current active tasks.
     */
    public List<Task> getActiveTasks();

    /**
     * @param processDefinitionKey
     * @return list of tasks for process
     */
    public List<Task> getTasks(String processDefinitionKey);

    /**
     * @param businessKey
     * @return list of tasks for process
     */
    public List<Task> getTasksByBusinessKey(String businessKey);

    /**
     * @param taskId
     * @return Task
     */
    public Task getTask(String taskId);

    /**
     * Completes the specified task.
     */
    public void completeTask(String taskId, Map<String, Object> variables);

    /**
     * @return variables for specified execution (task or process).
     */
    public Map<String, Object> getVariables(String executionId);

    /** Set up variable value for specified execution. */
    public void setVariable(String executionId, String variableName, Object value);

    /** Set up variable value for specified execution. */
    public void setVariableByBusinessKey(String businessKey, String variableName, Object value);

    /** Create signal event for notification. */
    public void createSignalEvent(String signalName);

}
