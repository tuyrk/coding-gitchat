package com.interview.other;

/**
 * synchronized 实现原理。反编译
 */
public class SynchronizedTest {
    public static void main(String[] args) {
        synchronized (SynchronizedTest.class) {
            System.out.println("Java");
        }
    }
}
