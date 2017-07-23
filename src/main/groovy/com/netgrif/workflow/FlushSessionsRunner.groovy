package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.data.redis.RedisOperationsSessionRepository
import org.springframework.stereotype.Component

@Component
class FlushSessionsRunner {

    private static final Logger log = Logger.getLogger(FlushSessionsRunner.class.name)

    @Autowired
    private JedisConnectionFactory connectionFactory
    @Autowired
    private UserRepository userRepository


    public void run(String... args){
        log.info("Flushing all users session")
        RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(connectionFactory)
        List<User> users = userRepository.findAll()
        users.each {user ->
            def sessions = sessionRepository.findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,user.email)
            sessions.each {session -> sessionRepository.delete(session.key)}
        }
    }
}
