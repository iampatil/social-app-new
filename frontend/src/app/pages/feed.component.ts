import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../api.service';
import { AuthService } from '../auth.service';
import { Comment, Post } from '../models';

interface PostView extends Post { showComments?: boolean; comments?: Comment[]; newComment?: string; }

@Component({
  standalone: true,
  imports: [FormsModule, DatePipe],
  template: `
    <div class="card">
      <textarea rows="3" maxlength="1000" placeholder="What's happening?" [(ngModel)]="draft"></textarea>
      <button (click)="publish()" [disabled]="!draft.trim()">Post</button>
    </div>

    @for (p of posts; track p.id) {
      <div class="card">
        <div class="row">
          <strong>&#64;{{ p.authorUsername }}</strong>
          <span class="meta">{{ p.createdAt | date:'medium' }}</span>
        </div>
        <p>{{ p.content }}</p>
        <div class="row">
          <span>
            <button class="ghost" [class.active]="p.likedByMe" (click)="like(p)">
              {{ p.likedByMe ? '♥' : '♡' }} {{ p.likeCount }}
            </button>
            <button class="ghost" (click)="toggleComments(p)">💬 {{ p.commentCount }}</button>
          </span>
          @if (p.authorId === auth.userId) { <button class="ghost" (click)="remove(p)">Delete</button> }
        </div>

        @if (p.showComments) {
          <hr>
          @for (c of p.comments ?? []; track c.id) {
            <div class="meta"><strong>&#64;{{ c.authorUsername }}</strong>: {{ c.text }}</div>
          }
          <div class="row" style="margin-top:8px">
            <input placeholder="Write a comment" [(ngModel)]="p.newComment" (keyup.enter)="comment(p)" style="margin:0">
            <button class="secondary" (click)="comment(p)">Send</button>
          </div>
        }
      </div>
    } @empty {
      <p class="meta">Nothing here yet. Post something or follow people from the People tab.</p>
    }`
})
export class FeedComponent implements OnInit {
  posts: PostView[] = [];
  draft = '';
  constructor(private api: ApiService, public auth: AuthService) {}

  ngOnInit() { this.load(); }
  load() { this.api.feed().subscribe(p => this.posts = p); }

  publish() {
    this.api.createPost(this.draft.trim()).subscribe(p => { this.posts.unshift(p); this.draft = ''; });
  }
  remove(p: PostView) {
    this.api.deletePost(p.id).subscribe(() => this.posts = this.posts.filter(x => x.id !== p.id));
  }
  like(p: PostView) {
    this.api.toggleLike(p.id).subscribe(s => { p.likeCount = s.likeCount; p.likedByMe = s.likedByMe; });
  }
  toggleComments(p: PostView) {
    p.showComments = !p.showComments;
    if (p.showComments) this.api.comments(p.id).subscribe(c => p.comments = c);
  }
  comment(p: PostView) {
    const text = p.newComment?.trim();
    if (!text) return;
    this.api.addComment(p.id, text).subscribe(c => {
      p.comments = [...(p.comments ?? []), c];
      p.commentCount++;
      p.newComment = '';
    });
  }
}
