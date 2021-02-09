package com.netgrif.workflow.petrinet.web;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.petrinet.web.responsebodies.*;
import com.netgrif.workflow.workflow.web.PublicAbstractController;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReferenceResource;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReferenceResourceAssembler;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.petrinet.web.responsebodies.*;
import com.netgrif.workflow.workflow.web.PublicAbstractController;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.netgrif.workflow.petrinet.web.PetriNetController.decodeUrl;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping({"/api/public/petrinet"})
@Slf4j
public class PublicPetriNetController extends PublicAbstractController {

    private final IPetriNetService service;

    @Autowired
    private IProcessRoleService roleService;

    private final StringToVersionConverter converter;

    public PublicPetriNetController(IPetriNetService service, IUserService userService, StringToVersionConverter converter) {
        super(userService);
        this.service = service;
        this.converter = converter;
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiOperation(value = "Get process by id")
    public PetriNetReferenceResource getOne(@PathVariable("id") String id, Locale locale) {
        return new PetriNetReferenceResource(IPetriNetService.transformToReference(this.service.getPetriNet(decodeUrl(id)), locale));
    }

    @ApiOperation(value = "Get process by identifier and version")
    @GetMapping(value = "/{identifier}/{version}", produces = MediaTypes.HAL_JSON_VALUE)
    @ResponseBody
    public PetriNetReferenceResource getOne(@PathVariable("identifier") String identifier, @PathVariable("version") String version, Locale locale) {
        return new PetriNetReferenceResource(this.service.getReference(identifier, this.converter.convert(version), getAnonymous(), locale));
    }

    @ApiOperation(value = "Search processes")
    @RequestMapping(value = "/search", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<PetriNetReferenceResource> searchPetriNets(@RequestBody Map<String, Object> criteria, Pageable pageable, PagedResourcesAssembler<PetriNetReference> assembler, Locale locale) {
        Page<PetriNetReference> nets = service.search(criteria, getAnonymous(), pageable, locale);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PublicPetriNetController.class)
                .searchPetriNets(criteria, pageable, assembler, locale)).withRel("search");
        PagedResources<PetriNetReferenceResource> resources = assembler.toResource(nets, new PetriNetReferenceResourceAssembler(), selfLink);
        PetriNetReferenceResourceAssembler.buildLinks(resources);
        return resources;
    }

    @ApiOperation(value = "Get roles of process")
    @RequestMapping(value = "/{netId}/roles", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public ProcessRolesResource getRoles(@PathVariable("netId") String netId, Locale locale) {
        netId = decodeUrl(netId);
        return new ProcessRolesResource(roleService.findAll(netId), service.getPetriNet(netId).getPermissions(), netId, locale);
    }

    @ApiOperation(value = "Get transactions of process")
    @RequestMapping(value = "/{netId}/transactions", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public TransactionsResource getTransactions(@PathVariable("netId") String netId, Locale locale) {
        PetriNet net = service.getPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId, locale);
    }

    @ApiOperation(value = "Get data fields of transitions")
    @RequestMapping(value = "/data", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public DataFieldReferencesResource getDataFieldReferences(@RequestBody List<TransitionReference> referenceBody, Locale locale) {
        return new DataFieldReferencesResource(service.getDataFieldReferences(referenceBody, locale));
    }

    @ApiOperation(value = "Get transitions of processes")
    @RequestMapping(value = "/transitions", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public TransitionReferencesResource getTransitionReferences(@RequestParam List<String> ids, Locale locale) {
        ids.forEach(id -> id = decodeUrl(id));
        return new TransitionReferencesResource(service.getTransitionReferences(ids, getAnonymous(), locale));
    }
}