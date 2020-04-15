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

    public String deployModel(String name, String model)
    {
        return deployModel(name, new ByteArrayInputStream(model.getBytes()));
    }

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

    public String startProcess(String processDefinitionKey, Map<String, Object> variables)
    {
        ProcessInstance pi = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processDefinitionKey, variables);

        return pi.getProcessInstanceId();
    }

    public String startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables)
    {
        ProcessInstance pi = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

        return pi.getProcessInstanceId();
    }

    @Override
    public ProcessDefinition getProcess(String key)
    {
        return processEngine.getRepositoryService()
                .createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult();
    }

    public List<Task> getActiveTasks()
    {
        return processEngine.getTaskService().createTaskQuery().active().list();
    }

    public List<Task> getTasks(String processDefinitionKey)
    {
        return processEngine.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey).list();
    }

    public List<Task> getTasksByBusinessKey(String businessKey)
    {
        return processEngine.getTaskService().createTaskQuery().processInstanceBusinessKey(businessKey).list();
    }

    public Task getTask(String taskId)
    {
        return processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    }

    public void completeTask(String taskId, Map<String, Object> variables)
    {
        processEngine.getTaskService().complete(taskId, variables);
    }

    public Map<String, Object> getVariables(String executionId)
    {
        return processEngine.getRuntimeService().getVariables(executionId);
    }

    public void setVariable(String executionId, String variableName, Object value)
    {
        processEngine.getRuntimeService().setVariable(executionId, variableName, value);
    }

    public void setVariableByBusinessKey(String businessKey, String variableName, Object value)
    {
        ProcessInstance pi = processEngine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey).active().singleResult();

        processEngine.getRuntimeService().setVariable(pi.getId(), variableName, value);
    }

    @Override
    public void createSignalEvent(String signalName)
    {
        processEngine.getRuntimeService().createSignalEvent(signalName);
    }
}
