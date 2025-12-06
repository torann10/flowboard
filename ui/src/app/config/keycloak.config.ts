import { KeycloakConfig } from 'keycloak-js';

export const keycloakConfig: KeycloakConfig = {
  url: 'http://localhost:9090',
  realm: 'flowboard',
  clientId: 'flowboard'
};

export const keycloakInitOptions = {
  config: keycloakConfig,
  initOptions: {
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    checkLoginIframe: false,
    pkceMethod: 'S256'
  },
  bearerExcludedUrls: [
    '/assets',
    '/clients/public'
  ]
};

















