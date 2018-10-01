package org.megastage.vase.test;

import org.megastage.vase.SqlNotNull;
import org.megastage.vase.SqlTableName;
import org.megastage.vase.SqlUnique;

@SqlTableName("pojo_b")
public class PojoB {
    @SqlNotNull
    @SqlUnique
    public int foobar;
}
