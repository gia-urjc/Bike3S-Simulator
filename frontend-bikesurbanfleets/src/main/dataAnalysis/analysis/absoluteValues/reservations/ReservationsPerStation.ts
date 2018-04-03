import  { Station, Reservation } from '../../../systemDataTypes/Entities';
import { Observer } from '../../ObserverPattern';
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { ReservationData } from './ReservationData';

export class ReservationsPerStation implements SystemInfo, Observer {
    basicData: Array<Station>;
    data: Data;
    
    public constructor(stations: Array<Station>) {
        this.basicData = stations;
        this.data = new ReservationData();
    }
    
    public async init() {
        try {
            this.data.init(this.basicData);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
   
    public static async create(stations: Array<Station>): Promise<ReservationsPerStation> {
        let reservationValues = new ReservationsPerStation(stations);
        try {
            await reservationValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
        return reservationValues;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.station.id;
        
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
