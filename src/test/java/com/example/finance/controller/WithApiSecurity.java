package com.example.finance.controller;

import com.example.finance.config.SecurityConfig;
import com.example.finance.security.JsonAccessDeniedHandler;
import com.example.finance.security.JsonAuthenticationEntryPoint;
import com.example.finance.security.JsonErrorWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Loads the application's real security configuration into a {@code @WebMvcTest} slice, so controller
 * tests exercise the same 401/permitAll rules as production.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class,
        JsonErrorWriter.class})
public @interface WithApiSecurity {
}
