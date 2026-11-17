export type Relationship = 'SELF' | 'FRIENDS' | 'REQUEST_SENT' | 'REQUEST_RECEIVED' | 'NONE';
export type Visibility = 'PUBLIC' | 'FRIENDS' | 'ONLY_ME';

export interface AuthResponse { token: string; id: number; username: string; role: string; }
export interface User { id: number; username: string; displayName?: string; avatarUrl?: string | null; bio?: string | null;
  followers: number; following: number; followedByMe: boolean; relationship?: Relationship; }
export interface UserCard { id: number; username: string; displayName: string; avatarUrl?: string | null; mutualFriends: number; }
export interface Profile { id: number; username: string; displayName: string; avatarUrl?: string | null; coverUrl?: string | null;
  bio?: string | null; location?: string | null; birthday?: string | null; visibility: Visibility;
  friendCount: number; followers: number; following: number; mutualFriends: number;
  relationship: Relationship; followedByMe: boolean; restricted: boolean; }
export interface Post { id: number; authorId: number; authorUsername: string; content: string; createdAt: string;
  likeCount: number; likedByMe: boolean; commentCount: number; }
export interface Comment { id: number; authorId: number; authorUsername: string; text: string; createdAt: string; }
export interface LikeState { likeCount: number; likedByMe: boolean; }

export function friendLabel(rel?: Relationship): string {
  switch (rel) {
    case 'FRIENDS': return 'Unfriend';
    case 'REQUEST_SENT': return 'Cancel request';
    case 'REQUEST_RECEIVED': return 'Accept request';
    default: return 'Add friend';
  }
}
