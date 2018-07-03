package com.cassandra.dto.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailObject {

	/**
	 * 收件人
	 * */
	private List<String> toList;

	/**
	 * 主题
	 * */
	private String subject;

	/**
	 * 内容
	 * */
	private String content;
}
