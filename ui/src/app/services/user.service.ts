import { inject, Injectable } from '@angular/core';
import { UserControllerApiService } from '@anna/flow-board-api';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private userController: UserControllerApiService = inject(UserControllerApiService);

  public user = toSignal(this.userController.getCurrentUser());
}
