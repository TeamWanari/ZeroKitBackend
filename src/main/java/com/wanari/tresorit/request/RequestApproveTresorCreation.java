package com.wanari.tresorit.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by zcsongor on 2017.02.28..
 */
public class RequestApproveTresorCreation {
	@JsonProperty("TresorId")
	private String tresorId;
}
