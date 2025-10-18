import { effect, inject, Injectable, Signal } from "@angular/core";
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEvent, KeycloakEventType, ReadyArgs, typeEventArgs } from "keycloak-angular";

/**
 * This class is for being able to check if the initialization of Keycloak has finished.
 * In the app.module's APP_INITIALIZER section, isInitialized is called to check if it has finished or not
 */
@Injectable({
    providedIn: "root"
})
export class KeycloakServiceInitializer {

    keycloakSignal: Signal<KeycloakEvent> = inject(KEYCLOAK_EVENT_SIGNAL);
    initialized: Promise<boolean>;

    constructor() {
        this.initialized = new Promise<boolean>((resolve) => {
            const stop = effect(() => {
                const event = this.keycloakSignal();
                if (event.type === KeycloakEventType.Ready) {
                    resolve(typeEventArgs<ReadyArgs>(event.args))
                    stop.destroy();
                }
            })
        })
    }

    public isInitialized() {
        return this.initialized;
    }
}
