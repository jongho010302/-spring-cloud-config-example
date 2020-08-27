package net.yogurt.config.env;

import net.yogurt.crypto.AESConfig;
import net.yogurt.crypto.AESUtil;
import net.yogurt.crypto.Base64Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.*;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.util.*;


// 여기서는 환경이 다 구축되기 전이므로 Slf4j가 동작하지 않음. 따라서 필요시 System.out을 사용해야 함
// 프로퍼티 설정 중 암호화된 것을 해당 클래스에서 복호화하는 과정을 별도로 작성하지 않고
// 한번에 복호화하기 위해서  EnvironmentPostProcessor를 사용했음.
// EnvironmentPostProcessor를 사용하려면 src/main/resources/META-INF/spring.factories 파일에 아래와 같이 지정해야 함
// org.springframework.boot.env.EnvironmentPostProcessor=net.yogurt.config.env.ConfigEnvironmentPostProcessor
public class ConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {
		
	public static final String PROPERTY_SOURCE_NAME = "yogurt_config";
	public static final String CONF_DIR_NAME = "yogurt_config_dir";
	public static final String DEFAULT_CONF_DIR = "/yogurt/config/conf";

	// 읽을 프로퍼티 파일 목록
	private final List<String> PROPERTY_FILE_LIST = new ArrayList<String>(
			Arrays.asList(
					"config.properties",
					"db.properties"
			)
		);

	// 복호화할 변수 목록
	private final List<String> DECRYPT_COLUMN_LIST = new ArrayList<String>(
			Arrays.asList(
					"db.jdbc-url",
					"db.username",
					"db.password"
			)
		);

	// config.properties 파일의 aes.key를 읽어서 복호화한 뒤 설정에 다시 저장
	private void decryptPropertyValues(MutablePropertySources propertySources) throws Exception {
		if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
			PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);

			String aesKey = (String)source.getProperty("aes.key");
			AESConfig aesConfig = new AESConfig();
			aesConfig.setKey(aesKey);
			
			if (source instanceof MapPropertySource) {
				MapPropertySource target = (MapPropertySource) source;

				Map<String, Object> dataMap = target.getSource();
				for (String columnName : DECRYPT_COLUMN_LIST) {
					String encValue = (String)dataMap.get(columnName);
					if (!StringUtils.isEmpty(encValue)) {
						String decValue = AESUtil.decryptString(Base64Util.decode(encValue), aesConfig);
						dataMap.put(columnName, decValue);
					}
				}
			}
		}
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			String noticeConfigDir = System.getProperty(CONF_DIR_NAME);
			if (noticeConfigDir == null) {
				System.setProperty(CONF_DIR_NAME, DEFAULT_CONF_DIR);
				// 시스템에 세팅된 값을 읽기 위해 일부러 다시 읽음
				noticeConfigDir = System.getProperty(CONF_DIR_NAME);
			}

			Properties allProperties = new Properties();

			for (String propertyFile : PROPERTY_FILE_LIST) {
				Resource path = new PathResource(noticeConfigDir + "/" + propertyFile);
				Properties properties = PropertiesLoaderUtils.loadProperties(path);
				allProperties.putAll(properties);
			}

			PropertySource<?> noticePropertySource = new PropertiesPropertySource(PROPERTY_SOURCE_NAME, allProperties);

			if (noticePropertySource != null) {
				environment.getPropertySources().addLast(noticePropertySource);

				decryptPropertyValues(environment.getPropertySources());
			}
		} catch (Exception e) {
			// 여기서는 환경이 다 구축되기 전이므로 Slf4j가 동작하지 않음. 따라서 필요시 System.out을 사용해야 함
			e.printStackTrace();
		}
	}
}