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

//    public JsonApiModel(ResourceData data, Object meta, Map<String, String> links)
//    {
//        this.data = data;
//        this.meta = meta;
//        this.links = links;
//    }
//
//    public JsonApiModel(ResourceData[] data, Object meta, Map<String, String> links)
//    {
//        this.data = data;
//        this.meta = meta;
//        this.links = links;
//    }

    private JsonApiModel(ResourceData data, ErrorModel[] errors, Object meta, ResourceData[] included, Map<String, String> links)
    {
        this.data = data;
        this.errors = errors;
        this.meta = meta;
        this.included = included;
        this.links = links;
    }

    public static JsonApiModel data(ResourceData data, Object meta, Map<String, String> links)
    {
        return new JsonApiModel(data, null, meta, null, links);
    }

    public static JsonApiModel data(ResourceData data, ResourceData[] included, Object meta, Map<String, String> links)
    {
        return new JsonApiModel(data, null, meta, included, links);
    }

    public static JsonApiModel error(ErrorModel error, Object meta, Map<String, String> links)
    {
        return new JsonApiModel(null, new ErrorModel[]{error}, meta, null, links);
    }

    public static JsonApiModel error(ErrorModel error, ResourceData[] included, Object meta, Map<String, String> links)
    {
        return new JsonApiModel(null, new ErrorModel[]{error}, meta, included, links);
    }

//    public JsonApiModel(ErrorObject[] errors, Object meta, Map<String, String> links)
//    {
//        this.errors = errors;
//        this.meta = meta;
//        this.links = links;
//    }

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

/*
нужно добавить время вызова для предотвращения открытия на фронтенде старых запросов после долгой загрузки.
static final String TIMESTAMP_PARAM = "_ts_";

ссылку на самого себя
"links": {
 "self": "http://example.com/articles"
 },

* */