import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {ConfigService} from './config.service';
import {map} from 'rxjs/operators';
import { User } from '../models/user';
import { HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  currentUser = null;
  userRole : any = null;

  constructor(
    private apiService: ApiService,
    private config: ConfigService
  ) {
  }

  getMyInfo() {
    return this.apiService.get(this.config.whoami_url)
      .pipe(map(user => {
        this.currentUser = user;
        return user;
      }));
  }

  getOne(id:string) {
    return this.apiService.get(this.config.users_url + '/' + id);
  }

  getAll() {
    return this.apiService.get(this.config.users_url);
  }

  delete(user : User) {
    return this.apiService.post(this.config.user_delete_url, user);
  }

  addNewAdmin(user) {
    const signupHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    return this.apiService.post(this.config.admin_url, JSON.stringify(user), signupHeaders)
      .pipe(map(() => {
        console.log('New admin created');
      }));
  }

  addNewUser(user) {
    const signupHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    return this.apiService.post(this.config.user_url, JSON.stringify(user), signupHeaders)
      .pipe(map(() => {
        console.log('New user created');
      }));
  }

}
