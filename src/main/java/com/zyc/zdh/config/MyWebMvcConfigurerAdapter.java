package com.zyc.zdh.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Configuration
@EnableWebMvc
public class MyWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

	@Autowired
	Environment ev;
//	@Bean
//	public Converter<String, Timestamp> stringToTimeStampConvert() {
//		return new Converter<String, Timestamp>() {
//			@Override
//			public Timestamp convert(String source) {
//				Timestamp date = null;
//				try {
//					date = Timestamp.valueOf(source);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return date;
//			}
//		};
//	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}


//	@Bean
//	public EmbeddedServletContainerCustomizer containerCustomizer() {
//
//		return new EmbeddedServletContainerCustomizer() {
//			@Override
//			public void customize(ConfigurableEmbeddedServletContainer container) {
//				ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
//				container.addErrorPages(error404Page);
//			}
//		};
//	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		// viewResolver.setPrefix("/WEB-INF/classes/views/");
        System.out.println("打印web.path:"+ev.getProperty("web.path"));
		viewResolver.setPrefix(ev.getProperty("web.path"));
		viewResolver.setSuffix(".html");
		viewResolver.setViewClass(JstlView.class);
		return viewResolver;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//registry.addResourceHandler("/**").addResourceLocations("/");
		registry.addResourceHandler("/register**","/statics/**","/css/**","/js/**","/fonts/**","/img/**",
				"/plugins/**","/zdh_flow/**","/favi**","/etl/js/**","etl/css/**","/statics/**","/404**","cron/**")
				.addResourceLocations(ev.getProperty("web.path"))
				.addResourceLocations("/statics/")
				.addResourceLocations(ev.getProperty("web.path")+"css/")
				.addResourceLocations(ev.getProperty("web.path")+"cron/")
				.addResourceLocations(ev.getProperty("web.path")+"js/")
				.addResourceLocations(ev.getProperty("web.path")+"fonts/")
				.addResourceLocations(ev.getProperty("web.path")+"img/")
				.addResourceLocations(ev.getProperty("web.path")+"plugins/")
				.addResourceLocations(ev.getProperty("web.path")+"zdh_flow/")
				.addResourceLocations(ev.getProperty("web.path")+"statics/");

	}

}
