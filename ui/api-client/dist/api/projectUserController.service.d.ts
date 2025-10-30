import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectUserCreateRequestDto } from '../model/projectUserCreateRequestDto';
import { ProjectUserDto } from '../model/projectUserDto';
import { ProjectUserUpdateRequestDto } from '../model/projectUserUpdateRequestDto';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class ProjectUserControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Create project-user relationship
     * Creates a new project-user relationship
     * @param projectUserCreateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createProjectUser(projectUserCreateRequestDto: ProjectUserCreateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ProjectUserDto>;
    createProjectUser(projectUserCreateRequestDto: ProjectUserCreateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ProjectUserDto>>;
    createProjectUser(projectUserCreateRequestDto: ProjectUserCreateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ProjectUserDto>>;
    /**
     * Delete project-user relationship
     * Deletes a project-user relationship by its ID
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    deleteProjectUser(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    deleteProjectUser(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    deleteProjectUser(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    /**
     * Get all project-user relationships
     * Retrieves all project-user relationships
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getAllProjectUsers(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<Array<ProjectUserDto>>;
    getAllProjectUsers(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<Array<ProjectUserDto>>>;
    getAllProjectUsers(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<Array<ProjectUserDto>>>;
    /**
     * Get project-user relationship by ID
     * Retrieves a project-user relationship by its ID
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getProjectUserById(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ProjectUserDto>;
    getProjectUserById(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ProjectUserDto>>;
    getProjectUserById(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ProjectUserDto>>;
    /**
     * Get project-user relationship count
     * Retrieves the total number of project-user relationships
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getProjectUserCount(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<number>;
    getProjectUserCount(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<number>>;
    getProjectUserCount(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<number>>;
    /**
     * Update project-user relationship
     * Updates an existing project-user relationship
     * @param id
     * @param projectUserUpdateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    updateProjectUser(id: string, projectUserUpdateRequestDto: ProjectUserUpdateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<ProjectUserDto>;
    updateProjectUser(id: string, projectUserUpdateRequestDto: ProjectUserUpdateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<ProjectUserDto>>;
    updateProjectUser(id: string, projectUserUpdateRequestDto: ProjectUserUpdateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<ProjectUserDto>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ProjectUserControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ProjectUserControllerApiService>;
}
