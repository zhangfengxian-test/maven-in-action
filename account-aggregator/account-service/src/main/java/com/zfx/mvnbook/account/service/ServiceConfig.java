package com.zfx.mvnbook.account.service;

import com.zfx.mvnbook.account.email.EmailConfig;
import com.zfx.mvnbook.account.persist.PersistConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.zfx.mvnbook.account")
@Import(value = {PersistConfig.class, EmailConfig.class})
public class ServiceConfig {
}