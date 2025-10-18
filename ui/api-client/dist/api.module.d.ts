import { ModuleWithProviders } from '@angular/core';
import { FlowBoardConfiguration } from './configuration';
import { HttpClient } from '@angular/common/http';
import * as i0 from "@angular/core";
export declare class FlowBoardApiModule {
    static forRoot(configurationFactory: () => FlowBoardConfiguration): ModuleWithProviders<FlowBoardApiModule>;
    constructor(parentModule: FlowBoardApiModule, http: HttpClient);
    static ɵfac: i0.ɵɵFactoryDeclaration<FlowBoardApiModule, [{ optional: true; skipSelf: true; }, { optional: true; }]>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<FlowBoardApiModule, never, never, never>;
    static ɵinj: i0.ɵɵInjectorDeclaration<FlowBoardApiModule>;
}
