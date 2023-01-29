package com.zyc.zdh.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorPageConfig implements ErrorPageRegistrar {
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage error400Page=new ErrorPage(HttpStatus.BAD_REQUEST,"/error400" );
        ErrorPage error401Page=new ErrorPage(HttpStatus.UNAUTHORIZED,"/error401");
        ErrorPage error500Page=new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR,"/error500");
        registry.addErrorPages(error400Page,error401Page,error500Page);
    }
}
