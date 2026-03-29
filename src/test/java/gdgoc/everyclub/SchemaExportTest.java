package gdgoc.everyclub;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.jpa.properties.javax.persistence.schema-generation.scripts.action=create",
    "spring.jpa.properties.javax.persistence.schema-generation.scripts.create-target=test-schema.sql",
    "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create",
    "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=test-schema.sql",
    "spring.flyway.enabled=false"
})
@Disabled("Development utility test - not needed for CI")
class SchemaExportTest {

    @Test
    void exportSchema() {
        System.out.println("Schema generation executed!");
    }
}
