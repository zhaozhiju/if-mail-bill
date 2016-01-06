package net.umpay.forum.web.service.impl.base;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * 加载上下文
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class Context {

	/**
	 * logger for this class
	 */
	private static Logger log = LoggerFactory.getLogger(Context.class);

	public ApplicationContext apc;

	@Before
	public void init() {
		// PropertyConfigurator.configure("./conf/log4j.properties");
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(lc);
			lc.reset();
			log.debug("user.dir:" + System.getProperty("user.dir"));
			configurator.doConfigure(System.getProperty("user.dir")
					+ "/conf/logback.xml");
		} catch (JoranException e) {
			log.error(e.getMessage());
		}
		log.info("初始化资源文件...");
		apc = new FileSystemXmlApplicationContext(
				"src/main/webapp/WEB-INF/applicationContext.xml");
	}

}
