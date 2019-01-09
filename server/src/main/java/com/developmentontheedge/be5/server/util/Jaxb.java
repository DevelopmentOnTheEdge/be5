package com.developmentontheedge.be5.server.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class Jaxb
{
    public static <T> String toXml(Class<T> klass, T object)
    {
        StringWriter out = new StringWriter();

        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(klass);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(object, out);
        }
        catch (JAXBException e)
        {
            throw new RuntimeException(e);
        }

        return out.toString();
    }

}
