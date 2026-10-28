package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.modules.core.mcp.oauth.McpOAuthConfig;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.operation.util.OperationUtils;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.impl.QueryMetaHelper;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.query.util.QueryUtils;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.model.Base64File;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.impl.FileUploadWrapper;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import com.google.common.base.Strings;
import com.google.inject.OutOfScopeException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_JAVASCRIPT;
import static com.developmentontheedge.be5.metadata.model.Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS;
import static com.developmentontheedge.be5.metadata.model.Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD;
import static com.developmentontheedge.be5.operation.OperationConstants.SELECTED_ROWS;
import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;

/**
 * Queries and operations as the UI shows and runs them, for the logged in user: the tools of
 * {@link McpService} with which an AI agent reads records and performs business actions.
 */
@Singleton
public class QueryOperationService
{
    private static final Logger log = Logger.getLogger(QueryOperationService.class.getName());

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 200;
    /** Longer option lists (all the users, say) are cut, the agent finds a value with a query. */
    static final int MAX_OPTIONS = 200;

    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final UserInfoProvider userInfoProvider;
    private final QueryExecutorFactory queryExecutorFactory;
    private final OperationExecutor operationExecutor;
    private final OperationService operationService;
    private final OperationLogging operationLogging;
    private final Provider<Request> requestProvider;
    private final McpFileStore fileStore;

    @Inject
    public QueryOperationService(Meta meta, UserAwareMeta userAwareMeta, UserInfoProvider userInfoProvider,
                                 QueryExecutorFactory queryExecutorFactory, OperationExecutor operationExecutor,
                                 OperationService operationService, OperationLogging operationLogging,
                                 Provider<Request> requestProvider, McpFileStore fileStore)
    {
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.userInfoProvider = userInfoProvider;
        this.queryExecutorFactory = queryExecutorFactory;
        this.operationExecutor = operationExecutor;
        this.operationService = operationService;
        this.operationLogging = operationLogging;
        this.requestProvider = requestProvider;
        this.fileStore = fileStore;
    }

