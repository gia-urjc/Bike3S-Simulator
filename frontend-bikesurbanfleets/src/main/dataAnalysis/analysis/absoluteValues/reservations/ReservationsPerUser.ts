import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { Observer } from '../../ObserverPattern';
import { ReservationsInfo } from './ReservationsInfo';

export class ReservationsPerUser implements Observer {
    private users: Array<User>;
    private reservations: ReservationsInfo;
    
    public constructor(users: Array<User>) {
        this.users = users;
        this.reservations = new ReservationsInfo('USER');
    }
    
    public async init(): Promise<void> {
        try {
            this.reservations.initData(this.users);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
   
    public static async create(): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser();
        try {
            await reservationValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data'+error);
        }
        return reservationValues;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        
        switch (reservation.type) { 
            case 'BIKE': { 
                if (reservation.state === 'FAILED') {
                    this.reservations.increaseFailedBikeReservations(key);
                }
                else {
                    this.reservations.increaseSuccessfulBikeReservations(key);
                }
                break;
            }
                
            case 'SLOT': { 
                if (reservation.state === 'FAILED') {
                    this.reservations.increaseFailedSlotReservations(key);
                }
                else {
                    this.reservations.increaseSuccessfulSlotReservations(key);
                }
                break;
            }
                
            default: 
                throw new Error('Reservation type not identified');
        }
    }
    
    public getReservations(): ReservationsInfo {
        return this.reservations;
    }
              
}     
        
