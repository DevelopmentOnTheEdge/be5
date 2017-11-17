package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.*;


public class GroovyOperationTest extends SqlMockOperationTest
{
    @Test
    public void emptyValues()
    {
        Either<FormPresentation, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestGroovyOp", "0","{}");

        assertEquals("{'values':{'name':'Test','beginDate':'2017-07-01','reason':'vacation','reasonMulti':['vacation','sick']},'meta':{'/name':{'displayName':'Имя'},'/beginDate':{'displayName':'Дата начала','type':'Date','readOnly':true},'/reason':{'displayName':'Причина снятия предыдущего работника','tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]},'/reasonMulti':{'displayName':'Множественный выбор','type':'String[]','multipleSelectionList':true,'tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]}},'order':['/name','/beginDate','/reason','/reasonMulti']}",
                oneQuotes(generate.getFirst().getBean().toString()));
    }

    @Test
    public void getParametersTest()
    {
        Either<FormPresentation, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestGroovyOp", "0",
                        doubleQuotes("{'beginDate':'2017-12-20','name':'testValue','reason':'fired'," +
                                "'reasonMulti':['fired','other']}"));

        assertEquals("{'values':{'name':'testValue','beginDate':'2017-07-01','reason':'fired','reasonMulti':['fired','other']},'meta':{'/name':{'displayName':'Имя'},'/beginDate':{'displayName':'Дата начала','type':'Date','readOnly':true},'/reason':{'displayName':'Причина снятия предыдущего работника','tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]},'/reasonMulti':{'displayName':'Множественный выбор','type':'String[]','multipleSelectionList':true,'tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]}},'order':['/name','/beginDate','/reason','/reasonMulti']}",
                oneQuotes(generate.getFirst().getBean().toString()));
    }

    @Test
    public void getLayout()
    {
        Either<FormPresentation, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestGroovyOp", "0","{}");
        assertEquals(ImmutableMap.of("type","custom", "name","addresses"),
                generate.getFirst().getLayout());
    }

}