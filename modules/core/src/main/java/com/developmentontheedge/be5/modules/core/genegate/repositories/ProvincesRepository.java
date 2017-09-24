package com.developmentontheedge.be5.modules.core.genegate.repositories;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.modules.core.genegate.RepositorySupport;
import com.developmentontheedge.be5.modules.core.genegate.entities.Provinces;
import com.developmentontheedge.be5.modules.core.genegate.fields.ProvincesFields;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstSelect;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.util.HashMap;
import java.util.Map;


public class ProvincesRepository extends RepositorySupport<Provinces, String>
{
    public ProvincesRepository(SqlService db, DpsHelper dpsHelper, Validator validator, Entity entity)
    {
        super(db, dpsHelper, validator, entity);

        this.primaryKeyName = ProvincesFields.ID;
        beanHandler = new BeanHandler<>(Provinces.class);
        beanListHandler = new BeanListHandler<>(Provinces.class);
    }

    @Override
    public String add(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class) final Closure values)
    {
        return entityModelBase.add(toMap(values));
    }

    @Override
    public Provinces findFirst(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class) Closure conditions)
    {
        Map<String, Object> conditionsMap = toMap(conditions);

        AstSelect sql = Ast.selectAll().from(entityName).where(conditionsMap);
        return db.query(sql.format(), beanHandler, conditionsMap.values().toArray());
    }

    @Override
    public Iterable<Provinces> findAll(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class) Closure conditions)
    {
        Map<String, Object> conditionsMap = toMap(conditions);

        AstSelect sql = Ast.selectAll().from(entityName).where(conditionsMap);
        return db.query(sql.format(), beanListHandler, conditionsMap.values().toArray());
    }

    public Long count(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class) final Closure conditions)
    {
        Map<String, Object> conditionsMap = toMap(conditions);

        AstSelect sql = Ast.selectCount().from(entityName).where(conditionsMap);
        return db.getLong(sql.format(), conditionsMap.values().toArray());
    }

    @Override
    public void removeAll(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class) Closure conditions)
    {
        Map<String, Object> conditionsMap = toMap(conditions);

        db.update(dpsHelper.generateDelete(entity, conditionsMap), conditionsMap.values().toArray());
    }

    @Override
    public Map<String, Object> toMap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Provinces.class)
                                     final Closure config)
    {
        Provinces entity = new Provinces();
        Closure code = config.rehydrate(entity, entity, entity);//todo need this
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();

        return toMap(entity);
    }

    @Override
    public Map<String, Object> toMap(Provinces entity)
    {
        Map<String, Object> values = new HashMap<>();
        if(entity.ID != null)values.put(fields.ID, entity.ID);
        if(entity.name != null)values.put(fields.name, entity.name);
        if(entity.countryID != null)values.put(fields.countryID, entity.countryID);

        return values;
    }
}
