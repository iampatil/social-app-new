import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { Comment, LikeState, Post, Profile, Relationship, User, UserCard } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private api = environment.apiUrl;
  constructor(private http: HttpClient) {}

  feed(page = 0) { return this.http.get<Post[]>(`${this.api}/posts/feed?page=${page}`); }
  createPost(content: string) { return this.http.post<Post>(`${this.api}/posts`, { content }); }
  deletePost(id: number) { return this.http.delete<void>(`${this.api}/posts/${id}`); }
  toggleLike(id: number) { return this.http.post<LikeState>(`${this.api}/posts/${id}/like`, {}); }
  comments(id: number) { return this.http.get<Comment[]>(`${this.api}/posts/${id}/comments`); }
  addComment(id: number, text: string) { return this.http.post<Comment>(`${this.api}/posts/${id}/comments`, { text }); }

  searchUsers(q: string) { return this.http.get<User[]>(`${this.api}/users/search?q=${encodeURIComponent(q)}`); }
  follow(id: number) { return this.http.post<User>(`${this.api}/users/${id}/follow`, {}); }
  unfollow(id: number) { return this.http.delete<User>(`${this.api}/users/${id}/follow`); }

  // ---- profile ----
  profile(id: number) { return this.http.get<Profile>(`${this.api}/users/${id}/profile`); }
  updateProfile(body: unknown) { return this.http.put<Profile>(`${this.api}/users/me/profile`, body); }

  // ---- friends ----
  friends() { return this.http.get<UserCard[]>(`${this.api}/friends`); }
  requests() { return this.http.get<UserCard[]>(`${this.api}/friends/requests`); }
  sentRequests() { return this.http.get<UserCard[]>(`${this.api}/friends/requests/sent`); }
  suggestions() { return this.http.get<UserCard[]>(`${this.api}/friends/suggestions`); }
  sendRequest(id: number) { return this.http.post(`${this.api}/friends/requests/${id}`, {}); }
  acceptRequest(id: number) { return this.http.post(`${this.api}/friends/requests/${id}/accept`, {}); }
  declineRequest(id: number) { return this.http.delete(`${this.api}/friends/requests/${id}`); }
  unfriend(id: number) { return this.http.delete(`${this.api}/friends/${id}`); }
  blocked() { return this.http.get<UserCard[]>(`${this.api}/blocks`); }
  block(id: number) { return this.http.post<void>(`${this.api}/blocks/${id}`, {}); }
  unblock(id: number) { return this.http.delete<void>(`${this.api}/blocks/${id}`); }

  /** One button, four behaviours depending on the current relationship. */
  relationAction(rel: Relationship | undefined, id: number): Observable<unknown> {
    switch (rel) {
      case 'REQUEST_SENT': return this.declineRequest(id);
      case 'REQUEST_RECEIVED': return this.acceptRequest(id);
      case 'FRIENDS': return this.unfriend(id);
      default: return this.sendRequest(id);
    }
  }
}
