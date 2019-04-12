package invokeinconstructor;

import java.time.Instant;

/**
 * 子类，添加了一个成员变量并在可覆盖方法中使用了它
 *
 * @author LightDance
 */
public class SubClass extends SuperClass{

    private Instant instance = Instant.now();

    @Override
    protected void overrideMe() {
        System.out.println(instance);
    }
}
