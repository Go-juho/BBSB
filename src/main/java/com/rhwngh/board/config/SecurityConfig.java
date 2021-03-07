package com.rhwngh.board.config;

import com.rhwngh.board.service.MemberService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity //Spring Security 설정할 클래스라고 정의
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter { // WebSecurityConfigurer 인스턴스를 편리하게 생성하기 위한 클래스
    private MemberService memberService;

    @Bean //Service에서 비밀번호를 암호화할 수 있도록 Bean으로 등록
    public PasswordEncoder passwordEncoder() { // Spring Security에서 제공하는 비밀번호 암호화 객체
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception // FilterChainProxy를 생성하는 필터
    {
        // static 디렉터리의 하위 파일 목록은 인증 무시 ( = 항상통과 )
        // 해당 경로의 파일들은 Spring Security가 무시
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/lib/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception { // HTTP 요청에 대한 웹 기반 보안을 구성
        http.authorizeRequests() // HttpServletRequest에 따라 접근(access)을 제한
                // 페이지 권한 설정
                .antMatchers("/admin/**").hasRole("ADMIN") // /admin 으로 시작하는 경로는 ADMIN 롤을 가진 사용자만 접근 가능
                .antMatchers("/user/myinfo").hasRole("MEMBER") // /user/myinfo 경로는 MEMBER 롤을 가진 사용자만 접근 가능
                .antMatchers("/board/**").hasRole("MEMBER")
                //.antMatchers("/**").permitAll() // 모든 경로에 대해서는 권한없이 접근 가능
                .and() // 로그인 설정
                .formLogin() // form 기반으로 인증
                .loginPage("/user/login") // 기본 제공되는 form 말고, 커스텀 로그인 폼을 사용
                .defaultSuccessUrl("/user/login/result") // 로그인이 성공했을 때 이동되는 페이지
                .permitAll()
                .and() // 로그아웃 설정
                .logout() // 로그아웃을 지원하는 메서드이며, WebSecurityConfigurerAdapter를 사용할 때 자동으로 적용, 기본적으로 "/logout"에 접근하면 HTTP 세션을 제거
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // 로그아웃의 기본 URL(/logout) 이 아닌 다른 URL로 재정의
                .logoutSuccessUrl("/user/logout/result")
                .invalidateHttpSession(true) // HTTP 세션을 초기화하는 작업
                .and()
                // 403 예외처리 핸들링
                .exceptionHandling().accessDeniedPage("/user/denied");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 모든 인증은 AuthenticationManager를 통해 이루어지며 AuthenticationManager를 생성하기 위해서는 AuthenticationManagerBuilder를 사용
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }
}
