package com.developmentontheedge.be5.operation.databasemodel;


public interface EntityModelAdapter<R extends RecordModel> extends EntityModel<R> {

    String getAdditionalConditions();
    
}
