package org.megastage.vase.test;

import org.megastage.vase.VaseComponent;
import org.megastage.vase.VaseInject;

@VaseComponent(100)
public class ComponentA {

    @VaseInject
    private ComponentB componentB;

    @VaseInject
    private ComponentC componentC;

    public void initialize() {
        System.out.println("ComponentA.initialize");
    }

    public void shutdown() {
        System.out.println("ComponentA.shutdown");
        System.out.println("componentB = " + componentB);
        System.out.println("componentC = " + componentC);
    }
}
