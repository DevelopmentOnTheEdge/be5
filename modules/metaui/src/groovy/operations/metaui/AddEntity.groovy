package metaui

import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.GOperationSupport


class AddEntity extends GOperationSupport
{
    @Inject ProjectProvider projectProvider

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps.add("test", "Test")

        return dpsHelper.setValues(dps, presetValues);
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
//        final String entityName = page.getEntityName();
//        final EntityType type = page.getType();
//        final Module application = BeanExplorerProjectProvider.getInstance().getProject().getApplication();
//        final Entity entity = new Entity( entityName, application, type );
//        entity.setDisplayName( page.getDisplayName() );
//        entity.setPrimaryKey( page.getPrimaryKey() );
//
//        if ( page.mustCreateSqlTable() )
//        {
//            final TableDef def = new TableDef( entity );
//            final ColumnDef primaryKeyDef = new ColumnDef( page.getPrimaryKey(), def.getColumns() );
//            primaryKeyDef.setPrimaryKey( true );
//            primaryKeyDef.setType( new SqlColumnType( SqlColumnType.TYPE_KEY ) );
//            primaryKeyDef.setAutoIncrement( true );
//            DataElementUtils.saveQuiet( primaryKeyDef );
//            DataElementUtils.saveQuiet( def );
//            // refresh
//
//            BeanExplorerProjectProvider.getInstance().structuralChange( entity );
//            final BeanExplorerProjectView projectView = BeanExplorerProjectView.getSharedInstance();
//
//            if ( projectView != null )
//            {
//                projectView.expand( entity );
//                projectView.expand( def );
//            }
//        }
//
//        DataElementUtils.saveQuiet( entity );

    }
}
