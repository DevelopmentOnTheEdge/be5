package com.developmentontheedge.be5.maven;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.yaml.snakeyaml.Yaml;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableRef;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;

/**
 * Generates documentation for BE5 application.
 * 
 * Usage example:
 * mvn be5:generate-doc -DBE5_DOC_PATH=../doc/gtrd-doc
 */
@Mojo(name = "generate-doc")
public class GenerateDocMojo extends Be5Mojo
{
    @Parameter(property = "BE5_DOC_PATH")
    protected File docPath;

    protected File tablesPath;
    protected File diagramsPath;

    public static String DIAGRAMS_DIR = "diagrams";
    public static String TABLES_DIR   = "tables";
    public static String YAML_FILE    = ".be5.yaml";

    public static String YAML_ERROR   = "Error on YAML file configuration: ";
    public static String YAML_DIAGRAMS_KEY = "diagrams";
    public static String YAML_TABLES_KEY   = "tables";
    public static String YAML_NESTED_TABLES_KEY = "nested_tables";
    
    public static String TABLES_TOC_FILE = "__tables.rst";
    public static String TABLES_APP_FILE = "__tables_app.rst";
    public static String TABLES_BE5_FILE = "__tables_be5.rst";
    
    protected Map<String, String> be5TablesMap;
    protected String[] be5Tables = new String[]{"be5columnSettings", "be5eventParams", "be5events",
                                    "be5operationLogParams", "be5operationLogs", "be5querySettings",
                                    "categories", "classifications", "countries", "languages",
                                    "mimeTypes", "persistent_logins", "provinces", "systemSettings",
                                    "user_prefs", "user_roles", "users"};
    protected Map<String, String> be5FieldsMap;
    protected String[] be5Fields = new String[]{"whoInserted___", "whoModified___", "creationDate___", "modificationDate___"};

    public static String nl = System.lineSeparator();
    
    protected Map<String, Object> configuration;
    
    protected Project be5Project;
    protected List<TableDef> tables;
    protected Map<String, TableDef> tablesMap;
    
    @Override
    public void execute()
    {
        logger.info("Generating documentation");

		//init();
        
        try
        {
            loadProject();
            
            validateDocPath();
            readConfigurationYaml();
            
            generateTables();
            generateNestedTables();
            generateDiagrams();
        }
        catch(Throwable t)
        {
            logger.error("Unexpected error: " + t.getMessage());
            t.printStackTrace();
        }
    }

    protected void loadProject() throws Exception
    {
        logger.info("Load project from '" + projectPath + "'");
        be5Project = ModuleLoader2.loadProjectWithModules(projectPath.toPath(), logger);
        
        tables = be5Project.findTableDefinitions();
        Collections.sort(tables,(TableDef t1, TableDef t2) -> 
            t1.getEntityName().compareToIgnoreCase(t2.getEntityName() ) );
        
        tablesMap = new HashMap<>();
        for(TableDef t : tables)
        	tablesMap.put(t.getEntityName(), t);
    }

    protected void validateDocPath() throws Exception
    {
        if( docPath == null )
        {
            logger.error("Path to documention is not specified." + nl +
                         "Please use: -DBE5_DOC_PATH=your_doc_path");
            return; 
        }
        
        if( !docPath.exists() )
        {
            logger.error("Path to documentation does not exist: " + docPath.getCanonicalPath());
            return;
        }

        if( !docPath.getCanonicalPath().endsWith(File.pathSeparator + "be5") )
        {
            docPath = new File(docPath, "be5");

            if( docPath.exists() )
                logger.info("Use be5 subdirectory.");
            else
            {
                logger.info("Creates be5 subdirectory.");
                Files.createDirectories(docPath.toPath());
            }
                
        }

        logger.info("Path to generated documentation: " + docPath.getCanonicalPath());
        
        tablesPath = new File(docPath, TABLES_DIR);
        if( ! tablesPath.exists() )
        {
            logger.info("Creates be5/tables subdirectory.");
            Files.createDirectories(tablesPath.toPath());
        }
        
        diagramsPath = new File(docPath, DIAGRAMS_DIR);
        if( ! diagramsPath.exists() )
        {
            logger.info("Creates be5/diagrams subdirectory.");
            Files.createDirectories(diagramsPath.toPath());
        }
    }       

    protected void readConfigurationYaml() throws Exception
    {
        File yaml = new File(docPath.getParent(), YAML_FILE);
        
        if( yaml.exists() )
        {
            configuration = new Yaml().load(new FileInputStream(yaml));
            logger.info("Load configuration from .be5.yaml");
        }
        else
        {
            logger.info("Configuration file .be5.yaml is not found in ../be5 directory.");
            configuration = new HashMap<String, Object>();
        }
    }

