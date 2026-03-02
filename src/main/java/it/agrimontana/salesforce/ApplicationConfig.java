package it.agrimontana.salesforce;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@ApplicationPath("/api")
@OpenAPIDefinition(
        info = @Info(
                title = "API Agrimontana",
                version = "1.0.0",
                description = "Documentazione delle REST API"
        )
)
public class ApplicationConfig extends Application {
}
