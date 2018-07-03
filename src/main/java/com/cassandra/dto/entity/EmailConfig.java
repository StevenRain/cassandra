package com.cassandra.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfig {

	/**
	 * id
	 * */
	private int id;

	/**
	 * 用户名
	 * */
	private String username;

	/**
	 * 密码
	 * */
	private String password;

	/**
	 * SMTP 地址
	 * */
	private String smtpServer;
}