    protected void generateTables() throws Exception
    {
    	be5TablesMap = new HashMap<>();
    	for(String table : be5Tables)
    		be5TablesMap.put(table, table);

    	be5FieldsMap = new HashMap<>();
    	for(String field : be5Fields)
    		be5FieldsMap.put(field, field);
    	
    	PrintWriter tocTree = new PrintWriter(new File(tablesPath, TABLES_TOC_FILE));
    
        tocTree.println(
"Схема базы данных"    + nl +
"================="    + nl + nl +
".. toctree::"         + nl + nl + 
"      " + TABLES_APP_FILE + nl +  
"      " + TABLES_BE5_FILE);

        generateTables(TABLES_APP_FILE, true);        
        generateTables(TABLES_BE5_FILE, false);
        tocTree.flush();
    }
    
    protected void generateTables(String file, boolean appTables) throws Exception
    {
        String type = appTables ? "application" : "be5";
    	logger.info("Generate " + type + " tables");

        PrintWriter tocTree = new PrintWriter(new File(tablesPath, file));
        
        if( appTables )
        	tocTree.println(
"Таблицы приложения" + nl +
"==================" + nl);
        else
        	tocTree.println(
        	"Таблицы BE5 (служебные)" + nl +
        	"=======================" + nl);
        	
    	tocTree.println(
".. toctree::"         + nl); 

        int n = 0;
    	for(TableDef table : tables)
        {
    		if( appTables && be5TablesMap.containsKey(table.getEntityName()) )
    			continue;

    		if( !appTables && !be5TablesMap.containsKey(table.getEntityName()) )
    			continue;
    		
    		tocTree.println("  " + table.getEntityName() + ".rst");
            generateTable(table);
            n++;
        }
        
        tocTree.flush();
        logger.info("  " + n + " tables were generated");
    }

    protected void generateTable(TableDef table) throws Exception
    {
    	generateTable(table, "rst", "=", false);
    }
    
    protected void generateTable(TableDef table, String extension, String headingUnderline, boolean skipBe5Fields) throws Exception
    {
        String name = table.getEntityName();
        PrintWriter file = new PrintWriter(new File(tablesPath, name+"."+extension));        

        file.println(name);
        //file.println(headingUnderline.repeat(name.length()));
        file.println( String.format("%0" + name.length() + "d", 0).replace("0", headingUnderline) );
        file.println();

        String displayName = table.getEntity().getDisplayName();
        if( displayName != null && displayName.length() > 0 && !displayName.equalsIgnoreCase(name) )
            file.println(displayName);

		String doc = table.getEntity().getComment();
		if( doc != null && doc.length() > 0 )
    		file.println("  " + doc);

    	file.println(nl +
".. list-table::" 			 + nl +
"   :header-rows: 1"		 + nl + nl +

"   * - Колонка"  	+ nl +
"     - Тип" 		+ nl +
"     - Описание" 	+ nl);

		BeCaseInsensitiveCollection<ColumnDef> columns = table.getColumns();
		String be5Fields = null;
		for(ColumnDef column : columns)
		{
			String columnName = column.getName();
			
			if( skipBe5Fields && be5FieldsMap.containsKey(columnName) )
			{
				if( be5Fields == null )
					be5Fields = columnName;
				else
					be5Fields += ", " + columnName;
			}
			
			String columnType = column.getType().toString();
			if( column.isPrimaryKey() )
				columnType += " PK";

			String shift = nl + nl + "       ";
			if( column.isAutoIncrement() )
				columnType += shift  + "autoincrement";

			if( column.isCanBeNull() )
				columnType += shift +"can be null";
			
			if( column.getDefaultValue() != null )
				columnType += shift +"Defult value: " + column.getDefaultValue() ;

			if( column.getTableTo() != null )
				columnType += shift +"Reference: " + column.getTableTo();
	
			String columnDoc = column.getComment() == null ? "" : column.getComment();  
		
	    	file.println(
"   * - " + columnName + nl +
"     - " + columnType + nl +
"     - " + columnDoc  + nl);
		} 

		if( skipBe5Fields && be5Fields != null )
	    	file.println("Служебные поля: " + be5Fields + nl);
		
		if( table.getIndices() != null && table.getIndices().getSize() > 0 )
		{
	    	file.print("**Индексы**");
	    	
	    	for(IndexDef index : table.getIndices())
	    	{
	    		file.print(nl + "   * " + index.getName() + ": ");
	    		
	    		if( index.isUnique() )
	    			file.print("UNIQUE ");

				int n = index.getAvailableElements().size();
		    	for(IndexColumnDef indexColumn : index.getAvailableElements())
		    	{
		    		file.print(indexColumn.getAsString());
		    		
		    		if( n>1 )
	    			file.print(", ");
	    			
	    			n--;
    			}
	    	} 		
    	}
    	
    	file.flush();
    }

