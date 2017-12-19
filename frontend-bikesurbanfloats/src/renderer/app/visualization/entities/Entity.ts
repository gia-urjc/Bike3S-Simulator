export abstract class Entity {
    constructor(private $id: number) {}

    get id() {
        return this.$id;
    }
}
