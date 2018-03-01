package com.netgrif.workflow.auth.web.responsebodies;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.web.UserController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.util.ArrayList;
import java.util.Locale;

public class UserResource extends Resource<LocalisedUser> {

    public UserResource(User content, String selfRel, Locale locale) {
        super(new LocalisedUser(content, locale), new ArrayList<>());
        buildLinks(selfRel);
    }

    public UserResource(User content, String selfRel, Locale locale, boolean small) {
        this(content, selfRel, locale);
        if (small) {
            getContent().setTelNumber(null);
            getContent().setGroups(null);
            getContent().setAuthorities(null);
            getContent().setPassword(null);
            getContent().setProcessRoles(null);
        }
    }

    private void buildLinks(String selfRel) {
        ControllerLinkBuilder getLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .getUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("profile") ? getLink.withSelfRel() : getLink.withRel("profile"));

        ControllerLinkBuilder smallLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .getSmallUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("small") ? smallLink.withSelfRel() : smallLink.withRel("small"));

        ControllerLinkBuilder roleLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .assignRolesToUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignProcessRole") ? roleLink.withSelfRel() : roleLink.withRel("assignProcessRole"));

        ControllerLinkBuilder authorityLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .assignAuthorityToUser(getContent().getId(), null));
        add(selfRel.equalsIgnoreCase("assignRole") ? authorityLink.withSelfRel() : authorityLink.withRel("assignRole"));
    }
}