    protected void generateNestedTables()
    {
    	Object d = configuration.get(YAML_NESTED_TABLES_KEY);
    	if( d == null )
    		return;
    	
    	if( !(d instanceof List) )
    	{    		
            logger.info(YAML_ERROR + YAML_NESTED_TABLES_KEY + "should be an arry.");
            return;
    	}
    	
    	for(Object o : ((List)d) )
    	{
    		if( !(o instanceof String) )
    		{
                logger.error(YAML_ERROR + YAML_NESTED_TABLES_KEY + "each item shoud be a table name.");
                break;
    		}
    		
    		try
    		{
    			String tableName = (String)o;
    			TableDef table = null;
    			for(TableDef td : tables)
    			{
    				if( td.getEntityName().equals(tableName) )
    				{
    					table = td;
    					break;
    				}
    			}

    			if( table == null )
    				throw new Exception("Table '" + tableName  + "' is missing.");
    					 
    			generateTable(table, "rstincl", "^", true);
    		}
    		catch(Exception t)
    		{
                logger.error(YAML_ERROR + YAML_NESTED_TABLES_KEY + "each item shoud be a valid table name.");
                logger.error(t.getMessage());
                break;
    		}
    	}
    }
    	
    protected void generateDiagrams()
    {
    	Object d = configuration.get(YAML_DIAGRAMS_KEY);
    	if( d == null )
    		return;
    	
    	if( !(d instanceof List) )
    	{    		
            logger.info(YAML_ERROR + YAML_DIAGRAMS_KEY + "should be an arry.");
            return;
    	}
    	
    	for(Object o : ((List)d) )
    	{
    		if( !(o instanceof Map) )
    		{
                logger.error(YAML_ERROR + YAML_DIAGRAMS_KEY + "each diagram item shoud be a map.");
                break;
    		}
    		
    		try
    		{
    			Map diagramItem = (Map)o;
    			String name = (String)diagramItem.keySet().iterator().next();
    			Map values = (Map)diagramItem.get(name);
    			
    			generateDiagram(name, values);
    		}
    		catch(Exception t)
    		{
                logger.error(YAML_ERROR + YAML_DIAGRAMS_KEY + "each diagram item shoud be a map.");
                logger.error(t.getMessage());
                break;
    		}
    	}
    }
    
    protected void generateDiagram(String name, Map values) throws Exception
    {
    	List<String> tables = null;
    	
    	try
    	{
    		tables = (List<String>)values.get(YAML_TABLES_KEY);
    		if( tables.size() == 0 )
    			throw new Exception("Tables list is empty." );
    	}
    	catch(Exception e)
    	{
    		logger.error(YAML_ERROR + " diagram '" + name + "' should contan array list of tables.");
            return;
        }

        PrintWriter puml = new PrintWriter(new File(diagramsPath, name+".puml"));
        
        puml.println(
"   hide circle" + nl +
"   skinparam linetype ortho" + nl);

		for(String table : tables)
		{
			generateTablePUML(table, puml);
		}

		
		List<TableRef> refs = be5Project.findTableReferences();
		for(TableRef ref : refs)
		{
			String fromTable = ref.getTableFrom();
			String toTable   = ref.getTableTo();
			
			if( tables.contains(fromTable) && tables.contains(toTable) )
		    	puml.println(
"   " + fromTable + " }o..|| " + toTable);
		}
		
		
        puml.flush();
    }
    
    protected void generateTablePUML(String table, PrintWriter puml) throws Exception
    {
    	TableDef t = tablesMap.get(table);
    	if( t == null )
    	{
    		logger.error(YAML_ERROR + " diagram '" + table + "' - table '" + table + "' is missing in the project.");
            return;
    	}

    	puml.println(
"   entity \"" + table + "\" as " + table + "{" + nl +
"     --");
    	
		BeCaseInsensitiveCollection<ColumnDef> columns = t.getColumns();
		for(ColumnDef column : columns)
		{
			String columnName = column.getName();
			if( column.isPrimaryKey() )
				columnName = "*" + columnName;

			String columnType = column.getType().toString();

	    	puml.println(
"     " + columnName + " : " + columnType);
		} 

    	puml.println(
"    }" + nl);
		
    }
   
}
