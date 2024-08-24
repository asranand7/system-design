package org.system.design;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "org.system.design")
@SpringBootApplication
public class SystemDesignApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemDesignApplication.class, args);
	}

}
