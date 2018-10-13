package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.domain.QTask;
import com.netgrif.workflow.workflow.domain.Task;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class TaskSearchService extends MongoSearchService<Task> {

    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String ROLE = "role";
    public static final String USER = "user";
    public static final String PROCESS = "process";
    public static final String CASE = "case";
    public static final String TRANSITION = "transition";
    public static final String FULL_TEXT = "fullText";

    public String roleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"roles." + obj + "\":" + exists(true));
        builder.put(ArrayList.class, o -> {
            StringBuilder expression = new StringBuilder();
            ((List) o).forEach(role -> {
                expression.append("{\"roles.");
                expression.append(role);
                expression.append("\":");
                expression.append(exists(true));
                expression.append("},");
            });
            return expression.substring(1, expression.length() - 2);
        });

        return buildQueryPart(null, obj, builder);
    }

    public String caseQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");

        return buildQueryPart("caseId", obj, builder);
    }

    public String titleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));

        return buildQueryPart("title", obj, builder);
    }

    public String userQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(Long.class, o -> ((Long) o).toString());
        builder.put(Integer.class, o -> ((Integer) o).toString());
        builder.put(ArrayList.class, o -> in((List<Object>) obj, oo -> oo.toString(), ob -> ob instanceof Long || ob instanceof Integer));
        builder.put(String.class, o -> {
            Long id = resolveAuthorByEmail((String) obj);
            return id != null ? id.toString() : "";
        });

        return buildQueryPart("userId", obj, builder);
    }

    public String transitionQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));

        return buildQueryPart("transitionId", obj, builder);
    }

    public String processQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));

        return buildQueryPart("processId", obj, builder);
    }


//    ********************
//    *     QueryDSL     *
//    ********************

    public Predicate role(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            ((ArrayList<String>) query).stream().map(this::roleString).forEach(builder::or);
            return builder;
        } else if (query instanceof String)
            return roleString((String) query);

        return null;
    }

    private Predicate roleString(String role) {
        return QTask.task.roles.containsKey(role);
    }

    public Predicate useCase(Object query) {
        if (query instanceof HashMap) {
            return caseObject((HashMap<String, Object>) query);
        } else if (query instanceof ArrayList) {
            return caseArray((ArrayList<String>) query, ID);
        } else if (query instanceof String) {
            return caseId((String) query);
        }

        return null;
    }

    private Predicate caseObject(Map<String, Object> query) {
        if (query.containsKey(TITLE)) {
            return query.get(TITLE) instanceof ArrayList ? caseArray((ArrayList<String>) query.get(TITLE), TITLE) : caseTitle((String) query.get(TITLE));
        } else if (query.containsKey(ID)) {
            return query.get(ID) instanceof ArrayList ? caseArray((ArrayList<String>) query.get(ID), ID) : caseId((String) query.get(ID));
        }

        return null;
    }

    private Predicate caseArray(ArrayList<String> query, String key) {
        BooleanBuilder builder = new BooleanBuilder();
        query.stream().map(q -> {
            if (key.equalsIgnoreCase(TITLE))
                return caseTitle(q);
            else
                return caseId(q);
        }).forEach(builder::or);
        return builder;
    }

    private Predicate caseId(String caseId) {
        return QTask.task.caseId.eq(caseId);
    }

    private Predicate caseTitle(String caseTitle) {
        return QTask.task.caseTitle.containsIgnoreCase(caseTitle);
    }

    public Predicate title(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            ((ArrayList<String>) query).stream().map(this::titleString).forEach(builder::or);
            return builder;
        } else if (query instanceof String)
            return titleString((String) query);

        return null;
    }

    private Predicate titleString(String query) {
        return QTask.task.title.defaultValue.containsIgnoreCase(query);
    }

    public Predicate user(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            ((ArrayList<Number>) query).stream().map(this::userLong).forEach(builder::or);
            return builder;
        } else if (query instanceof Integer)
            return userLong(Long.valueOf(((Integer) query).longValue()));
        else if (query instanceof Long)
            return userLong((Long) query);
        else if (query instanceof String)
            return userString((String) query);

        return null;
    }

    private Predicate userLong(Number userId) {
        if (userId instanceof Integer)
            return QTask.task.userId.eq(Long.valueOf(((Integer) userId).longValue()));
        else if (userId instanceof Long)
            return QTask.task.userId.eq((Long) userId);
        return null;
    }

    private Predicate userString(String userEmail) {
        Long id = resolveAuthorByEmail(userEmail);
        if (id != null)
            return userLong(id);
        return null;
    }

    public Predicate transition(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            ((ArrayList<String>) query).stream().map(this::transitionString).forEach(builder::or);
            return builder;
        } else if (query instanceof String)
            return transitionString((String) query);

        return null;
    }

    private Predicate transitionString(String transitionId) {
        return QTask.task.transitionId.eq(transitionId);
    }

    public Predicate process(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            ((ArrayList<String>) query).stream().map(this::processString).forEach(builder::or);
            return builder;
        } else if (query instanceof String)
            return processString((String) query);

        return null;
    }

    private Predicate processString(String processId) {
        return QTask.task.processId.eq(processId);
    }

    public Predicate fullText(String query) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.or(QTask.task.title.defaultValue.containsIgnoreCase(query));
        builder.or(QTask.task.caseTitle.containsIgnoreCase(query));
        return builder;
    }


}
