package com.cassandra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


//@Profile(value = {"dev", "sit"})
@Controller
@Slf4j
public class HomeController {
	@RequestMapping(value = "/")
	public String index() {
		log.info("swagger-ui.html");
		return "redirect:swagger-ui.html";
	}
}
