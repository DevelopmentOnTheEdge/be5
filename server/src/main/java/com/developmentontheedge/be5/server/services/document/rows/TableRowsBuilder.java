package com.developmentontheedge.be5.server.services.document.rows;

import java.util.List;

public interface TableRowsBuilder<RowT>
{
    List<RowT> build();
}
