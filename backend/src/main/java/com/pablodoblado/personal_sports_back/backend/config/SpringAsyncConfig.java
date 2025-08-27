package com.pablodoblado.personal_sports_back.backend.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.pablodoblado.personal_sports_back.backend.customs.CustomAsyncExceptionHandler;

@EnableAsync
@Configuration
public class SpringAsyncConfig {
	
	@Bean(name = "asyncExecutor")
	public Executor asyncExecutor() {
		
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(25);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(300);
		executor.initialize();
		return executor;
		
	}
	
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler();
	}
	
	

}
