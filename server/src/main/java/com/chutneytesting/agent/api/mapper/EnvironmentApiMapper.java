package com.chutneytesting.agent.api.mapper;

import static com.google.common.base.Strings.nullToEmpty;
import static com.chutneytesting.engine.domain.environment.SecurityInfo.Credential;
import static java.util.Optional.ofNullable;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.SecurityApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import com.chutneytesting.engine.domain.environment.SecurityInfo.SecurityInfoBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentApiMapper {

    public Environment fromDto(EnvironmentApiDto environmentApiDto) {
        return Environment.builder()
            .withName(environmentApiDto.name)
            .withTargets(environmentApiDto.targetsConfiguration.stream().map(t -> fromDto(t, environmentApiDto.name)).collect(Collectors.toSet()))
            .build();
    }

    private Target fromDto(TargetsApiDto targetsApiDto, String env) {
        Map<String, String> properties = new LinkedHashMap<>(targetsApiDto.properties);

        return Target.builder()
            .withId(Target.TargetId.of(targetsApiDto.name, env))
            .withUrl(targetsApiDto.url)
            .withProperties(properties)
            .withSecurity(fromDto(targetsApiDto.security))
            .build();
    }

    private SecurityInfo fromDto(SecurityApiDto security) {
        SecurityInfoBuilder builder = SecurityInfo.builder();

        if (security.username != null || security.password != null) {
            builder.credential(Credential.of(nullToEmpty(security.username), nullToEmpty(security.password)));
        }
        ofNullable(security.keyStore).ifPresent(k -> builder.keyStore(k));
        ofNullable(security.keyStorePassword).ifPresent(k -> builder.keyStorePassword(k));
        ofNullable(security.trustStore).ifPresent(k -> builder.trustStore(k));
        ofNullable(security.trustStorePassword).ifPresent(k -> builder.trustStorePassword(k));

        return builder.build();
    }

    public EnvironmentApiDto toDto(Environment environment) {
        return new EnvironmentApiDto(environment.name, environment.targets.stream().map(t -> toDto(t)).collect(Collectors.toSet()));
    }

    private TargetsApiDto toDto(Target target) {
        Map<String, String> properties = new LinkedHashMap<>(target.properties);
        return new TargetsApiDto(target.name, target.url, properties, toDto(target.security));
    }

    private SecurityApiDto toDto(SecurityInfo security) {
        String username = security.credential().map(Credential::username).orElse(null);
        String password = security.credential().map(Credential::password).orElse(null);
        String keyStore = security.keyStore().orElse(null);
        String keyStorePassword = security.keyStorePassword().orElse(null);
        String trustStore = security.trustStore().orElse(null);
        String trustStorePassword = security.trustStorePassword().orElse(null);
        return new SecurityApiDto(username, password, keyStore, keyStorePassword, trustStore, trustStorePassword);
    }
}
