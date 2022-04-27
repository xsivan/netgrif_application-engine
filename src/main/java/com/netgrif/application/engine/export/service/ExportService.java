package com.netgrif.application.engine.export.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.export.configuration.ExportConfiguration;
import com.netgrif.application.engine.export.domain.ExportDataConfig;
import com.netgrif.application.engine.export.service.interfaces.IExportService;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
class ExportService implements IExportService {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IElasticTaskService elasticTaskService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private TaskRepository taskRepository;


    @Override
    public Set<String> buildDefaultCsvCaseHeader(List<Case> exportCases) {
        Set<String> header = new LinkedHashSet<>();
        exportCases.forEach(exportCase ->
                header.addAll(exportCase.getImmediateDataFields())
        );
        return header;
    }

    @Override
    public Set<String> buildDefaultCsvTaskHeader(List<Task> exportTasks) {
        Set<String> header = new LinkedHashSet<>();
        exportTasks.forEach(
                exportTask ->
                        header.addAll(exportTask.getImmediateDataFields())
        );
        return header;
    }

    @Override
     public OutputStream fillCsvCaseData(Predicate predicate, File outFile, ExportDataConfig config, int pageSize) throws FileNotFoundException {
        QCase qCase = new QCase("case");
        int numOfPages = (int) (((caseRepository.count(predicate)) / pageSize) + 1);
        List<Case> exportCases = new ArrayList<>();
        for (int i = 0; i < numOfPages; i++) {
            exportCases.addAll(workflowService.search(predicate, PageRequest.of(i, pageSize)).getContent());
        }
        return buildCaseCsv(exportCases, config, outFile);
    }

    @Override
    public OutputStream fillCsvCaseData(List<CaseSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize, Locale locale, Boolean isIntersection) throws FileNotFoundException {
        int numOfPages = (int) ((elasticCaseService.count(requests, user, locale, isIntersection) / pageSize) + 1);
        List<Case> exportCases = new ArrayList<>();
        for (int i = 0; i < numOfPages; i++){
            exportCases.addAll(elasticCaseService.search(requests, user, PageRequest.of(i, pageSize), locale, isIntersection).toList());
        }
        return buildCaseCsv(exportCases, config, outFile);
    }

    private OutputStream buildCaseCsv(List<Case> exportCases, ExportDataConfig config, File outFile) throws FileNotFoundException {
        Set<String> csvHeader = config == null ? buildDefaultCsvCaseHeader(exportCases) : config.getDataToExport();
        OutputStream outStream = new FileOutputStream(outFile, false);
        PrintWriter writer = new PrintWriter(outStream, true);
        writer.println(String.join(",", csvHeader));
        for (Case exportCase : exportCases) {
            writer.println(String.join(",", buildRecord(csvHeader, exportCase)).replace("\n", "\\n"));
        }
        writer.close();
        return outStream;
    }

    @Override
    public OutputStream fillCsvTaskData(List<ElasticTaskSearchRequest> requests, File outFile, ExportDataConfig config,
                                        LoggedUser user, int pageSize, Locale locale, Boolean isIntersection) throws FileNotFoundException {
        int numberOfTasks = (int) ((elasticTaskService.count(requests, user, locale, isIntersection) / pageSize) + 1);
        List<Task> exportTasks = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {
            exportTasks.addAll(elasticTaskService.search(requests, user, PageRequest.of(0, pageSize), locale, isIntersection).toList());
        }
        return buildTaskCsv(exportTasks, config, outFile);
    }

    @Override
    public OutputStream fillCsvTaskData(Predicate predicate, File outFile, ExportDataConfig config, int pageSize) throws FileNotFoundException {
        QTask qTask = new QTask("task");
        int numberOfTasks = (int) taskRepository.count(predicate);
        List<Task> exportTasks = new ArrayList<>();
        for (int i = 0; i < numberOfTasks; i++) {
            exportTasks.addAll(taskService.search(predicate, PageRequest.of(i, pageSize)).getContent());
        }
        return buildTaskCsv(exportTasks, config, outFile);
    }

    private OutputStream buildTaskCsv(List<Task> exportTasks, ExportDataConfig config, File outFile) throws FileNotFoundException {
        Set<String> csvHeader = config == null ? buildDefaultCsvTaskHeader(exportTasks) : config.getDataToExport();
        OutputStream outStream = new FileOutputStream(outFile, false);
        PrintWriter writer = new PrintWriter(outStream, true);
        writer.println(String.join(",", csvHeader));
        for (Task exportTask : exportTasks) {
            Case taskCase = workflowService.findOne(exportTask.getCaseId());
            writer.println(String.join(",", buildRecord(csvHeader, taskCase)).replace("\n", "\\n"));
        }
        writer.close();
        return outStream;
    }

    private List<String> buildRecord(Set<String> csvHeader, Case exportCase) {
        List<String> record = new LinkedList<>();
        for (String dataFieldId : csvHeader) {
            if (exportCase.getDataSet().containsKey(dataFieldId)) {
                record.add(StringEscapeUtils.escapeCsv(resolveFieldValue(exportCase, dataFieldId)));
            } else
                record.add("");
        }
        return record;
    }

    private String resolveFieldValue(Case exportCase, String exportFieldId) {
        String fieldValue;
        Field field = exportCase.getField(exportFieldId);
        if (field.getValue() == null && exportCase.getDataSet().get(exportFieldId).getValue() == null) {
            return "";
        }
        switch (field.getType()) {
            case MULTICHOICE_MAP:
                fieldValue = ((MultichoiceMapField) field).getValue().stream()
                    .filter(value -> ((MultichoiceMapField) field).getOptions().containsKey(value.trim()))
                        .map(value -> ((MultichoiceMapField) field).getOptions().get(value.trim()).getDefaultValue())
                        .collect(Collectors.joining(","));
                break;
            case ENUMERATION_MAP:
                fieldValue = ((EnumerationMapField) field).getOptions().get(field.getValue()).getDefaultValue();
                break;
            case MULTICHOICE:
                fieldValue = String.join(",", ((MultichoiceField) field).getValue().stream().map(I18nString::getDefaultValue).collect(Collectors.toSet()));
                break;
            case FILE:
                fieldValue = ((FileField)field).getValue().toString();
                break;
            case FILELIST:
                fieldValue = String.join(",", ((FileListField)field).getValue().getNamesPaths().stream().map(FileFieldValue::toString).collect(Collectors.toSet()));
                break;
            case TASK_REF:
                fieldValue = String.join(";", ((TaskField) field).getValue());
                break;
            case USER:
                fieldValue = ((UserField) field).getValue().getEmail();
                break;
            case USERLIST:
                fieldValue = String.join(";", ((UserListField) field).getValue());
                break;
            default:
                fieldValue = field.getValue() == null ? (String) exportCase.getDataSet().get(exportFieldId).getValue()  : (String) field.getValue();
                break;
        }
        return fieldValue;
    }
}