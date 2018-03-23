import { HistoryEntitiesJson } from "../../../../shared/history";
import { JsonObject } from "../../../../shared/util";
import { HistoryReader } from "../../../util";
import { Station, User, Entity } from "../../systemDataTypes/Entities";
import { AbsoluteValue } from "../absoluteValues/AbsoluteValue";
//import { Data } from "../absoluteValues/Data";
import { SystemInfo } from "../absoluteValues/SystemInfo";
import { RentalAndReturnAbsoluteValue } from "../absoluteValues/rentalsAndReturns/RentalAndReturnAbsoluteValue";
import { RentalAndReturnData } from "../absoluteValues/rentalsAndReturns/RentalAndReturnData";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationAbsoluteValue } from "../absoluteValues/reservations/ReservationAbsoluteValue";
import { ReservationData } from "../absoluteValues/reservations/ReservationData";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import * as json2csv from 'json2csv';
import * as fs from 'fs';
import {SystemGlobalValues} from '../SystemGlobalValues';

export class CsvGenerator {
  private titles: Array<string>;
<<<<<<< HEAD
  private stationData: Array<JsonObject>;
  private userData: Array<JsonObject>;
  private path: string;
=======
	private stationData: Array<JsonObject>;
	private userData: Array<JsonObject>;
    private path: string;
    private schemaPath: string | null;
    private globalValuesFields: Array<string>;
    private globalValues: JsonObject;
    private csvPath: string;
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
  
  public constructor(path: string, globalValues: SystemGlobalValues, csvPath:string, schemaPath?: string| null) {
    this.titles = new Array();
    this.stationData = new Array();
    this.userData = new Array();
    this.globalValuesFields = new Array();
    this.globalValues = {};
    this.globalValues.demand_satisfaction = globalValues.getDemandSatisfaction();
    this.globalValues.hire_efficiency = globalValues.getRentalEfficiency();
    this.globalValues.return_efficiency = globalValues.getReturnEfficiency();
    this.path = path;
    this.schemaPath = schemaPath == null ? null : schemaPath;
    this.csvPath = csvPath;
  }
<<<<<<< HEAD
    
	 public async generate(info: Map<string, SystemInfo>): Promise<void> {
      try {
          await this.init(info);
          this.transformToCsv();
      }
      catch(error) {
          throw new Error('Error initializing csv generator: '+error);
      }
      return;
	 }
     
  public createJsonFor(entities: Array<Entity>, data: Array<JsonObject>, reservations: SystemInfo, rentalsAndReturns: SystemInfo): void {
    let i: number = 1;  // title index
    let j: number = 0;  // data index
      
    for (let entity of entities) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = entity.id;
        
        while (j < 4) {   // adding reservations' data
            let reservationValues: AbsoluteValue | undefined = reservations.getData().absoluteValues.get(entity.id);
            if (reservationValues !== undefined) {
                let absValueList: Array<number>= reservationValues.getAbsoluteValuesAsArray();
                jsonObj[this.titles[i]] = absValueList[j]; 
                i++;
                j++;
            }
        }
        
        j = 0;
        while (j < 4) {   // adding rentals and returns' data
            let rentalAndReturnValues: AbsoluteValue | undefined = rentalsAndReturns.getData().absoluteValues.get(entity.id);
            if (rentalAndReturnValues !== undefined) {
                let absValueList: Array<number> = rentalAndReturnValues.getAbsoluteValuesAsArray();
                jsonObj[this.titles[i]] = absValueList[j];
                i++;   
                j++;
            }
        }
        data.push(jsonObj);
    }
  } 

	 public async init(info: Map<string, SystemInfo>): Promise<void> {
      this.titles.push('id');
      ReservationData.NAMES.forEach( (name) => this.titles.push(name));
      RentalAndReturnData.NAMES.forEach( (name) => this.titles.push(name));
          
      let history: HistoryReader = await HistoryReader.create(this.path);
         
      let historyStations: HistoryEntitiesJson = await history.getEntities('stations');    
      let stations: Array<Station> = <Station[]> historyStations.instances;
         
      let reservations: SystemInfo | undefined = info.get(ReservationsPerStation.name);
      let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerStation.name);
      if (reservations && rentalsAndReturns) {
        this.createJsonFor(stations, this.stationData, reservations, rentalsAndReturns);
      }
         
      let historyUsers: HistoryEntitiesJson = await history.getEntities('users');
      let users: Array<User> = <User[]> historyUsers.instances;
             
      reservations = info.get(ReservationsPerUser.name);
      rentalsAndReturns = info.get(RentalsAndReturnsPerUser.name);
      if (reservations && rentalsAndReturns) {   
        this.createJsonFor(users, this.userData, reservations, rentalsAndReturns);
      }
