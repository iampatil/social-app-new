import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { AvatarComponent } from '../avatar.component';
import { User, friendLabel } from '../models';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink, AvatarComponent],
  template: `
    <div class="card">
      <input placeholder="Search people" [(ngModel)]="q" (ngModelChange)="search()">
    </div>
    @for (u of users; track u.id) {
      <div class="card row">
        <div class="person">
          <app-avatar [name]="u.displayName || u.username" [url]="u.avatarUrl" />
          <div>
            <a [routerLink]="['/profile', u.id]">{{ u.displayName || u.username }}</a>
            <div class="meta">&#64;{{ u.username }} · {{ u.followers }} followers</div>
          </div>
        </div>
        <div class="actions">
          <button [class.secondary]="u.relationship !== 'NONE'" (click)="friendAction(u)">{{ label(u) }}</button>
          <button class="secondary" (click)="toggleFollow(u)">{{ u.followedByMe ? 'Unfollow' : 'Follow' }}</button>
        </div>
      </div>
    } @empty {
      <p class="meta">No users found.</p>
    }`
})
export class PeopleComponent implements OnInit {
  q = ''; users: User[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() { this.search(); }
  search() { this.api.searchUsers(this.q).subscribe(u => this.users = u); }
  label(u: User) { return friendLabel(u.relationship); }
  friendAction(u: User) { this.api.relationAction(u.relationship, u.id).subscribe(() => this.search()); }
  toggleFollow(u: User) {
    const call = u.followedByMe ? this.api.unfollow(u.id) : this.api.follow(u.id);
    call.subscribe(updated => Object.assign(u, updated));
  }
}
