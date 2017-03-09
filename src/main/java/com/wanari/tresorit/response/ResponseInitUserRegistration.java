package com.wanari.tresorit.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseInitUserRegistration {

	@JsonProperty("UserId")
	private String userId;
	@JsonProperty("RegSessionId")
	private String regSessionId;
	@JsonProperty("RegSessionVerifier")
	private String regSessionVerifier;

	public String getRegSessionVerifier() {
		return regSessionVerifier;
	}

	public static ResponseAdminApiInitUserRegistrationDTO createDTOFromEntity(ResponseInitUserRegistration responseInitUserRegistration) {
		ResponseAdminApiInitUserRegistrationDTO initUserDTO = new ResponseAdminApiInitUserRegistrationDTO();
		initUserDTO.userId = responseInitUserRegistration.userId;
		initUserDTO.regSessionId = responseInitUserRegistration.regSessionId;
		return initUserDTO;
	}

	public static class ResponseAdminApiInitUserRegistrationDTO {
		@JsonProperty("UserId")
		private String userId;
		@JsonProperty("RegSessionId")
		private String regSessionId;
	}
}
