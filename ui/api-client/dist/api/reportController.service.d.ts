import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateCOCReportRequestDto } from '../model/createCOCReportRequestDto';
import { CreateEmployeeMatrixReportRequestDto } from '../model/createEmployeeMatrixReportRequestDto';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class ReportControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Create COC report
     * Creates a new report PDF for the current user
     * @param createCOCReportRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createCocReport(createCOCReportRequestDto: CreateCOCReportRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    createCocReport(createCOCReportRequestDto: CreateCOCReportRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    createCocReport(createCOCReportRequestDto: CreateCOCReportRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    /**
     * Create employee matrix report
     * Creates a new report PDF for the current user
     * @param createEmployeeMatrixReportRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createEmployeeMatrixReport(createEmployeeMatrixReportRequestDto: CreateEmployeeMatrixReportRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    createEmployeeMatrixReport(createEmployeeMatrixReportRequestDto: CreateEmployeeMatrixReportRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    createEmployeeMatrixReport(createEmployeeMatrixReportRequestDto: CreateEmployeeMatrixReportRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/pdf';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ReportControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ReportControllerApiService>;
}
