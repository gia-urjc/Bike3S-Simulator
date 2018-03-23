import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from "../../ObserverPattern";
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { RentalAndReturnData } from './RentalAndReturnData';

export class RentalsAndReturnsPerUser implements SystemInfo, Observer {
    basicData: Array<User>;
    data: RentalAndReturnData;
    
    public constructor(users: Array<User>) {
        this.basicData = users;
        this.data = new RentalAndReturnData();
    }
<<<<<<< HEAD
  
    public async init() {
        try {
            await this.data.initData(this.basicData);
=======
    
    public async init(path: string, schemaPath?:string | null): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path, schemaPath);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
                
            for(let user of this.users) {
                this.bikeFailedRentalsPerUser.set(user.id, 0);
                this.bikeSuccessfulRentalsPerUser.set(user.id, 0);
                this.bikeFailedReturnsPerUser.set(user.id, 0);            
                this.bikeSuccessfulReturnsPerUser.set(user.id, 0);            
            }
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }

<<<<<<< HEAD
    public static async create(users: Array<User>) {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser(users);
        try {
            await rentalsAndReturnsValues.init();
=======
    public static async create(path: string, schemaPath?: string | null): Promise<RentalsAndReturnsPerUser> {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser();
        try {
            await rentalsAndReturnsValues.init(path, schemaPath);
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
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
                    this.data.increaseSuccessfulRentals(key);
                    break;
                }
                
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    this.data.increaseSuccessfulReturns(key);
                    break;
                }
            
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.data.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.data.increaseFailedRentals(key);
                    }
                    break;
                }
    
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.data.increaseSuccessfulReturns(key);
                    }
                    else {
                      this.data.increaseFailedReturns(key);
                    }
                    break;
                }
            }
        }
    }

    public getData(): RentalAndReturnData {
        return this.data;
    }
      
}