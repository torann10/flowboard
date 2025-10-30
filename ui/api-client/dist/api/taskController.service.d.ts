import { HttpClient, HttpResponse, HttpEvent, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TaskCreateRequestDto } from '../model/taskCreateRequestDto';
import { TaskDto } from '../model/taskDto';
import { TaskUpdateRequestDto } from '../model/taskUpdateRequestDto';
import { FlowBoardConfiguration } from '../configuration';
import { BaseService } from '../api.base.service';
import * as i0 from "@angular/core";
export declare class TaskControllerApiService extends BaseService {
    protected httpClient: HttpClient;
    constructor(httpClient: HttpClient, basePath: string | string[], configuration?: FlowBoardConfiguration);
    /**
     * Create task
     * Creates a new task for the current user
     * @param taskCreateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    createTask(taskCreateRequestDto: TaskCreateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<TaskDto>;
    createTask(taskCreateRequestDto: TaskCreateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<TaskDto>>;
    createTask(taskCreateRequestDto: TaskCreateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<TaskDto>>;
    /**
     * Delete task
     * Deletes a task by its ID for the current user
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    deleteTask(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<any>;
    deleteTask(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<any>>;
    deleteTask(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: undefined;
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<any>>;
    /**
     * Get all tasks
     * Retrieves all tasks for the current user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getAllTasks(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<Array<TaskDto>>;
    getAllTasks(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<Array<TaskDto>>>;
    getAllTasks(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<Array<TaskDto>>>;
    /**
     * Get task by ID
     * Retrieves a task by its ID for the current user
     * @param id
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getTaskById(id: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<TaskDto>;
    getTaskById(id: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<TaskDto>>;
    getTaskById(id: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<TaskDto>>;
    /**
     * Get task count
     * Retrieves the total number of tasks for the current user
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getTaskCount(observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<number>;
    getTaskCount(observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<number>>;
    getTaskCount(observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<number>>;
    /**
     * Get tasks by project
     * Retrieves all tasks for a specific project
     * @param projectId
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    getTasksByProject(projectId: string, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<Array<TaskDto>>;
    getTasksByProject(projectId: string, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<Array<TaskDto>>>;
    getTasksByProject(projectId: string, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<Array<TaskDto>>>;
    /**
     * Update task
     * Updates an existing task for the current user
     * @param id
     * @param taskUpdateRequestDto
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    updateTask(id: string, taskUpdateRequestDto: TaskUpdateRequestDto, observe?: 'body', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<TaskDto>;
    updateTask(id: string, taskUpdateRequestDto: TaskUpdateRequestDto, observe?: 'response', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpResponse<TaskDto>>;
    updateTask(id: string, taskUpdateRequestDto: TaskUpdateRequestDto, observe?: 'events', reportProgress?: boolean, options?: {
        httpHeaderAccept?: 'application/json' | '*/*';
        context?: HttpContext;
        transferCache?: boolean;
    }): Observable<HttpEvent<TaskDto>>;
    static ɵfac: i0.ɵɵFactoryDeclaration<TaskControllerApiService, [null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<TaskControllerApiService>;
}
