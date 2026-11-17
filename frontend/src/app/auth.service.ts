import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../environments/environment';
import { AuthResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = environment.apiUrl;
  constructor(private http: HttpClient, private router: Router) {}

  get token(): string | null { return localStorage.getItem('token'); }
  get username(): string | null { return localStorage.getItem('username'); }
  get userId(): number { return Number(localStorage.getItem('userId')); }
  get isLoggedIn(): boolean { return !!this.token; }

  login(username: string, password: string) {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, { username, password }).pipe(tap(r => this.store(r)));
  }
  register(username: string, email: string, password: string) {
    return this.http.post<AuthResponse>(`${this.api}/auth/register`, { username, email, password }).pipe(tap(r => this.store(r)));
  }
  logout() { localStorage.clear(); this.router.navigate(['/login']); }

  private store(r: AuthResponse) {
    localStorage.setItem('token', r.token);
    localStorage.setItem('username', r.username);
    localStorage.setItem('userId', String(r.id));
  }
}
