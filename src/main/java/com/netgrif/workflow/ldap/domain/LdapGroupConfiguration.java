package com.netgrif.workflow.ldap.domain;

import com.netgrif.workflow.configuration.properties.NaeLdapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;


@Configuration
public class LdapGroupConfiguration {

    @Bean
    @DependsOn("LdapGroupRef")
    public ContextSource ldapContextSource(NaeLdapProperties ldapProperties) {
        final LdapContextSource source = new LdapContextSource();
        source.setBase(ldapProperties.getGroupSearchBase());
        return source;
    }

}
