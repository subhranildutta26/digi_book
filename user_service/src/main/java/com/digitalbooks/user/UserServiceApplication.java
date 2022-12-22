package com.digitalbooks.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;



@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);

	}
	
	@Bean
	public RestTemplate restTemplate()
	{
		return new RestTemplate();
	}
	
//	@PostConstruct
//	public void initUsesr() {
//		List<Users> users= Arrays.asList(
//			new Users(1, "Rudra", "pwd1", "rudra@gmail.com", new Roles(2, "Author"), new Date()),
//			new Users(2, "RuIshanidra", "pwd2", "RuIshanidra@gmail.com", new Roles(3, "Reader"), new Date()),
//			new Users(3, "Rai", "pwd3", "rai@gmail.com", new Roles(1, "Guest"), new Date()));
//				
//				
//		userRepository.saveAll(users);
	
//	}
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
	    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
	    multipartResolver.setMaxUploadSize(10000000);
	    return multipartResolver;
	}

}
