package com.developmentontheedge.be5.server.services.impl.rows;

import java.util.List;

public interface TableRowsBuilder<RowT>
{
    List<RowT> build();
}
