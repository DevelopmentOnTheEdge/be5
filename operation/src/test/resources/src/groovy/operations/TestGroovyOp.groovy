package src.groovy.operations

import com.developmentontheedge.be5.base.services.UserAwareMeta
import com.developmentontheedge.be5.base.services.impl.UserAwareMetaImpl
import com.developmentontheedge.be5.operation.model.Operation
import com.developmentontheedge.be5.operation.support.TestOperationSupport
import com.developmentontheedge.beans.DynamicPropertySetSupport

import javax.inject.Inject
import java.sql.Date

class TestGroovyOp extends TestOperationSupport implements Operation
{
    @Inject
    private UserAwareMeta userAwareMeta

    DynamicPropertySetSupport dps = new DynamicPropertySetSupport()

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if (!userAwareMeta instanceof UserAwareMetaImpl) throw new RuntimeException()

        dps << [
                name        : "name",
                DISPLAY_NAME: "Имя",
                value       : presetValues.getOrDefault("name", "Test")
        ]

        dps << [
                name        : "beginDate",
                DISPLAY_NAME: "Дата начала",
                TYPE        : Date,
                value       : presetValues.getOrDefault("beginDate", "2017-07-01")
        ]

        dps << [
                name         : "reason",
                DISPLAY_NAME : "Причина снятия предыдущего работника",
                TAG_LIST_ATTR: [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                value        : presetValues.getOrDefault("reason", "vacation")
        ]

        dps << [
                name                   : "reasonMulti",
                TYPE                   : String,
                DISPLAY_NAME           : "Множественный выбор",
                MULTIPLE_SELECTION_LIST: true,
                TAG_LIST_ATTR          : [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][],
                value                  : presetValues.getOrDefault("reasonMulti", ["vacation", "sick"] as String[])
        ]

        //@Deprecated
        dps.getProperty("beginDate") << [READ_ONLY: true]
        //dps.edit("beginDate") { READ_ONLY = true }

//        dps.add {
//            name = "name"
//            DISPLAY_NAME = "Имя"
//            value = "Test"
//        }
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
//        java.util.Date utilDate = df.parse("2017-07-01")
//
//        dps.add {
//            name = "beginDate"
//            DISPLAY_NAME = "Дата начала"
//            TYPE = Date
//            value = new Date(utilDate.getTime())
//        }
//
//        dps.add {
//            name = "reason"
//            DISPLAY_NAME = "Причина снятия предыдущего работника"
//            TAG_LIST_ATTR = [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][]
//            value = "vacation"
//        }
//
//        dps.add {
//            name = "reasonMulti"
//            DISPLAY_NAME = "Множественный выбор"
//            MULTIPLE_SELECTION_LIST = true
//            TAG_LIST_ATTR = [["fired", "Уволен"], ["vacation", "Отпуск"], ["sick", "На больничном"], ["other", "Иная причина"]] as String[][]
//            value = ["vacation", "sick"] as String[]
//        }
//
//        dps.edit("beginDate") { READ_ONLY = true }

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        db.update("update fakeTable set name = ?,beginDate = ?,reason = ?", dps.$name, dps.$beginDate, dps.$reason)
    }

}
