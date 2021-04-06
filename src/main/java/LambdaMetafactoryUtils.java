import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class LambdaMetafactoryUtils {
  private static final Lookup LOOKUP = MethodHandles.lookup();

  private LambdaMetafactoryUtils() {
  }

  protected static Method findAbstractMethod(Class<?> functionalInterface) {
    for (Method method : functionalInterface.getMethods()) {
      if ((method.getModifiers() & Modifier.ABSTRACT) != 0) {
        return method;
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T createLambda(Object instance, Method instanceMethod, Class<?> functionalIntfCls) {
    try {
      Method intfMethod = findAbstractMethod(functionalIntfCls);
      MethodHandle methodHandle = LOOKUP.unreflect(instanceMethod);

      MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
      MethodType instanceMethodType = MethodType
          .methodType(instanceMethod.getReturnType(), instanceMethod.getParameterTypes());
      CallSite callSite = LambdaMetafactory.metafactory(
          LOOKUP,
          intfMethod.getName(),
          MethodType.methodType(functionalIntfCls, instance.getClass()),
          intfMethodType,
          methodHandle,
          instanceMethodType);

      return (T) callSite.getTarget().bindTo(instance).invoke();
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T createLambda(Method instanceMethod, Class<?> functionalIntfCls) {
    if (Modifier.isNative(instanceMethod.getModifiers())) {
      // fix "Failed to create lambda from public final native java.lang.Class java.lang.Object.getClass()"
      return null;
    }
    try {
      Method intfMethod = findAbstractMethod(functionalIntfCls);
      MethodHandle methodHandle = LOOKUP.unreflect(instanceMethod);

      MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());

      // the return type of fluent setter is object instead of void, but we can assume the return type is void. it doesn't matter
      MethodType instanceMethodType = MethodType
          .methodType(intfMethod.getReturnType(), methodHandle.type().parameterList());
      CallSite callSite = LambdaMetafactory.metafactory(
          LOOKUP,
          intfMethod.getName(),
          MethodType.methodType(functionalIntfCls),
          intfMethodType,
          methodHandle,
          instanceMethodType);

      return (T) callSite.getTarget().invoke();
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
    }
  }
  private static void checkAccess(Field field) {
    // This check is not accurate. Most of time package visible and protected access can be ignored, so simply do this.
    if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
      throw new IllegalStateException(
          String.format("Can not access field, a public field or accessor is required."
                  + "Declaring class is %s, field is %s",
              field.getDeclaringClass().getName(),
              field.getName()));
    }
  }
}
