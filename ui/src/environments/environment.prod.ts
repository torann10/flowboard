export const environment = {
  production: true,
  basePath: 'https://__INGRESS_HOST_PLACEHOLDER__/api',
  websocketUrl: 'https://__INGRESS_HOST_PLACEHOLDER__/api/ws',
  keycloak: {
    url: '__KEYCLOAK_HOST_PLACEHOLDER__/auth',
    realm: 'DigitalComparisonTool',
    clientId: 'DigitalComparisonToolFrontend',
  },
};
