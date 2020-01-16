package com.developmentontheedge.be5.modules.bpmn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

public class CamundaBpmnServiceImpl implements BpmnService
{
    public static String BPMN_SUFFIX = ".bpmn";

    static ProcessEngine processEngine;

    public CamundaBpmnServiceImpl() {
        InputStream cfg = this.getClass().getClassLoader().getResourceAsStream("camunda.cfg.xml");

        ProcessEngineConfiguration pec = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromInputStream(cfg)
                .setJdbcDriver("org.postgresql.Driver");
        processEngine = pec.buildProcessEngine();
    }

    /**
     * Deploys BPMN model.
     *
     * Model name should end with ".bpmn". The method checks it and adds ".bpmn" if needed.
     *
     * @return key (process definition key) for deployed model.
     */
    public String deployModel(String name, String model)
    {
        return deployModel(name, new ByteArrayInputStream(model.getBytes()));
    }

    /**
     * Deploys BPMN model from the stream.
     *
     * Model name should end with ".bpmn". The method checks it and adds ".bpmn" if needed.
     *
     * @return id for deployed model.
     */
    public String deployModel(String name, InputStream is)
    {
        if (!name.endsWith(BPMN_SUFFIX))
            name += BPMN_SUFFIX;

        Deployment deployment = processEngine.getRepositoryService().createDeployment().addInputStream(name, is).deploy();

        ProcessDefinition pd = processEngine.getRepositoryService()
                .createProcessDefinitionQuery().deploymentId(deployment.getId())
                .singleResult();

        return pd.getKey();
    }

    /**
     * Delete BPMN model with the specified key (process definition key) and all
     * related resources (cascade deletion to process instances, history process
     * instances and jobs).
     *
     * If several versions exist, all versions will be deleted.
     */
    public void deleteModel(String key)
    {
        List<ProcessDefinition> pds = processEngine.getRepositoryService()
                .createProcessDefinitionQuery().processDefinitionKey(key)
                .list();

        for (ProcessDefinition pd : pds)
        {
            processEngine.getRepositoryService().deleteDeployment(pd.getDeploymentId(), true);
        }
    }

    /**
     * Starts BPMN model with the specified key (process definition key) and
     * variables.
     *
     * If several versions exist, the latest version will be started.
     *
     * @return id for started process instance.
     */
    public String startProcess(String key, Map<String, Object> variables)
    {
        ProcessDefinition pd = processEngine.getRepositoryService()
                .createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult();

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceById(pd.getId(), variables);

        return pi.getProcessInstanceId();
    }

    /**
     * @return list of current active tasks.
     */
    public List<Task> getActiveTasks()
    {
        return processEngine.getTaskService().createTaskQuery().active().list();
    }

    /**
     * Completes the specified task.
     */
    public void completeTask(String taskId, Map<String, Object> variables)
    {
        processEngine.getTaskService().complete(taskId, variables);
    }

    /**
     * @return variables for specified execution (task or process).
     */
    public Map<String, Object> getVariables(String executionId)
    {
        return processEngine.getRuntimeService().getVariables(executionId);
    }

    /** Set up variable value for specified execution. */
    public void setVariable(String executionId, String variableName, Object value)
    {
        processEngine.getRuntimeService().setVariable(executionId, variableName, value);
    }

}
