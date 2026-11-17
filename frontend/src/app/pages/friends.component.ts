import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../api.service';
import { AvatarComponent } from '../avatar.component';
import { UserCard } from '../models';

@Component({
  standalone: true,
  imports: [RouterLink, AvatarComponent],
  template: `
    @if (requests.length) {
      <div class="card">
        <h3>Friend requests ({{ requests.length }})</h3>
        @for (c of requests; track c.id) {
          <div class="row" style="margin-bottom:10px">
            <div class="person">
              <app-avatar [name]="c.displayName" [url]="c.avatarUrl" />
              <div><a [routerLink]="['/profile', c.id]">{{ c.displayName }}</a>
                <div class="meta">{{ c.mutualFriends }} mutual friends</div></div>
            </div>
            <div class="actions">
              <button (click)="run(api.acceptRequest(c.id))">Confirm</button>
              <button class="secondary" (click)="run(api.declineRequest(c.id))">Delete</button>
            </div>
          </div>
        }
      </div>
    }

    <div class="card">
      <h3>People you may know</h3>
      @for (c of suggestions; track c.id) {
        <div class="row" style="margin-bottom:10px">
          <div class="person">
            <app-avatar [name]="c.displayName" [url]="c.avatarUrl" />
            <div><a [routerLink]="['/profile', c.id]">{{ c.displayName }}</a>
              @if (c.mutualFriends) { <div class="meta">{{ c.mutualFriends }} mutual friends</div> }</div>
          </div>
          <button (click)="run(api.sendRequest(c.id))">Add friend</button>
        </div>
      } @empty { <p class="meta">No suggestions right now.</p> }
    </div>

    @if (sent.length) {
      <div class="card">
        <h3>Sent requests</h3>
        @for (c of sent; track c.id) {
          <div class="row" style="margin-bottom:10px">
            <div class="person"><app-avatar [name]="c.displayName" [url]="c.avatarUrl" />
              <a [routerLink]="['/profile', c.id]">{{ c.displayName }}</a></div>
            <button class="secondary" (click)="run(api.declineRequest(c.id))">Cancel</button>
          </div>
        }
      </div>
    }

    <div class="card">
      <h3>Friends ({{ friends.length }})</h3>
      @for (c of friends; track c.id) {
        <div class="row" style="margin-bottom:10px">
          <div class="person"><app-avatar [name]="c.displayName" [url]="c.avatarUrl" />
            <a [routerLink]="['/profile', c.id]">{{ c.displayName }}</a></div>
          <button class="secondary" (click)="run(api.unfriend(c.id))">Unfriend</button>
        </div>
      } @empty { <p class="meta">You have no friends yet. Send a few requests!</p> }
    </div>`
})
export class FriendsComponent implements OnInit {
  requests: UserCard[] = []; sent: UserCard[] = []; suggestions: UserCard[] = []; friends: UserCard[] = [];
  constructor(public api: ApiService) {}

  ngOnInit() { this.load(); }

  load() {
    forkJoin({ r: this.api.requests(), s: this.api.sentRequests(), g: this.api.suggestions(), f: this.api.friends() })
      .subscribe(x => { this.requests = x.r; this.sent = x.s; this.suggestions = x.g; this.friends = x.f; });
  }

  run(call: import('rxjs').Observable<unknown>) { call.subscribe(() => this.load()); }
}
