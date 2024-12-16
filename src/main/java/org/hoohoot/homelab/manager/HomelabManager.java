package org.hoohoot.homelab.manager;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
        tags = {
                @Tag(name = "Notifications", description = "Notification System")
        },
        info = @Info(title = "Homelab Manager API", version = "0.0.1", contact = @Contact(name = "ldclrcq")))
public class HomelabManager extends Application {
}
