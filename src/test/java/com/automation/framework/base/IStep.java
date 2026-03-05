package com.automation.framework.base;

public interface IStep<T extends IStep<T>> {
    T step(String step);
}