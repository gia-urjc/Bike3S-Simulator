import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { Observer } from '../../ObserverPattern';
import { Data } from "../../Data";
import { SystemInfo } from "../../SystemInfo";
import { ReservationData } from './ReservationData';

export class ReservationsPerUser implements SystemInfo, Observer {
    basicData: Array<User>;
    data: Data;

    public static async create(users: Array<User>): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser(users);
        try {
            await reservationValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data'+error);
        }
        return reservationValues;
    }

    public constructor(users: Array<User>) {
        this.basicData = users;
        this.data = new ReservationData();
    }
    
    public async init(): Promise<void> {
        try {
            this.data.init(this.basicData);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        
        switch (reservation.type) { 
            case 'BIKE': { 
                if (reservation.state === 'FAILED') {
                    this.data.increaseFailedBikeReservations(key);
                }
                else {
                    this.data.increaseSuccessfulBikeReservations(key);
                }
                break;
            }
                
            case 'SLOT': { 
                if (reservation.state === 'FAILED') {
                    this.data.increaseFailedSlotReservations(key);
                }
                else {
                    this.data.increaseSuccessfulSlotReservations(key);
                }
                break;
            }
                
            default: 
                throw new Error('Reservation type not identified');
        }
    }
    
    public getData(): Data {
        return this.data;
    }
              
}    
      
