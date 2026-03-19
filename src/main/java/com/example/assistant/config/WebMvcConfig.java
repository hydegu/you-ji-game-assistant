package com.example.assistant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final FileStorageProperties fileStorageProperties;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 配置静态资源映射
		// 访问路径：/files/**
		// 实际路径：fileStorageProperties.getLocal().getBasePath()
		String basePath = fileStorageProperties.getLocal().getBasePath();
		// 1. 获取绝对路径 (Paths.get 会自动处理相对/绝对路径和系统分隔符)
		String absolutePath = Paths.get(basePath).toAbsolutePath().normalize().toString();

		// 2. 转换为 URI 格式 (例如 file:///D:/uploads/ 或 file:/usr/local/uploads/)
		// toUri().toString() 会自动处理所有 file: 前缀和斜杠问题
		String resourceLocation = Paths.get(absolutePath).toUri().toString();

		// 3. 配置映射
		registry.addResourceHandler("/files/**")
				.addResourceLocations(resourceLocation)
				.setCachePeriod(3600);
	}

}