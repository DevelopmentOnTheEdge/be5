package com.developmentontheedge.be5.databasemodel;


public interface EntityModelAdapter<R extends RecordModel> extends EntityModel<R> {

    String getAdditionalConditions();
    
}
