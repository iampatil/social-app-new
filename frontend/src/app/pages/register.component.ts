import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="card">
      <h2>Create account</h2>
      <input placeholder="Username (3+ chars)" [(ngModel)]="username" name="u">
      <input type="email" placeholder="Email" [(ngModel)]="email" name="e">
      <input type="password" placeholder="Password (6+ chars)" [(ngModel)]="password" name="p">
      @if (error) { <div class="error">{{ error }}</div> }
      <button (click)="submit()">Register</button>
      <p class="meta">Already registered? <a routerLink="/login">Log in</a></p>
    </div>`
})
export class RegisterComponent {
  username = ''; email = ''; password = ''; error = '';
  constructor(private auth: AuthService, private router: Router) {}
  submit() {
    this.auth.register(this.username, this.email, this.password).subscribe({
      next: () => this.router.navigate(['/feed']),
      error: e => this.error = e.status === 409 ? (e.error?.message ?? 'Already exists') : 'Please check your inputs'
    });
  }
}
