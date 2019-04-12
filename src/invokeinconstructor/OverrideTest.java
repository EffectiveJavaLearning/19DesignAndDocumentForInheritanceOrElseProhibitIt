package invokeinconstructor;

/**
 * {@link SuperClass}和{@link SubClass}的测试类。
 *
 * 根据初始化的先后顺序，父类的构造方法会在子类构造方法之前被调用，
 * 因此如果父类的构造方法中调用了可被覆盖的方法，而子类又恰好覆盖了它，并在其中操作新添加的元素，
 * 由于子类未被初始化，因此该元素为null，所以这里打印出的第一行为null，这还全靠println允许参数为null，
 * 而且如果再严重一点，比如{@link SubClass#overrideMe()}中调用了{@link SubClass#instance}的方法，
 * 那么就会抛出{@link NullPointerException}.
 *
 * @author LightDance
 */
public class OverrideTest {

    public static void main(String[] args) {
        //print null
        SubClass sub = new SubClass();
        //print current-time
        sub.overrideMe();
    }
}
