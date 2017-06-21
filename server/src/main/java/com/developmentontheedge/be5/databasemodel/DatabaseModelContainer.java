package com.developmentontheedge.be5.databasemodel;

public interface DatabaseModelContainer {

    EntityAccess<EntityModel<RecordModel>> getDatabase();
    void setDatabase(EntityAccess<EntityModel<RecordModel>> database);
    
}
