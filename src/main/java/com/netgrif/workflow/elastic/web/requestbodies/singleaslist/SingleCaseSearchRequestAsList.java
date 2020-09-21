package com.netgrif.workflow.elastic.web.requestbodies.singleaslist;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.workflow.utils.SingleItemAsList;
import com.netgrif.workflow.utils.SingleItemAsListDeserializer;

@JsonDeserialize(using = SingleItemAsListDeserializer.class, contentAs = CaseSearchRequest.class)
public class SingleCaseSearchRequestAsList extends SingleItemAsList<CaseSearchRequest> {}