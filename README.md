# JavaBenchmark
java语言基础特性的一些性能测试

## jdk 8

### LambdaFactory代替反射

java的反射特性可以做到hark私有方法操作，但也一直因为破坏封装(jdk 9以上反射私有方法会报warn)以及低效的性能遭到吐槽，
java 8 的 lambda factory 特性可以代替反射换取更高的调用速度，在反复反射调用某一方法的场景下可以显著优化性能

测试代码：LambdaFactoryTest.java
代码说明：针对一个空方法（直接返回null）进行重复调用一千万次，比较不同调用方式的耗时差别。

| 调用方式             | 时间   |
| ------------------ | ------ |
| LambdaFactory call | 22ms   |
| Reflect call       | 2850ms |
| Native call        | 3ms    |

以上结果可见，原生调用和LambdaFactory调用速度相差一个数量级，LambdaFactory和反射调用相差两个数量级。

### Lambda表达式循环速度

todo: