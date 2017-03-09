package com.wanari.tresorit.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanari.tresorit.response.ResponseInitUserRegistration;

public class RequestValidateUser {
	@JsonProperty("UserId")
	private String userId;
	@JsonProperty("RegSessionId")
	private String regSessionId;
	@JsonProperty("RegSessionVerifier")
	private String regSessionVerifier;
	@JsonProperty("RegValidationVerifier")
	private String regValidationVerifier;
	@JsonProperty("UserName")
	private String userName;

	public void setRegSessionVerifier(String regSessionVerifier) {
		this.regSessionVerifier = regSessionVerifier;
	}

	public static RequestValidateUserDTO createDTOFromEntity(RequestValidateUser requestValidateUser) {
		RequestValidateUserDTO dto = new RequestValidateUserDTO();
		dto.userId = requestValidateUser.userId;
		dto.userName = requestValidateUser.userName;
		return dto;
	}

	public static class RequestValidateUserDTO {
		@JsonProperty("UserId")
		private String userId;
		@JsonProperty("UserName")
		private String userName;

		public String getUserName() {
			return userName;
		}
	}
}
