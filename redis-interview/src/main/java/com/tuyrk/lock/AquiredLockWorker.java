package com.tuyrk.lock;

public interface AquiredLockWorker<T> {
    T invokeAfterLockAquire() throws Exception;
}
