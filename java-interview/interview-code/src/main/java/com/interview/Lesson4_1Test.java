package com.interview;


import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * java.io 包下的类有哪些 + 面试题
 */
public class Lesson4_1Test {
    /**
     * NIO 多路复用
     */
    @Test
    public void nio() throws InterruptedException {
        int port = 6666;
        ExecutorService server = Executors.newFixedThreadPool(1);
        server.execute(() -> {
            try (Selector selector = Selector.open();
                 ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();) {
                serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                while (true) {
                    selector.select(); // 阻塞等待就绪的 Channel
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        try (SocketChannel channel = ((ServerSocketChannel) key.channel()).accept()) {
                            channel.write(Charset.defaultCharset().encode("老王，你好~"));
                        }
                        iterator.remove();
                    }
                    System.out.println("while...");
                }
            } catch (IOException ignored) {
            }
        });

        TimeUnit.SECONDS.sleep(3);


        ExecutorService client = Executors.newFixedThreadPool(2);
        client.execute(() -> {
            // Socket 客户端（接收信息并打印）
            try (Socket cSocket = new Socket(InetAddress.getLocalHost(), port)) {
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
                bufferedReader1.lines().forEach(s -> System.out.println("客户端 1 打印：" + s));
            } catch (IOException ignored) {
            }
        });

        client.execute(() -> {
            // Socket 客户端（接收信息并打印）
            try (Socket cSocket = new Socket(InetAddress.getLocalHost(), port)) {
                BufferedReader bufferedReader12 = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
                bufferedReader12.lines().forEach(s -> System.out.println("客户端 2 打印：" + s));
            } catch (IOException ignored) {
            }
        });

        while (true) {
            // System.out.println(client.isShutdown() + ":" + client.isTerminated());
        }
        // TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void nioClient() throws InterruptedException {
        // Socket 客户端（接收信息并打印）
        try (Socket cSocket = new Socket(InetAddress.getLocalHost(), 6666)) {
            BufferedReader bufferedReader12 = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
            bufferedReader12.lines().forEach(s -> System.out.println("客户端 2 打印：" + s));
        } catch (IOException ignored) {
        }
    }

    @Test
    public void aio() throws ExecutionException, InterruptedException, IOException {
        int port2 = 8888;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> {
            try {
                AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(4));
                AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group)
                        .bind(new InetSocketAddress(InetAddress.getLocalHost(), port2));
                server.accept(null, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
                        server.accept(null, this); // 接收下一个请求
                        try {
                            Future<Integer> f = result.write(Charset.defaultCharset().encode("Hi, 老王"));
                            f.get();
                            System.out.println("服务端发送时间：" + DateFormat.getDateTimeInstance().format(new Date()));
                            result.close();
                        } catch (InterruptedException | ExecutionException | IOException ignored) {
                        }
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                    }
                });
                group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (IOException | InterruptedException ignored) {
            }
        });

        TimeUnit.SECONDS.sleep(2);

        // Socket 客户端
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        Future<Void> future = client.connect(new InetSocketAddress(InetAddress.getLocalHost(), port2));
        future.get();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        client.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                System.out.println("客户端打印：" + new String(buffer.array()));
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread.sleep(10 * 1000);
    }


    @Test
    public void inetaddress() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println(localHost);
        System.out.println(localHost.getCanonicalHostName());
        System.out.println(localHost.getHostName());
        System.out.println(localHost.getHostAddress());
        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        System.out.println(loopbackAddress);
        System.out.println(loopbackAddress.getCanonicalHostName());
        System.out.println(loopbackAddress.getHostName());
        System.out.println(loopbackAddress.getHostAddress());
        InetAddress[] allByName = InetAddress.getAllByName("baidu.com");
        System.out.println(Arrays.toString(allByName));
    }

    @Test
    public void createFile() throws IOException {
        Files.createFile(Paths.get("c:\\pf.txt"), PosixFilePermissions.asFileAttribute(
                EnumSet.of(PosixFilePermission.OWNER_READ)));
    }
}
