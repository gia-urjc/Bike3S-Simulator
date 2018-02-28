import  { Station, Reservation } from '../../../systemDataTypes/Entities';
import { Observer } from '../../ObserverPattern';
import { ReservationsInfo } from './ReservationsInfo';

export class ReservationsPerStation implements Observer {
    private stations: Array<Station>;
    private reservations: ReservationsInfo;
    
    public constructor(stations: Array<Station>) {
        this.stations = stations;
        this.reservations = new ReservationsInfo();
    }
    
    public async init() {
        try {
            this.reservations.initData(this.stations);
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
