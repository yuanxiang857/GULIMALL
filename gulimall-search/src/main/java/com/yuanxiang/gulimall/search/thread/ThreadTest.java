package com.yuanxiang.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService execute = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("begin----");
//        CompletableFuture<Integer> future=CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果" + i);
//            return i;
//        }, execute).whenComplete((res,exception)->{
//            System.out.println("结果是"+res);
//            System.out.println("异常是"+exception);
//        }).exceptionally(throwable ->{
//            //感知异常并且修改返回结果
//            return 10;
//        });
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果" + i);
//            return i;
//        }, execute).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (thr != null) {
//                return 0;
//            }
//            return 0;
//        });
//        Integer integer = future.get();
//        System.out.println("end----" + integer);
        //线程串行化
        /**
         * 1、thenRunSync不接受上一步的结果
         * 2、thenAcceptSync 接受上一步结果，但是没有返回值
         * 3、thenApply 接受上一步结果，且有返回值
         */
//        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果" + i);
//            return i;
//        }, execute).thenApplyAsync(res -> {
//            System.out.println("任务2启动了" + res);
//            return "hello" + res;
//        }, execute);
//        //
//        System.out.println("end"+stringCompletableFuture.get());
        //两个都完成
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务一开始：" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("任务一结束" + i);
            return i;
        }, execute);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务二开始：" + Thread.currentThread().getId());
            System.out.println("任务二结束：");
            return "hello";
        }, execute);

//        future01.runAfterBothAsync(future02, () -> System.out.println("任务三开始"),execute);//不能感知结果
        future01.thenAcceptBothAsync(future02, (f1,f2) -> System.out.println("任务三开始"+"--->"+f1+"--->"+f2),execute);//不能感知结果
        //thenCombineAsync 可以返回结果
        System.out.println("=====end============");

        /**runAfterEitherAsync() 不感知结果，自己也无返回值。
         *acceptEitherAsync() 感知结果，自己也无返回值。 返回值类型得相同
         *appleToEitherAsync() 感知结果，自己有返回值。
         */
        /**
         * allof()等待所有结果完成
         * anyof()一个成功就行
         */

    }

    public void thread(String[] args) throws ExecutionException, InterruptedException {
        //1、继承Thread
//        System.out.println("main...start");
//        Thread01 thread = new Thread01();
//        thread.start();
//        System.out.println("main....end");
        //2、实现Runable接口
//        System.out.println("main...start");
//        Runable01 runable01 = new Runable01();
//        new Thread(runable01).start();
//        System.out.println("main....end");
        //3、继承Callable接口
        System.out.println("main...start");
//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        //阻塞等待整个线程执行完毕，获取返回结果
//        Integer integer=futureTask.get();
        //4、线程池
        //保证当前系统线程池只有一两个
        /**
         * 线程池创建的7大参数
         * Creates a new {@code ThreadPoolExecutor} with the given initial
         * parameters and default thread factory.
         *
         * @param corePoolSize the number of threads to keep in the pool, even
         *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
         *        核心线程数
         * @param maximumPoolSize the maximum number of threads to allow in the
         *        pool
         *        最大线程数量
         * @param keepAliveTime when the number of threads is greater than
         *        the core, this is the maximum time that excess idle threads
         *        will wait for new tasks before terminating.
         *        存活时间
         * @param unit the time unit for the {@code keepAliveTime} argument
         *        时间单位
         * @param workQueue the queue to use for holding tasks before they are
         *        executed.  This queue will hold only the {@code Runnable}
         *        tasks submitted by the {@code execute} method.
         *        阻塞队列。只要有线程空闲，就会去队伍中取
         * @threadFactory: 线程的执行工厂
         * @param handler the handler to use when execution is blocked
         *        because the thread bounds and queue capacities are reached
         *        如果队列满了，拒绝任务
         * @throws IllegalArgumentException if one of the following holds:<br>
         *         {@code corePoolSize < 0}<br>
         *         {@code keepAliveTime < 0}<br>
         *         {@code maximumPoolSize <= 0}<br>
         *         {@code maximumPoolSize < corePoolSize}
         * @throws NullPointerException if {@code workQueue}
         *         or {@code handler} is null
         *
         *工作流程
         *  1、线程池创建，准备好core数量的核心线程。准备执行任务
         *  2、core满了，就将放进来的任务放入阻塞队列中。空闲的core就回去阻塞队列中获取任务执行
         *  3、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         *  4、max都执行完成，有很多空闲，在指定的时间以后开始释放max-core数量的线程。
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        /**
         * 区别
         *  1、2不能得到返回值。3可以获得返回值
         *  1、2、3都不能控制资源
         *  4可以控制资源，性能稳定
         */
        execute.execute(new Thread01());
        System.out.println("main....end");

    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }

    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }
    }


}
