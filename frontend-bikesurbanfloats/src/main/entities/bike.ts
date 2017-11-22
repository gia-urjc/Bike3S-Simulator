export class Bike extends Entity {
    constructor(private reserved: boolean){}
    
    isReserved() {
        return this.reserved;
    }
  
}
