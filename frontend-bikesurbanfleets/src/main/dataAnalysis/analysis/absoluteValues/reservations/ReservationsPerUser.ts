import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { Observer } from '../../ObserverPattern';
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { ReservationData } from './ReservationData';

export class ReservationsPerUser implements SystemInfo, Observer {
    basicData: Array<User>;
    data: Data;
    
    public constructor(users: Array<User>) {
        this.basicData = users;
        this.data = new ReservationData();
    }
    
<<<<<<< HEAD
    public async init(): Promise<void> {
        try {
            this.data.init(this.basicData);
=======
    public async init(path: string, schemaPath?: string | null): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path, schemaPath);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
              
            for(let user of this.users) {
                this.bikeFailedReservationsPerUser.set(user.id, 0);
                this.slotFailedReservationsPerUser.set(user.id, 0);
                this.bikeSuccessfulReservationsPerUser.set(user.id, 0);
                this.slotSuccessfulReservationsPerUser.set(user.id, 0);
            }
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
   
<<<<<<< HEAD
    public static async create(users: Array<User>): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser(users);
        try {
            await reservationValues.init();
=======
    public static async create(path: string, schemaPath?: string | null): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser();
        try {
            await reservationValues.init(path, schemaPath);
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
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
      
