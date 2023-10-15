package personal.yeongyulgori.user.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import personal.yeongyulgori.user.security.CustomAuthenticationEntryPoint;
import personal.yeongyulgori.user.security.JwtAuthenticationFilter;
import personal.yeongyulgori.user.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint) // 인증되지 않았을 시 custom entry point 사용
                .and()
                .httpBasic().disable() // JWT 사용
                .csrf().disable() // REST API는 Stateless한 특성을 가지므로, Session 미사용 -> CSRF 보호 비활성화
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .antMatchers("/", "/swagger-ui/**", "/v2/api-docs", "/swagger-resources/**",
                        "/**/signup", "/**/login", "/users/v1", "/users/v1/auto-complete",
                        "/users/v1/password-reset/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/users/v1/{username}").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter
                                (jwtTokenProvider, customAuthenticationEntryPoint),
                        UsernamePasswordAuthenticationFilter.class)
                .logout().permitAll();

        return http.build();

    }

}
