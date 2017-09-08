package src.groovy.operations

import com.developmentontheedge.be5.api.helpers.UserAwareMeta
import com.developmentontheedge.be5.api.helpers.impl.UserAwareMetaImpl
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

import java.sql.Date
import java.text.SimpleDateFormat

class TestGroovyOp extends OperationSupport implements Operation
{
    @Inject private UserAwareMeta userAwareMeta

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if(!userAwareMeta instanceof UserAwareMetaImpl) throw new RuntimeException()

        dps << [
                name         : "name",
                DISPLAY_NAME : "Имя",
                value        : "Test"
        ]

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        java.util.Date utilDate = df.parse("2017-07-01")

        dps << [
                name         : "beginDate",
                DISPLAY_NAME : "Дата начала",
                TYPE         : Date,
                value        : new Date(utilDate.getTime())
        ]

        dps << [
                name         : "reason",
                DISPLAY_NAME : "Причина снятия предыдущего работника",
                TAG_LIST_ATTR: [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                value        : "vacation"
        ]

        dps << [
                name                   : "reasonMulti",
                DISPLAY_NAME           : "Множественный выбор",
                MULTIPLE_SELECTION_LIST: true,
                TAG_LIST_ATTR          : [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                value                  : ["vacation","sick"] as String[]
        ]

        dps["beginDate"] << [READ_ONLY: true]

        dpsHelper.setValues(dps, presetValues)

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }

    @Override
    Object getLayout() {
        return [type:'custom', name:'addresses']
    }
}
