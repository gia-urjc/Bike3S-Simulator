import { Entity } from '../entities/Entity';

export interface VisualOptions {
    fromJson: string,
}

export function Visual(options: VisualOptions) {
    return function (target: any) {
        return new Proxy(target, {});
    }
}
