package com.interview;

import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * java.io 包下的类有哪些 + 面试题
 */
public class Lesson4_1 {
    // Writer使用
    @Test
    public void writer() throws IOException {
        Writer writer = new FileWriter("io.txt", false);
        writer.append("老王");
        writer.close();
    }

    // Reader 使用
    @Test
    public void reader() throws IOException {
        Reader reader = new FileReader("io.txt");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String str = null;
        while (null != (str = bufferedReader.readLine())) {
            System.out.println(str);
        }
        bufferedReader.close();
        reader.close();
    }

    // InputStream 使用
    @Test
    public void inputStream() throws IOException {
        InputStream inputStream = new FileInputStream(new File("io.txt"));
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String content = new String(bytes, StandardCharsets.UTF_8);
        inputStream.close();
        System.out.println(content);
    }

    // OutputStream 使用
    @Test
    public void outputStream() throws IOException {
        OutputStream outputStream = new FileOutputStream(new File("io.txt"), true);
        outputStream.write("写入信息".getBytes());
        outputStream.close();
    }

    // 简单的读写文件操作
    @Test
    public void simple() throws IOException {
        // 读取文件
        byte[] rBytes = Files.readAllBytes(Paths.get("io.txt"));
        // 转换为字符串
        String rContent = new String(rBytes, StandardCharsets.UTF_8);
        System.out.println("rContent = " + rContent);
        // 写入文件
        Files.write(Paths.get("io.txt"), "追加内容".getBytes(), StandardOpenOption.APPEND);
    }
}
