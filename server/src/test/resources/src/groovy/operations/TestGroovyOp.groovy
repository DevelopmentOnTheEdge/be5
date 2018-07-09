package src.groovy.operations

import com.developmentontheedge.be5.base.services.UserAwareMeta
import com.developmentontheedge.be5.base.services.impl.UserAwareMetaImpl
import com.developmentontheedge.be5.base.util.DpsUtils
import com.developmentontheedge.be5.operation.model.Operation
import com.developmentontheedge.be5.server.operations.support.GOperationSupport

import javax.inject.Inject
import java.sql.Date

class TestGroovyOp extends GOperationSupport implements Operation
{
    @Inject
    private UserAwareMeta userAwareMeta

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if (!userAwareMeta instanceof UserAwareMetaImpl) throw new RuntimeException()

        params << [
                name        : "name",
                DISPLAY_NAME: "Имя",
                value       : "Test"
        ]

        params << [
                name        : "beginDate",
                DISPLAY_NAME: "Дата начала",
                TYPE        : Date,
                value       : "2017-07-01"
        ]

        params << [
                name         : "reason",
                DISPLAY_NAME : "Причина снятия предыдущего работника",
                TAG_LIST_ATTR: [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                value        : "vacation"
        ]

        params << [
                name                   : "reasonMulti",
                TYPE                   : String,
                DISPLAY_NAME           : "Множественный выбор",
                MULTIPLE_SELECTION_LIST: true,
                TAG_LIST_ATTR          : [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                value                  : ["vacation", "sick"] as String[]
        ]

        //@Deprecated
        params.getProperty("beginDate") << [READ_ONLY: true]
        //params.edit("beginDate") { READ_ONLY = true }

//        params.add {
//            name = "name"
//            DISPLAY_NAME = "Имя"
//            value = "Test"
//        }
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
//        java.util.Date utilDate = df.parse("2017-07-01")
//
//        params.add {
//            name = "beginDate"
//            DISPLAY_NAME = "Дата начала"
//            TYPE = Date
//            value = new Date(utilDate.getTime())
//        }
//
//        params.add {
//            name = "reason"
//            DISPLAY_NAME = "Причина снятия предыдущего работника"
//            TAG_LIST_ATTR = [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][]
//            value = "vacation"
//        }
//
//        params.add {
//            name = "reasonMulti"
//            DISPLAY_NAME = "Множественный выбор"
//            MULTIPLE_SELECTION_LIST = true
//            TAG_LIST_ATTR = [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][]
//            value = ["vacation", "sick"] as String[]
//        }
//
//        params.edit("beginDate") { READ_ONLY = true }

        return DpsUtils.setValues(params, presetValues)
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        db.update("update fakeTable set name = ?,beginDate = ?,reason = ?", params.$name, params.$beginDate, params.$reason)
    }

}
