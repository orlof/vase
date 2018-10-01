package org.megastage.vase.test;

import org.megastage.vase.VaseComponent;
import org.megastage.vase.VaseInject;

@VaseComponent(300)
public class ComponentB {

    @VaseInject
    private ComponentA componentA;

    @VaseInject
    private ComponentC componentC;

    public void initialize() {
        System.out.println("ComponentB.initialize");
    }

    public void shutdown() {
        System.out.println("ComponentB.shutdown");
        System.out.println("componentA = " + componentA);
        System.out.println("componentC = " + componentC);
    }
}
