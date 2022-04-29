package com.skylerdache.spacepong.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(CustomLogoutHandler logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    protected void configure (HttpSecurity http) throws Exception {
        http
            .csrf()
                .ignoringAntMatchers("/h2/**")
                .and()
            .headers()
                .frameOptions().sameOrigin().and()
            .authorizeRequests()
                .antMatchers(
                        "/", "/login", "/register", "/h2/**", "/css/*", "/js/**",
                        "/register_presence"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin((form) -> form.loginPage("/login").permitAll())
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler(logoutHandler)
                .logoutSuccessUrl("/")
                .permitAll();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
