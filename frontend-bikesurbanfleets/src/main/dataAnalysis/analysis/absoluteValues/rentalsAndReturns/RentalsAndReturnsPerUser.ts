import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import { Observer } from '../../ObserverPattern';
import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';
import { RentalsAndReturnsData } from './RentalsAndReturnsData';

export class RentalsAndReturnsPerUser extends RentalsAndReturnsData {
    private users: Array<User>;
    
    public constructor() {
        super('USER');
    }
  
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
                
            await this.initData(users);
        }
        catch(error) {
            throw new Error('Error accessing to users: '+error);
        }
        return;
    }

    public static async create(path: string): Promise<RentalsAndReturnsPerUser> {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser();
        try {
            await rentalsAndReturnsValues.init(path);
        }
        catch(error) {
            throw new Error('Error initializing users of requested data: '+error);
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
                    this.increaseSuccessfulRentals(key);
                    break;
                }
                
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    this.increaseSuccessfulReturns(key);
                    break;
                }
            
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.increaseFailedRentals(key);
                    }
                    break;
                }
    
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.increaseFailedRentals(key);
                    }
                    break;
                }
            }
        }
    }
      
}