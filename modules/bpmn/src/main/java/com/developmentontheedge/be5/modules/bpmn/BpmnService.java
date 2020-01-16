package com.developmentontheedge.be5.modules.bpmn;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
    public String startProcess(String key, Map<String, Object> variables);

    /**
     * @return list of current active tasks.
     */
    public List<Task> getActiveTasks();

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
}