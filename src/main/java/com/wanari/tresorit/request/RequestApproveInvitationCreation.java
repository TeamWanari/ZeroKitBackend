package com.wanari.tresorit.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by zcsongor on 2017.03.01..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestApproveInvitationCreation {
	@JsonProperty("OperationId")
	private String operationId;
	@JsonProperty("AdditionalInfo")
	private String additionalInfo;
}
