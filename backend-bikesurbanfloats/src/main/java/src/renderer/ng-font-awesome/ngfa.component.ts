import { Component, ElementRef, Input, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

type FaSize = '1x' | 'lg' | '2x' | '3x' | '4x' | '5x';
type FaAnimation = 'none' | 'spin' | 'pulse';

@Component({
    selector: 'fa',
    styles: [':host { display: inline-block }'],
    template: `<span class="fa fa-{{icon}} fa-{{size}} fa-{{animation}}"
                     [ngClass]="{ 'fa-fw': fixed }" [style.transform]="transform()"></span>`
})
export class FaIcon {
    @Input() icon: string;
    @Input() size: FaSize = '1x';
    @Input() fixed = false;
    @Input() animation: FaAnimation = 'none';
    @Input() rotate = 0;
    @Input() flipHorizontal = false;
    @Input() flipVertical = false;

    constructor(protected sanitizer: DomSanitizer) {}

    transform() {
        if (this.animation !== 'none') return 'none';

        let r = (360 + this.rotate) % 360;
        let rotate = r ? `rotate(${r}deg) ` : '';

        let h = this.flipHorizontal ? -1 : 1;
        let v = this.flipVertical ? -1 : 1;
        let scale = h + v < 2 ? `scale(${h}, ${v})` : '';

        return this.sanitizer.bypassSecurityTrustStyle(`${rotate}${scale}` || 'none');
    }
}

@Component({
    selector: 'fa-li',
    styles: [':host { display: list-item; position: relative }'],
    template: `<span class="fa fa-li fa-{{icon}} fa-{{size}} fa-{{animation}}"
                     [ngClass]="{ 'fa-fw': fixed }" [style.transform]="transform()"></span>
    <ng-content></ng-content>`
})
export class FaListIcon extends FaIcon implements OnInit {

    constructor(protected sanitizer: DomSanitizer, private element: ElementRef) {
        super(sanitizer);
    }

    ngOnInit() {
        let fa = this.element.nativeElement;
        if (fa.parentElement.tagName.toLowerCase() === 'ul') {
            fa.parentElement.classList.add('fa-ul');
        }
    }
}
