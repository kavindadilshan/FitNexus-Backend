package com.fitnexus.server.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;


@Configuration
@EnableResourceServer
@EnableAsync
@RequiredArgsConstructor
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "resource_id";

    private final Environment environment;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        String base = environment.getRequiredProperty("spring.mvc.servlet.path");
        httpSecurity
//                .anonymous().disable()
                .authorizeRequests()
                //The order of the rules matters and the more specific rules should go first.

                .antMatchers(base + "/users/register/**", base + "/users/authenticate/**", base + "/coach/password/**").permitAll()
                .antMatchers(base + "/application/version/**", base + "/public/**",base + "/application/aia/user-check").permitAll()
                .antMatchers(base + "/application/version/**", base + "/payhere/**").permitAll()

                .antMatchers(base + "/admin/class/type/create",base + "/admin/class/type/update",base + "/admin/class/type/remove")
                .access("hasRole('ROLE_SUPER_ADMIN')")
                .antMatchers(base + "/users/**")
                .access("hasRole('ROLE_PUBLIC_USER')")
                .antMatchers(base + "/coach/**")
                .access("hasAnyRole('ROLE_TRAINER','ROLE_INSTRUCTOR','ROLE_INSTRUCT_TRAINER')")
                .antMatchers(base + "/admin/**")
                .access("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_BUSINESS_PROFILE_MANAGER')")
                .antMatchers(base + "/manager/**")
                .access("hasAnyRole('ROLE_BUSINESS_PROFILE_MANAGER')")
                .and().csrf().disable()
                .exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
