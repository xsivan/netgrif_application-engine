package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.service.AfterRegistrationAuthService;
import com.netgrif.application.engine.auth.service.interfaces.IAfterRegistrationAuthService;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifBasicAuthenticationProvider;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifLdapAuthenticationProvider;
import com.netgrif.application.engine.configuration.authentication.providers.ldap.UserDetailsContextMapperImpl;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.configuration.properties.SecurityConfigProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.RestAuthenticationEntryPoint;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashSet;

import static org.springframework.http.HttpMethod.OPTIONS;


@Slf4j
@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class NAESecurityConfiguration extends AbstractSecurityConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IJwtService jwtService;

    @Autowired
    private IUserService userService;

    @Autowired
    private NetgrifBasicAuthenticationProvider netgrifBasicAuthenticationProvider;

    private final NetgrifLdapAuthenticationProvider netgrifLdapAuthenticationProvider;

    @Autowired
    private EncryptionConfiguration passwordEncoder;

    @Autowired
    private SecurityConfigProperties properties;

    @Value("${nae.security.server-patterns}")
    private String[] serverPatterns;

    @Value("${nae.security.anonymous-exceptions}")
    private String[] anonymousExceptions;


    @Autowired
    protected NaeLdapProperties ldapProperties;

    @Autowired
    private LdapUserService ldapUserService;

    @Autowired
    private ILdapUserRefService ldapUserRefService;

    private static final String ANONYMOUS_USER = "anonymousUser";

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("X-Auth-Token");
        config.addExposedHeader("X-Jwt-Token");
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    public NAESecurityConfiguration(NetgrifLdapAuthenticationProvider netgrifLdapAuthenticationProvider) {
        netgrifLdapAuthenticationProvider.setUserDetailsContextMapper(new UserDetailsContextMapperImpl(ldapUserService, ldapUserRefService, ldapProperties));
        this.netgrifLdapAuthenticationProvider = netgrifLdapAuthenticationProvider;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        netgrifBasicAuthenticationProvider.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());
        netgrifLdapAuthenticationProvider.setUserDetailsContextMapper(new UserDetailsContextMapperImpl(ldapUserService, ldapUserRefService, ldapProperties));

        auth
                .authenticationProvider(netgrifBasicAuthenticationProvider)
                .authenticationProvider(netgrifLdapAuthenticationProvider);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuration with frontend separated");
//        @formatter:off
        http
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .cors()
                .and()
                .addFilterBefore(createPublicAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .antMatchers(OPTIONS).permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                .and()
                .headers()
                .frameOptions().disable()
                .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000)
                .and()
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "frame-src: 'none'"));
//        @formatter:on
        setCsrf(http);
    }

    @Bean
    protected IAfterRegistrationAuthService authenticationService() throws Exception {
        return new AfterRegistrationAuthService(authenticationManager());
    }

    @Override
    protected ProviderManager authenticationManager() throws Exception {
        return (ProviderManager) super.authenticationManager();
    }

    @Override
    protected boolean isOpenRegistration() {
        return this.serverAuthProperties.isOpenRegistration();
    }

    @Override
    protected boolean isCsrfEnabled() {
        return properties.isCsrf();
    }

    @Override
    protected String[] getStaticPatterns() {
        return new String[]{
                "/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
        };
    }

    @Override
    protected String[] getServerPatterns() {
        return this.serverPatterns;
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    private PublicAuthenticationFilter createPublicAuthenticationFilter() throws Exception {
        Authority authority = authorityService.getOrCreate(Authority.anonymous);
        authority.setUsers(new HashSet<>());
        return new PublicAuthenticationFilter(
                authenticationManager(),
                new AnonymousAuthenticationProvider(ANONYMOUS_USER),
                authority,
                this.serverPatterns,
                this.anonymousExceptions,
                this.jwtService,
                this.userService
        );
    }
}
