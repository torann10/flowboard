import { inject, Injectable, Signal } from "@angular/core";
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEvent, KeycloakEventType } from "keycloak-angular";
import { distinctUntilChanged, filter, firstValueFrom, map, Observable, shareReplay } from "rxjs";
import Keycloak from 'keycloak-js';
import { toObservable } from '@angular/core/rxjs-interop';

/**
 * This service is responsibele for broadcasting JWT tokens once they are refreshed
 * We also need to be able to manually update the token, because it might be needed for websocket communication.
 */
@Injectable({
    providedIn: "root"
})
export class KeycloakSubscriberService {

    token$: Observable<string>;
    keycloakSignal: Signal<KeycloakEvent> = inject(KEYCLOAK_EVENT_SIGNAL);
    private tokenUpdateSignals: KeycloakEventType[] =
        [KeycloakEventType.AuthRefreshSuccess, KeycloakEventType.AuthSuccess, KeycloakEventType.Ready];

    constructor(private readonly keycloak: Keycloak) {
        this.token$ = toObservable(this.keycloakSignal)
            .pipe(
                filter((event: KeycloakEvent) => this.tokenUpdateSignals.includes(event.type)),
                map(_ => this.keycloak.token),
                filter((token: string | undefined) => token != undefined),
                distinctUntilChanged(),
                shareReplay(1),
            );
    }

    init() {
        return () => firstValueFrom(this.token$);
    }

    getKeycloakToken(): Observable<String> {
        return this.token$;
    }
}
