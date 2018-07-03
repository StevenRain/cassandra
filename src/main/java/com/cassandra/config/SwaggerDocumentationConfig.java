package com.cassandra.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


//@Profile(value = {"dev", "sit"})
@Configuration
@EnableSwagger2
public class SwaggerDocumentationConfig {

	private ApiKey apiKey() {

		return new ApiKey("Authorization", "api_key", "header");
	}


	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("cassandra")
				.license("")
				.licenseUrl("")
				.termsOfServiceUrl("")
				.version("1.0.0")
				.contact(new Contact("", "", ""))
				.build();
	}

	@Bean
	public Docket customImplementation() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.cassandra.web"))
				.build() .securitySchemes(Lists.newArrayList(apiKey()))
				.apiInfo(apiInfo());
	}
}
