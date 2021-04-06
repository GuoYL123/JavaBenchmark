
import java.util.function.Supplier;

public class LambdaFactoryTest {

	private static Supplier<Object> getter;

	public static void main(String[] args) {
		System.out.print("Native call         :");
		TestDemo demo = new TestDemo();
		timer(demo::test);
		System.out.print("LambdaFactory call  :");
		timer(LambdaFactoryTest::testLambdaFactory);
		System.out.print("Reflect call        :");
		timer(LambdaFactoryTest::testReflect);
	}

	public static void timer(Supplier<Object> func) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			func.get();
		}
		long end = System.currentTimeMillis();
		System.out.println("process time: " + (end - start) + "ms");
	}

	public static Object testLambdaFactory() {
		try {
			if (getter == null) {
				getter = LambdaMetafactoryUtils.createLambda(new TestDemo(), TestDemo.class.getMethod("test"), Supplier.class);
			}
			return getter.get();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object testReflect() {
		try {
			return TestDemo.class.getMethod("test").invoke(new TestDemo());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	static class TestDemo {

		public Object test() {
			return null;
		}
	}
}
