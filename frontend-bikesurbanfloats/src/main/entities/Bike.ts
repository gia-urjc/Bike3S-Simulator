import { Entity } from './Entity';

export class Bike extends Entity {

    private reserved: boolean;

    constructor(id: number, reserved: boolean) {
        super(id);
    }

    isReserved() {
        return this.reserved;
    }

}
