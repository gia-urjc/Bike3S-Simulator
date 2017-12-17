export abstract class Entity {
    constructor(private $id: number) {}

    get id() {
        return this.$id;
    }
}

export interface VisualOptions {
    fromJson: string,
}

export const EntityMetaKey = Symbol('entity-meta-key');

export function VisualEntity<J>(options: VisualOptions) {
    return function <E extends Entity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(EntityMetaKey, options, Target);
        return Target;
    }
}
