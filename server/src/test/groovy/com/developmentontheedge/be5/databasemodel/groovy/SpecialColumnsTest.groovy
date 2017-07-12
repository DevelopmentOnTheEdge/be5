package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.databasemodel.EntityModel
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.*

import static org.junit.Assert.*

class SpecialColumnsTest extends AbstractProjectIntegrationH2Test
{
    DatabaseModel database = injector.get(DatabaseModel.class);
    def db = injector.getSqlService();
    EntityModel table = database.meters;
    def entityName = table.getEntityName()

    @BeforeClass
    static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)
    }

    @AfterClass
    static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Before
    void before(){
        db.update("DELETE FROM $entityName")
    }

    @Test
    void testInsert() throws Exception
    {
        def id = table << [
                "name": "test",
                "value": 1]

        assertEquals 1, db.getLong("select count(*) from $entityName")
    }

    @Test
    void testDelete()
    {
        def id = table << [
                "name": "test",
                "value": 1]

        assertEquals "no", table[ id ].isDeleted___

        table.remove(id);

        assertEquals "yes", table[ id ].isDeleted___

        assertTrue table[ id ].creationDate___ < table[ id ].modificationDate___
        assertEquals table[ id ].whoModified___, table[ id ].whoInserted___
    }

}
