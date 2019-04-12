package invokeinconstructor;

import java.time.Instant;

/**
 * 子类，添加了一个成员变量并在可覆盖方法中使用了它，而父类的构造方法中又调用了这个可覆盖方法。
 * 幸好这里println为null没问题，不然就异常退出了
 *
 * @author LightDance
 */
public class SubClass extends SuperClass{

    private Instant instance;

    public SubClass(){
        instance = Instant.now();
    }
    @Override
    protected void overrideMe() {
        System.out.println(instance);
    }
}
