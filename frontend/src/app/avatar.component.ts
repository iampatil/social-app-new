import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-avatar',
  standalone: true,
  template: `
    @if (url) {
      <img class="avatar" [src]="url" [style.width.px]="size" [style.height.px]="size" alt="">
    } @else {
      <span class="avatar" [style.width.px]="size" [style.height.px]="size" [style.font-size.px]="size / 2">{{ initial }}</span>
    }`
})
export class AvatarComponent {
  @Input() name = '';
  @Input() url?: string | null;
  @Input() size = 40;
  get initial(): string { return (this.name || '?').charAt(0).toUpperCase(); }
}
