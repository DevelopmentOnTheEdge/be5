importClass(Packages.java.util.HashMap);

function getParameters( output, op, connector, parameters, presetValues )
{
    parameters.name.CAN_BE_NULL = false;

    parameters.moveBefore( "parentID", "name" );

    var extras = new HashMap();
    if( presetValues.$entity ) 
    {
        extras.put( "entity", presetValues.$entity );
    }
    if( presetValues.$companyID ) 
    {
        extras.put( "companyID", presetValues.$companyID );
    }
    if( presetValues.$rootPublicID ) 
    {
        extras.put( "rootPublicID", presetValues.$rootPublicID );
    }
    if( !extras.isEmpty() ) 
    {
        parameters.parentID.TAG_LIST_ATTR = self.getTagsFromSelectionView( connector, "categories", extras );
        parameters.parentID.EXTERNAL_TAG_LIST = "";
        parameters.parentID.CAN_BE_NULL = presetValues.$rootPublicID ? false : true;
    }
}
 