package com.developmentontheedge.be5.model.jsonapi;

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
    private Object data;
    private Object[] errors;
    private Object meta;

    private Object[] included;
    private Map<String, String> links;

    public JsonApiModel(ResourceData data, Object meta, Map<String, String> links)
    {
        this.data = data;
        this.meta = meta;
        this.links = links;
    }

    public JsonApiModel(ResourceData[] data, Object meta, Map<String, String> links)
    {
        this.data = data;
        this.meta = meta;
        this.links = links;
    }

//    public JsonApiModel(ErrorObject[] errors, Object meta, Map<String, String> links)
//    {
//        this.errors = errors;
//        this.meta = meta;
//        this.links = links;
//    }

    public Object getData()
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

    public Object[] getIncluded()
    {
        return included;
    }

    public Map<String, String> getLinks()
    {
        return links;
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