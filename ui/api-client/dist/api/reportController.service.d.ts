import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportCreateRequestDto } from '../model/reportCreateRequestDto';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class ReportControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Create report
     * Creates a new report PDF for the current user
     * @param reportCreateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createReport(reportCreateRequestDto: ReportCreateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    createReport(reportCreateRequestDto: ReportCreateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    createReport(reportCreateRequestDto: ReportCreateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ReportControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ReportControllerApiService>;
}
