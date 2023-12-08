package com.example.demokeycloakspring.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts a {@link Jwt} token to an {@link AbstractAuthenticationToken}
 * by extracting and combining authorities from different sources within the JWT.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;
    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    /**
     * Converts a Jwt token to an AbstractAuthenticationToken.
     *
     * @param jwt the Jwt token to convert
     * @return an AbstractAuthenticationToken containing the authorities and claims from the Jwt
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> jwtAuthorities = getJwtAuthorities(jwt);
        Collection<GrantedAuthority> combinedAuthorities = getCombinedAuthorities(jwtAuthorities, jwt);
        return new JwtAuthenticationToken(jwt, combinedAuthorities, getPrincipleClaimName(jwt));
    }

    /**
     * Extracts JWT authorities.
     *
     * @param jwt the Jwt token
     * @return a collection of GrantedAuthority based on the Jwt token
     */
    private Collection<GrantedAuthority> getJwtAuthorities(Jwt jwt) {
        return Optional.of(jwtGrantedAuthoritiesConverter.convert(jwt))
                .orElse(Collections.emptyList());
    }

    /**
     * Combines JWT authorities with resource roles.
     *
     * @param jwtAuthorities the JWT authorities
     * @param jwt the Jwt token
     * @return a combined collection of GrantedAuthority
     */
    private Collection<GrantedAuthority> getCombinedAuthorities(Collection<GrantedAuthority> jwtAuthorities, Jwt jwt) {
        if (jwtAuthorities.isEmpty()) {
            return Collections.emptySet();
        }
        return Stream.concat(jwtAuthorities.stream(), extractResourceRoles(jwt).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the principal claim name from a Jwt token.
     *
     * @param jwt the Jwt token
     * @return the principal claim name
     */
    private String getPrincipleClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (principleAttribute != null) {
            claimName = principleAttribute;
        }
        return jwt.getClaim(claimName);
    }

    /**
     * Extracts resource roles from a Jwt token.
     *
     * @param jwt the Jwt token
     * @return a collection of GrantedAuthority representing the resource roles
     */
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        if (!hasResourceAccess(jwt)) {
            return Collections.emptySet();
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Map<String, Object> resource = getResource(resourceAccess);
        if (resource == null) {
            return Collections.emptySet();
        }

        return getRolesAsAuthorities(resource);
    }

    /**
     * Checks if the JWT contains a 'resource_access' claim.
     *
     * @param jwt the Jwt token to check
     * @return true if 'resource_access' claim is present, false otherwise
     */
    private boolean hasResourceAccess(Jwt jwt) {
        return jwt.getClaim("resource_access") != null;
    }

    /**
     * Retrieves the resource from the 'resource_access' claim based on a specified resource ID.
     *
     * @param resourceAccess the map containing the 'resource_access' claim
     * @return a map representing the resource, or null if the resource is not found
     */
    private Map<String, Object> getResource(Map<String, Object> resourceAccess) {
        Object resourceObj = resourceAccess.get(resourceId);
        if (resourceObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) resourceObj;
            return resource;
        }
        return Collections.emptyMap();
    }


    /**
     * Extracts and converts the roles from a resource into GrantedAuthority objects.
     * This method attempts to cast the roles to a collection of strings and then
     * maps them to GrantedAuthority objects.
     *
     * @param resource the resource map containing the roles
     * @return a collection of GrantedAuthority objects, or an empty set if a cast exception occurs
     */
    private Collection<? extends GrantedAuthority> getRolesAsAuthorities(Map<String, Object> resource) {
        try {
            Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
            return resourceRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
        } catch (ClassCastException e) {
            return Collections.emptySet();
        }
    }

}