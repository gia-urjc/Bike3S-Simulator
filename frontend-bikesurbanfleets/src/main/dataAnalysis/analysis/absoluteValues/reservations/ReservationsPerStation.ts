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
    
<<<<<<< HEAD
    public async init() {
        try {
            this.data.init(this.basicData);
=======
    public async init(path: string, schemaPath?: string | null): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path, schemaPath);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
        
            for(let station of this.stations) {
                this.bikeFailedReservationsPerStation.set(station.id, 0);
                this.slotFailedReservationsPerStation.set(station.id, 0);            
                this.bikeSuccessfulReservationsPerStation.set(station.id, 0);
                this.slotSuccessfulReservationsPerStation.set(station.id, 0);
            }
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
   
<<<<<<< HEAD
    public static async create(stations: Array<Station>): Promise<ReservationsPerStation> {
        let reservationValues = new ReservationsPerStation(stations);
        try {
            await reservationValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
=======
    public static async create(path: string, schemaPath?: string | null): Promise<ReservationsPerStation> {
        let reservationValues = new ReservationsPerStation();
        await reservationValues.init(path, schemaPath);
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
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
