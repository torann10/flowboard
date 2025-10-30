import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectCreateRequestDto } from '../model/projectCreateRequestDto';
import { ProjectDto } from '../model/projectDto';
import { ProjectUpdateRequestDto } from '../model/projectUpdateRequestDto';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class ProjectControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Create project
     * Creates a new project and assigns the current user as admin
     * @param projectCreateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createProject(projectCreateRequestDto: ProjectCreateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ProjectDto>;
    createProject(projectCreateRequestDto: ProjectCreateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ProjectDto>>;
    createProject(projectCreateRequestDto: ProjectCreateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ProjectDto>>;
    /**
     * Delete project
     * Deletes a project by its ID for the current user
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    deleteProject(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    deleteProject(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    deleteProject(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    /**
     * Get all projects
     * Retrieves all projects for the current user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getAllProjects(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<Array<ProjectDto>>;
    getAllProjects(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<Array<ProjectDto>>>;
    getAllProjects(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<Array<ProjectDto>>>;
    /**
     * Get project by ID
     * Retrieves a project by its ID for the current user
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getProjectById(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ProjectDto>;
    getProjectById(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ProjectDto>>;
    getProjectById(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ProjectDto>>;
    /**
     * Get project count
     * Retrieves the total number of projects for the current user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getProjectCount(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<number>;
    getProjectCount(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<number>>;
    getProjectCount(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<number>>;
    /**
     * Update project
     * Updates an existing project for the current user
     * @param id
     * @param projectUpdateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    updateProject(id: string, projectUpdateRequestDto: ProjectUpdateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ProjectDto>;
    updateProject(id: string, projectUpdateRequestDto: ProjectUpdateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ProjectDto>>;
    updateProject(id: string, projectUpdateRequestDto: ProjectUpdateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ProjectDto>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ProjectControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ProjectControllerApiService>;
}
