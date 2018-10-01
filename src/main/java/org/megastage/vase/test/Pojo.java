package org.megastage.vase.test;

import org.megastage.vase.SqlReferences;
import org.megastage.vase.SqlTableName;
import org.megastage.vase.SqlUnique;

@SqlTableName("pojo")
public class Pojo {
    @SqlReferences(PojoA.class)
    public int pojo_a;

    @SqlReferences(PojoB.class)
    public int pojo_b;

    @SqlReferences(PojoB.class)
    public int pojo_b2;

    @SqlUnique
    public TestEnum testEnum;
}
