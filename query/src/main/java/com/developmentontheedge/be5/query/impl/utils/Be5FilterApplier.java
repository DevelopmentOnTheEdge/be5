package com.developmentontheedge.be5.query.impl.utils;

import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.format.FilterApplier;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstInValueList;
import com.developmentontheedge.sql.model.SimpleNode;

import java.util.List;
import java.util.Map;

import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_EQ;
import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_IN;
import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_LIKE;
import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_UPPER;
import static java.util.stream.Collectors.toList;


public class Be5FilterApplier extends FilterApplier
{
    @Override
    public void setConditions(SimpleNode where, Map<ColumnRef, List<Object>> conditions)
    {
        for (Map.Entry<ColumnRef, List<Object>> entry : conditions.entrySet())
        {
            List<Object> parameter = entry.getValue();
            AstFunNode node;
            if (parameter.size() == 1)
            {
                Object value = parameter.get(0);
                if (value.getClass() == String.class)
                {
                    AstFunNode op1 = FUNC_UPPER.node(entry.getKey().asNode());
                    AstFunNode op2 = FUNC_UPPER.node(toNode("%" + parameter.get(0) + "%"));
                    node = FUNC_LIKE.node(op1, op2);
                }
                else
                {
                    node = FUNC_EQ.node(entry.getKey().asNode(), toNode(parameter.get(0)));
                }
            }
            else
            {
                List<SimpleNode> nodes = parameter.stream().map(this::toNode).collect(toList());
                node = FUNC_IN.node(entry.getKey().asNode(), AstInValueList.of(nodes));
            }
            where.addChild(node);
        }
    }
}
