package com.easytoolsoft.easyreport.web.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Tom Deng
 * @date 2017-04-11
 **/
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "easytoolsoft.easyreport.env")
public class EnvProperties {
    public static final String PRODUCT = "prod";
    private String appName;
    private String name;
    private String version;
}
