import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from '../../ObserverPattern';
import { RentalsAndReturnsInfo } from './RentalsAndReturnsInfo';
import { Info } from '../Info';

export class RentalsAndReturnsPerUser implements Observer, Info {
    private users: Array<User>;
    private rentalsAndReturns: RentalsAndReturnsInfo;
    
    public constructor(users: Array<User>) {
        this.users = users;
        this.rentalsAndReturns = new RentalsAndReturnsInfo();
    }
  
    public async init() {
        try {
            await this.rentalsAndReturns.initData(this.users);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }

    public static async create(users: Array<User>) {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser(users);
        try {
            await rentalsAndReturnsValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
        return rentalsAndReturnsValues;
    }
  
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let key: number;

        for(let event of events) {
            key = event.changes.users[0].id;
            
            switch(event.name) { 
                case 'EventUserArrivesAtStationToRentBikeWithReservation': {
                    this.rentalsAndReturns.increaseSuccessfulRentals(key);
                    break;
                }
                
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    this.rentalsAndReturns.increaseSuccessfulReturns(key);
                    break;
                }
            
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.rentalsAndReturns.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.rentalsAndReturns.increaseFailedRentals(key);
                    }
                    break;
                }
    
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.rentalsAndReturns.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.rentalsAndReturns.increaseFailedRentals(key);
                    }
                    break;
                }
            }
        }
    }

    public getRentalsAndReturns(): RentalsAndReturnsInfo {
        return this.rentalsAndReturns;
    }
      
}