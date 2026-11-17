import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="card">
      <h2>Log in</h2>
      <input placeholder="Username" [(ngModel)]="username" name="u">
      <input type="password" placeholder="Password" [(ngModel)]="password" name="p" (keyup.enter)="submit()">
      @if (error) { <div class="error">{{ error }}</div> }
      <button (click)="submit()">Log in</button>
      <p class="meta">No account? <a routerLink="/register">Register</a></p>
    </div>`
})
export class LoginComponent {
  username = ''; password = ''; error = '';
  constructor(private auth: AuthService, private router: Router) {}
  submit() {
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/feed']),
      error: () => this.error = 'Invalid username or password'
    });
  }
}
