import {Entity} from './Entity'
import {GeoPoint} from "../../shared/util";
import {Bike} from "./Bike";

export class Station extends Entity {

    private capacity: number;
    private position: GeoPoint;
    private reservedBikes: number;
    private reservedSlots: number;
    private bikes: Bike[];

    constructor(id: number, capacity: number, position: GeoPoint, reservedBikes: number, reservedSlots: number, bikes: Bike[]) {
        super(id);
        this.capacity = capacity;
        this.position = position;
        this.reservedBikes = reservedBikes;
        this.reservedSlots = reservedSlots;
        this.bikes = bikes;
    }

    getCapacity() {
        return this.capacity;
    }

    getPosition() {
        return this.position;
    }

    getReservedBikes() {
        return this.reservedBikes;
    }

    getReservedSlots() {
        return this.reservedSlots;
    }

    getBikes() {
        return this.bikes;
    }

    availableBikes() {}

    availableSlots() {}

}
