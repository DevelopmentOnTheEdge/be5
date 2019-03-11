package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationSet;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.util.Collections3;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.util.JsonUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class DocumentOperationsPlugin implements DocumentPlugin
{
    private final UserInfoProvider userInfoProvider;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public DocumentOperationsPlugin(UserInfoProvider userInfoProvider, UserAwareMeta userAwareMeta,
                                    DocumentGenerator documentGenerator)
    {
        this.userInfoProvider = userInfoProvider;
        this.userAwareMeta = userAwareMeta;
        documentGenerator.addDocumentPlugin("documentOperations", this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        List<TableOperationPresentation> operations = collectOperations(query);
        if (operations.size() > 0)
        {
            return new ResourceData("documentOperations", operations, null);
        }

        return null;
    }

    private List<TableOperationPresentation> collectOperations(Query query)
    {
        List<TableOperationPresentation> operations = new ArrayList<>();
        List<String> userRoles = userInfoProvider.getCurrentRoles();

        for (Operation operation : getQueryOperations(query))
        {
            if (isAllowed(operation, userRoles))
            {
                operations.add(presentOperation(query, operation));
            }
        }

        operations.sort(Comparator.comparing(TableOperationPresentation::getTitle));

        return operations;
    }

    private List<Operation> getQueryOperations(Query query)
    {
        List<Operation> queryOperations = new ArrayList<>();
        OperationSet operationNames = query.getOperationNames();

        for (String operationName : operationNames.getFinalValues())
        {
            Operation op = query.getEntity().getOperations().get(operationName);
            if (op != null)
                queryOperations.add(op);
        }

        return queryOperations;
    }

    private TableOperationPresentation presentOperation(Query query, Operation operation)
    {
        String visibleWhen = determineWhenVisible(operation);
        String title = userAwareMeta.getLocalizedOperationTitle(query.getEntity().getName(), operation.getName());
        boolean requiresConfirmation = operation.isConfirm();
        boolean isClientSide = Operation.OPERATION_TYPE_JAVASCRIPT.equals(operation.getType());
        String action = null;
        if (isClientSide)
        {
            action = operation.getCode();
        }
        Map<String, Object> layout = JsonUtils.getMapFromJson(operation.getLayout());
        return new TableOperationPresentation(operation.getName(), title, visibleWhen, requiresConfirmation,
                isClientSide, action, layout);
    }

    private static String determineWhenVisible(Operation operation)
    {
        switch (operation.getRecords())
        {
            case Operation.VISIBLE_ALWAYS:
            case Operation.VISIBLE_ALL_OR_SELECTED:
                return "always";
            case Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD:
                return "oneSelected";
            case Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS:
                return "anySelected";
            case Operation.VISIBLE_WHEN_HAS_RECORDS:
                return "hasRecords";
            default:
                throw new AssertionError();
        }
    }

    private static boolean isAllowed(Operation operation, List<String> userRoles)
    {
        return Collections3.containsAny(userRoles, operation.getRoles().getFinalRoles());
    }

    public static class TableOperationPresentation
    {
        public final String name;
        public final String title;
        public final String visibleWhen;
        public final boolean requiresConfirmation;
        public final boolean clientSide;
        public final String action;
        private final Object layout;

        public TableOperationPresentation(String name, String title, String visibleWhen, boolean requiresConfirmation,
                                          boolean clientSide, String action, Object layout)
        {
            this.name = name;
            this.title = title;
            this.visibleWhen = visibleWhen;
            this.requiresConfirmation = requiresConfirmation;
            this.clientSide = clientSide;
            this.action = action;
            this.layout = layout;
        }

        public String getName()
        {
            return name;
        }

        public String getTitle()
        {
            return title;
        }

        public String getVisibleWhen()
        {
            return visibleWhen;
        }

        public boolean isRequiresConfirmation()
        {
            return requiresConfirmation;
        }

        public boolean isClientSide()
        {
            return clientSide;
        }

        public String getAction()
        {
            return action;
        }

        public Object getLayout()
        {
            return layout;
        }
    }
}
