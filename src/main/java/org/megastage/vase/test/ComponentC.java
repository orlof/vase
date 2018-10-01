package org.megastage.vase.test;

import org.megastage.vase.VaseComponent;
import org.megastage.vase.VaseInject;

@VaseComponent(200)
public class ComponentC {

    @VaseInject
    private ComponentA componentA;

    @VaseInject
    private ComponentB componentB;

    public void initialize() {
        System.out.println("ComponentC.initialize");
    }

    public void shutdown() {
        System.out.println("ComponentC.shutdown");
        System.out.println("componentA = " + componentA);
        System.out.println("componentB = " + componentB);
    }
}
