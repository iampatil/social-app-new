import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';
import { LoginComponent } from './pages/login.component';
import { RegisterComponent } from './pages/register.component';
import { FeedComponent } from './pages/feed.component';
import { PeopleComponent } from './pages/people.component';
import { FriendsComponent } from './pages/friends.component';
import { ProfileComponent } from './pages/profile.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'feed', component: FeedComponent, canActivate: [authGuard] },
  { path: 'people', component: PeopleComponent, canActivate: [authGuard] },
  { path: 'friends', component: FriendsComponent, canActivate: [authGuard] },
  { path: 'profile/:id', component: ProfileComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'feed' },
  { path: '**', redirectTo: 'feed' }
];
