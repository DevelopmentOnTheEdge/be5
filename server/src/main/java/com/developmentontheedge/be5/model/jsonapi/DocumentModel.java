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
public abstract class DocumentModel
{
    Object data;
    Object[] errors;
    Object meta;

    Object[] included;
    Map<String, String> links;
}

/*
нужно добавить время вызова для предотвращения открытия на фронтенде старых запросов после долгой загрузки.
static final String TIMESTAMP_PARAM = "_ts_";

ссылку на самого себя для разработки
"links": {
 "self": "http://example.com/articles"
 },

* */