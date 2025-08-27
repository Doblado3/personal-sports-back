package com.pablodoblado.personal_sports_back.backend.customs;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
	
	@Override
	public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
		log.warn("Exception message - " + throwable.getMessage());
		log.info("Method name - " + method.getName());
		
		for (Object param : obj) {
			log.info("Parameter value - " + param);
		}
	}

}
