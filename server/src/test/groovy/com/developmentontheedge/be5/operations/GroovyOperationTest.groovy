package com.developmentontheedge.be5.operations

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.SqlMockOperationTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.be5.util.Either
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify


class GroovyOperationTest extends SqlMockOperationTest
{
    @Test
    void emptyValues()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestGroovyOp", "0","{}")

        assertEquals("{'values':{'name':'Test','beginDate':'2017-07-01','reason':'vacation','reasonMulti':['vacation','sick']},'meta':{'/name':{'displayName':'Имя'},'/beginDate':{'displayName':'Дата начала','type':'Date','readOnly':true},'/reason':{'displayName':'Причина снятия предыдущего работника','tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]},'/reasonMulti':{'displayName':'Множественный выбор','multipleSelectionList':true,'tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]}},'order':['/name','/beginDate','/reason','/reasonMulti']}",
                oneQuotes(JsonFactory.bean(generate.getFirst())))
    }

    @Test
    void getParametersTest()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestGroovyOp", "0",
                        ['beginDate':'2017-12-20', 'name':'testValue', 'reason':'fired', 'reasonMulti':['fired','other'] as String[]])

        assertEquals("{'values':{'name':'testValue','beginDate':'2017-07-01','reason':'fired','reasonMulti':['fired','other']},'meta':{'/name':{'displayName':'Имя'},'/beginDate':{'displayName':'Дата начала','type':'Date','readOnly':true},'/reason':{'displayName':'Причина снятия предыдущего работника','tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]},'/reasonMulti':{'displayName':'Множественный выбор','multipleSelectionList':true,'tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]}},'order':['/name','/beginDate','/reason','/reasonMulti']}",
                oneQuotes(JsonFactory.bean(generate.getFirst())))
    }

    @Test
    void execute()
    {
        Operation operation = getOperation("testtableAdmin", "All records", "TestGroovyOp", "0")
        Either<Object, OperationResult> generate = executeOperation(operation,
                ['beginDate':'2017-12-20','name':'testValue','reason':'fired', 'reasonMulti':['fired','other'] as String[]])

        assertEquals(OperationStatus.REDIRECTED, operation.getStatus())

        verify(SqlServiceMock.mock).update(eq("update fakeTable set name = ?,beginDate = ?,reason = ?"),
                eq("testValue"),
                eq(parseDate("2017-07-01")),
                eq("fired"))
    }

}