=======

	public async generate(data: Map<string, any>): Promise<void> {
        await this.init(data);
		this.transformToCsv();
   return;
	}

	public async init(data: Map<string, any>): Promise<void> {
		this.titles.push('id');
		this.titles.push('bike_failed_reservations');
		this.titles.push('slot_failed_reservations');
		this.titles.push('bike_successful_reservations');
		this.titles.push('slot_successful_reservations');
		this.titles.push('bike_failed_rentals');
		this.titles.push('bike_failed_returns');
		this.titles.push('bike_successful_rentals');
		this.titles.push('bike_successful_returns');

		this.globalValuesFields.push('demand_satisfaction');
		this.globalValuesFields.push('hire_efficiency');
		this.globalValuesFields.push('return_efficiency');
    
    let history: HistoryReader = await HistoryReader.create(this.path, this.schemaPath);
    let entities: HistoryEntitiesJson = await history.getEntities('stations');    
    let stations: Array<Station> = <Station[]> entities.instances;
    entities = await history.getEntities('users');
    let users: Array<User> = <User[]> entities.instances;
    
    let reservations, rentalsAndReturns: any;
    let value: number;
    
    reservations = data.get(ReservationsPerStation.name);
    rentalsAndReturns = data.get(RentalsAndReturnsPerStation.name);
    for (let station of stations) {
      
      let stationJson: JsonObject = {};
      
      stationJson.id = station.id;
      value = reservations.getBikeFailedReservationsOfStation(station.id);
      stationJson.bike_failed_reservations = value;
      
      value = reservations.getSlotFailedReservationsOfStation(station.id);
      stationJson.slot_failed_reservations = value;
      
      value = reservations.getBikeSuccessfulReservationsOfStation(station.id);
      stationJson.bike_successful_reservations = value;
      
      value = reservations.getSlotSuccessfulReservationsOfStation(station.id);
      stationJson.slot_successful_reservations = value;
      
      value = rentalsAndReturns.getBikeFailedRentalsOfStation(station.id);
      stationJson.bike_failed_rentals = value;
      
      value = rentalsAndReturns.getBikeFailedReturnsOfStation(station.id);
      stationJson.bike_failed_returns = value;
      
      value = rentalsAndReturns.getBikeSuccessfulRentalsOfStation(station.id);
      stationJson.bike_successful_rentals = value;
      
      value = rentalsAndReturns.getBikeSuccessfulReturnsOfStation(station.id);
      stationJson.bike_successful_returns = value;
      
      this.stationData.push(stationJson);
      
    }
    
    reservations = data.get(ReservationsPerUser.name);
    reservations.print();
    rentalsAndReturns = data.get(RentalsAndReturnsPerUser.name);
    for (let user of users) {
        let userJson: JsonObject = {};

        userJson.id = user.id;
        value = reservations.getBikeFailedReservationsOfUser(user.id);
        userJson.bike_failed_reservations = value;
        value = reservations.getSlotFailedReservationsOfUser(user.id);
        userJson.slot_failed_reservations = value;
        value = reservations.getBikeSuccessfulReservationsOfUser(user.id);
        userJson.bike_successful_reservations = value;
        value = reservations.getSlotSuccessfulReservationsOfUser(user.id);
        userJson.slot_successful_reservations = value;

        value = rentalsAndReturns.getBikeFailedRentalsOfUser(user.id);
        userJson.bike_failed_rentals = value;
        value = rentalsAndReturns.getBikeFailedReturnsOfUser(user.id);
        userJson.bike_failed_returns = value;
        value = rentalsAndReturns.getBikeSuccessfulRentalsOfUser(user.id);
        userJson.bike_successful_rentals = value;
        value = rentalsAndReturns.getBikeSuccessfulReturnsOfUser(user.id);
        userJson.bike_successful_returns = value;

        this.userData.push(userJson);

    }

    return;
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
	}

	public transformToCsv(): void {
        let csv = json2csv({ data: this.stationData, fields: this.titles, withBOM: true });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stations.csv`, csv, (err) => {
          if (err) throw err;
          console.log('stations file saved');
        });
        csv = json2csv({ data: this.userData, fields: this.titles, withBOM: true });
        fs.writeFile (`${this.csvPath}/users.csv`, csv, (err) => {
          if (err) throw err;
          console.log('user file saved');
        });
        csv = json2csv({data: [this.globalValues], fields: this.globalValuesFields, withBOM: true });
        fs.writeFile(`${this.csvPath}/global_values.csv`, csv, (err) => {
            if (err) throw err;
            console.log('global values file saved');
        });
	}

	private checkFolders(): void {
        if(!fs.existsSync(this.csvPath)) {
            fs.mkdirSync(this.csvPath);
        }
    }

}  