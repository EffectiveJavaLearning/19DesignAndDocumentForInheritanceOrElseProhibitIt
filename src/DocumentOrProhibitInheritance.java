import java.util.Collection;

/**
 * 在item18中提到了对于不是专门用于继承又没有文档说明的类来讲，继承它是件十分危险的事。
 * 那么怎样才算“良好”的为继承而设计的类呢？
 *
 * 先说明下什么叫可重写方法。可重写方法是指非final的public或者protected型方法。
 *
 * 首先，类必须对子类覆盖自己方法所带来的影响做出准确说明，也就是说，要记录可重写方法的
 * “自我调用情况”，对每个public型或者protected型方法，文档必须说明该方法调用了哪些可重写方法，
 * 调用顺序如何，调用结果对后续程序运行情况的影响等。换句话说，类必须记录所有可能调用其可重写方法的情况，
 * 比如来自后台线程或者静态初始化块中对它们的调用。
 *
 * 如果某方法中调用了可重写的方法，那么应当在文档中提供关于这些调用的描述信息。
 * 对于文档中说明的格式也是有规范的，一般会写在文档注释的末尾处，以“Implementation Requirements,”开头，
 * 或者用Javadoc中的“@ImplSpec”，该标签可以在转换文档时自动转换过去。然后描述该方法中相关内部实现。比如
 * {@link java.util.AbstractCollection#remove(Object)}的注释，"@inheritDoc"表示继承了基类对应注释，
 * 不过这里是Java1.8,还没有@ImplSpec标记，我下载了个Java10就有了。所以把相关的注释摘过来以方便查阅：
 *  “
 *        {@inheritDoc}
 *
 *        @implSpec
 *        This implementation iterates over the collection looking for the
 *        specified element.  If it finds the element, it removes the element
 *        from the collection using the iterator's remove method.
 *
 *        <p>Note that this implementation throws an
 *        {@code UnsupportedOperationException} if the iterator returned by this
 *        collection's iterator method does not implement the {@code remove}
 *        method and this collection contains the specified object.
 *
 *        @ throws UnsupportedOperationException {@inheritDoc}
 *        @ throws ClassCastException            {@inheritDoc}
 *        @ throws NullPointerException          {@inheritDoc}
 *  ”
 * 由此可得，重写iterator()方法会令remove()受影响，且文档中具体说明了iterator()方法返回的
 * {@link java.util.Iterator}是如何影响remove()的。而与之相反的item18中，
 * 程序员就无法准确说明覆盖{@link java.util.HashSet#add(Object)}是否会对
 * {@link java.util.HashSet#addAll(Collection)}产生影响。
 *      
 * 但是这不又违反了Java文档“描述功能，屏蔽细节”的准则吗？确实。这就是继承破坏了封装(encapsulated)
 * 之后带来的不良影响，这也导致了若要保证继承安全，只能描述本不应出现的实现细节。
 *
 * “@ImplSpec”标签出现在Java8中，并在Java9及之后被大量使用。默认情况下应当加上这个标签，
 * 但实际使用的时候仍然不免忽略它。这个问题可以通过在命令行中使用 -tag "apiNote:a:API Note:"解决。
 *
 * 为了继承而设计不仅要记录下本类中调用它时的相关信息，为了让想继承它的程序员写出高效、无异常的代码，
 * 还应提供能进入其内部工作流程中的“钩子”(hook)，可以是精心设计的protected型方法，
 * 极少数情况下也可以是protected型成员变量，比如
 * {@link java.util.AbstractList#removeRange(int, int)}的注释：
 *  “
 *        Removes from this list all of the elements whose index is between
 *        {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
 *        Shifts any succeeding elements to the left (reduces their index).
 *        This call shortens the list by {@code (toIndex - fromIndex)} elements.
 *        (If {@code toIndex==fromIndex}, this operation has no effect.)
 *       
 *        <p>This method is called by the {@code clear} operation on this list
 *        and its subLists.  Overriding this method to take advantage of
 *        the internals of the list implementation can <i>substantially</i>
 *        improve the performance of the {@code clear} operation on this list
 *        and its subLists.
 *       
 *        @implSpec
 *        This implementation gets a list iterator positioned before
 *        {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
 *        followed by {@code ListIterator.remove} until the entire range has
 *        been removed.  <b>Note: if {@code ListIterator.remove} requires linear
 *        time, this implementation requires quadratic time.</b>
 *  ”
 * 这个方法的说明文档并不是写给最终用户看的，它的意义在于使子类能够更容易地实现对列表元素的快速清除。
 * 如果没有这个方法，那么子类实现同样功能时将不得不多次调用clear()方法，这将花费平方级别的时间代价——
 * 或者你愿意自己重新实现一个subList机制。
 *
 * 所有应该如何决定暴露哪些protected型方法或者成员变量呢？这一问题上并没有通用的准则，
 * 只能靠程序员自己预测、思考、测试。但暴露的方法或成员变量应该尽量少，
 * 因为每一次暴露都意味着公布实现细节并在之后的版本中兼容它们；但话说回来，又不可以暴露得太少，
 * 因为如果有该暴没暴的成员变量或方法存在，就可能导致继承这个类的子类无法使用。
 *
 * 对用于继承的类进行测试，唯一方法是为其编写子类。这样如果没有暴露关键的protected型成员，
 * 在子类继承的过程中会变得非常明显；而如果编写若干子类都没有用到某个protected型成员，
 * 那么就可以把它重新恢复成private型。实验表明，编写三个子类去测试其继承特性足矣，
 * 但测试过程中应有至少一个子类由开发者之外的人编写。
 *
 * 当编写有可能被广泛使用、继承的基类时，应该意识到需永远保证其中的方法或成员变量在本类内部
 * 被调用情况的文档说明与当前版本匹配，并对公开哪些关键字段以便子类继承完全负责。
 * 这些承诺有可能会在之后的版本中由于性能提高需求或功能改变而变得困难，因此必须在发放release
 * 版本之前认真地对其进行测试。
 *
 * 此外，应当注意的还有，与继承相关特殊说明文档实际上会打乱正常的文档信息，
 * 从而对那些想直接用这个类的程序员造成困扰。在《Effective Java 3rd》出版时，
 * 还仍然没有工具或者注释规范能把两者分开。
 *
 * 为了允许继承，还要遵守一些其他的约束。
 *
 * 首先，在构造方法中调用final型、private型、static型方法是安全的，
 * 因为这几种都不能被重写；但绝对不可以在构造函数中调用可能会被覆盖的方法,
 * 否则极有可能会导致程序崩溃{@link invokeinconstructor.OverrideTest}.
 *
 * 其次，为{@link Cloneable}和{@link java.io.Serializable}这两个接口设计继承类的时候，
 * 往往会有些困难，因为它俩为设计继承类的程序员增加了额外的负担。不过，可以通过一些技巧，
 * 来避开这些额外的工作量(见item 13&item 86)
 * 如果决定在设计基类时让基类implements Cloneable或者Serializable，那么应当注意，
 * 其中的readObject()和clone()的特征跟构造方法非常类似，也不可以随便调用可能会被覆盖的方法，
 * 例如{@link Object#clone()}中，重写后的方法会在子类的clone()被调用以前先被调用，从而导致程序异常，
 * 甚至还会对被克隆的原始对象造成破坏。例如企图让克隆副本的深层数据结构(deep structure)与原对象不同，
 * 而克隆操作还未完成时。
 *
 * 最后，如果决定在基类中implements{@link java.io.Serializable}接口，而这个基类又有
 * readResolve或writeReplace方法，那么应当把它俩的访问限定符设置为protected型，而不是private型。
 * 因为如果设置为private型，那么子类会自动忽略它们。这也是“为了继承将细节实现作为API的一部分”
 * 的另一种情况。
 *
 * 可见，设计一个基类往往要做很多工作，而且需要对其作出许多限制。一些情况下，这么做是没毛病的，
 * 比如抽象类，接口的框架实现(item20)，但另一些情况下就不太好，比如item17中提到过的不可变类。
 * 但对于普通的实体类呢？一般来说，由于这种类既非final型也不是专门为继承而设计的基类，因此比较危险。
 * 每次对这种类进行修改，都可能会导致其子类被破坏，而且这种情况下导致的子类bug报告也挺常见。
 *
 * 最保险的解决方案是把没有继承相关防范措施的类的子类化途径全部禁掉。简单一点就是把class设置成final型；
 * 也可以将构造方法设置成private型或package-private型，添加静态工厂方法获取实例以禁止继承。
 * (实际上不能子类化也是工厂方法替代构造方法的缺陷之一)
 *
 * 这一条建议或许有一点争议，因为许多程序员已经习惯了对普通类进行子类化，比如instrumentation
 * (仪表工具，如计数显示等)，notification(通知机制)，synchronization(同步功能)，
 * limit functionality(功能限制)等，都是通过子类化以添加新的功能。
 *
 * 如果类实现了反映其本质的接口，比如Set，Map这种，那么就最好禁掉继承，反而复合会更加适合这种情况。
 * 而如果类没有implements这样的接口，则禁掉继承或许会对其他程序员带来不便。但是若要允许这种继承的话，
 * 就有必要确保该类内部不会调用其任何可以被覆盖的方法，然后在文档中加以说明。换句话说，
 * 就是要消除一切可重写方法的自用(self-use)情况。这样创建的类非常安全，重写时也不会对其行为(behavior)
 * 造成影响。
 *
 * 可以通过在不改变类行为的前提下，消除此类中所有可重写方法自我调用的情况。例如，将它们改成private型，
 * 或将方法体搬到private型“助手方法”(helper method)中然后让可重写方法调用助手方法等。
 *
 *
 *
 *
 *
 *
 * @author LightDance
 */
public class DocumentOrProhibitInheritance {
}
