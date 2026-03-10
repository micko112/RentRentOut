import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from './services/auth.service';
import {inject} from '@angular/core';

export const authGuard: CanActivateFn = (route, state) =>{
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = localStorage.getItem('authToken');

  if(token){
    return true;
  } else {
    router.navigate(['/login'], {queryParams: {redirect: state.url}});
    return false;
  }

}
