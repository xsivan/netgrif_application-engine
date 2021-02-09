package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class PublicAbstractController {

    protected final IUserService userService;


    public PublicAbstractController(IUserService userService) {
        this.userService = userService;
    }

    protected LoggedUser getAnonymous() {
        return userService.getAnonymousLogged();
    }
}