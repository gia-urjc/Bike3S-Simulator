import { TimeEntry } from '../../../dataTypes/SystemInternalData';
export class BikeFailedRentals {
    private bikeFailedRentalsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.bikeFailedRentalsPerUser;
    }
     
     public update(timeEntry: TimeEntry): void {
         
     }
    
}

export class BikeSuccessfulRentls { 
    private bikeSuccessfulRentalsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.bikeSuccessfulRentalsPerUser;
    }
    
    public update(timeEntry: TimeEntry): void {
        
    }
    
}

export class BikeFailedReturns { 
    private bikeFailedReturnsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.bikeFailedReturnsPerUser;
    }
    
    public update(timeEntry: TimeEntry): void {
        
    }
        
}

export class BikeSuccessfulReturns {
    private bikeSuccessfulReturnsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.bikeSuccessfulReturnsPerUser;
    }
    
    public update(timeEntry: TimeEntry): void {
        
    }
    
}   
