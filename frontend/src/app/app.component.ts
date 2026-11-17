import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <div class="container">
      @if (auth.isLoggedIn) {
        <div class="card row">
          <strong>Social</strong>
          <span>
            <a routerLink="/feed">Feed</a> ·
            <a routerLink="/friends">Friends</a> ·
            <a routerLink="/people">People</a> ·
            <a [routerLink]="['/profile', auth.userId]">Profile</a>
          </span>
          <span class="meta">&#10010;{{ auth.username }} <button class="ghost" (click)="auth.logout()">Logout</button></span>
        </div>
      }
      <router-outlet />
    </div>`
})
export class AppComponent {
  constructor(public auth: AuthService) {}
}
