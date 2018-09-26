package com.developmentontheedge.be5.metadata.scripts;

import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.ViewDef;


public class AppDropAllTables extends AppDb
{
    protected void processProject()
    {
        processAllModules(module -> processDdlElements(module, ViewDef.class, DdlElement::getDropDdl));
        processAllModules(module -> processDdlElements(module, TableDef.class, DdlElement::getDropDdl));
    }
}
