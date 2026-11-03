package com.autoescuela.erp;

import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ManDsApplication
{
	public static void main(String[] args)
	{
		// Detectar perfil activo para cargar el archivo de variables adecuado (.env.render o .env)
		String activeProfile = System.getProperty("spring.profiles.active");
		if (activeProfile == null || activeProfile.isBlank())
		{
			activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
		}
		if (activeProfile == null || activeProfile.isBlank())
		{
			try (InputStream is = ManDsApplication.class.getResourceAsStream("/application.properties"))
			{
				if (is != null)
				{
					Properties props = new Properties();
					props.load(is);
					activeProfile = props.getProperty("spring.profiles.active", "docker");
				}
			}
			catch (Exception ignored)
			{
				activeProfile = "docker";
			}
		}

		// Carga .env.render si el perfil activo es deploy; de lo contrario carga .env
		String envFile = (activeProfile != null && activeProfile.contains("deploy")) ? ".env.render" : ".env";
		Dotenv dotenv = Dotenv.configure().filename(envFile).ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		// Fallback complementario: si se cargó .env.render, inyectar variables secundarias de .env sin sobrescribir
		if (!".env".equals(envFile))
		{
			Dotenv fallbackDotenv = Dotenv.configure().ignoreIfMissing().load();
			fallbackDotenv.entries().forEach(entry -> {
				if (System.getProperty(entry.getKey()) == null)
				{
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		}

		// Ejecutamos la aplicación Spring Boot
		SpringApplication.run(ManDsApplication.class, args);
	}

}
