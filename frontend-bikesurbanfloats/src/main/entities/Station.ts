import {Entity} from "./Entity"

export class Station extends Entity{

    private capacity: number;

    constructor(id:number, capacity:number){
        super(id);
        this.capacity = capacity;
    }

}