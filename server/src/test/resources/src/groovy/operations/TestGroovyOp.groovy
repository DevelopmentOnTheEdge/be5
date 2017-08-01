package src.groovy.operations

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

import java.sql.Date
import java.text.SimpleDateFormat

class TestGroovyOp extends OperationSupport implements Operation
{

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps << [
                name        : "name",
                DISPLAY_NAME: "Имя",
                DEFAULT_VALUE: "Test"
        ]

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        java.util.Date utilDate = df.parse("2017-07-01")

        dps << [
                name         : "beginDate",
                DISPLAY_NAME : "Дата начала",
                TYPE         : Date,
                DEFAULT_VALUE: new Date(utilDate.getTime())
        ]

        dps << [
                name         : "reason",
                DISPLAY_NAME : "Причина снятия предыдущего работника",
                TAG_LIST_ATTR: [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                DEFAULT_VALUE: "vacation"
        ]
        //TODO add mock test TAG_LIST_ATTR: operationHelper.getTags("testtableAdmin", "value", "name")

        dps << [
                name                   : "reasonMulti",
                DISPLAY_NAME           : "Множественный выбор",
                MULTIPLE_SELECTION_LIST: true,
                TAG_LIST_ATTR          : [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                DEFAULT_VALUE          : ["vacation","sick"] as String[]
        ]

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        //String sql = generateSql( connector, false );
        //db.insert(sql);
    }

}
