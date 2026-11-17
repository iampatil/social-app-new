import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api.service';
import { AuthService } from '../auth.service';
import { AvatarComponent } from '../avatar.component';
import { Profile, friendLabel } from '../models';

@Component({
  standalone: true,
  imports: [FormsModule, DatePipe, AvatarComponent],
  template: `
    @if (notFound) { <div class="card">This profile isn't available.</div> }

    @if (p; as pr) {
      <div class="card" style="padding:0; overflow:hidden">
        @if (pr.coverUrl) { <img class="cover" [src]="pr.coverUrl" alt=""> } @else { <div class="cover"></div> }
        <div style="padding:16px">
          <div class="row">
            <div class="person">
              <app-avatar [name]="pr.displayName" [url]="pr.avatarUrl" [size]="88" />
              <div>
                <h2 style="margin:0">{{ pr.displayName }}</h2>
                <div class="meta">&#64;{{ pr.username }}</div>
                @if (!pr.restricted) {
                  <div class="meta">{{ pr.friendCount }} friends · {{ pr.followers }} followers · {{ pr.following }} following</div>
                }
                @if (pr.relationship !== 'SELF' && pr.mutualFriends) {
                  <div class="meta">{{ pr.mutualFriends }} mutual friends</div>
                }
              </div>
            </div>
            <div class="actions">
              @if (pr.relationship === 'SELF') {
                <button class="secondary" (click)="edit = !edit">{{ edit ? 'Cancel' : 'Edit profile' }}</button>
              } @else {
                <button [class.secondary]="pr.relationship !== 'NONE'" (click)="friendAction(pr)">{{ label(pr) }}</button>
                <button class="secondary" (click)="toggleFollow(pr)">{{ pr.followedByMe ? 'Unfollow' : 'Follow' }}</button>
                <button class="danger" (click)="block(pr)">Block</button>
              }
            </div>
          </div>

          @if (pr.restricted) {
            <p class="meta" style="margin-top:14px">🔒 Only friends can see more of this profile.</p>
          } @else {
            @if (pr.bio) { <p>{{ pr.bio }}</p> }
            @if (pr.location) { <div class="meta">📍 {{ pr.location }}</div> }
            @if (pr.birthday) { <div class="meta">🎂 {{ pr.birthday | date:'longDate' }}</div> }
          }
        </div>
      </div>

      @if (edit && pr.relationship === 'SELF') {
        <div class="card">
          <h3>Edit profile</h3>
          <input placeholder="Display name" [(ngModel)]="form.displayName" name="dn">
          <textarea rows="3" placeholder="Bio" [(ngModel)]="form.bio" name="bio"></textarea>
          <input placeholder="Location" [(ngModel)]="form.location" name="loc">
          <input placeholder="Avatar image URL (https://...)" [(ngModel)]="form.avatarUrl" name="av">
          <input placeholder="Cover image URL (https://...)" [(ngModel)]="form.coverUrl" name="cv">
          <label class="meta">Birthday</label>
          <input type="date" [(ngModel)]="form.birthday" name="bd">
          <label class="meta">Who can see my profile details?</label>
          <select [(ngModel)]="form.visibility" name="vis">
            <option value="PUBLIC">Public</option>
            <option value="FRIENDS">Friends only</option>
            <option value="ONLY_ME">Only me</option>
          </select>
          @if (error) { <div class="error">{{ error }}</div> }
          <button (click)="save()">Save</button>
        </div>
      }
    }`
})
export class ProfileComponent implements OnInit {
  p: Profile | null = null;
  notFound = false; edit = false; error = '';
  form = { displayName: '', bio: '', location: '', avatarUrl: '', coverUrl: '', birthday: '', visibility: 'PUBLIC' };

  constructor(private route: ActivatedRoute, private api: ApiService, public auth: AuthService) {}

  ngOnInit() { this.route.paramMap.subscribe(m => this.load(Number(m.get('id')))); }

  load(id: number) {
    this.notFound = false;
    this.api.profile(id).subscribe({
      next: p => {
        this.p = p;
        this.form = { displayName: p.displayName, bio: p.bio ?? '', location: p.location ?? '', avatarUrl: p.avatarUrl ?? '',
          coverUrl: p.coverUrl ?? '', birthday: p.birthday ?? '', visibility: p.visibility };
      },
      error: () => { this.p = null; this.notFound = true; }
    });
  }

  label(p: Profile) { return friendLabel(p.relationship); }
  friendAction(p: Profile) { this.api.relationAction(p.relationship, p.id).subscribe(() => this.load(p.id)); }
  toggleFollow(p: Profile) {
    (p.followedByMe ? this.api.unfollow(p.id) : this.api.follow(p.id)).subscribe(() => this.load(p.id));
  }
  block(p: Profile) {
    if (confirm(`Block ${p.displayName}? You will be unfriended and unable to see each other.`))
      this.api.block(p.id).subscribe(() => { this.p = null; this.notFound = true; });
  }
  save() {
    this.error = '';
    this.api.updateProfile({ ...this.form, birthday: this.form.birthday || null }).subscribe({
      next: p => { this.p = p; this.edit = false; },
      error: () => this.error = 'Please check your inputs (image URLs must start with http:// or https://).'
    });
  }
}
