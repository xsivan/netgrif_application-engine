package com.fmworkflow;

import com.fmworkflow.auth.domain.repositories.RoleRepository;
import com.fmworkflow.auth.domain.repositories.UserProcessRoleRepository;
import com.fmworkflow.auth.service.interfaces.IUserService;
import com.fmworkflow.importer.Importer;
import com.fmworkflow.petrinet.domain.repositories.PetriNetRepository;
import com.fmworkflow.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile({"!test"})
public class InsuranceCommandLineRunner implements CommandLineRunner {

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Override
    public void run(String... strings) throws Exception {
//        Role roleUser = new Role("user");
//        roleUser = roleRepository.save(roleUser);
//        Role roleAdmin = new Role("admin");
//        roleAdmin = roleRepository.save(roleAdmin);
//
//        User user = new User("poistenec@gmail.com", "password", "Jožko", "Poistenec");
//        HashSet<Role> roles = new HashSet<>();
//        roles.add(roleUser);
//        user.setRoles(roles);
//        userService.save(user);
//        User admin = new User("agent@gmail.com", "password", "Agent", "Smith");
//        HashSet<Role> adminRoles = new HashSet<>();
//        adminRoles.add(roleAdmin);
//        admin.setRoles(adminRoles);
//        userService.save(admin);
//
//        mongoTemplate.getDb().dropDatabase();
//        // TODO: 26/04/2017 title, initials
//        importer.importPetriNet(new File("src/test/resources/poistenie_rozsirene.xml"), "p", "p");
//        PetriNet net = petriNetRepository.findAll().get(0);
//        for (int i = 0; i < 5; i++) {
//            workflowService.createCase(net.getStringId(), "Poisťovací prípad " + i, randomColor());
//        }
//
//        List<ProcessRole> proles = new LinkedList<>(net.getRoles().values().stream().sorted(Comparator.comparing(ProcessRole::getName)).collect(Collectors.toList()));
//        ProcessRole userPR = proles.get(0);
//        ProcessRole agentPR = proles.get(1);
//
//        UserProcessRole proleClient = new UserProcessRole();
//        proleClient.setRoleId(userPR.getStringId());
//        proleClient = userProcessRoleRepository.save(proleClient);
//        user.addProcessRole(proleClient);
//        userService.save(user);
//
//        UserProcessRole proleFm = new UserProcessRole();
//        proleFm.setRoleId(agentPR.getStringId());
//        proleFm = userProcessRoleRepository.save(proleFm);
//        admin.addProcessRole(proleFm);
//        userService.save(admin);
//
//        User superAdmin = new User("super@fmworkflow.com", "password", "Super", "Truuper");
//        HashSet<Role> superRoles = new HashSet<>();
//        superRoles.add(roleAdmin);
//        superAdmin.setRoles(superRoles);
    }

    private String randomColor() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500";
            case 1:
                return "color-fg-blue-grey-500";
            case 2:
                return "color-fg-amber-500";
            case 3:
                return "color-fg-indigo-500";
            case 4:
                return "color-fg-teal-500";
            default:
                return "color-fg-fm-500";
        }
    }
}