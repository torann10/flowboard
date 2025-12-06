import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateCOCReportRequestDto } from '../model/createCOCReportRequestDto';
import { CreateEmployeeMatrixReportRequestDto } from '../model/createEmployeeMatrixReportRequestDto';
import { CreateProjectActivityReportRequestDto } from '../model/createProjectActivityReportRequestDto';
import { DownloadReportDto } from '../model/downloadReportDto';
import { ReportDto } from '../model/reportDto';
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
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<string>;
    createCocReport(createCOCReportRequestDto: CreateCOCReportRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<string>>;
    createCocReport(createCOCReportRequestDto: CreateCOCReportRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<string>>;
    /**
     * Create employee matrix report
     * Creates a new report PDF for the current user
     * @param createEmployeeMatrixReportRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createEmployeeMatrixReport(createEmployeeMatrixReportRequestDto: CreateEmployeeMatrixReportRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<string>;
    createEmployeeMatrixReport(createEmployeeMatrixReportRequestDto: CreateEmployeeMatrixReportRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<string>>;
    createEmployeeMatrixReport(createEmployeeMatrixReportRequestDto: CreateEmployeeMatrixReportRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<string>>;
    /**
     * Create project activity report
     * Creates a new report PDF for the current user
     * @param createProjectActivityReportRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createProjectActivityReport(createProjectActivityReportRequestDto: CreateProjectActivityReportRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<string>;
    createProjectActivityReport(createProjectActivityReportRequestDto: CreateProjectActivityReportRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<string>>;
    createProjectActivityReport(createProjectActivityReportRequestDto: CreateProjectActivityReportRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<string>>;
    /**
     * Delete a report
     * Deletes a report with the given unique identifier
     * @param reportId
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    deleteReport(reportId: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    deleteReport(reportId: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    deleteReport(reportId: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    /**
     * Retrieve a report download url
     * Retrieves a short lived download url for the report
     * @param reportId
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getReportDownloadUrl(reportId: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<DownloadReportDto>;
    getReportDownloadUrl(reportId: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<DownloadReportDto>>;
    getReportDownloadUrl(reportId: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<DownloadReportDto>>;
    /**
     * Lists the reports for the user
     * Lists the available reports for the user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    listReportsForUser(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<Array<ReportDto>>;
    listReportsForUser(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<Array<ReportDto>>>;
    listReportsForUser(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<Array<ReportDto>>>;
    /**
     * Renames a report
     * Renames a report with the given unique identifier
     * @param reportId
     * @param name
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    renameReport(reportId: string, name: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    renameReport(reportId: string, name: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    renameReport(reportId: string, name: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ReportControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ReportControllerApiService>;
}
