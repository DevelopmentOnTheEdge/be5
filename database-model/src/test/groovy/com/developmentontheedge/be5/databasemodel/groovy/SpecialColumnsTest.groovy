package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.base.exceptions.Be5Exception
import com.developmentontheedge.be5.database.DbService
import com.developmentontheedge.be5.databasemodel.DatabaseModel
import com.developmentontheedge.be5.databasemodel.DatabaseModelProjectDbTest
import com.developmentontheedge.be5.databasemodel.EntityModel
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.testbase.StaticUserInfoProvider
import com.developmentontheedge.beans.BeanInfoConstants
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import javax.inject.Inject
import java.util.stream.Collectors
import java.util.stream.StreamSupport

import static org.junit.Assert.*

class SpecialColumnsTest extends DatabaseModelProjectDbTest {
    @Inject
    private DatabaseModel database
    @Inject
    private DbService db

    EntityModel table
    String tableName

    @Before
    void beforeClass() {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)
        StaticUserInfoProvider.userInfo.setRemoteAddr("192.168.0.1")
    }

    @Before
    void before() {
        table = database.meters
        tableName = table.getEntityName()
        db.update("DELETE FROM $tableName")
    }

    @Test
    void testInsert() {
        def id = table << [
                name : "test",
                value: (Short) 1
        ]

        assertEquals 1, db.oneLong("select count(*) from $tableName")

        setStaticUserInfo(RoleType.ROLE_GUEST)
        table << [
                name : "test2",
                value: (Short) 2
        ]
    }

    @Test
    void testPropertyNamesFromYaml() {
        def id = table << [
                "name" : "test",
                "value": (Short) 1]

        def propertyList = StreamSupport.stream(table.get(id).spliterator(), false)
                .map({ p -> p.getName() }).collect(Collectors.toList())

        assertEquals(["ID", "name", "value", "whoInserted___", "whoModified___",
                      "creationDate___", "modificationDate___",
                      "ipInserted___", "ipModified___", "isDeleted___"], propertyList)
    }

    @Test
    void useGetDpsWithoutSelectingTags() {
        def id = table << [
                "name" : "test",
                "value": (Short) 1]

        assertNull table.get(id).getProperty("isDeleted___").getAttribute(BeanInfoConstants.TAG_LIST_ATTR)
    }

    @Test
    void testDelete() {
        def id = table << [
                "name" : "test",
                "value": (Short) 1]

        assertEquals "no", table[id].$isDeleted___

        Thread.sleep(1)
        table.remove(id)

        assertEquals "yes", table[id].$isDeleted___

        assertTrue table[id].$creationDate___ < table[id].$modificationDate___
        assertEquals table[id].$whoModified___, table[id].$whoInserted___
    }
//
//    @Test
//    void testSetMany()
//    {
//        def id1 = table << [ "name": "TestName", "value": 1]
//        def id2 = table << [ "name": "TestName", "value": 2]
//        def id3 = table << [ "name": "TestName2", "value": 2]
//
//        table.setMany(["isDeleted___": "yes"], ["name": "TestName"])
//
//        assertEquals "yes", table[ id1 ].$isDeleted___
//        assertEquals "yes", table[ id2 ].$isDeleted___
//        assertEquals "no",  table[ id3 ].$isDeleted___

//TODO
//        assertTrue table[ id1 ].creationDate___     <  table[ id1 ].modificationDate___
//        assertTrue table[ id2 ].creationDate___     <  table[ id2 ].modificationDate___
//
//        assertTrue table[ id1 ].modificationDate___ == table[ id2 ].modificationDate___
//    }

    @Test
    void testDeleteAll() {
        table << ["name": "TestName", "value": (Short) 1]
        table << ["name": "TestName", "value": (Short) 2]
        table << ["name": "TestName2", "value": (Short) 2]

        table.removeBy(["name": "TestName"])

        assertEquals 2, db.oneLong("SELECT count(*) FROM $tableName WHERE isDeleted___ = ? AND name = ? ",
                "yes", "TestName")
        assertEquals 1, db.oneLong("SELECT count(*) FROM $tableName WHERE isDeleted___ = ? AND name = ? ",
                "no", "TestName2")
    }

    @Test
    void testEdit() {
        def id = table << [
                "name" : "test",
                "value": (Short) 1
        ]

        assertEquals "192.168.0.1", table[id].$ipInserted___
        assertEquals "192.168.0.1", table[id].$ipModified___

        Thread.sleep(1)

        StaticUserInfoProvider.userInfo.setRemoteAddr("192.168.0.2")

        table[id] = [
                "name": "editName",
        ]

        assertEquals "editName", table[id].$name

        assertEquals "192.168.0.1", table[id].$ipInserted___
        assertEquals "192.168.0.2", table[id].$ipModified___

        assertTrue table[id].$creationDate___ < table[id].$modificationDate___
        assertEquals table[id].$whoModified___, table[id].$whoInserted___
        assertEquals "no", table[id].$isDeleted___
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none()

    @Test
    void checkDpsColumnsTest() {
        expectedEx.expect(Be5Exception.class)
        expectedEx.expectMessage("Dps columns errors for modelElements 'meters'\n" +
                "Dps not contain notNull column 'value'\n" +
                "Entity not contain column 'value2'")

        table << [
                "name"  : "test",
                "value2": (Short) 1
        ]
    }
}
