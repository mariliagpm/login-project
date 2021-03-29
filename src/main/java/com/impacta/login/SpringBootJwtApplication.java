package com.impacta.login;

 

import javax.annotation.Resource;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;

import com.impacta.login.controller.AbstractController;


@EnableAutoConfiguration
@ComponentScan({"com.impacta","com.impacta.login","com.impacta.login.controller","com.impacta.login.service","com.impacta.login.repository","com.impacta.login.controller","com.impacta.login.repository","com.impacta.login.model"})
@SpringBootApplication
public class SpringBootJwtApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

	private static final String PROPERTY_NAME_MESSAGESOURCE_BASENAME = "message.source.basename";
	private static final String PROPERTY_NAME_MESSAGESOURCE_USE_CODE_AS_DEFAULT_MESSAGE = "message.source.use.code.as.default.message";
	@Resource
	private Environment environment;

	public static void main(String[] args) {
 	SpringApplication.run(SpringBootJwtApplication.class, args);
	}

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(environment.getRequiredProperty(PROPERTY_NAME_MESSAGESOURCE_BASENAME));
		messageSource.setUseCodeAsDefaultMessage(Boolean.parseBoolean(
				environment.getRequiredProperty(PROPERTY_NAME_MESSAGESOURCE_USE_CODE_AS_DEFAULT_MESSAGE)));
		return messageSource;
	}

	 
	
}
