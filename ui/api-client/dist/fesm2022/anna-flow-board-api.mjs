import * as i0 from '@angular/core';
import { InjectionToken, Optional, Inject, Injectable, SkipSelf, NgModule, makeEnvironmentProviders } from '@angular/core';
import * as i1 from '@angular/common/http';
import { HttpHeaders, HttpContext } from '@angular/common/http';

const BASE_PATH = new InjectionToken('basePath');
const COLLECTION_FORMATS = {
    'csv': ',',
    'tsv': '   ',
    'ssv': ' ',
    'pipes': '|'
};

/**
 * Custom HttpParameterCodec
 * Workaround for https://github.com/angular/angular/issues/18261
 */
class CustomHttpParameterCodec {
    encodeKey(k) {
        return encodeURIComponent(k);
    }
    encodeValue(v) {
        return encodeURIComponent(v);
    }
    decodeKey(k) {
        return decodeURIComponent(k);
    }
    decodeValue(v) {
        return decodeURIComponent(v);
    }
}

class FlowBoardConfiguration {
    /**
     *  @deprecated Since 5.0. Use credentials instead
     */
    apiKeys;
    username;
    password;
    /**
     *  @deprecated Since 5.0. Use credentials instead
     */
    accessToken;
    basePath;
    withCredentials;
    /**
     * Takes care of encoding query- and form-parameters.
     */
    encoder;
    /**
     * Encoding of various path parameter
     * <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#style-values">styles</a>.
     * <p>
     * See {@link README.md} for more details
     * </p>
     */
    encodeParam;
    /**
     * The keys are the names in the securitySchemes section of the OpenAPI
     * document. They should map to the value used for authentication
     * minus any standard prefixes such as 'Basic' or 'Bearer'.
     */
    credentials;
    constructor({ accessToken, apiKeys, basePath, credentials, encodeParam, encoder, password, username, withCredentials } = {}) {
        if (apiKeys) {
            this.apiKeys = apiKeys;
        }
        if (username !== undefined) {
            this.username = username;
        }
        if (password !== undefined) {
            this.password = password;
        }
        if (accessToken !== undefined) {
            this.accessToken = accessToken;
        }
        if (basePath !== undefined) {
            this.basePath = basePath;
        }
        if (withCredentials !== undefined) {
            this.withCredentials = withCredentials;
        }
        if (encoder) {
            this.encoder = encoder;
        }
        this.encodeParam = encodeParam ?? (param => this.defaultEncodeParam(param));
        this.credentials = credentials ?? {};
        // init default oauth2 credential
        if (!this.credentials['oauth2']) {
            this.credentials['oauth2'] = () => {
                return typeof this.accessToken === 'function'
                    ? this.accessToken()
                    : this.accessToken;
            };
        }
    }
    /**
     * Select the correct content-type to use for a request.
     * Uses {@link FlowBoardConfiguration#isJsonMime} to determine the correct content-type.
     * If no content type is found return the first found type if the contentTypes is not empty
     * @param contentTypes - the array of content types that are available for selection
     * @returns the selected content-type or <code>undefined</code> if no selection could be made.
     */
    selectHeaderContentType(contentTypes) {
        if (contentTypes.length === 0) {
            return undefined;
        }
        const type = contentTypes.find((x) => this.isJsonMime(x));
        if (type === undefined) {
            return contentTypes[0];
        }
        return type;
    }
    /**
     * Select the correct accept content-type to use for a request.
     * Uses {@link FlowBoardConfiguration#isJsonMime} to determine the correct accept content-type.
     * If no content type is found return the first found type if the contentTypes is not empty
     * @param accepts - the array of content types that are available for selection.
     * @returns the selected content-type or <code>undefined</code> if no selection could be made.
     */
    selectHeaderAccept(accepts) {
        if (accepts.length === 0) {
            return undefined;
        }
        const type = accepts.find((x) => this.isJsonMime(x));
        if (type === undefined) {
            return accepts[0];
        }
        return type;
    }
    /**
     * Check if the given MIME is a JSON MIME.
     * JSON MIME examples:
     *   application/json
     *   application/json; charset=UTF8
     *   APPLICATION/JSON
     *   application/vnd.company+json
     * @param mime - MIME (Multipurpose Internet Mail Extensions)
     * @return True if the given MIME is JSON, false otherwise.
     */
    isJsonMime(mime) {
        const jsonMime = new RegExp('^(application\/json|[^;/ \t]+\/[^;/ \t]+[+]json)[ \t]*(;.*)?$', 'i');
        return mime !== null && (jsonMime.test(mime) || mime.toLowerCase() === 'application/json-patch+json');
    }
    lookupCredential(key) {
        const value = this.credentials[key];
        return typeof value === 'function'
            ? value()
            : value;
    }
    addCredentialToHeaders(credentialKey, headerName, headers, prefix) {
        const value = this.lookupCredential(credentialKey);
        return value
            ? headers.set(headerName, (prefix ?? '') + value)
            : headers;
    }
    addCredentialToQuery(credentialKey, paramName, query) {
        const value = this.lookupCredential(credentialKey);
        return value
            ? query.set(paramName, value)
            : query;
    }
    defaultEncodeParam(param) {
        // This implementation exists as fallback for missing configuration
        // and for backwards compatibility to older typescript-angular generator versions.
        // It only works for the 'simple' parameter style.
        // Date-handling only works for the 'date-time' format.
        // All other styles and Date-formats are probably handled incorrectly.
        //
        // But: if that's all you need (i.e.: the most common use-case): no need for customization!
        const value = param.dataFormat === 'date-time' && param.value instanceof Date
            ? param.value.toISOString()
            : param.value;
        return encodeURIComponent(String(value));
    }
}

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
class BaseService {
    basePath = 'http://localhost:8080';
    defaultHeaders = new HttpHeaders();
    configuration;
    encoder;
    constructor(basePath, configuration) {
        this.configuration = configuration || new FlowBoardConfiguration();
        if (typeof this.configuration.basePath !== 'string') {
            const firstBasePath = Array.isArray(basePath) ? basePath[0] : undefined;
            if (firstBasePath != undefined) {
                basePath = firstBasePath;
            }
            if (typeof basePath !== 'string') {
                basePath = this.basePath;
            }
            this.configuration.basePath = basePath;
        }
        this.encoder = this.configuration.encoder || new CustomHttpParameterCodec();
    }
    canConsumeForm(consumes) {
        return consumes.indexOf('multipart/form-data') !== -1;
    }
    addToHttpParams(httpParams, value, key, isDeep = false) {
        // If the value is an object (but not a Date), recursively add its keys.
        if (typeof value === 'object' && !(value instanceof Date)) {
            return this.addToHttpParamsRecursive(httpParams, value, isDeep ? key : undefined, isDeep);
        }
        return this.addToHttpParamsRecursive(httpParams, value, key);
    }
    addToHttpParamsRecursive(httpParams, value, key, isDeep = false) {
        if (value === null || value === undefined) {
            return httpParams;
        }
        if (typeof value === 'object') {
            // If JSON format is preferred, key must be provided.
            if (key != null) {
                return isDeep
                    ? Object.keys(value).reduce((hp, k) => hp.append(`${key}[${k}]`, value[k]), httpParams)
                    : httpParams.append(key, JSON.stringify(value));
            }
            // Otherwise, if it's an array, add each element.
            if (Array.isArray(value)) {
                value.forEach(elem => httpParams = this.addToHttpParamsRecursive(httpParams, elem, key));
            }
            else if (value instanceof Date) {
                if (key != null) {
                    httpParams = httpParams.append(key, value.toISOString());
                }
                else {
                    throw Error("key may not be null if value is Date");
                }
            }
            else {
                Object.keys(value).forEach(k => {
                    const paramKey = key ? `${key}.${k}` : k;
                    httpParams = this.addToHttpParamsRecursive(httpParams, value[k], paramKey);
                });
            }
            return httpParams;
        }
        else if (key != null) {
            return httpParams.append(key, value);
        }
        throw Error("key may not be null if value is not object or array");
    }
}

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class AuthControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    getCurrentUserAuth(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/auth/user`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: AuthControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: AuthControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: AuthControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class ProjectControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    createProject(projectCreateRequestDto, observe = 'body', reportProgress = false, options) {
        if (projectCreateRequestDto === null || projectCreateRequestDto === undefined) {
            throw new Error('Required parameter projectCreateRequestDto was null or undefined when calling createProject.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/projects`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('post', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: projectCreateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    deleteProject(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteProject.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/projects/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('delete', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getAllProjects(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/projects`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getProjectById(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getProjectById.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/projects/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getProjectCount(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/projects/count`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    updateProject(id, projectUpdateRequestDto, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling updateProject.');
        }
        if (projectUpdateRequestDto === null || projectUpdateRequestDto === undefined) {
            throw new Error('Required parameter projectUpdateRequestDto was null or undefined when calling updateProject.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/projects/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('put', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: projectUpdateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ProjectControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ProjectControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ProjectControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class ProjectUserControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    createProjectUser(projectUserCreateRequestDto, observe = 'body', reportProgress = false, options) {
        if (projectUserCreateRequestDto === null || projectUserCreateRequestDto === undefined) {
            throw new Error('Required parameter projectUserCreateRequestDto was null or undefined when calling createProjectUser.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/project-users`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('post', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: projectUserCreateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    deleteProjectUser(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteProjectUser.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/project-users/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('delete', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getAllProjectUsers(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/project-users`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getProjectUserById(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getProjectUserById.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/project-users/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getProjectUserCount(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/project-users/count`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    updateProjectUser(id, projectUserUpdateRequestDto, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling updateProjectUser.');
        }
        if (projectUserUpdateRequestDto === null || projectUserUpdateRequestDto === undefined) {
            throw new Error('Required parameter projectUserUpdateRequestDto was null or undefined when calling updateProjectUser.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/project-users/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('put', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: projectUserUpdateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ProjectUserControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ProjectUserControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ProjectUserControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class ReportControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    createReport(reportCreateRequestDto, observe = 'body', reportProgress = false, options) {
        if (reportCreateRequestDto === null || reportCreateRequestDto === undefined) {
            throw new Error('Required parameter reportCreateRequestDto was null or undefined when calling createReport.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/reports`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('post', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: reportCreateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    deleteReport(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteReport.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/reports/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('delete', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getAllReports(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/reports`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getReportById(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getReportById.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/reports/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getReportCount(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/reports/count`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    updateReport(id, reportUpdateRequestDto, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling updateReport.');
        }
        if (reportUpdateRequestDto === null || reportUpdateRequestDto === undefined) {
            throw new Error('Required parameter reportUpdateRequestDto was null or undefined when calling updateReport.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/reports/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('put', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: reportUpdateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ReportControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ReportControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: ReportControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class TaskControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    createTask(taskCreateRequestDto, observe = 'body', reportProgress = false, options) {
        if (taskCreateRequestDto === null || taskCreateRequestDto === undefined) {
            throw new Error('Required parameter taskCreateRequestDto was null or undefined when calling createTask.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/tasks`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('post', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: taskCreateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    deleteTask(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteTask.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/tasks/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('delete', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getAllTasks(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/tasks`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getTaskById(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getTaskById.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/tasks/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getTasksByProject(projectId, observe = 'body', reportProgress = false, options) {
        if (projectId === null || projectId === undefined) {
            throw new Error('Required parameter projectId was null or undefined when calling getTasksByProject.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/tasks/project/${this.configuration.encodeParam({ name: "projectId", value: projectId, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    updateTask(id, taskUpdateRequestDto, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling updateTask.');
        }
        if (taskUpdateRequestDto === null || taskUpdateRequestDto === undefined) {
            throw new Error('Required parameter taskUpdateRequestDto was null or undefined when calling updateTask.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/tasks/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('put', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: taskUpdateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: TaskControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: TaskControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: TaskControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class TimeLogControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    createTimeLog(timeLogUpdateRequestDto, observe = 'body', reportProgress = false, options) {
        if (timeLogUpdateRequestDto === null || timeLogUpdateRequestDto === undefined) {
            throw new Error('Required parameter timeLogUpdateRequestDto was null or undefined when calling createTimeLog.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/time-logs`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('post', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: timeLogUpdateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    deleteTimeLog(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteTimeLog.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/time-logs/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('delete', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getAllTimeLogs(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/time-logs`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getTimeLogById(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getTimeLogById.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/time-logs/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getTimeLogCount(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/time-logs/count`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    updateTimeLog(id, timeLogUpdateRequestDto, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling updateTimeLog.');
        }
        if (timeLogUpdateRequestDto === null || timeLogUpdateRequestDto === undefined) {
            throw new Error('Required parameter timeLogUpdateRequestDto was null or undefined when calling updateTimeLog.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/time-logs/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('put', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: timeLogUpdateRequestDto,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: TimeLogControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: TimeLogControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: TimeLogControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */
class UserControllerApiService extends BaseService {
    httpClient;
    constructor(httpClient, basePath, configuration) {
        super(basePath, configuration);
        this.httpClient = httpClient;
    }
    createUser(userCreateRequest, observe = 'body', reportProgress = false, options) {
        if (userCreateRequest === null || userCreateRequest === undefined) {
            throw new Error('Required parameter userCreateRequest was null or undefined when calling createUser.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('post', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: userCreateRequest,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    deleteUser(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteUser.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('delete', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getAllUsers(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getCurrentUser(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users/me`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getUserById(id, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getUserById.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    getUserCount(observe = 'body', reportProgress = false, options) {
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users/count`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('get', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    updateUser(id, userUpdateRequest, observe = 'body', reportProgress = false, options) {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling updateUser.');
        }
        if (userUpdateRequest === null || userUpdateRequest === undefined) {
            throw new Error('Required parameter userUpdateRequest was null or undefined when calling updateUser.');
        }
        let localVarHeaders = this.defaultHeaders;
        // authentication (oauth2) required
        localVarHeaders = this.configuration.addCredentialToHeaders('oauth2', 'Authorization', localVarHeaders, 'Bearer ');
        const localVarHttpHeaderAcceptSelected = options?.httpHeaderAccept ?? this.configuration.selectHeaderAccept([
            'application/json',
            '*/*'
        ]);
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }
        const localVarHttpContext = options?.context ?? new HttpContext();
        const localVarTransferCache = options?.transferCache ?? true;
        // to determine the Content-Type header
        const consumes = [
            'application/json'
        ];
        const httpContentTypeSelected = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }
        let responseType_ = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            }
            else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            }
            else {
                responseType_ = 'blob';
            }
        }
        let localVarPath = `/users/${this.configuration.encodeParam({ name: "id", value: id, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: "uuid" })}`;
        const { basePath, withCredentials } = this.configuration;
        return this.httpClient.request('put', `${basePath}${localVarPath}`, {
            context: localVarHttpContext,
            body: userUpdateRequest,
            responseType: responseType_,
            ...(withCredentials ? { withCredentials } : {}),
            headers: localVarHeaders,
            observe: observe,
            transferCache: localVarTransferCache,
            reportProgress: reportProgress
        });
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: UserControllerApiService, deps: [{ token: i1.HttpClient }, { token: BASE_PATH, optional: true }, { token: FlowBoardConfiguration, optional: true }], target: i0.ɵɵFactoryTarget.Injectable });
    static ɵprov = i0.ɵɵngDeclareInjectable({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: UserControllerApiService, providedIn: 'root' });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: UserControllerApiService, decorators: [{
            type: Injectable,
            args: [{
                    providedIn: 'root'
                }]
        }], ctorParameters: () => [{ type: i1.HttpClient }, { type: undefined, decorators: [{
                    type: Optional
                }, {
                    type: Inject,
                    args: [BASE_PATH]
                }] }, { type: FlowBoardConfiguration, decorators: [{
                    type: Optional
                }] }] });

const APIS = [AuthControllerApiService, ProjectControllerApiService, ProjectUserControllerApiService, ReportControllerApiService, TaskControllerApiService, TimeLogControllerApiService, UserControllerApiService];

var ProjectCreateRequestDto;
(function (ProjectCreateRequestDto) {
    ProjectCreateRequestDto.StatusEnum = {
        Active: 'ACTIVE',
        Archived: 'ARCHIVED',
        Completed: 'COMPLETED'
    };
    ProjectCreateRequestDto.TypeEnum = {
        TimeBased: 'TIME_BASED',
        StoryPointBased: 'STORY_POINT_BASED'
    };
})(ProjectCreateRequestDto || (ProjectCreateRequestDto = {}));

var ProjectDto;
(function (ProjectDto) {
    ProjectDto.StatusEnum = {
        Active: 'ACTIVE',
        Archived: 'ARCHIVED',
        Completed: 'COMPLETED'
    };
    ProjectDto.TypeEnum = {
        TimeBased: 'TIME_BASED',
        StoryPointBased: 'STORY_POINT_BASED'
    };
})(ProjectDto || (ProjectDto = {}));

var ProjectUpdateRequestDto;
(function (ProjectUpdateRequestDto) {
    ProjectUpdateRequestDto.StatusEnum = {
        Active: 'ACTIVE',
        Archived: 'ARCHIVED',
        Completed: 'COMPLETED'
    };
    ProjectUpdateRequestDto.TypeEnum = {
        TimeBased: 'TIME_BASED',
        StoryPointBased: 'STORY_POINT_BASED'
    };
})(ProjectUpdateRequestDto || (ProjectUpdateRequestDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
var ProjectUserCreateRequestDto;
(function (ProjectUserCreateRequestDto) {
    ProjectUserCreateRequestDto.RoleEnum = {
        Maintainer: 'MAINTAINER',
        Editor: 'EDITOR',
        Member: 'MEMBER',
        Reporter: 'REPORTER'
    };
})(ProjectUserCreateRequestDto || (ProjectUserCreateRequestDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
var ProjectUserDto;
(function (ProjectUserDto) {
    ProjectUserDto.RoleEnum = {
        Maintainer: 'MAINTAINER',
        Editor: 'EDITOR',
        Member: 'MEMBER',
        Reporter: 'REPORTER'
    };
})(ProjectUserDto || (ProjectUserDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
var ProjectUserUpdateRequestDto;
(function (ProjectUserUpdateRequestDto) {
    ProjectUserUpdateRequestDto.RoleEnum = {
        Maintainer: 'MAINTAINER',
        Editor: 'EDITOR',
        Member: 'MEMBER',
        Reporter: 'REPORTER'
    };
})(ProjectUserUpdateRequestDto || (ProjectUserUpdateRequestDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
var TaskCreateRequestDto;
(function (TaskCreateRequestDto) {
    TaskCreateRequestDto.StatusEnum = {
        Open: 'OPEN',
        InProgress: 'IN_PROGRESS',
        Done: 'DONE',
        Canceled: 'CANCELED'
    };
})(TaskCreateRequestDto || (TaskCreateRequestDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
var TaskDto;
(function (TaskDto) {
    TaskDto.StatusEnum = {
        Open: 'OPEN',
        InProgress: 'IN_PROGRESS',
        Done: 'DONE',
        Canceled: 'CANCELED'
    };
})(TaskDto || (TaskDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
var TaskUpdateRequestDto;
(function (TaskUpdateRequestDto) {
    TaskUpdateRequestDto.StatusEnum = {
        Open: 'OPEN',
        InProgress: 'IN_PROGRESS',
        Done: 'DONE',
        Canceled: 'CANCELED'
    };
})(TaskUpdateRequestDto || (TaskUpdateRequestDto = {}));

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

/**
 * OpenAPI definition
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

class FlowBoardApiModule {
    static forRoot(configurationFactory) {
        return {
            ngModule: FlowBoardApiModule,
            providers: [{ provide: FlowBoardConfiguration, useFactory: configurationFactory }]
        };
    }
    constructor(parentModule, http) {
        if (parentModule) {
            throw new Error('FlowBoardApiModule is already loaded. Import in your base AppModule only.');
        }
        if (!http) {
            throw new Error('You need to import the HttpClientModule in your AppModule! \n' +
                'See also https://github.com/angular/angular/issues/20575');
        }
    }
    static ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: FlowBoardApiModule, deps: [{ token: FlowBoardApiModule, optional: true, skipSelf: true }, { token: i1.HttpClient, optional: true }], target: i0.ɵɵFactoryTarget.NgModule });
    static ɵmod = i0.ɵɵngDeclareNgModule({ minVersion: "14.0.0", version: "19.2.15", ngImport: i0, type: FlowBoardApiModule });
    static ɵinj = i0.ɵɵngDeclareInjector({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: FlowBoardApiModule });
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "19.2.15", ngImport: i0, type: FlowBoardApiModule, decorators: [{
            type: NgModule,
            args: [{
                    imports: [],
                    declarations: [],
                    exports: [],
                    providers: []
                }]
        }], ctorParameters: () => [{ type: FlowBoardApiModule, decorators: [{
                    type: Optional
                }, {
                    type: SkipSelf
                }] }, { type: i1.HttpClient, decorators: [{
                    type: Optional
                }] }] });

// Returns the service class providers, to be used in the [ApplicationConfig](https://angular.dev/api/core/ApplicationConfig).
function provideApi(configOrBasePath) {
    return makeEnvironmentProviders([
        typeof configOrBasePath === "string"
            ? { provide: BASE_PATH, useValue: configOrBasePath }
            : {
                provide: FlowBoardConfiguration,
                useValue: new FlowBoardConfiguration({ ...configOrBasePath }),
            },
    ]);
}

/**
 * Generated bundle index. Do not edit.
 */

export { APIS, AuthControllerApiService, BASE_PATH, COLLECTION_FORMATS, FlowBoardApiModule, FlowBoardConfiguration, ProjectControllerApiService, ProjectCreateRequestDto, ProjectDto, ProjectUpdateRequestDto, ProjectUserControllerApiService, ProjectUserCreateRequestDto, ProjectUserDto, ProjectUserUpdateRequestDto, ReportControllerApiService, TaskControllerApiService, TaskCreateRequestDto, TaskDto, TaskUpdateRequestDto, TimeLogControllerApiService, UserControllerApiService, provideApi };
//# sourceMappingURL=anna-flow-board-api.mjs.map
