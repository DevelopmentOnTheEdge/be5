package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.beans.annot.PropertyName;

public class JavaScriptOperationExtender extends OperationExtender
{
    public static final String JAVASCRIPT_EXTENDER_CLASS_NAME = "com.beanexplorer.enterprise.operations.JavaScriptOperationExtenderSupport";

    public JavaScriptOperationExtender(Operation owner, String module)
    {
        super(owner, module);
    }
    
    /**
     * Copy constructor
     * @param owner
     * @param orig
     */
    public JavaScriptOperationExtender( Operation owner, JavaScriptOperationExtender orig )
    {
        super( owner, orig );
        setFileName( orig.getFileName() );
        setCode( orig.getCode() );
    }
    
    /**
     * Copy constructor
     * @param owner
     * @param orig
     */
    public JavaScriptOperationExtender( Operation owner, OperationExtender orig )
    {
        super( owner, orig );
    }
    
    private String fileName = getOperation().getEntity().getName() + " - " + getOperation().getName() + " - " + getName() + ".js";
    private SourceFile file;

    @PropertyName("Source file name")
    public String getFileName()
    {
        SourceFile sourceFile = getSourceFile();
        if(sourceFile != null)
            return sourceFile.getName();
        if(getProject().getProjectOrigin().equals( getOriginModuleName() ))
            return fileName;
        return "(module code)";
    }
    
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
        fireChanged();
    }

    public String getCode()
    {
        SourceFile file = getSourceFile();
        return file == null ? "" : file.getSource();
    }

    public SourceFile getSourceFile()
    {
        Project project = getProject();
        Module module = project.getModule( getOriginModuleName() );
        SourceFile sourceFile = module == null ? null : module.getSourceFile( SourceFileCollection.NAMESPACE_JAVASCRIPT_EXTENDER, fileName );
        if(sourceFile != null)
            return sourceFile;
        if(file == null && module != project.getApplication())
            file = new SourceFile( "(module code)", null );
        return file;
    }

    public void setCode( String code )
    {
        SourceFile file = getSourceFile();
        if(file == null)
        {
            String newFileName = SourceFile.extractFileNameFromCode( code );
            if(newFileName != null)
            {
                fileName = newFileName;
            }
            file = getSourceFile();
        }
        if(file == null)
        {
            getProject().getApplication().addSourceFile( SourceFileCollection.NAMESPACE_JAVASCRIPT_EXTENDER, fileName, code );
        }
        else
        {
            file.setSource( code );
        }
        
        fireChanged();
    }

    @Override
    public String getClassName()
    {
        return JAVASCRIPT_EXTENDER_CLASS_NAME;
    }

    @Override
    public void setClassName( String className )
    {
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( !super.equals( obj ) || getClass() != obj.getClass() )
            return false;
        JavaScriptOperationExtender other = ( JavaScriptOperationExtender ) obj;
        return getCode().equals( other.getCode() );
    }
    
    @Override
    public OperationExtender copyFor( Operation operation )
    {
        return new JavaScriptOperationExtender( operation, this );
    }
}
