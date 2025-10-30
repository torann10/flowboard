import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportCreateRequestDto } from '../model/reportCreateRequestDto';
import { ReportDto } from '../model/reportDto';
import { ReportUpdateRequestDto } from '../model/reportUpdateRequestDto';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class ReportControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Create report
     * Creates a new report for the current user
     * @param reportCreateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createReport(reportCreateRequestDto: ReportCreateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ReportDto>;
    createReport(reportCreateRequestDto: ReportCreateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ReportDto>>;
    createReport(reportCreateRequestDto: ReportCreateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ReportDto>>;
    /**
     * Delete report
     * Deletes a report by its ID for the current user
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    deleteReport(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    deleteReport(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    deleteReport(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    /**
     * Get all reports
     * Retrieves all reports for the current user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getAllReports(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<Array<ReportDto>>;
    getAllReports(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<Array<ReportDto>>>;
    getAllReports(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<Array<ReportDto>>>;
    /**
     * Get report by ID
     * Retrieves a report by its ID for the current user
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getReportById(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ReportDto>;
    getReportById(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ReportDto>>;
    getReportById(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ReportDto>>;
    /**
     * Get report count
     * Retrieves the total number of reports for the current user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getReportCount(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<number>;
    getReportCount(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<number>>;
    getReportCount(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<number>>;
    /**
     * Update report
     * Updates an existing report for the current user
     * @param id
     * @param reportUpdateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    updateReport(id: string, reportUpdateRequestDto: ReportUpdateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ReportDto>;
    updateReport(id: string, reportUpdateRequestDto: ReportUpdateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ReportDto>>;
    updateReport(id: string, reportUpdateRequestDto: ReportUpdateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ReportDto>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ReportControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ReportControllerApiService>;
}
