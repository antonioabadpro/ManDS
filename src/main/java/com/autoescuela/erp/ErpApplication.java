package com.autoescuela.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ErpApplication
{
	public static void main(String[] args)
	{
		// Carga el archivo .env si existe y lo inyecta en las propiedades del sistema
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		// Ejecutamos la aplicación Spring Boot
		SpringApplication.run(ErpApplication.class, args);
	}

}
