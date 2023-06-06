package io.narayana.util;

@FunctionalInterface
public interface RunnableWithException {
    public void run() throws Throwable;
}