package pl.karol202.bow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import pl.karol202.bow.robot.Robot;

@SpringBootApplication
public class StarterApplication extends SpringBootServletInitializer
{
	public static void main(String[] args)
	{
		ConfigurableApplicationContext context = SpringApplication.run(StarterApplication.class, args);
		context.getBean(Robot.class).start();
	}
}
