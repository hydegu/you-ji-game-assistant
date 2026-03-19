package com.example.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

	/**
	 * 存储类型：local
	 */
	private String type = "local";

	/**
	 * 本地存储配置
	 */
	private Local local = new Local();

	/**
	 * 允许的文件类型（MIME类型）
	 */
	private List<String> allowedTypes = new ArrayList<>();

	/**
	 * 最大文件大小（字节）
	 */
	private Long maxSize = 104857600L; // 默认100MB

	@Data
	public static class Local {

		/**
		 * 本地存储基础路径
		 */
		private String basePath = "./data/upload/files";

		/**
		 * 文件访问URL前缀
		 */
		private String urlPrefix = "http://localhost:9102/files";
	}

}