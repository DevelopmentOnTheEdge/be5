package com.developmentontheedge.be5.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.JavaScriptForm;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.base.BeVectorCollection;
import com.developmentontheedge.be5.metadata.serialization.Serialization;

public class ConvertJsForms extends TestCase
{
    private static final String PROJECT_PATH = "C:\\projects\\java\\e-city_be4";
    
    public void testConvertForms() throws Exception
    {
        convertForms(PROJECT_PATH);
    }

    private void convertForms( String projectPath ) throws Exception
    {
        Path path = Paths.get( projectPath );
        Project project = Serialization.load( path );
        Path oldForms = path.resolve( "src/sql/jsforms.xml" );
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                if (systemId.contains("dataset.dtd")) {
                    return new InputSource(new StringReader(""));
                } else {
                    return null;
                }
            }
        });
        Document document;
        try(InputStream is = Files.newInputStream( oldForms ))
        {
            document = builder.parse( is );
        }
        Element tableElement = ( Element ) document.getDocumentElement().getElementsByTagName( "table" ).item( 0 );
        NodeList rows = tableElement.getElementsByTagName( "row" );
        int length = rows.getLength();
        BeVectorCollection<JavaScriptForm> formsCollection = project.getApplication().getOrCreateCollection( Module.JS_FORMS,
                JavaScriptForm.class );
        for(int i=0; i<length; i++)
        {
            Element row = ( Element ) rows.item( i );
            NodeList vals = row.getElementsByTagName( "value" );
            String key = vals.item( 0 ).getTextContent();
            String value = vals.item( 1 ).getTextContent();
            JavaScriptForm form = new JavaScriptForm( key, formsCollection );
            System.out.println(key + "-" +value);
            if(value.startsWith( "${SRCDIR}/js/forms/" ))
            {
                form.setModule( project.getProjectOrigin() );
                form.setRelativePath( value.substring( "${SRCDIR}/js/forms/".length() ) );
            } else if(value.startsWith( "${BEMODULESDIR}/" ))
            {
                int pos = value.indexOf( '/', "${BEMODULESDIR}/".length() );
                if(pos > 0)
                {
                    form.setModule( value.substring( "${BEMODULESDIR}/".length(), pos ) );
                    form.setRelativePath( value.substring( pos + "/src/jsforms/".length() ) );
               }
            }
            form.load();
            DataElementUtils.saveQuiet( form );
        }
        Serialization.save( project, path );
    }
}
