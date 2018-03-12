package com.developmentontheedge.be5.model.jsonapi;

import java.util.Arrays;
import java.util.Map;

/**
 http://jsonapi.org

 A document MUST contain at least one of the following top-level members:

     data: the document’s “primary data”
     errors: an array of error objects
     meta: a meta object that contains non-standard meta-information.

 A document MAY contain any of these top-level members:

     links: a links object related to the primary data.
     included: an array of resource objects that are related to the primary data and/or each other (“included resources”).


 */
public class JsonApiModel
{
    private ResourceData data;
    private ErrorModel[] errors;
    private Object meta;

    private ResourceData[] included;
    private Map<String, String> links;

    private JsonApiModel(ResourceData data, ErrorModel[] errors, Object meta, ResourceData[] included, Map<String, String> links)
    {
        this.data = data;
        this.errors = errors;
        this.meta = meta;
        this.included = included;
        this.links = links;
    }

    public static JsonApiModel data(ResourceData data, Object meta)
    {
        return new JsonApiModel(data, null, meta, null, null);
    }

    public static JsonApiModel data(ResourceData data, ResourceData[] included, Object meta)
    {
        return new JsonApiModel(data, null, meta, included, null);
    }

    public static JsonApiModel data(ResourceData data, ResourceData[] included, Object meta, Map<String, String> links)
    {
        return new JsonApiModel(data, null, meta, included, links);
    }

    public static JsonApiModel error(ErrorModel error, Object meta)
    {
        return new JsonApiModel(null, new ErrorModel[]{error}, meta, null, null);
    }

    public static JsonApiModel error(ErrorModel error, ResourceData[] included, Object meta)
    {
        return new JsonApiModel(null, new ErrorModel[]{error}, meta, included, null);
    }

    public static JsonApiModel error(ErrorModel error, ResourceData[] included, Object meta, Map<String, String> links)
    {
        return new JsonApiModel(null, new ErrorModel[]{error}, meta, included, links);
    }

    public ResourceData getData()
    {
        return data;
    }

    public Object[] getErrors()
    {
        return errors;
    }

    public Object getMeta()
    {
        return meta;
    }

    public ResourceData[] getIncluded()
    {
        return included;
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    public void setMeta(Object meta)
    {
        this.meta = meta;
    }

    @Override
    public String toString()
    {
        return "JsonApiModel{" +
                (data!=null ? "data=" + data : "") +
                (errors!=null ? "errors=" + Arrays.toString(errors) : "") +
                (meta!=null ? ", meta=" + meta : "") +
                (included!=null ? ", included=" + Arrays.toString(included) : "") +
                (links!=null ? ", links=" + links : "") +
        '}';
    }
}
