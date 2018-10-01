package org.megastage.vase.test;

import org.megastage.vase.SqlNotNull;
import org.megastage.vase.SqlReferences;
import org.megastage.vase.SqlTableName;
import org.megastage.vase.SqlUnique;

@SqlTableName("pojo_a")
public class PojoA {
    @SqlNotNull
    @SqlUnique
    @SqlReferences(PojoB.class)
    public int pojo_b;

    public TestEnum testEnum;
    public TestEnum testEnum1;

}