    /**
     * The queries the user may open, grouped by entity; of one entity when the name is given.
     */
    public List<Map<String, Object>> listQueries(String entityName)
    {
        List<String> roles = userInfoProvider.getCurrentRoles();
        List<Entity> entities = entityName == null ? meta.getEntities()
                : Collections.singletonList(meta.getEntity(entityName));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Entity entity : entities)
        {
            List<Map<String, Object>> queries = new ArrayList<>();
            for (Query query : entity.getQueries())
            {
                // selection views only fill drop-down lists
                if (query.isAvailable() && query.isAiAccessible() && meta.isAvailableFor(query, roles)
                        && !query.getName().startsWith("***"))
                {
                    queries.add(queryHeader(query, false));
                }
            }
            if (!queries.isEmpty())
            {
                Map<String, Object> entityInfo = new LinkedHashMap<>();
                entityInfo.put("entityName", entity.getName());
                entityInfo.put("title", userAwareMeta.getLocalizedEntityTitle(entity));
                entityInfo.put("queries", queries);
                result.add(entityInfo);
            }
        }
        return result;
    }

    /**
     * A query with its parameters and the operations the user may run on its rows.
     */
    public Map<String, Object> describeQuery(String entityName, String queryName)
    {
        Query query = getQuery(entityName, queryName);
        Map<String, Object> result = queryHeader(query, true);
        result.put("primaryKey", query.getEntity().getPrimaryKey());
        addQueryParameters(result, query);

        String filterOperation = query.getParametrizingOperationName();
        if (filterOperation.isEmpty())
        {
            filterOperation = (String) JsonUtils.getMapFromJson(query.getLayout()).get("topForm");
        }
        com.developmentontheedge.be5.metadata.model.Operation filter = filterOperation == null ? null
                : query.getEntity().getOperations().get(filterOperation);
        if (filter == null || filter.isAiAccessible())
        {
            putIfNotEmpty(result, "filterOperation", filterOperation);
        }

        List<String> roles = userInfoProvider.getCurrentRoles();
        List<Map<String, Object>> operations = new ArrayList<>();
        for (String operationName : query.getOperationNames().getFinalValues())
        {
            com.developmentontheedge.be5.metadata.model.Operation operation =
                    query.getEntity().getOperations().get(operationName);
            // client side (JavaScript) operations run in the browser only
            if (operation != null && operation.isAvailable() && operation.isAiAccessible()
                    && meta.isAvailableFor(operation, roles)
                    && !OPERATION_TYPE_JAVASCRIPT.equals(operation.getType()))
            {
                operations.add(operationHeader(new OperationInfo(operation)));
            }
        }
        result.put("operations", operations);
        return result;
    }

    /**
     * One page of the rows of a query, with the id of each row (that operations get as a selected row).
     */
    public Map<String, Object> executeQuery(String entityName, String queryName, Map<String, Object> params,
                                            int offset, int limit)
    {
        Query query = getQuery(entityName, queryName);
        if (!isRunnable(query))
        {
            throw new IllegalArgumentException("Query '" + queryName + "' of type " + query.getType().getName()
                    + " has no rows; its operations run with execute_operation");
        }
        if (offset < 0) offset = 0;
        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();
        limit = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, maxLimit > 0 ? Math.min(MAX_LIMIT, maxLimit) : MAX_LIMIT);

        Map<String, Object> queryParams = toContextParams(params);
        queryParams.put(OFFSET, Integer.toString(offset));
        queryParams.put(LIMIT, Integer.toString(limit));
        return readRows(query, queryParams);
    }

    /**
     * The number of the rows of a query with the parameters (filters), without reading them: SELECT COUNT(*) over
     * the query as the user opens it, so only the records the query shows the user are counted.
     */
    public Map<String, Object> countRecords(String entityName, String queryName, Map<String, Object> params)
    {
        Query query = getQuery(entityName, queryName);
        if (!isRunnable(query))
        {
            throw new IllegalArgumentException("Query '" + queryName + "' of type " + query.getType().getName()
                    + " has no rows to count");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityName", query.getEntity().getName());
        result.put("queryName", query.getName());
        result.put("count", countRows(query, toContextParams(params)));
        return result;
    }

    @LogBe5Event
    long countRows(Query query, Map<String, Object> queryParams)
    {
        QueryExecutor queryExecutor = queryExecutorFactory.get(query, queryParams);
        // a Java or Groovy query counts its rows by reading them: count() of these is the size of a page
        return query.getType() == QueryType.D1 || query.getType() == QueryType.D1_UNKNOWN
                ? queryExecutor.count() : queryExecutor.execute().size();
    }

    /**
     * The rows of the query, the page of which (offset and limit) is in the parameters.
     */
    @LogBe5Event
    Map<String, Object> readRows(Query query, Map<String, Object> queryParams)
    {
        int offset = Integer.parseInt((String) queryParams.get(OFFSET));
        int limit = Integer.parseInt((String) queryParams.get(LIMIT));
        QueryExecutor queryExecutor = queryExecutorFactory.get(query, queryParams);
        List<QRec> records = queryExecutor.execute();

        List<Map<String, Object>> columns = new ArrayList<>();
        if (!records.isEmpty())
        {
            for (DynamicProperty property : records.get(0))
            {
                if (!QueryUtils.shouldBeSkipped(property))
                {
                    Map<String, Object> column = new LinkedHashMap<>();
                    column.put("name", property.getName());
                    column.put("title", property.getDisplayName());
                    columns.add(column);
                }
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (QRec record : records)
        {
            Map<String, Object> values = new LinkedHashMap<>();
            for (DynamicProperty property : record)
            {
                if (!QueryUtils.shouldBeSkipped(property))
                {
                    values.put(property.getName(), plainValue(property.getValue()));
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            Object id = record.getValue(ID_COLUMN_LABEL);
            if (id != null) row.put("id", id.toString());
            row.put("values", values);
            rows.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityName", query.getEntity().getName());
        result.put("queryName", query.getName());
        result.put("columns", columns);
        result.put("rows", rows);
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("totalNumberOfRows", records.size() < limit ? offset + records.size() : queryExecutor.count());
        return result;
    }

    /**
     * The form of an operation (getParameters with the given values), without running it - also for
     * an operation without parameters, which the UI runs at once.
     */
    public Map<String, Object> getOperationParameters(String entityName, String queryName, String operationName,
                                                      Map<String, Object> params, List<String> selectedRows,
                                                      Map<String, Object> values)
    {
        Operation operation = createOperation(entityName, queryName, operationName, params, selectedRows);
        Submission submission = prepare(operation, params, selectedRows, values);
        Object parameters = withFiles(submission, () -> generate(operation, submission.values));
        if (operation.getStatus() == OperationStatus.ERROR)
        {
            throw new IllegalStateException(errorMessage(operation.getResult()));
        }

        Map<String, Object> result = operationHeader(operation.getInfo());
        result.put("fields", describeFields(parameters));
        if (parameters == null)
        {
            result.put("note", "The operation has no parameters, execute_operation runs it at once");
        }
        else if (!(parameters instanceof DynamicPropertySet))
        {
            result.put("note", "The parameters are a " + parameters.getClass().getSimpleName()
                    + " the form of which cannot be shown here");
        }
        putIfNotEmpty(result, "message", operation.getResult().getMessage());
        return result;
    }

    /**
     * Submits the form of an operation as the UI does: getParameters with the values, then invoke.
     * When the values are not accepted the form comes back, with messages on the fields.
     */
    public Map<String, Object> executeOperation(String entityName, String queryName, String operationName,
                                                Map<String, Object> params, List<String> selectedRows,
                                                Map<String, Object> values)
    {
        Operation operation = createOperation(entityName, queryName, operationName, params, selectedRows);
        Submission submission = prepare(operation, params, selectedRows, values);
        Either<Object, OperationResult> either = withFiles(submission, () -> execute(operation, submission.values));

        Map<String, Object> result = operationHeader(operation.getInfo());
        if (either.isFirst())
        {
            result.put("executed", false);
            OperationResult operationResult = operation.getResult();
            result.put("status", operationResult.getStatus().toString());
            putIfNotEmpty(result, "message", operationResult.getStatus() == OperationStatus.ERROR
                    ? errorMessage(operationResult) : operationResult.getMessage());
            result.put("fields", describeFields(either.getFirst()));
            return result;
        }

        OperationResult operationResult = either.getSecond();
        if (operationResult.getStatus() == OperationStatus.ERROR)
        {
            throw new IllegalStateException(errorMessage(operationResult));
        }
        result.put("executed", true);
        result.put("status", operationResult.getStatus().toString());
        putIfNotEmpty(result, "message", operationResult.getMessage());
        Object details = operationResult.getDetails();
        if (details instanceof String || details instanceof Number || details instanceof Boolean)
        {
            result.put("details", details);
        }
        return result;
    }

    /**
     * getParameters only: unlike {@link OperationService#generate} it never runs an operation without parameters.
     */
    @LogBe5Event
    Object generate(Operation operation, Map<String, Object> presetValues)
    {
        return operationExecutor.generate(operation, presetValues);
    }

    @LogBe5Event
    Either<Object, OperationResult> execute(Operation operation, Map<String, Object> presetValues)
    {
        Either<Object, OperationResult> result = operationService.execute(operation, presetValues);
        operationLogging.saveOperationLog(operation, loggedValues(presetValues));
        return result;
    }

    /**
     * A one-time link for the user (or the agent) to upload a file for a file field of an operation.
     */
    public Map<String, Object> createUpload(String purpose)
    {
        McpFileStore.Entry entry = fileStore.create(userInfoProvider.getUserName(), purpose);
        Request request = currentRequest();
        HttpServletRequest raw = request == null ? null : request.getRawRequest();
        // without a request (a chatbot thread) the link is relative to the application
        String issuer = raw == null ? null : McpOAuthConfig.issuer(raw);
        String url = (issuer == null ? "/api/mcp" : McpOAuthConfig.resourceUrl(issuer)) + "/upload/" + entry.token;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileId", entry.fileId);
        result.put("uploadUrl", url);
        result.put("expiresInMinutes", McpFileStore.UPLOAD_TTL_MS / 60000);
        result.put("maxSize", McpFileStore.maxSize());
        result.put("next", "Give the user the link to open in a browser and choose the file there, or upload it "
                + "yourself: curl -F file=@<path> '" + url + "'. Then pass {\"fileId\": \"" + entry.fileId
                + "\"} as the value of the file field. The file can be used for an hour.");
        return result;
    }

    /**
     * An operation with its values, the files given to it by multipart (as the UI gives files of the type File)
     * and the files sent inline, which are deleted after the call.
     */
    private static final class Submission
    {
        final Map<String, Object> values;
        final Map<String, List<FileItem>> multipartFiles;
        final List<FileItem> inlineFiles;

        Submission(Map<String, Object> values, Map<String, List<FileItem>> multipartFiles,
                   List<FileItem> inlineFiles)
        {
            this.values = values;
            this.multipartFiles = multipartFiles;
            this.inlineFiles = inlineFiles;
        }
    }

    /**
     * The values as the UI submits them. A file is {"fileId": ...} (from upload_file) or {"name", "mimeType",
     * "base64"}; a field of the type Base64File gets it as such, any other one as an uploaded file of the request,
     * which is found by its name (OperationSupport.getFileItem).
     */
    private Submission prepare(Operation operation, Map<String, Object> params, List<String> selectedRows,
                               Map<String, Object> values)
    {
        Map<String, Object> plainValues = new LinkedHashMap<>();
        Map<String, List<FileItem>> files = new LinkedHashMap<>();
        List<FileItem> inlineFiles = new ArrayList<>();
        try
        {
            for (Map.Entry<String, Object> entry : values.entrySet())
            {
                List<Map<?, ?>> fileValues = fileValues(entry.getValue());
                if (fileValues == null)
                {
                    plainValues.put(entry.getKey(), entry.getValue());
                    continue;
                }
                List<FileItem> items = new ArrayList<>();
                for (Map<?, ?> fileValue : fileValues)
                {
                    if (fileValue.get("fileId") != null)
                    {
                        items.add(fileStore.get(fileValue.get("fileId").toString(), userInfoProvider.getUserName()));
                    }
                    else
                    {
                        FileItem item = inlineFile(entry.getKey(), fileValue);
                        inlineFiles.add(item);
                        items.add(item);
                    }
                }
                files.put(entry.getKey(), items);
            }

            Map<String, Object> presetValues = OperationUtils.replaceEmptyStringToNull(toRequestValues(plainValues));
            if (files.isEmpty()) return new Submission(presetValues, null, inlineFiles);

            OperationInfo info = operation.getInfo();
            Set<String> base64Fields = base64FileFields(createOperation(info.getEntityName(),
                    operation.getContext().getQueryName(), info.getName(), params, selectedRows),
                    presetValues, files.keySet());
            Map<String, List<FileItem>> multipartFiles = new LinkedHashMap<>();
            for (Map.Entry<String, List<FileItem>> field : files.entrySet())
            {
                List<FileItem> items = field.getValue();
                if (base64Fields.contains(field.getKey()))
                {
                    Base64File[] base64Files = new Base64File[items.size()];
                    for (int i = 0; i < base64Files.length; i++)
                    {
                        FileItem item = items.get(i);
                        base64Files[i] = new Base64File(item.getName(), item.get(),
                                item.getContentType() == null ? "" : item.getContentType());
                    }
                    presetValues.put(field.getKey(), base64Files.length == 1 ? base64Files[0] : base64Files);
                }
                else
                {
                    multipartFiles.put(field.getKey(), items);
                    String[] names = items.stream().map(FileItem::getName).toArray(String[]::new);
                    presetValues.put(field.getKey(), names.length == 1 ? names[0] : names);
                }
            }
            return new Submission(presetValues, multipartFiles.isEmpty() ? null : multipartFiles, inlineFiles);
        }
        catch (RuntimeException e)
        {
            inlineFiles.forEach(FileItem::delete);
            throw e;
        }
    }

    /**
     * The fields of the type Base64File, from the form without the files (getParameters, not logged).
     */
    private Set<String> base64FileFields(Operation probe, Map<String, Object> presetValues, Set<String> fields)
    {
        Set<String> result = new HashSet<>();
        Object parameters;
        try
        {
            parameters = operationExecutor.generate(probe, new LinkedHashMap<>(presetValues));
        }
        catch (RuntimeException e)
        {
            log.log(Level.FINE, "Cannot get the form of " + probe.getInfo().getName(), e);
            return result;
        }
        if (parameters instanceof DynamicPropertySet)
        {
            for (String field : fields)
            {
                DynamicProperty property = findProperty((DynamicPropertySet) parameters, field);
                if (property != null && "Base64File".equals(fileClassName(property))) result.add(field);
            }
        }
        return result;
    }

    private static DynamicProperty findProperty(DynamicPropertySet dps, String name)
    {
        for (DynamicProperty property : dps)
        {
            if (property.getName().equals(name)) return property;
            if (property.getValue() instanceof DynamicPropertySet)
            {
                DynamicProperty nested = findProperty((DynamicPropertySet) property.getValue(), name);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    /**
     * Runs the call with the multipart files attached to the request, as if it was a multipart one.
     */
    private <T> T withFiles(Submission submission, Supplier<T> call)
    {
        Request request = submission.multipartFiles == null ? null : currentRequest();
        if (submission.multipartFiles != null && request == null)
        {
            throw new IllegalStateException("Files for " + submission.multipartFiles.keySet()
                    + " can be given only within a request of the user");
        }
        if (request != null)
        {
            request.setAttribute(FileUploadWrapper.ATTRIBUTE,
                    new FileUploadWrapper(request.getRawRequest(), submission.multipartFiles));
        }
        try
        {
            return call.get();
        }
        finally
        {
            if (request != null) request.setAttribute(FileUploadWrapper.ATTRIBUTE, null);
            submission.inlineFiles.forEach(FileItem::delete);
        }
    }

    /**
     * A file in the values, or several of them, or null.
     */
    @SuppressWarnings("unchecked")
    private static List<Map<?, ?>> fileValues(Object value)
    {
        if (isFileValue(value)) return Collections.singletonList((Map<?, ?>) value);
        if (value instanceof List && !((List<?>) value).isEmpty()
                && ((List<?>) value).stream().allMatch(QueryOperationService::isFileValue))
        {
            return (List<Map<?, ?>>) value;
        }
        return null;
    }

    private static boolean isFileValue(Object value)
    {
        return value instanceof Map && (((Map<?, ?>) value).containsKey("fileId")
                || ((Map<?, ?>) value).containsKey("base64"));
    }

    /**
     * A file sent in the values: its name, MIME type and content in base64 (or a data: URL).
     */
    static FileItem inlineFile(String field, Map<?, ?> value)
    {
        Object nameValue = value.get("name");
        String name = nameValue == null || nameValue.toString().trim().isEmpty() ? "file" : nameValue.toString().trim();
        Object mimeValue = value.get("mimeType");
        String mimeType = mimeValue == null ? null : mimeValue.toString().trim();
        String data = value.get("base64") == null ? "" : value.get("base64").toString().trim();
        if (data.startsWith("data:") && data.indexOf(',') > 0)
        {
            String header = data.substring("data:".length(), data.indexOf(','));
            if (Strings.isNullOrEmpty(mimeType) && header.contains(";"))
            {
                mimeType = header.substring(0, header.indexOf(';'));
            }
            data = data.substring(data.indexOf(',') + 1);
        }
        data = data.replaceAll("\\s", "");

        long maxSize = McpFileStore.maxSize();
        if (data.length() / 4L * 3 > maxSize + 3)
        {
            throw new IllegalArgumentException("File " + name + " is larger than " + maxSize
                    + " bytes, upload it by a link from upload_file");
        }
        byte[] bytes;
        try
        {
            bytes = (data.indexOf('-') >= 0 || data.indexOf('_') >= 0 ? Base64.getUrlDecoder() : Base64.getDecoder())
                    .decode(data);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The content of file " + name + " is not base64: " + e.getMessage());
        }
        if (bytes.length > maxSize)
        {
            throw new IllegalArgumentException("File " + name + " is larger than " + maxSize
                    + " bytes, upload it by a link from upload_file");
        }

        FileItem item = new DiskFileItemFactory().createItem(field,
                Strings.isNullOrEmpty(mimeType) ? "application/octet-stream" : mimeType, false, name);
        try (OutputStream out = item.getOutputStream())
        {
            out.write(bytes);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot keep file " + name, e);
        }
        return item;
    }

    /**
     * Files are logged by their names, not by their content.
     */
    private static Map<String, Object> loggedValues(Map<String, Object> values)
    {
        Map<String, Object> result = new LinkedHashMap<>(values);
        result.replaceAll((name, value) -> value instanceof Base64File || value instanceof Base64File[]
                ? fileNames(value) : value);
        return result;
    }

    /**
     * The name (or names) of the files a file field has, the content left out.
     */
    private static Object fileNames(Object value)
    {
        if (value instanceof Base64File) return ((Base64File) value).getName();
        if (value instanceof Object[])
        {
            List<Object> names = new ArrayList<>();
            for (Object item : (Object[]) value)
            {
                Object itemName = fileNames(item);
                if (itemName != null) names.add(itemName);
            }
            return names;
        }
        return value instanceof String ? value : null;
    }

    /**
     * Base64File or File when the field takes files (by the simple class name, as the UI decides), otherwise null.
     */
    private static String fileClassName(DynamicProperty property)
    {
        Class<?> type = property.getType();
        if (type == null) return null;
        if (type.isArray()) type = type.getComponentType();
        String name = type.getSimpleName();
        return "Base64File".equals(name) || "File".equals(name) ? name : null;
    }

    private Operation createOperation(String entityName, String queryName, String operationName,
                                      Map<String, Object> params, List<String> selectedRows)
    {
        // as in the UI, an operation is run from a query the user may open
        getQuery(entityName, queryName);
        OperationInfo info = new OperationInfo(userAwareMeta.getOperation(entityName, queryName, operationName));
        if (!info.getModel().isAiAccessible())
        {
            throw new IllegalArgumentException("Operation '" + operationName + "' is not accessible to AI agents");
        }
        if (OPERATION_TYPE_JAVASCRIPT.equals(info.getType()))
        {
            throw new IllegalArgumentException("Operation '" + operationName + "' runs in the browser only");
        }
        if (info.getRecords() == VISIBLE_WHEN_ONE_SELECTED_RECORD && selectedRows.size() != 1)
        {
            throw new IllegalArgumentException("Operation '" + operationName
                    + "' needs exactly one selected row (selectedRows), not " + selectedRows.size());
        }
        if (info.getRecords() == VISIBLE_WHEN_ANY_SELECTED_RECORDS && selectedRows.isEmpty())
        {
            throw new IllegalArgumentException("Operation '" + operationName
                    + "' needs at least one selected row (selectedRows)");
        }

        Map<String, Object> operationParams = toContextParams(params);
        if (!selectedRows.isEmpty())
        {
            operationParams.put(SELECTED_ROWS, selectedRows.toArray(new String[0]));
        }
        Operation operation = operationExecutor.create(info, queryName, operationParams);
        Request request = currentRequest();
        if (operation instanceof OperationSupport && request != null)
        {
            ((OperationSupport) operation).setRequest(request);
        }
        return operation;
    }

    /**
     * A query the user may open (by the roles) that is accessible to AI agents (aiAccessible of the query).
     */
    private Query getQuery(String entityName, String queryName)
    {
        Query query = userAwareMeta.getQuery(entityName, queryName);
        if (!query.isAiAccessible())
        {
            throw new IllegalArgumentException("Query '" + queryName + "' is not accessible to AI agents");
        }
        return query;
    }

    /**
     * The request of the user, or null outside of a request (in a thread of a chatbot, say).
     */
    private Request currentRequest()
    {
        try
        {
            return requestProvider.get();
        }
        catch (RuntimeException e)
        {
            for (Throwable cause = e; cause != null; cause = cause.getCause())
            {
                if (cause instanceof OutOfScopeException) return null;
            }
            throw e;
        }
    }

    private Map<String, Object> queryHeader(Query query, boolean withEntity)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (withEntity) result.put("entityName", query.getEntity().getName());
        result.put("queryName", query.getName());
        result.put("title", userAwareMeta.getLocalizedQueryTitle(query.getEntity().getName(), query.getName()));
        result.put("type", query.getType().getName());
        putIfNotEmpty(result, "description", query.getShortDescription());
        return result;
    }

    private Map<String, Object> operationHeader(OperationInfo info)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("operationName", info.getName());
        result.put("title", userAwareMeta.getLocalizedOperationTitle(info.getModel()));
        putIfNotEmpty(result, "description", info.getModel().getShortDescription());
        switch (info.getRecords())
        {
            case VISIBLE_WHEN_ONE_SELECTED_RECORD:
                result.put("selectedRows", "exactlyOne");
                break;
            case VISIBLE_WHEN_ANY_SELECTED_RECORDS:
                result.put("selectedRows", "atLeastOne");
                break;
            default:
                result.put("selectedRows", "optional");
        }
        if (info.isConfirm()) result.put("requiresConfirmation", true);
        return result;
    }

    /**
     * The {@code <parameter:name/>} placeholders of a BE-SQL query and the entity whose columns filter it.
     */
    private static void addQueryParameters(Map<String, Object> result, Query query)
    {
        if (query.getType() != QueryType.D1 && query.getType() != QueryType.D1_UNKNOWN) return;
        AstStart ast;
        try
        {
            ast = SqlQuery.parse(query.getFinalQuery());
            new MacroExpander().expandMacros(ast);
        }
        catch (RuntimeException e)
        {
            log.log(Level.FINE, "Cannot parse query " + query.getCompletePath(), e);
            return;
        }

        Map<String, Map<String, Object>> parameters = new LinkedHashMap<>();
        ast.tree().select(AstBeParameterTag.class).forEach(tag -> {
            if (parameters.containsKey(tag.getName())) return;
            Map<String, Object> parameter = new LinkedHashMap<>();
            parameter.put("name", tag.getName());
            putIfNotEmpty(parameter, "defaultValue", tag.getDefValue());
            if (tag.getMultiple() != null && !"false".equalsIgnoreCase(tag.getMultiple()))
            {
                parameter.put("multiple", true);
            }
            putIfNotEmpty(parameter, "refColumn", tag.getRefColumn());
            parameters.put(tag.getName(), parameter);
        });
        result.put("parameters", new ArrayList<>(parameters.values()));
        putIfNotEmpty(result, "filterEntity", QueryMetaHelper.getMainTableDefName(ast));
    }

    private static boolean isRunnable(Query query)
    {
        QueryType type = query.getType();
        return type == QueryType.D1 || type == QueryType.D1_UNKNOWN || type == QueryType.JAVA
                || type == QueryType.GROOVY;
    }

    private static List<Map<String, Object>> describeFields(Object parameters)
    {
        List<Map<String, Object>> fields = new ArrayList<>();
        if (parameters instanceof DynamicPropertySet)
        {
            addFields(fields, (DynamicPropertySet) parameters);
        }
        return fields;
    }

    private static void addFields(List<Map<String, Object>> fields, DynamicPropertySet dps)
    {
        for (DynamicProperty property : dps)
        {
            if (property.isHidden()) continue;
            if (property.getValue() instanceof DynamicPropertySet)
            {
                addFields(fields, (DynamicPropertySet) property.getValue());
            }
            else
            {
                fields.add(describeField(property));
            }
        }
    }

    static Map<String, Object> describeField(DynamicProperty property)
    {
        Map<String, Object> field = new LinkedHashMap<>();
        String type = fieldType(property);
        field.put("name", property.getName());
        field.put("title", property.getDisplayName());
        field.put("type", type);
        Object value = "file".equals(type) ? fileNames(property.getValue()) : plainValue(property.getValue());
        if (value != null && !"".equals(value)) field.put("value", value);
        if ("file".equals(type) && property.getType() != null && property.getType().isArray())
        {
            field.put("multiple", true);
        }

        boolean input = !"label".equals(type) && !"button".equals(type);
        if (property.isReadOnly()) field.put("readOnly", true);
        else if (input && !property.isCanBeNull()) field.put("required", true);
        if (property.getBooleanAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST)) field.put("multiple", true);
        if (property.getBooleanAttribute(BeanInfoConstants.RELOAD_ON_CHANGE)) field.put("reloadOnChange", true);
        if (property.getBooleanAttribute(BeanInfoConstants.RELOAD_ON_CLICK)) field.put("reloadOnClick", true);

        String description = property.getShortDescription();
        if (description != null && !description.equals(property.getName())
                && !description.equals(property.getDisplayName()))
        {
            putIfNotEmpty(field, "description", description);
        }
        Object placeholder = property.getAttribute(BeanInfoConstants.PLACEHOLDER);
        if (placeholder != null) putIfNotEmpty(field, "placeholder", placeholder.toString());
        addOptions(field, property.getAttribute(BeanInfoConstants.TAG_LIST_ATTR));

        Object status = property.getAttribute(BeanInfoConstants.STATUS);
        if (status != null) field.put("status", status.toString());
        Object message = property.getAttribute(BeanInfoConstants.MESSAGE);
        if (message != null) putIfNotEmpty(field, "message", message.toString());
        return field;
    }

    /**
     * The values a select offers: {value, title} pairs or plain values, as the tag list is given.
     */
    private static void addOptions(Map<String, Object> field, Object tags)
    {
        Object[] items = tags instanceof Object[] ? (Object[]) tags : null;
        if (items == null) return;

        List<Object> options = new ArrayList<>();
        for (int i = 0; i < items.length && i < MAX_OPTIONS; i++)
        {
            if (items[i] instanceof Object[] && ((Object[]) items[i]).length >= 2)
            {
                Object[] tag = (Object[]) items[i];
                Map<String, Object> option = new LinkedHashMap<>();
                option.put("value", plainValue(tag[0]));
                option.put("title", plainValue(tag[1]));
                options.add(option);
            }
            else
            {
                options.add(plainValue(items[i]));
            }
        }
        field.put("options", options);
        if (items.length > MAX_OPTIONS) field.put("optionsTotal", items.length);
    }

    private static String fieldType(DynamicProperty property)
    {
        if (property.getBooleanAttribute(BeanInfoConstants.LABEL_FIELD)) return "label";
        Object extraAttrs = property.getAttribute(BeanInfoConstants.EXTRA_ATTRS);
        if (extraAttrs instanceof Map && BeanInfoConstants.BUTTON_FIELD.equalsIgnoreCase(
                String.valueOf(((Map<?, ?>) extraAttrs).get(BeanInfoConstants.PROPERTY_INPUT_TYPE))))
        {
            return "button";
        }
        if (fileClassName(property) != null) return "file";
        Class<?> type = property.getType();
        if (type == null) return "string";
        if (type.isArray()) type = type.getComponentType();
        if (type == Boolean.class || type == boolean.class) return "boolean";
        if (type == Integer.class || type == int.class || type == Long.class || type == long.class
                || type == Short.class || type == short.class || type == java.math.BigInteger.class)
        {
            return "integer";
        }
        if (Number.class.isAssignableFrom(type) || type == double.class || type == float.class) return "number";
        if (java.sql.Timestamp.class.isAssignableFrom(type)) return "datetime";
        if (java.sql.Time.class.isAssignableFrom(type)) return "time";
        if (java.util.Date.class.isAssignableFrom(type)) return "date";
        return "string";
    }

    /**
     * A value as JSON shows it: dates in ISO form (yyyy-MM-dd), binary data as "Blob".
     */
    static Object plainValue(Object value)
    {
        if (value == null || value instanceof String || value instanceof Boolean) return value;
        if (value instanceof Number)
        {
            double number = ((Number) value).doubleValue();
            return Double.isNaN(number) || Double.isInfinite(number) ? value.toString() : value;
        }
        if (value instanceof byte[] || value instanceof Blob) return "Blob";
        if (value instanceof Object[])
        {
            List<Object> list = new ArrayList<>();
            for (Object item : (Object[]) value) list.add(plainValue(item));
            return list;
        }
        return value.toString();
    }

    /**
     * Values as the UI sends them in a request: strings, or arrays of strings for several values.
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> toRequestValues(Map<String, Object> values)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            Object value = entry.getValue();
            if (value instanceof List || value instanceof Object[])
            {
                List<Object> items = value instanceof List ? (List<Object>) value : Arrays.asList((Object[]) value);
                String[] strings = new String[items.size()];
                for (int i = 0; i < strings.length; i++) strings[i] = toRequestValue(items.get(i));
                result.put(entry.getKey(), strings);
            }
            else
            {
                result.put(entry.getKey(), toRequestValue(value));
            }
        }
        return result;
    }

    /**
     * The parameters of a query (filters) as the UI passes them, which are never null.
     */
    static Map<String, Object> toContextParams(Map<String, Object> params)
    {
        Map<String, Object> result = toRequestValues(params);
        result.values().removeIf(value -> value == null);
        result.replaceAll((name, value) -> value instanceof String[]
                ? Arrays.stream((String[]) value).filter(item -> item != null).toArray(String[]::new) : value);
        return result;
    }

    private static String toRequestValue(Object value)
    {
        if (value == null) return null;
        if (value instanceof BigDecimal) return ((BigDecimal) value).toPlainString();
        if (value instanceof Map) return JsonFactory.jsonb.toJson(value);
        return value.toString();
    }

    private String errorMessage(OperationResult result)
    {
        String message = result.getMessage();
        if (Strings.isNullOrEmpty(message) && result.getDetails() instanceof Throwable)
        {
            message = ((Throwable) result.getDetails()).getMessage();
        }
        return Strings.isNullOrEmpty(message) ? "The operation failed"
                : userAwareMeta.getLocalizedExceptionMessage(message);
    }

    private static void putIfNotEmpty(Map<String, Object> map, String key, String value)
    {
        if (!Strings.isNullOrEmpty(value)) map.put(key, value);
    }
}
