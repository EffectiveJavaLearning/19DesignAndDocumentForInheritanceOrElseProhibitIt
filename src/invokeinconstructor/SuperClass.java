package invokeinconstructor;

/**
 * 父类，构造方法中调用了可被覆盖的方法
 *
 * @author LightDance
 */
public class SuperClass {

    protected void overrideMe(){}

    SuperClass(){
        overrideMe();
    }
}
