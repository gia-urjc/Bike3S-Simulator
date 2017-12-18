import { HistoryEntity } from '../../../../shared/history';

export abstract class Entity {
    constructor(private $id: number) {}

    get id() {
        return this.$id;
    }
}

export const EntityMetaKey = Symbol('entity-meta-key');

export interface VisualOptions {
    fromJson: string,
}

export function VisualEntity(options: VisualOptions) {
    return function <E extends Entity, J extends HistoryEntity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(EntityMetaKey, options, Target);
        return Target;
    }
}
