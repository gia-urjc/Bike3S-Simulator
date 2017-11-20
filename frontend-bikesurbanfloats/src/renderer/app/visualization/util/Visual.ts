import { Entity } from '../entities/Entity';

export function Visual(options: {
    fromJson: string,
}) {
    return function (target: any) {
        return new Proxy(target, {});
    }
}
