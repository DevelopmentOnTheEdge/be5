package com.developmentontheedge.be5.modules.core.genegate.repositories;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.modules.core.genegate.Repository;
import com.developmentontheedge.be5.modules.core.genegate.entities.Provinces;
import com.developmentontheedge.be5.modules.core.genegate.fields.ProvincesFields;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.HashMap;
import java.util.Map;


public class ProvincesRepository implements Repository<Provinces, String>
{
    private EntityModelBase entityModelBase;

    public ProvincesFields fields;

    public ProvincesRepository(SqlService db, DpsHelper dpsHelper, Validator validator, Entity entity)
    {
        entityModelBase = new EntityModelBase(db, dpsHelper, validator, entity);
    }

    @Override
    public String add(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class) final Closure config)
    {
        Provinces entity = new Provinces();
        Closure code = config.rehydrate(entity, entity, entity);//todo need this
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();

        Map<String, Object> values = new HashMap<>();
        if(entity.ID != null)values.put(fields.ID, entity.ID);
        if(entity.name != null)values.put(fields.name, entity.name);
        if(entity.countryID != null)values.put(fields.countryID, entity.countryID);

        return entityModelBase.add(values);
    }

    @Override
    public Provinces findOne(String primaryKey) {
        RecordModel dynamicProperties = entityModelBase.get(primaryKey);

        Provinces provinces = new Provinces();
        provinces.ID = (String) dynamicProperties.getValue(fields.ID);
        provinces.name = (String) dynamicProperties.getValue(fields.name);
        provinces.countryID = (String) dynamicProperties.getValue(fields.countryID);

        return provinces;
    }

    public Entity getEntity()
    {
        return entityModelBase.getEntity();
    }
}
