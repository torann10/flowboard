import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class AuthControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Get current user authentication info
     * Retrieves the current user\&#39;s authentication status and details
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getCurrentUserAuth(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<string>;
    getCurrentUserAuth(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<string>>;
    getCurrentUserAuth(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<string>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<AuthControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<AuthControllerApiService>;
}
