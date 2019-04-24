package com.developmentontheedge.be5.query.impl.beautifiers;

import com.developmentontheedge.be5.query.model.beans.QRec;

import java.util.List;

public interface SubQueryBeautifier
{
    String print(List<QRec> lists);
}
